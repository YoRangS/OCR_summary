package egovframework.ocr.sample.web;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

public class UseGPT {
	/**
     * OpenAI의 AI 모델을 사용하기 위한 연결과 인풋 텍스트와 명령을 기반으로 결과 출력
     * @param prompt ChatGPT에게 명령을 주기위한 명령 텍스트
     * @param content ChatGPT에게 보낼 텍스트
	 * @return 
     * @see Prompts.java
     * @see Keys.java
     * @return ChatGPT를 통해 생성된 텍스트
     */
	
	public static int maxInputToken = 12000;  // GPT3.5 Turbo 기준 입출력 토큰 16,385. 16385-4000=12385
	//public static int maxInputToken = 120000;  // GPT4 Turbo 기준 입출력 토큰 128,000
	public static int maxOutputToken = 4000; // GPT3.5 Turbo 기준 출력 최대 토큰 4,096
	
    public static String useGPT(String prompt, String content) {
        Keys keysInstance = Keys.getInstance(); // OpenAI API 활용을 위한 키 인스턴스
        String gptKey = keysInstance.getGptKey(); // 인스턴스의 키값
        String gptModel = keysInstance.getGptModel();
        OpenAiService service = new OpenAiService(gptKey,Duration.ofMinutes(9999)); // OpenAI 서비스 연결

        List<ChatMessage> message = new ArrayList<ChatMessage>(); // GPT에게 보낼 메세지 어레이
        message.add(new ChatMessage("user", prompt));
        message.add(new ChatMessage("user", content));
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder() // OpenAI의 모델 선택하여 메세지 전달후 결과 텍스트 받기
                .messages(message)
                .model(gptModel) // 터보 3.5모델 사용.
                .maxTokens(4000) // 입력과 출력중 출력에 할당되는 최대 토큰 값. 현재 입출력 최대 토큰 16,385
                .temperature((double) 0.3f) // 답변의 자유도 설정
                .build();

        return service.createChatCompletion(completionRequest).getChoices().get(0)
                .getMessage().getContent();
    }
}
