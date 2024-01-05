package egovframework.ocr.sample.web;

import java.awt.Color;
import java.awt.Dimension;
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

import javax.servlet.annotation.MultipartConfig;

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
* @author 캡스톤팀 정은총, 송창우
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
    /** 이미지 업로드 디렉토리 */
	public static final String UPLOAD_DIR = KeyValue.uploadDir;
	
    @GetMapping("/{name}")
    public String sayHello(@PathVariable String name) {
        String result="Hello eGovFramework!! name : " + name;  
        return result;
    }
    /**
    * tess_sum이름의 POST 타입 호출을 받아 텍스트 추출 및 요약
    * @param file 이미지/pdf 폴더
    * @param language 오타수정에 사용할 언어
    * @return 파일 이름, 언어, 오타수정 요청을 거친 텍스트를 보관한 response 해시맵
    * @throws IOException 
    * @throws IllegalStateException 
    * @see ocrTestApplication.java
    * @see UseGPT.useGPT
    */
    @PostMapping("/tess_sum")
    public ResponseEntity<?> tess(@RequestParam("file") MultipartFile file, @RequestParam("language") String language) throws IllegalStateException, IOException {
        //MultipartFile file = (MultipartFile) input.get("file");
        //String language = (String) input.get("language");
        
        String fullPath = null; // path to upload image file
        if(!file.isEmpty()) {
            fullPath = UPLOAD_DIR + file.getOriginalFilename(); // set path if file is not empty
            System.out.println("File Save fullPath = " + fullPath);
            file.transferTo(new File(fullPath));
        } else {
            System.out.println("isEmpty!");
        }
        System.out.println("Here!");
        String prompt = ""; // ChatGPT에게 보낼 명령어
        String result = ""; // 테서렉트를 돌리고 안의 스페이스와 "을 없앤 텍스트
        String preprocessingResult = ""; // ChatGPT에게 오타수정을 요청한 후 텍스트
        String summaryText = ""; // 요약 텍스트
        String fileName = file.getOriginalFilename(); // 파일의 이름
        
        result = OcrTesseract.ocrTess(file.getOriginalFilename(), language); 
        
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
        
        if (fullPath != null) { // remove temporary file
            File doDelete = new File(fullPath);
            if(doDelete.exists()) {
                doDelete.delete();
            }
        }
        
        return ResponseEntity.ok(response);
    }
    /**
     * 요약이 필요한 텍스트와 언어를 받아 요약 결과 출력
     * @param text 요약을 위해 받는 텍스트
     * @param lang 텍스트 요약에 사용할 언어
     * @return 요약텍스트를 가진 response 해시맵
     * @see Prompts.java
     * @see UseGPT.useGPT
     */
    public String summary(String text, String language) {
        String prompt = "SUMMARY_" + language.toUpperCase(); // SUMMARY_KOR, SUMMARY_ENG등 언어에 맞는 요약 요청 프롬포트
        String summaryText = ""; // 요약 텍스트를 보관
        
        summaryText = UseGPT.useGPT(Prompts.getPrompt(prompt), text);
        summaryText = summaryText.replaceAll("\\.", ".\n"); // .뒤에 엔터키를 적용"
        summaryText = summaryText.replaceAll("(?m)^[\\s&&[^\\n]]+|^[\n]", ""); // 엔터키로 인해 생긴 스페이스를 지워줌

        return summaryText;
    }
    
    /**
     * tess_sum이름의 POST 타입 호출을 받아 텍스트 추출 및 요약
     * @param file 이미지/pdf 폴더
     * @param language 오타수정에 사용할 언어
     * @return 파일 이름, 언어, 오타수정 요청을 거친 텍스트를 보관한 response 해시맵
     * @throws IOException 
     * @throws IllegalStateException 
     * @see ocrTestApplication.java
     * @see UseGPT.useGPT
     */
     @PostMapping("/tag")
     public ResponseEntity<?> vision(@RequestParam("scanResult") String scanResult, @RequestParam("language") String language) throws IllegalStateException, IOException {
    	 String prompt = "TAG_" + language.toUpperCase(); // TAG_KOR, TAG_ENG등 언어에 맞는 요약 요청 프롬포트
         String jsonTag = ""; // json 형식의 요약 태그를 보관
         String topTags = ""; // 태그들중 가장 빈도수가 높은 태그 5가지
         String purpose = ""; // 태그를 기반으로 한 텍스트의 의도 추출
         String tagAndText = "";
         
         System.out.println("[rest] prompt: " + prompt);
         System.out.println("[rest] getPrompt: " + Prompts.getPrompt(prompt));
         System.out.println("[rest] scanResult: " + scanResult);
         
         jsonTag = UseGPT.useGPT(Prompts.getPrompt(prompt), scanResult);
         
         prompt = "TOP_TAG_" + language.toUpperCase(); // TAG_KOR, TAG_ENG등 언어에 맞는 요약 요청 프롬포트
         
         System.out.println("[rest] prompt: " + prompt);
         System.out.println("[rest] getPrompt: " + Prompts.getPrompt(prompt));
         System.out.println("[rest] tags: " + jsonTag);
         
         topTags = UseGPT.useGPT(Prompts.getPrompt(prompt), jsonTag); // 가장 빈도수 높은 태그 5개로 추리기
         
         prompt = "PUR_" + language.toUpperCase();
         tagAndText = topTags + "\n" + scanResult;
         purpose = UseGPT.useGPT(Prompts.getPrompt(prompt), tagAndText); // topTags 기반으로 텍스트의 의도 추출
         
         System.out.println("[rest] prompt: " + prompt);
         System.out.println("[rest] getPrompt: " + Prompts.getPrompt(prompt));
         System.out.println("[rest] Top 5 tags: " + topTags);
         System.out.println("[rest] Tag and Text: " + tagAndText);
         System.out.println("[rest] Purpose" + purpose);
         
         Map<String, String> response = new HashMap<>();
         response.put("jsonTag", jsonTag);
         response.put("topTags", topTags);
         response.put("purpose", purpose);
         response.put("imgLink", convertToLink(jsonTag));
         
         return ResponseEntity.ok(response);
     }
     
     private static String convertToLink(String jsonString) {
         // JSON 문자열을 중괄호를 기준으로 나누어 배열로 변환
         String[] keyValuePairs = jsonString.substring(1, jsonString.length() - 1).split(",");

         // 원하는 형식의 문자열로 변환
         StringBuilder queryString = new StringBuilder();

         for (String pair : keyValuePairs) {
             // 각 키-값 쌍을 콜론을 기준으로 나누기
             String[] entry = pair.split(":");

             // 특수 문자 처리를 위해 key를 변환
             String key = entry[0].trim().replace("\"", "").replace(" ", "%20").replace("_", "%5F");

             // 쿼리 스트링에 추가
             queryString.append(key)
                     .append(":")
                     .append(entry[1].trim())
                     .append(",");
         }

         // 마지막 쉼표 제거
         if (queryString.length() > 0) {
             queryString.deleteCharAt(queryString.length() - 1);
         }

         return "https://quickchart.io/wordcloud?text=" + queryString.toString() + "&useWordList=true";
     }
    
    
}