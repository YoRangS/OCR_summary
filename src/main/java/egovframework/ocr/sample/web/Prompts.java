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
    private static String FIX_TYPO_ENG = "Fix the typo in the text if there are any and then show the text regardless of typo.";
    private static String TAG_ENG = "The following is the text we want to get the tags for."
    		+ " From the text extract the keywords and their frequency and show them in the form of json format {Example: 3} without other explanation."
    		+ " Punctuation mark should not be considered as a keyword except\"\"."
    		+ " If there is \",\" like \"100,000\" inside the keyword, remove the \",\" and add the result such as \"100000\" as a keyword into the json. "
    		+ " If the nunber of keyword is over 30, show only top 30 keywords with highest frequency.";
    private static String TOP_TAG_ENG = "The follow json is a list of tags. From the json extract 5 tags with the most frequency and show them in the form of json format {Example: 3} without other explanation.";
    private static  String PUR_ENG = "The following json and text are the json and text for extracting the purpose of the text." 
    		+ " Try to figure out the purpose of the text based on the tags in short sentence and show them in the form of json format {Purpose: } without other explanation.";
    private static String SUMMARY_KOR = "해당 글을 5줄 이내로 요약해주세요. 엔터키도 적당히 섞어주세요.";
    private static String FIX_TYPO_KOR = "해당 텍스트를 그대로 출력해주세요. 오타가 있을 경우 수정하여 출력하고, 오타를 어떻게 고쳤는지에 대해 설명을 생략해주세요.";
    private static String TAG_KOR = "다음 문장은 주요 태그를 추출하려고 하는 텍스트 전문입니다."
    		+ " 아래 텍스트에서 중요한 키워드와 빈도수를 같이 추출한 뒤 다른 설명 없이 {단어: 3} 와 같이 json 형식으로만 보여주세요. \"\" 를 제외한 문장 부호는 태그에 추가하면 안됩니다."
    		+ " \"100,000\"와 같이 키워드 안에 \",\"가 있을경우 \",\"를 제거하여 \"100000\"와 같은 형태로 키워드를 json에 추가하여 주세요."
    		+ " 키워드의 갯수가 30개 이상일경우 빈도수가 가장 높은 30개의 키워드만 보여주세요.";
    private static String TOP_TAG_KOR = "다음 json은 태그의 모음집입니다. json에서 가장 빈도수가 높은 5개의 태그를 다른 설명 없이 {예시: 3} 와 같이 json 형식으로만 보여주세요.";
    private static String PUR_KOR = "다음 json와 텍스트는 의도파악을 위해 사용하려는 태그와 텍스트 입니다."
    		+ " 다음의 텍스트를 태그를 기반으로 의도를 파악하여 다른 설명없이 짧은 문장으로 {의도: }와 같은 json 형식으로만 보여주세요.";

    private static HashMap<String, String> promptMap = new HashMap<>(); // 명령어들을 모이기 위한 해시맵
    static { // 해시맵에 명령어들 추가
        promptMap.put("SUMMARY_ENG", SUMMARY_ENG);
        promptMap.put("FIX_TYPO_ENG", FIX_TYPO_ENG);
        promptMap.put("TAG_ENG", TAG_ENG);
        promptMap.put("TOP_TAG_ENG", TOP_TAG_ENG);
        promptMap.put("PUR_ENG", PUR_ENG);
        promptMap.put("SUMMARY_KOR", SUMMARY_KOR);
        promptMap.put("FIX_TYPO_KOR", FIX_TYPO_KOR);
        promptMap.put("TAG_KOR", TAG_KOR);
        promptMap.put("TOP_TAG_KOR", TOP_TAG_KOR);
        promptMap.put("PUR_KOR", PUR_KOR);
    }

    public static String getPrompt(String key) { //key값과 동일한 명령어 반환
        return promptMap.get(key);
    }
}
