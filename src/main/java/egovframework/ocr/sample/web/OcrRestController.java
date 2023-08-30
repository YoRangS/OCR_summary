package egovframework.ocr.sample.web;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
* @see OcrTestApplication.java
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
    //public static final String UPLOAD_DIR = "C:/Users/JEC/eclipse-workspace/ocr/src/main/java/saveImage/";
	public static final String UPLOAD_DIR = "C:/Users/Admin/git/OCR_summary/src/main/java/saveImage/";
	
    @GetMapping("/{name}")
    public String sayHello(@PathVariable String name) {
        String result="Hello eGovFramework!! name : " + name;  
        return result;
    }
    /**
    * test.do이름의 POST 타입 호출을 받아 텍스트 추출
    * @param file 이미지/pdf 폴더
    * @param lang 오타수정에 사용할 언어
    * @return 파일 이름, 언어, 오타수정 요청을 거친 텍스트를 보관한 response 해시맵
    * @throws IOException 
    * @throws IllegalStateException 
    * @see ocrTestApplication.java
    * @see useGPT
    */
    @PostMapping("/tess_sum")
    public ResponseEntity<?> test(@RequestParam("file") MultipartFile file, @RequestParam("language") String language) throws IllegalStateException, IOException {
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
        
        result = OcrTestApplication.OcrTest(file.getOriginalFilename(), language); 
        
        prompt = "FIX_TYPO_" + language.toUpperCase(); // FIX_TYPO_KOR, FIX_TYPO_ENG
        preprocessingResult = useGPT(Prompts.getPrompt(prompt), result); // text after using ChatGPT to fix typos
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
     * @see useGPT
     */
    public String summary(String text, String language) {
        String prompt = "SUMMARY_" + language.toUpperCase(); // SUMMARY_KOR, SUMMARY_ENG등 언어에 맞는 요약 요청 프롬포트
        String summaryText = ""; // 요약 텍스트를 보관
        
        summaryText = useGPT(Prompts.getPrompt(prompt), text);
        summaryText = summaryText.replaceAll("\\.", ".\n"); // .뒤에 엔터키를 적용"
        summaryText = summaryText.replaceAll("(?m)^[\\s&&[^\\n]]+|^[\n]", ""); // 엔터키로 인해 생긴 스페이스를 지워줌

        return summaryText;
    }
    
    /**
     * data.do이름의 POST 타입 호출을 받아 텍스트를 지정경로에 텍스트 파일로 저장
     * @param prompt ChatGPT에게 명령을 주기위한 명령 텍스트
     * @param content ChatGPT에게 보낼 텍스트
     * @see Prompts.java
     * @see Keys.java
     * @return ChatGPT를 통해 생성된 텍스트
     */
    public String useGPT(String prompt, String content) {
        Keys keysInstance = Keys.getInstance(); // OpenAI API 활용을 위한 키 인스턴스
        String gptKey = keysInstance.getGptKey(); // 인스턴스의 키값
        OpenAiService service = new OpenAiService(gptKey,Duration.ofMinutes(9999)); // OpenAI 서비스 연결

        List<ChatMessage> message = new ArrayList<ChatMessage>(); // GPT에게 보낼 메세지 어레이
        message.add(new ChatMessage("user", prompt));
        message.add(new ChatMessage("user", content));
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder() // OpenAI의 모델 선택하여 메세지 전달후 결과 텍스트 받기
                .messages(message)
                .model("gpt-3.5-turbo") // 터보 모델 사용
                .maxTokens(1000)
                .temperature((double) 1.0f) // 답변의 자유도 설정
                .build();

        return service.createChatCompletion(completionRequest).getChoices().get(0)
                .getMessage().getContent();
    }
}