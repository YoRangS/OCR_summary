package egovframework.ocr.sample.web;

import java.util.HashMap;
/**
* ChatGPT 모델에 인풋으로 활용될 명령 저장 클라스
* @author 캡스톤팀 정은총, 송창우
* @since 2023.08
* @version 0.15
* @see OcrSampleController
* 
* == 개정이력(Modification Information) ==
* 수정 내용
* 2023.08 정은총 최초생성
* 2023.08 송창우 사용자의 언어 인풋에 맞추어 활용하기 위해 HashMap 구조 추가
*/
public class Prompts {
    /*ChatGPT에 사용될 명령어들*/
    private static String SUMMARY_ENG = "Summarize the text in maximum 5 lines.";
    private static String FIX_TYPO_ENG = "Fix the typo in the text if there are any.";
    private static String SUMMARY_KOR = "해당 글을 5줄 이내로 요약해줘. 엔터키도 적당히 섞어줘.";
    private static String FIX_TYPO_KOR = "해당 글에 오타가 있을경우 수정해서 출력해줘.";

    private static HashMap<String, String> promptMap = new HashMap<>(); // 명령어들을 모이기 위한 해시맵
    static { // 해시맵에 명령어들 추가
        promptMap.put("SUMMARY_ENG", SUMMARY_ENG);
        promptMap.put("FIX_TYPO_ENG", FIX_TYPO_ENG);
        promptMap.put("SUMMARY_KOR", SUMMARY_KOR);
        promptMap.put("FIX_TYPO_KOR", FIX_TYPO_KOR);
    }

    public static String getPrompt(String key) { //key값과 동일한 명령어 반환
        return promptMap.get(key);
    }
}
