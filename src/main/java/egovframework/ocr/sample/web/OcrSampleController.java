package egovframework.ocr.sample.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;


import egovframework.rte.fdl.property.EgovPropertyService;

/**
 * jsp파일들의 호출을 처리하는 컨트롤러 클래스
 * 
 * @author 캡스톤팀 정은총, 송창우
 * @since 2023.08
 * @version 0.15
 * @see ocrSampleList.jsp
 * @see ocrSummary.jsp
 * @see OcrTesseract.java
 * 
 *      == 개정이력(Modification Information) == 수정 내용 2023.08 정은총 최초 생성 2023.08 송창우
 *      언어 입력, 파일 위치 처리 추가
 */
@Controller
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class OcrSampleController {
	
	private int maxInputToken = 16385;
	private int maxOutputToken = 4096;

	@Autowired
    private ServletContext servletContext;

	@Resource(name="GPTPropertiesService")
    protected EgovPropertyService GPTPropertiesService;
	
	private final OcrFunction ocrFunction;
	@Autowired
    public OcrSampleController(OcrFunction ocrFunction) {
        this.ocrFunction = ocrFunction;
    }
	
	@RequestMapping(value = "/tess.do", method = RequestMethod.GET) // 시작 페이지로 가기
	public String test() {
		return "ocr/01_ocr/ocrSelectFile";
	}

	@RequestMapping(value = "/goToCrop.do", method = RequestMethod.GET) // 이미지 자르기 페이지로 가기
	public String goToCrop() {
		return "ocr/ocrCrop";
	}
	
	/**
	 * tess.do이름의 POST 타입 호출을 받아 텍스트 추출
	 * 
	 * @param file  이미지/pdf 폴더
	 * @param lang  오타수정에 사용할 언어
	 * @param model 페이지모델
	 * @return ocrSampleList 화면
	 * @see ocrTsseract.java
	 * @see UseGPT.useGPT
	 */
	@RequestMapping(value = "/tess.do", method = RequestMethod.POST)
	public String tess(@RequestParam MultipartFile file, String language, Model model, String tessType,
			String startPage, String endPage) throws IOException, ServletException {
		
		String UPLOAD_DIR = servletContext.getRealPath("/WEB-INF/classes/saveImage/");
		String fullPath = null;
		System.out.println(UPLOAD_DIR);
		
		if (!file.isEmpty()) {
			fullPath =  UPLOAD_DIR + file.getOriginalFilename();
			file.transferTo(new File(fullPath));
		} else {
			System.out.println("isEmpty!");
		}
		
		System.out.println("fullPath: " + fullPath);
		System.out.println(file.getOriginalFilename());
		
		String originalFilename = file.getOriginalFilename();
        fullPath = ocrFunction.convertToPdf(fullPath, originalFilename);

		String fileName = ""; // 파일의 이름
		String prompt = ""; // ChatGPT에게 보낼 명령어
		String result = ""; // 테서렉트를 돌리고 안의 스페이스와 "을 없앤버전
		String preprocessingResult = ""; // ChatGPT에게 오타수정을 요청한 후 텍스트
		String afterDetectResult = ""; // 민감정보 검출 후 텍스트
		
		int start = 1, end = 1; // 페이지의 시작과 마무리
		
		try {
			start = Integer.parseInt(startPage); // tessLimit 옵션 선택시 시작 페이지
			end = Integer.parseInt(endPage); // tessLimit 옵션 선택시 끝나는 페이지
			if (start > end) { // 시작 페이지 값이 끝 페이지보다 큰 경우
				int tmp = start;
				start = end;
				end = tmp;
			}
		} catch (NumberFormatException e) { // start 와 end가 숫자로 변환되지 않을 경우 오류 출력
			e.printStackTrace();
		}

		System.out.println("Tess type:" + tessType);
		result = ocrFunction.checkTessType(language, tessType, UPLOAD_DIR, fullPath, result, start, end);
		
		System.out.println("result: " + result);
		
		language = ocrFunction.languageFirst(language).toUpperCase();
		prompt = "FIX_TYPO_" + language;
		preprocessingResult = ocrFunction.blockRequest(language, prompt, result, maxOutputToken);
		System.out.println("prompt: " + Prompts.getPrompt(prompt));
		System.out.println("preprocessingResult: " + preprocessingResult);
		prompt = "DETECT_SEN_" + language; // DETECT_SEN_KOR, DETECT_SEN_ENG
		afterDetectResult = ocrFunction.blockRequest(language, prompt, preprocessingResult, maxOutputToken);
		fileName = file.getOriginalFilename().replaceAll(" ", "_"); // replace all spaces with _ to prevent file name
		
		ocrFunction.addTextExtract(fileName, language, model, result, afterDetectResult);
		
		ocrFunction.removeFile(fullPath);

		return "ocr/01_ocr/ocrTessResult";
	}
	
	/**
	 * cropTess.do이름의 POST 타입 호출을 받아 텍스트 추출
	 * 
	 * @param cropImageURL  원본에서 자른 이미지를 URL 형태로 받아들여 이후 재조립
	 * @param fileName 원본 파일의 이름
	 * @param lang  오타수정에 사용할 언어
	 * @param model 페이지모델
	 * @return ocrSampleList 화면
	 * @see ocrTsseract.java
	 * @see UseGPT.useGPT
	 */
	@RequestMapping(value = "/cropTess.do", method = RequestMethod.POST)
	public String cropTess(@RequestParam String cropImageURL,String fileName, String language, Model model)
			throws IOException, ServletException {

		String UPLOAD_DIR = servletContext.getRealPath("/WEB-INF/classes/saveImage/");
		String fullPath = null;
		String[] imageParts = cropImageURL.split(",");
        String base64Image = imageParts[1];
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        
		System.out.println(UPLOAD_DIR+"\n");
		
		System.out.println("The image url is" + cropImageURL);
		
        fullPath = UPLOAD_DIR + fileName;
        
        try {
            // Write the decoded data to a file
            try (FileOutputStream fos = new FileOutputStream(new File(fullPath))) {
                fos.write(imageBytes);
            }

            System.out.println("Image successfully saved to: " + fullPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
		String prompt = ""; // ChatGPT에게 보낼 명령어
		String result = ""; // 테서렉트를 돌리고 안의 스페이스와 "을 없앤버전
		String preprocessingResult = ""; // ChatGPT에게 오타수정을 요청한 후 텍스트
		String afterDetectResult = ""; // 민감정보 검출 후 텍스트
		System.out.println("Doing tess!"); 
		
		result = OcrTesseract.ocrTess(fileName, language, UPLOAD_DIR);
		
		language = ocrFunction.languageFirst(language).toUpperCase(); // kor+eng의 경우 prompt를 kor로 하기 위함
		prompt = "FIX_TYPO_" + language;
		preprocessingResult = ocrFunction.blockRequest(language, prompt, result, maxOutputToken);
		prompt = "DETECT_SEN_" + language.toUpperCase(); // DETECT_SEN_KOR, DETECT_SEN_ENG
		afterDetectResult = ocrFunction.blockRequest(language, prompt, preprocessingResult, maxOutputToken);
		fileName = fileName.replaceAll(" ", "_"); // replace all spaces with _ to prevent file name being lost
		
		System.out.println(result);
		System.out.println(preprocessingResult);
		System.out.println(fileName);
		System.out.println(language);
		
		ocrFunction.addTextExtract(fileName, language, model, result, afterDetectResult);
		
		System.out.println(model.toString());
		
		ocrFunction.removeFile(fullPath);
		
		return "ocr/ocrSampleList";
	}
	
	/**
	 * summary.do이름의 POST 타입 호출을 받아 텍스트 요약
	 * 
	 * @param scanResult 추출하여 오타수정을 거친 텍스트
	 * @param lang       텍스트 요약에 사용할 언어
	 * @param fileName   이미지파일의 이름
	 * @param model      페이지모델
	 * @return ocrSummary 화면
	 * @see Prompts.java
	 * @see UseGPT.useGPT
	 */
	@RequestMapping(value = "/summary.do", method = RequestMethod.POST)
	public String summary(@RequestParam String scanResult, String fileName, String lang, Model model) {
		lang = ocrFunction.languageFirst(lang).toUpperCase(); // kor+eng의 경우 prompt를 kor로 하기 위함
		String fileTrim = fileName; // .png등 파일 포멧을 떼고 저장하기 위함
		String summaryText = ""; // 요약 텍스트를 보관
		
		int dotIndex = fileName.lastIndexOf('.');
		
		if (dotIndex != -1) {
			fileTrim = fileName.substring(0, dotIndex); // sample.png, sample.jpg와 같이 .을 기준으로 점 이전의 텍스트만 보관
		} else {
			System.out.println("파일에 . 이 존재하지 않습니다");
		}

		summaryText = ocrFunction.summary(scanResult, lang);

		/* 결과들을 웹페이지 모델에 요소들로 추가해줌 */
		model.addAttribute("fileTrim", fileTrim);
		model.addAttribute("summary", summaryText);

		return "ocr/01_ocr/ocrSummary";
	}

	/**
	 * data.do이름의 POST 타입 호출을 받아 텍스트를 지정경로에 텍스트 파일로 저장
	 * 
	 * @param summary  ChatGPT를 통해요약된 텍스트
	 * @param fileTrim 원본파일 이름
	 * @param location 파일이 저장될
	 * @param model    페이지모델
	 * @return ocrSummary 화면
	 */
	@RequestMapping(value = "/data.do", method = RequestMethod.POST)
	public String saveData(@RequestParam String summary, String fileTrim, String location, Model model) {
		String fileLoc = ""; // 파일이 최종 저장될 경로와 파일 이름을 합친 형태
		String message = ""; // 파일 저장 결과 출력 메세지

		System.out.println(fileLoc);
		if (location.endsWith("/")) {
			fileLoc = location + fileTrim + ".txt"; // 파일위치 location이 / 로 끝날경우 .txt만 추가함
		} else {
			fileLoc = location + "/" + fileTrim + ".txt"; // 파일위치 location이 /로 끝나지 않을 경우 /를 추가함
		}

		try { // fileLoc에 표시된 주소/파일이름.txt에 summary의 요약내용을 넣어 새로운 .txt파일 생성
			FileWriter writer = new FileWriter(fileLoc); // 파일처리를 위한 변수

			writer.write(summary);

			writer.close();

			message = "파일생성 완료!"; // 파일 저장 성공시 메세지
		} catch (IOException e) {
			message = "파일생성 실패: " + e.getMessage();
		}

		/* 결과들을 웹페이지 모델에 요소들로 추가해줌 */
		model.addAttribute("summary", summary);
		model.addAttribute("fileTrim", fileTrim);
		model.addAttribute("message", message);
		model.addAttribute("location", location);

		return "ocr/ocrSummary";
	}

	/**
	 * tag.do이름의 POST 타입 호출을 받아 태그 생성
	 * 
	 * @param scanResult 추출하여 오타수정을 거친 텍스트
	 * @param lang       텍스트 요약에 사용할 언어
	 * @param model      페이지모델
	 * @return ocrTag 화면
	 * @see Prompts.java
	 * @see UseGPT.useGPT
	 */
	@RequestMapping(value = "/tag.do", method = RequestMethod.POST)
	public String vision(@RequestParam String scanResult, String lang, Model model) {
		lang = ocrFunction.languageFirst(lang).toUpperCase(); // kor+eng의 경우 prompt를 kor로 하기 위함
		String prompt = "TAG_" + lang; // TAG_KOR, TAG_ENG등 언어에 맞는 요약 요청 프롬포트
		String jsonTag = ""; // json 형식의 요약 태그를 보관

		System.out.println("prompt: " + prompt);
		System.out.println("getPrompt: " + Prompts.getPrompt(prompt));
		System.out.println("scanResult: " + scanResult);

		jsonTag = ocrFunction.blockRequest(lang, prompt, scanResult, maxInputToken);
		jsonTag = ocrFunction.concatJson(jsonTag);
		/* 결과들을 웹페이지 모델에 요소들로 추가해줌 */
		model.addAttribute("result", scanResult);
		model.addAttribute("lang", lang);
		model.addAttribute("jsonTag", jsonTag);
		model.addAttribute("imgLink", ocrFunction.convertToLink(jsonTag));

		return "ocr/01_ocr/ocrTag";
	}

	/**
	 * purpose.do이름의 POST 타입 호출을 받아 태그 기반의 텍스트의 의도 추출
	 * 
	 * @param scanResult 추출하여 오타수정을 거친 텍스트
	 * @param lang       텍스트 요약에 사용할 언어
	 * @param jsonTag    텍스트로 부터 만들어진 태그
	 * @param model      페이지모델
	 * @return ocrTag 화면
	 * @see Prompts.java
	 * @see UseGPT.useGPT
	 */
	@RequestMapping(value = "/purpose.do", method = RequestMethod.POST)
	public String purpose(@RequestParam String scanResult, String lang, String jsonTag, Model model) {
		lang = ocrFunction.languageFirst(lang).toUpperCase(); // kor+eng의 경우 prompt를 kor로 하기 위함
		String prompt = "TOP_TAG_" + lang; // TAG_KOR, TAG_ENG등 언어에 맞는 요약 요청 프롬포트
		String topTags = ""; // 태그들중 가장 빈도수가 높은 태그 5가지
		String purpose = ""; // 태그를 기반으로 한 텍스트의 의도 추출
		String tagAndText = "";

		System.out.println("prompt: " + prompt);
		System.out.println("getPrompt: " + Prompts.getPrompt(prompt));
		System.out.println("tags: " + jsonTag);

		topTags = ocrFunction.blockRequest(lang, prompt, scanResult, maxInputToken);
		topTags = ocrFunction.concatJson(topTags);
		
		prompt = "PUR_" + lang;
		tagAndText = topTags + "\n" + scanResult;

		System.out.println("prompt: " + prompt);
		System.out.println("getPrompt: " + Prompts.getPrompt(prompt));
		System.out.println("Top 5 tags: " + topTags);
		System.out.println("Tag and Text: " + tagAndText);

		purpose = ocrFunction.blockRequest(lang, prompt, tagAndText, maxInputToken);

		/* 결과들을 웹페이지 모델에 요소들로 추가해줌 */
		model.addAttribute("result", scanResult);
		model.addAttribute("lang", lang);
		model.addAttribute("jsonTag", jsonTag);
		model.addAttribute("purpose", purpose);
		model.addAttribute("imgLink", ocrFunction.convertToLink(jsonTag));

		return "ocr/01_ocr/ocrTag";
	}
}