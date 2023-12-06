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
    private static String TAG_ENG = "The following is the text we want to get the tags for."
    		+ "From the text extract the keywords and their frequency and show them in the form of json format {Example: 3} without other explanation.";
    private static String SUMMARY_KOR = "해당 글을 5줄 이내로 요약해줘. 엔터키도 적당히 섞어줘.";
    private static String FIX_TYPO_KOR = "해당 글에 오타가 있을경우 수정해서 출력해줘.";
    private static String TAG_KOR = "다음 문장은 주요 태그를 추출하려고 하는 텍스트 전문입니다."
    		+ "아래 텍스트에서 중요한 키워드와 빈도수를 같이 추출한 뒤 다른 설명 없이 {단어: 3} 와 같이 json 형식으로만 보여주세요.";

    private static HashMap<String, String> promptMap = new HashMap<>(); // 명령어들을 모이기 위한 해시맵
    static { // 해시맵에 명령어들 추가
        promptMap.put("SUMMARY_ENG", SUMMARY_ENG);
        promptMap.put("FIX_TYPO_ENG", FIX_TYPO_ENG);
        promptMap.put("TAG_ENG", TAG_ENG);
        promptMap.put("SUMMARY_KOR", SUMMARY_KOR);
        promptMap.put("FIX_TYPO_KOR", FIX_TYPO_KOR);
        promptMap.put("TAG_KOR", TAG_KOR);
    }

    public static String getPrompt(String key) { //key값과 동일한 명령어 반환
        return promptMap.get(key);
    }
}
