package egovframework.ocr.sample.web;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;



public class SplitterRunnable implements Runnable{

	private String processString = new String();
	private int maxToken;
	private String prompt = new String();
	private String result = new String();
	
	public SplitterRunnable(String processString, int maxToken) {
		this.processString = processString;
		this.maxToken = maxToken;
	}

	@Override
	public void run() {
		Keys keysInstance = Keys.getInstance(); // OpenAI API 활용을 위한 키 인스턴스
        String gptKey = keysInstance.getGptKey(); // 인스턴스의 키값
        OpenAiService service = new OpenAiService(gptKey,Duration.ofMinutes(9999)); // OpenAI 서비스 연결

        List<ChatMessage> message = new ArrayList<ChatMessage>(); // GPT에게 보낼 메세지 어레이
        message.add(new ChatMessage("user", prompt));
        message.add(new ChatMessage("user", processString));
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder() // OpenAI의 모델 선택하여 메세지 전달후 결과 텍스트 받기
                .messages(message)
                .model(KeyValue.model) // 터보 3.5모델 사용.
                .maxTokens(maxToken) // 입력과 출력중 출력에 할당되는 최대 토큰 값. 현재 입출력 최대 토큰 16,385
                .temperature((double) 0.3f) // 답변의 자유도 설정
                .build();
        result = service.createChatCompletion(completionRequest).getChoices().get(0)
                .getMessage().getContent();
		
	}

}
