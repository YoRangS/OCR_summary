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
    * @see useGPT
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
     * tess_sum이름의 POST 타입 호출을 받아 텍스트 추출 및 요약
     * @param file 이미지/pdf 폴더
     * @param language 오타수정에 사용할 언어
     * @return 파일 이름, 언어, 오타수정 요청을 거친 텍스트를 보관한 response 해시맵
     * @throws IOException 
     * @throws IllegalStateException 
     * @see ocrTestApplication.java
     * @see useGPT
     */
     @PostMapping("/tag")
     public ResponseEntity<?> vision(@RequestParam("scanResult") String scanResult, @RequestParam("language") String language) throws IllegalStateException, IOException {
    	 String prompt = "TAG_" + language.toUpperCase(); // TAG_KOR, TAG_ENG등 언어에 맞는 요약 요청 프롬포트
         String jsonTag = ""; // json 형식의 요약 태그를 보관
         
         System.out.println("[rest] prompt: " + prompt);
         System.out.println("[rest] getPrompt: " + Prompts.getPrompt(prompt));
         System.out.println("[rest] scanResult: " + scanResult);
         
         jsonTag = useGPT(Prompts.getPrompt(prompt), scanResult);
         
         final List<WordFrequency> wordFrequencies = JsonTagToWordFrequency(jsonTag);
         
         for (WordFrequency wordFrequency : wordFrequencies) {
             System.out.println("Word: " + wordFrequency.getWord() + ", Frequency: " + wordFrequency.getFrequency());
         }
         
         Date date = new Date();
         SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
         String fileDate = sdf.format(date);
         String filePath = UPLOAD_DIR + "/" + fileDate + "_" + UUID.randomUUID().toString() + ".png";
         
         final Dimension dimension = new Dimension(600, 600);
         final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
         wordCloud.setPadding(2);
         wordCloud.setBackground(new CircleBackground(300));
         wordCloud.setColorPalette(new ColorPalette(new Color(0x4055F1), new Color(0x408DF1), new Color(0x40AAF1), new Color(0x40C5F1), new Color(0x40D3F1), new Color(0xFFFFFF)));
         wordCloud.setFontScalar(new SqrtFontScalar(10, 40));
         wordCloud.build(wordFrequencies);
         wordCloud.writeToFile(filePath);
         
         Map<String, String> response = new HashMap<>();
         response.put("jsonTag", jsonTag);
         response.put("filePath", filePath);
         
         return ResponseEntity.ok(response);
     }
     
     private List<WordFrequency> JsonTagToWordFrequency(String jsonTag) {
    	 List<WordFrequency> wordFrequencyList = new ArrayList<>();
    	 String trimmedJsonTag = jsonTag.substring(1, jsonTag.length() - 1);
    	 
    	 String[] parts = trimmedJsonTag.split(",");
         for (String part : parts) {
             String[] keyValue = part.split(":");
             if (keyValue.length == 2) {
                 String word = keyValue[0].trim().replace("\"", "");
                 int frequency = Integer.parseInt(keyValue[1].trim());
                 wordFrequencyList.add(new WordFrequency(word, frequency));
             }
         }
         
         return wordFrequencyList;
     }
     
//     public String generateTextCloud(String jsonTag, Model model) {
//    	 model.addAttribute("jsonTag", jsonTag);
//         
//    	 return "ocr/ocrTextCloud";
//     }
     
//     final String tag = jsonTag;
//     
//     // CompletableFuture를 사용하여 generateImgFile 함수를 비동기로 실행
//     CompletableFuture<Void> imgFileFuture = CompletableFuture.runAsync(() -> {
//         try {
//             generateTextCloud(tag);
//         } catch (Exception e) {
//             // 예외 처리 필요
//             e.printStackTrace();
//         }
//     });
//
//     // CompletableFuture가 완료될 때까지 대기
//     imgFileFuture.join();
     
//     public String generateImageFromJsonTag(String jsonTag) throws ServletException, IOException {
//    	 HttpServletRequest request;
//    	 HttpServletResponse response;
//         // 1. jsonTag라는 String 값을 Parameter로 jsp파일에 전달한다.
//         request.setAttribute("jsonTag", jsonTag);
//         request.getRequestDispatcher("example.jsp").forward(request, response);
//
//         // 2. jsp파일에서 jsonTag라는 String값과 js라이브러리를 통해 div id="container"에 이미지를 생성한다.
//         // 이 부분은 JSP 파일에서 구현하시면 됩니다.
//
//         // 3. jsp파일에서 생성된 이미지를 URL형식으로 java에 다시 전달한다.
//         String imageUrl = (String) request.getAttribute("imageUrl");
//
//         // 4. jsp파일에서 전달한 URL을 바탕으로 이미지 파일로 변환한다.
//         String base64Image = imageUrl.split(",")[1]; // Base64 형식의 이미지 데이터 추출
//
//         byte[] imageBytes = Base64.getDecoder().decode(base64Image);
//         String imagePath = "path/to/save/image.png"; // 이미지 파일을 저장할 경로 및 파일명 설정
//
//         try (OutputStream outputStream = new FileOutputStream(imagePath)) {
//             outputStream.write(imageBytes);
//         }
//
//         return imagePath;
//     }
    
    
    /**
     * OpenAI의 AI 모델을 사용하기 위한 연결과 인풋 텍스트와 명령을 기반으로 결과 출력
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