package egovframework.ocr.sample.web;
/**
* OpenAI API 연결에 필요한 Key값을 보관
* 임시로 정의된 이름과 값을 가지고 있으므로 실제 사용시 반드시 바꾸어 주어야 한다
* @author 캡스톤팀 정은총, 송창우, 조성준
* @since 2024.01
* @version 0.15
* @see Keys
*/
/*KeyValue_tmp라는 이름을 반드시 KeyValue로 바꾸어야 한다.*/
public class KeyValue_tmp {
	/*OpenAI 모델을 사용함에 필요한 키값. 반드시 본인의 키값으로 바꾸어야 한다.*/
	public static String gptKey = "Your key from OpenAI - https://platform.openai.com/docs/introduction";
	
	/*임시 이미지를 저장하는 위치. 반드시 본인이 파일을 임시 저장할 위치로 바꾸어야 한다. 또한 끝은 \\ 으로 두어야 한다.*/
	public static String uploadDir = "Directory name to change\\" + "eGovFrameDev-3.10.0-64bit\\workspace\\OCR_summary-main\\src\\main\\java\\saveImage\\";
}