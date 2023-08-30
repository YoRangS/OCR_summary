package egovframework.ocr.sample.web;
/**
* OpenAI API 연결에 필요한 Key값을 보관
* @author 캡스톤팀 정은총, 송창우
* @since 2023.08
* @version 0.15
* @see OcrSampleController
* 사용법: Keys variable1 = Keys.getInstance();
*     String variable2 = variable1.getGptKey();
* 
* == 개정이력(Modification Information) ==
* 수정 내용
* 2023.08 정은총 최초생성
* 2023.08 송창우 GPT_KEY 함수를 private 지정, GPT_KEY 값을 불러오기 위한 getGptKey 추가
*/
public class Keys {
    /*Key*/
    public static Keys keys;
    /*API 연결에 필요한 키값*/
    private static String gptKey = KeyValue.gptKey;
    
    
    /*키 인스턴스 생선*/
    public static Keys getInstance() {
        if (keys == null)
            keys = new Keys();
        return keys;
    }
    
    /*키값 불러오기*/
    public String getGptKey() {
        return gptKey;
    }
}
