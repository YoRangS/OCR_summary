package egovframework.ocr.sample.web;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.annotation.MultipartConfig;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
import com.kennycason.kumo.palette.ColorPalette;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

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
	
    @GetMapping("/{name}")
    public String sayHello(@PathVariable String name) {
        String result="Hello eGovFramework!! name : " + name;  
        return result;
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
        //MultipartFile file = (MultipartFile) input.get("file");
        //String language = (String) input.get("language");
        
        String fullPath = transferFile(file);
        String prompt = ""; // ChatGPT에게 보낼 명령어
        String result = ""; // 테서렉트를 돌리고 안의 스페이스와 "을 없앤 텍스트
        String preprocessingResult = ""; // ChatGPT에게 오타수정을 요청한 후 텍스트
        String summaryText = ""; // 요약 텍스트
        String fileName = file.getOriginalFilename(); // 파일의 이름
        
        result = OcrTesseract.ocrTess(file.getOriginalFilename(), language, UPLOAD_DIR); 
        
        language = languageFirst(language);
        prompt = "FIX_TYPO_" + language.toUpperCase(); // FIX_TYPO_KOR, FIX_TYPO_ENG
        preprocessingResult = UseGPT.useGPT(Prompts.getPrompt(prompt), result); // text after using ChatGPT to fix typos
        preprocessingResult = preprocessingResult.replaceAll("\"", ""); // restapi로 호출할때 오류를 일으키는 큰 따옴표 제거
        summaryText = summary(preprocessingResult, language);
        // 리턴값으로 돌려줄 파일이름, 언어, 오타수정 결과 텍스트
        Map<String, String> response = new HashMap<>();
        response.put("fileName", fileName);
        response.put("language", language);
        response.put("rawText", result);
        response.put("preprocessingResult", preprocessingResult);
        response.put("summary", summaryText);
        
        deleteFile(fullPath);
        
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
         
         String fullPath = transferFile(file);
         String prompt = ""; // ChatGPT에게 보낼 명령어
         String result = ""; // 테서렉트를 돌리고 안의 스페이스와 "을 없앤 텍스트
         String preprocessingResult = ""; // ChatGPT에게 오타수정을 요청한 후 텍스트
         String summaryText = ""; // 요약 텍스트
         String fileName = file.getOriginalFilename(); // 파일의 이름
         int start = 1, end = 1;
         

         result = pageSpecific(language, startPage, endPage, fullPath, result, start, end);
         
         language = languageFirst(language);
         prompt = "FIX_TYPO_" + language.toUpperCase(); // FIX_TYPO_KOR, FIX_TYPO_ENG
         preprocessingResult = UseGPT.useGPT(Prompts.getPrompt(prompt), result); // text after using ChatGPT to fix typos
         preprocessingResult = preprocessingResult.replaceAll("\"", ""); // restapi로 호출할때 오류를 일으키는 큰 따옴표 제거
         summaryText = summary(preprocessingResult, language);
         
         // 리턴값으로 돌려줄 파일이름, 언어, 오타수정 결과 텍스트
         Map<String, String> response = new HashMap<>();
         response.put("fileName", fileName);
         response.put("language", language);
         response.put("rawText", result);
         response.put("preprocessingResult", preprocessingResult);
         response.put("summary", summaryText);
         
         deleteFile(fullPath);
         
         return ResponseEntity.ok(response);
     }

     private void deleteFile(String fullPath) {
 		if (fullPath != null) { // remove temporary file
             File doDelete = new File(fullPath);
             if(doDelete.exists()) {
                 doDelete.delete();
             }
         }
 	}
     
     private String transferFile(MultipartFile file) throws IOException {
    	  UPLOAD_DIR = servletContext.getRealPath("/WEB-INF/classes/saveImage/");
        String fullPath = null; // path to upload image file
        if(!file.isEmpty()) {
            fullPath = UPLOAD_DIR + file.getOriginalFilename(); // set path if file is not empty
            System.out.println("File Save fullPath = " + fullPath);
            file.transferTo(new File(fullPath));
        } else {
            System.out.println("isEmpty!");
        }
 		return fullPath;
 	}
     
 	private String pageSpecific(String language, String startPage, String endPage, String fullPath, String result,
			int start, int end) {
		String pageText = ""; // PDF의 각 이미지 마다의 텍스트
		String imagePath = ""; // 새롭게 만들어질 각 이미지의 위치
		
		try {
 			start = Integer.parseInt(startPage); // tessLimit 옵션 선택시 시작 페이지
 			end = Integer.parseInt(endPage); // tessLimit 옵션 선택시 끝나는 페이지
 			if (start > end) { // 시작 페이지 값이 끝 페이지보다 큰 경우
				int tmp = start;
				start = end;
				end = tmp;
			}
 		 } catch (NumberFormatException e) { // start 와 end가 숫자로 변환되지 않을 경우 오류 출력
 			e.printStackTrace();
 		 }
         
         for (int i = start; i <= end; i++) { 
			System.out.println("Checking for " + i);
			imagePath = UPLOAD_DIR + "image_page_" + i + ".png"; // 각 페이지 마다 임시이미지 파일 생성. 
			System.out.println("Image directory" + imagePath); 
			
			try (PDDocument document = PDDocument.load(new File(fullPath))) { 
				PDFRenderer pdfRenderer = new PDFRenderer(document);
				BufferedImage image = pdfRenderer.renderImageWithDPI(i-1, 300); // 페이지, 300은 이미지 렌더링의 수준. 300은 높은수준.
				ImageIO.write(image, "png", new File(imagePath)); // 이미지를 imagePath의 디렉토리에 image_page 이름으로 저장.
				System.out.println("Saving image done");
			} 
			catch (IOException e) { //Handle the exception appropriately 
				e.printStackTrace(); 
			}
			
			File imgFile = new File(imagePath);
			pageText = OcrTesseract.ocrTess(imgFile.getName(), language, UPLOAD_DIR);
			result = result + pageText + "\n";
			
			imgFile.delete(); 
		}
		return result;
	}

    public String summary(String text, String language) {
        String prompt = "SUMMARY_" + language.toUpperCase(); // SUMMARY_KOR, SUMMARY_ENG등 언어에 맞는 요약 요청 프롬포트
        String summaryText = ""; // 요약 텍스트를 보관
        
        summaryText = UseGPT.useGPT(Prompts.getPrompt(prompt), text);
        summaryText = summaryText.replaceAll("\\.", ".\n"); // .뒤에 엔터키를 적용"
        summaryText = summaryText.replaceAll("(?m)^[\\s&&[^\\n]]+|^[\n]", ""); // 엔터키로 인해 생긴 스페이스를 지워줌

        return summaryText;
    }
    
    private String languageFirst(String language) {
		int plusIndex = language.indexOf('+');
		if (plusIndex != -1) { // '+'가 발견된 경우
			language = language.substring(0, plusIndex); // kor+eng의 경우 kor만 고려. prompt를 사용할 언어와의 상호작용을 위함
		}
		return language;
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
    	 language = languageFirst(language); // kor+eng와 같은 형태에서 앞의 kor만 사용
    	 String prompt = "TAG_" + language.toUpperCase(); // TAG_KOR, TAG_ENG등 언어에 맞는 요약 요청 프롬포트
         String jsonTag = ""; // json 형식의 요약 태그를 보관
         String topTags = ""; // 태그들중 가장 빈도수가 높은 태그 5가지
         String purpose = ""; // 태그를 기반으로 한 텍스트의 의도 추출
         
         jsonTag = getTags(scanResult, prompt);
         topTags = getTopTags(language, jsonTag);
         purpose = getPurpose(scanResult, language, topTags);
         
         Map<String, String> response = new HashMap<>();
         response.put("jsonTag", jsonTag);
         response.put("topTags", topTags);
         response.put("purpose", purpose);
         response.put("imgLink", convertToLink(jsonTag)); // 태그를 기반으로 만든 wordcloud 이미지의 링크
         
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
    	 
    	 String fullPath = transferFile(file);
         
         String prompt = ""; // ChatGPT에게 보낼 명령어
         String result = ""; // 테서렉트를 돌리고 안의 스페이스와 "을 없앤 텍스트
         String preprocessingResult = ""; // ChatGPT에게 오타수정을 요청한 후 텍스트
         
         result = OcrTesseract.ocrTess(file.getOriginalFilename(), language, UPLOAD_DIR); 
         
         language = languageFirst(language);  // kor+eng와 같은 형태에서 앞의 kor만 사용
         prompt = "FIX_TYPO_" + language.toUpperCase(); // FIX_TYPO_KOR, FIX_TYPO_ENG
         preprocessingResult = UseGPT.useGPT(Prompts.getPrompt(prompt), result); // text after using ChatGPT to fix typos
         preprocessingResult = preprocessingResult.replaceAll("\"", ""); // restapi로 호출할때 오류를 일으키는 큰 따옴표 제거
    	 
         prompt = "TAG_" + language.toUpperCase(); // TAG_KOR, TAG_ENG등 언어에 맞는 요약 요청 프롬포트
         String jsonTag = ""; // json 형식의 요약 태그를 보관
         String topTags = ""; // 태그들중 가장 빈도수가 높은 태그 5가지
         String purpose = ""; // 태그를 기반으로 한 텍스트의 의도 추출
         
         jsonTag = getTags(preprocessingResult, prompt);
         topTags = getTopTags(language, jsonTag);
         purpose = getPurpose(preprocessingResult, language, topTags);
         
         Map<String, String> response = new HashMap<>();
         response.put("jsonTag", jsonTag);
         response.put("topTags", topTags);
         response.put("purpose", purpose);
         response.put("imgLink", convertToLink(jsonTag)); // 태그를 기반으로 만든 wordcloud 이미지의 링크
         response.put("preprocessingResult", preprocessingResult);
         
         deleteFile(fullPath);
         
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
    	 
    	 String fullPath = transferFile(file);
         
         String prompt = ""; // ChatGPT에게 보낼 명령어
         String result = ""; // 테서렉트를 돌리고 안의 스페이스와 "을 없앤 텍스트
         String preprocessingResult = ""; // ChatGPT에게 오타수정을 요청한 후 텍스트
         int start = 1, end = 1;
         
         result = pageSpecific(language, startPage, endPage, fullPath, result, start, end);
         
         language = languageFirst(language);  // kor+eng와 같은 형태에서 앞의 kor만 사용
         prompt = "FIX_TYPO_" + language.toUpperCase(); // FIX_TYPO_KOR, FIX_TYPO_ENG
         preprocessingResult = UseGPT.useGPT(Prompts.getPrompt(prompt), result); // text after using ChatGPT to fix typos
         preprocessingResult = preprocessingResult.replaceAll("\"", ""); // restapi로 호출할때 오류를 일으키는 큰 따옴표 제거
    	 
         prompt = "TAG_" + language.toUpperCase(); // TAG_KOR, TAG_ENG등 언어에 맞는 요약 요청 프롬포트
         String jsonTag = ""; // json 형식의 요약 태그를 보관
         String topTags = ""; // 태그들중 가장 빈도수가 높은 태그 5가지
         String purpose = ""; // 태그를 기반으로 한 텍스트의 의도 추출
         
         jsonTag = getTags(preprocessingResult, prompt);
         topTags = getTopTags(language, jsonTag);
         purpose = getPurpose(preprocessingResult, language, topTags);
         
         Map<String, String> response = new HashMap<>();
         response.put("jsonTag", jsonTag);
         response.put("topTags", topTags);
         response.put("purpose", purpose);
         response.put("imgLink", convertToLink(jsonTag)); // 태그를 기반으로 만든 wordcloud 이미지의 링크
         response.put("preprocessingResult", preprocessingResult);
         
         deleteFile(fullPath);
         
         return ResponseEntity.ok(response);
     }
     
     private String getPurpose(String scanResult, String language, String topTags) {
 		String prompt;
 		String purpose;
 		String tagAndText;
 		
 		prompt = "PUR_" + language.toUpperCase();
          tagAndText = topTags + "\n" + scanResult;
          purpose = UseGPT.useGPT(Prompts.getPrompt(prompt), tagAndText); // topTags 기반으로 텍스트의 의도 추출
          
          System.out.println("[rest] prompt: " + prompt);
          System.out.println("[rest] getPrompt: " + Prompts.getPrompt(prompt));
          System.out.println("[rest] Top 5 tags: " + topTags);
          System.out.println("[rest] Tag and Text: " + tagAndText);
          System.out.println("[rest] Purpose" + purpose);
 		return purpose;
 	}
 	
 	private String getTopTags(String language, String jsonTag) {
 		String prompt;
 		String topTags;
 		
 		prompt = "TOP_TAG_" + language.toUpperCase(); // TAG_KOR, TAG_ENG등 언어에 맞는 요약 요청 프롬포트
          
          System.out.println("[rest] prompt: " + prompt);
          System.out.println("[rest] getPrompt: " + Prompts.getPrompt(prompt));
          System.out.println("[rest] tags: " + jsonTag);
          
          topTags = UseGPT.useGPT(Prompts.getPrompt(prompt), jsonTag); // 가장 빈도수 높은 태그 5개로 추리기
 		return topTags;
 	}
 	
 	private String getTags(String scanResult, String prompt) {
 		String jsonTag;
 		
 		System.out.println("[rest] prompt: " + prompt);
          System.out.println("[rest] getPrompt: " + Prompts.getPrompt(prompt));
          System.out.println("[rest] scanResult: " + scanResult);
          
          jsonTag = UseGPT.useGPT(Prompts.getPrompt(prompt), scanResult);
 		return jsonTag;
 	}
     
     private static String convertToLink(String jsonString) {
         // JSON 문자열을 중괄호를 기준으로 나누어 배열로 변환
         String[] keyValuePairs = jsonString.substring(1, jsonString.length() - 1).split(",");

         // 원하는 형식의 문자열로 변환
         StringBuilder queryString = new StringBuilder();
         int numbers = 0;
         for (String pair : keyValuePairs) {
             // 각 키-값 쌍을 콜론을 기준으로 나누기
             String[] entry = pair.split(":");
             System.out.println("Entry :" + entry);
             
             // 특수 문자 처리를 위해 key를 변환
             String key = entry[0].trim().replace("\"", "").replace(" ", "%20").replace("_", "%5F");
             System.out.println("key : " + key);
             numbers++;
             
             // 쿼리 스트링에 추가
             queryString.append(key)
                     .append(":")
                     .append(entry[1].trim())
                     .append(",");
         }
         System.out.println("Number of tags: " + numbers);
         // 마지막 쉼표 제거
         if (queryString.length() > 0) {
             queryString.deleteCharAt(queryString.length() - 1);
         }

         return "https://quickchart.io/wordcloud?text=" + queryString.toString() + "&useWordList=true";
     }
    
}