package egovframework.ocr.sample.web;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.annotation.MultipartConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import egovframework.rte.fdl.property.EgovPropertyService;

/**
* jsp파일들의 호출을 처리하는 컨트롤러 클래스
* @author 캡스톤팀 정은총, 송창우, 조성준
* @since 2023.08
* @version 0.15
* @see ocrSampleList.jsp
* @see ocrSummary.jsp
* @see OcrTesseract.java
* 
* == 개정이력(Modification Information) ==
* 수정 내용
* 2023.08 정은총 최초 생성
* 2023.08 송창우 언어 입력, 파일 위치 처리 추가
*/
@RestController
@RequestMapping("/ocrapi")
@MultipartConfig(
        fileSizeThreshold = 1024*1024,
        maxFileSize = 1024*1024*5,
        maxRequestSize = 1024*1024*5*5
        )
public class OcrRestController {
	
	
	@Autowired
    private ServletContext servletContext;

    /** 이미지 업로드 디렉토리 */
	private String UPLOAD_DIR;
	
	
	@Resource(name="GPTPropertiesService")
    protected EgovPropertyService GPTPropertiesService;
	
	private final OcrFunction ocrFunction;
	@Autowired
    public OcrRestController(OcrFunction ocrFunction) {
        this.ocrFunction = ocrFunction;
    }
	
    @GetMapping("/test")
    public String test() {
    	String key = GPTPropertiesService.getString("GPT_KEY");
    	String model = GPTPropertiesService.getString("GPT_MODEL");
    	String max = GPTPropertiesService.getString("GPT_MAXINPUTTOKEN");
    	String min = GPTPropertiesService.getString("GPT_MAXOUTPUTTOKEN");
    	
    	return "{ \"GPT_KEY\" : \"" + key + "\", \"GPT_MODEL\" : \"" + model + "\", \"GPT_MAXINPUTTOKEN\" : \"" + max + "\", \"GPT_MAXOUTPUTTOKEN\" : \"" + min + "\" }";
    	
    }
    
    
    /**
    * tess_sum이름의 POST 타입 호출을 받아 텍스트 추출 및 요약
    * @param file 이미지/pdf 파일
    * @param language 파일안 텍스트의 언어
    * @return 파일 이름, 언어, 오타수정 요청을 거친 텍스트를 보관한 response 해시맵
    * @throws IOException 
    * @throws IllegalStateException 
    * @see OcrTesseract.ocrTess
    * @see UseGPT.useGPT
    */
    @PostMapping("/tess_sum")
    public ResponseEntity<?> tess(@RequestParam("file") MultipartFile file, @RequestParam("language") String language) throws IllegalStateException, IOException {
    	int maxOutputToken = Integer.parseInt(GPTPropertiesService.getString("GPT_MAXOUTPUTTOKEN"));
    	UPLOAD_DIR = servletContext.getRealPath("/WEB-INF/classes/saveImage/");
        String fullPath = ocrFunction.transferFile(file);
        String prompt = ""; // ChatGPT에게 보낼 명령어
        String result = ""; // 테서렉트를 돌리고 안의 스페이스와 "을 없앤 텍스트
        String preprocessingResult = ""; // ChatGPT에게 오타수정을 요청한 후 텍스트
        String summaryText = ""; // 요약 텍스트
        String fileName = file.getOriginalFilename(); // 파일의 이름
        String afterDetectResult = ""; // 민감정보 검출 후 텍스트
        
        fullPath = ocrFunction.convertToPdf(fullPath, fileName);
        
        result = OcrTesseract.ocrTess(file.getOriginalFilename(), language, UPLOAD_DIR); 
        
        language = ocrFunction.languageFirst(language).toUpperCase();
        prompt = "FIX_TYPO_" + language.toUpperCase(); // FIX_TYPO_KOR, FIX_TYPO_ENG
        preprocessingResult = ocrFunction.blockRequest(language, prompt, result, maxOutputToken); // text after using ChatGPT to fix typos
        preprocessingResult = preprocessingResult.replaceAll("\"", ""); // restapi로 호출할때 오류를 일으키는 큰 따옴표 제거
        prompt = "DETECT_SEN_" + language.toUpperCase(); // DETECT_SEN_KOR, DETECT_SEN_ENG
        afterDetectResult = ocrFunction.blockRequest(language, prompt, preprocessingResult, maxOutputToken);
        afterDetectResult = afterDetectResult.replaceAll("\"", "");
        summaryText = ocrFunction.summary(afterDetectResult, language);
        // 리턴값으로 돌려줄 파일이름, 언어, 오타수정 결과 텍스트
        Map<String, String> response = new HashMap<>();
        response.put("fileName", fileName);
        response.put("language", language);
        response.put("rawText", result);
        response.put("preprocessingResult", preprocessingResult);
        response.put("afterDetectResult", afterDetectResult);
        response.put("summary", summaryText);
        
        ocrFunction.removeFile(fullPath);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * tess_specific이름의 POST 타입 호출을 받아 텍스트 추출 및 요약
     * @param file 이미지/pdf 파일
     * @param language 오타수정에 사용할 언어
     * @param startPage, endPage 추출을 원하는 시작과 끝 페이지
     * @return 파일 이름, 언어, 오타수정 요청을 거친 텍스트를 보관한 response 해시맵
     * @throws IOException 
     * @throws IllegalStateException 
     * @see OcrTesseract.ocrTess
     * @see UseGPT.useGPT
     */
     @PostMapping("/tess_specific")
     public ResponseEntity<?> tessSpecific(@RequestParam("file") MultipartFile file, @RequestParam("language") String language, 
    		 @RequestParam("startPage") String startPage, @RequestParam("endPage") String endPage) throws IllegalStateException, IOException {
         
         String fullPath = ocrFunction.transferFile(file);
         String prompt = ""; // ChatGPT에게 보낼 명령어
         String result = ""; // 테서렉트를 돌리고 안의 스페이스와 "을 없앤 텍스트
         String preprocessingResult = ""; // ChatGPT에게 오타수정을 요청한 후 텍스트
         String afterDetectResult = ""; // 민감정보 검출 후 텍스트
         String summaryText = ""; // 요약 텍스트
         String fileName = file.getOriginalFilename(); // 파일의 이름
         int start = 1, end = 1;
         
         fullPath = ocrFunction.convertToPdf(fullPath, fileName);

         result = ocrFunction.pageSpecific(language, startPage, endPage, fullPath, result, start, end);
         
         language = ocrFunction.languageFirst(language).toUpperCase();
         prompt = "FIX_TYPO_" + language; // FIX_TYPO_KOR, FIX_TYPO_ENG
         preprocessingResult = ocrFunction.blockRequest(language, prompt, result, Integer.parseInt(GPTPropertiesService.getString("GPT_MAXOUTPUTTOKEN"))); // text after using ChatGPT to fix typos
         preprocessingResult = preprocessingResult.replaceAll("\"", ""); // restapi로 호출할때 오류를 일으키는 큰 따옴표 제거
         prompt = "DETECT_SEN_" + language; // DETECT_SEN_KOR, DETECT_SEN_ENG
         afterDetectResult = ocrFunction.blockRequest(language, prompt, preprocessingResult, Integer.parseInt(GPTPropertiesService.getString("GPT_MAXOUTPUTTOKEN")));
         afterDetectResult = afterDetectResult.replaceAll("\"", "");
         summaryText = ocrFunction.summary(afterDetectResult, language);
         
         // 리턴값으로 돌려줄 파일이름, 언어, 오타수정 결과 텍스트
         Map<String, String> response = new HashMap<>();
         response.put("fileName", fileName);
         response.put("language", language);
         response.put("rawText", result);
         response.put("preprocessingResult", preprocessingResult);
         response.put("afterDetectResult", afterDetectResult);
         response.put("summary", summaryText);
         
         ocrFunction.removeFile(fullPath);
         
         return ResponseEntity.ok(response);
     }
    
    /**
     * tag 이름의 POST 타입 호출을 받아 테그 추출 및 의도 추출
     * @param scanResult 태그 및 의도 추출을 위한 텍스트 원문
     * @param language 텍스트의 언어
     * @return 태그, 상위태그, 의도, 이미지링크 텍스트를 보관한 response 해시맵
     * @throws IOException 
     * @throws IllegalStateException 
     * @see ocrTestApplication.java
     * @see UseGPT.useGPT
     */
     @PostMapping("/tag")
     public ResponseEntity<?> vision(@RequestParam("scanResult") String scanResult, @RequestParam("language") String language) throws IllegalStateException, IOException {
    	 language = ocrFunction.languageFirst(language).toUpperCase(); // kor+eng와 같은 형태에서 앞의 kor만 사용
    	 String prompt = "TAG_" + language.toUpperCase(); // TAG_KOR, TAG_ENG등 언어에 맞는 요약 요청 프롬포트
         String jsonTag = ""; // json 형식의 요약 태그를 보관
         String topTags = ""; // 태그들중 가장 빈도수가 높은 태그 5가지
         String purpose = ""; // 태그를 기반으로 한 텍스트의 의도 추출
         
         jsonTag = ocrFunction.getTags(language, scanResult, prompt);
         topTags = ocrFunction.getTopTags(language, jsonTag);
         purpose = ocrFunction.getPurpose(scanResult, language, topTags);
         
         Map<String, String> response = new HashMap<>();
         response.put("jsonTag", jsonTag);
         response.put("topTags", topTags);
         response.put("purpose", purpose);
         response.put("imgLink", ocrFunction.convertToLink(jsonTag)); // 태그를 기반으로 만든 wordcloud 이미지의 링크
         
         return ResponseEntity.ok(response);
     }
     
     /**
      * tag 이름의 POST 타입 호출을 받아 테그 추출 및 의도 추출
      * @param scanResult 태그 및 의도 추출을 위한 텍스트 원문
      * @param language 텍스트의 언어
      * @return 태그, 상위태그, 의도, 이미지링크, 오타수정 요청을 거친 텍스트를 보관한 response 해시맵
      * @throws IOException 
      * @throws IllegalStateException 
      * @see ocrTestApplication.java
      * @see UseGPT.useGPT
      */
     @PostMapping("/tag_file")
     public ResponseEntity<?> tagFile(@RequestParam("file") MultipartFile file, @RequestParam("language") String language) throws IllegalStateException, IOException {
    	 UPLOAD_DIR = servletContext.getRealPath("/WEB-INF/classes/saveImage/");
    	 String fullPath = ocrFunction.transferFile(file);
         
         String prompt = ""; // ChatGPT에게 보낼 명령어
         String result = ""; // 테서렉트를 돌리고 안의 스페이스와 "을 없앤 텍스트
         String preprocessingResult = ""; // ChatGPT에게 오타수정을 요청한 후 텍스트
         String afterDetectResult = ""; // 민감정보 검출 후 텍스트
         
         result = OcrTesseract.ocrTess(file.getOriginalFilename(), language, UPLOAD_DIR); 
         
         language = ocrFunction.languageFirst(language).toUpperCase();  // kor+eng와 같은 형태에서 앞의 kor만 사용
         prompt = "FIX_TYPO_" + language; // FIX_TYPO_KOR, FIX_TYPO_ENG
         preprocessingResult = ocrFunction.blockRequest(language, prompt, result, Integer.parseInt(GPTPropertiesService.getString("GPT_MAXOUTPUTTOKEN"))); // text after using ChatGPT to fix typos
         preprocessingResult = preprocessingResult.replaceAll("\"", ""); // restapi로 호출할때 오류를 일으키는 큰 따옴표 제거
         prompt = "DETECT_SEN_" + language; // DETECT_SEN_KOR, DETECT_SEN_ENG
         afterDetectResult = ocrFunction.blockRequest(language, prompt, preprocessingResult, Integer.parseInt(GPTPropertiesService.getString("GPT_MAXOUTPUTTOKEN")));
         afterDetectResult = afterDetectResult.replaceAll("\"", "");
         
         prompt = "TAG_" + language.toUpperCase(); // TAG_KOR, TAG_ENG등 언어에 맞는 요약 요청 프롬포트
         String jsonTag = ""; // json 형식의 요약 태그를 보관
         String topTags = ""; // 태그들중 가장 빈도수가 높은 태그 5가지
         String purpose = ""; // 태그를 기반으로 한 텍스트의 의도 추출
         
         jsonTag = ocrFunction.getTags(language, afterDetectResult, prompt);
         topTags = ocrFunction.getTopTags(language, jsonTag);
         purpose = ocrFunction.getPurpose(afterDetectResult, language, topTags);
         
         Map<String, String> response = new HashMap<>();
         response.put("jsonTag", jsonTag);
         response.put("topTags", topTags);
         response.put("purpose", purpose);
         response.put("imgLink", ocrFunction.convertToLink(jsonTag)); // 태그를 기반으로 만든 wordcloud 이미지의 링크
         response.put("preprocessingResult", preprocessingResult);
         response.put("afterDetectResult", afterDetectResult);
         
         ocrFunction.removeFile(fullPath);
         
         return ResponseEntity.ok(response);
     }
     
     /**
      * tag 이름의 POST 타입 호출을 받아 테그 추출 및 의도 추출
      * @param scanResult 태그 및 의도 추출을 위한 텍스트 원문
      * @param language 텍스트의 언어
      * @param startPage, endPage 추출을 원하는 시작과 끝 페이지
      * @return 태그, 상위태그, 의도, 이미지링크, 오타수정 요청을 거친 텍스트를 보관한 response 해시맵
      * @throws IOException 
      * @throws IllegalStateException 
      * @see ocrTestApplication.java
      * @see UseGPT.useGPT
      */
     @PostMapping("/tag_specific")
     public ResponseEntity<?> tagSpecific(@RequestParam("file") MultipartFile file, @RequestParam("language") String language, 
    		 @RequestParam("startPage") String startPage, @RequestParam("endPage") String endPage) throws IllegalStateException, IOException {
    	 
    	 String fullPath = ocrFunction.transferFile(file);
         
         String prompt = ""; // ChatGPT에게 보낼 명령어
         String result = ""; // 테서렉트를 돌리고 안의 스페이스와 "을 없앤 텍스트
         String preprocessingResult = ""; // ChatGPT에게 오타수정을 요청한 후 텍스트
         String afterDetectResult = ""; // 민감정보 검출 후 텍스트
         int start = 1, end = 1;
         
         result = ocrFunction.pageSpecific(language, startPage, endPage, fullPath, result, start, end);
         
         language = ocrFunction.languageFirst(language).toUpperCase();  // kor+eng와 같은 형태에서 앞의 kor만 사용
         prompt = "FIX_TYPO_" + language; // FIX_TYPO_KOR, FIX_TYPO_ENG
         preprocessingResult = ocrFunction.blockRequest(language, prompt, result, Integer.parseInt(GPTPropertiesService.getString("GPT_MAXOUTPUTTOKEN"))); // text after using ChatGPT to fix typos
         preprocessingResult = preprocessingResult.replaceAll("\"", ""); // restapi로 호출할때 오류를 일으키는 큰 따옴표 제거
         prompt = "DETECT_SEN_" + language; // DETECT_SEN_KOR, DETECT_SEN_ENG
         afterDetectResult = ocrFunction.blockRequest(language, prompt, preprocessingResult, Integer.parseInt(GPTPropertiesService.getString("GPT_MAXOUTPUTTOKEN")));
         afterDetectResult = afterDetectResult.replaceAll("\"", "");
         
         prompt = "TAG_" + language.toUpperCase(); // TAG_KOR, TAG_ENG등 언어에 맞는 요약 요청 프롬포트
         String jsonTag = ""; // json 형식의 요약 태그를 보관
         String topTags = ""; // 태그들중 가장 빈도수가 높은 태그 5가지
         String purpose = ""; // 태그를 기반으로 한 텍스트의 의도 추출
         
         jsonTag = ocrFunction.getTags(language, afterDetectResult, prompt);
         topTags = ocrFunction.getTopTags(language, jsonTag);
         purpose = ocrFunction.getPurpose(afterDetectResult, language, topTags);
         
         Map<String, String> response = new HashMap<>();
         response.put("jsonTag", jsonTag);
         response.put("topTags", topTags);
         response.put("purpose", purpose);
         response.put("imgLink", ocrFunction.convertToLink(jsonTag)); // 태그를 기반으로 만든 wordcloud 이미지의 링크
         response.put("preprocessingResult", preprocessingResult);
         response.put("afterDetectResult", afterDetectResult);
         
         ocrFunction.removeFile(fullPath);
         
         return ResponseEntity.ok(response);
     }
}