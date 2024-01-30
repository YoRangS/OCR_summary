package egovframework.ocr.sample.web;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

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
	
	@Autowired
    private ServletContext servletContext;
	
	@RequestMapping(value = "/tess.do", method = RequestMethod.GET)
	public String test() {
		return "ocr/ocrSampleList";
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

		String fileName = ""; // 파일의 이름
		String prompt = ""; // ChatGPT에게 보낼 명령어
		String result = ""; // 테서렉트를 돌리고 안의 스페이스와 "을 없앤버전
		String pageText = ""; // tessLimit 옵션을 사용할 경우의 각 페이지 마다의 텍스트.
		String preprocessingResult = ""; // ChatGPT에게 오타수정을 요청한 후 텍스트

		int start = 1, end = 1; // 페이지의 시작과 마무리
		String imagePath = "";
		
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
		if ("tess".equals(tessType)) { // 기본값으로 행동할 경우
			System.out.println("Doing tess!");
			result = OcrTesseract.ocrTess(file.getOriginalFilename(), language, UPLOAD_DIR);
		} else if ("tessLimit".equals(tessType)) { // 기본값으로 행동하지 않을 경우
			System.out.println("Not doing tess!");
			
			for (int i = start; i <= end; i++) { 
				System.out.println("Checking for " + i);
				imagePath = UPLOAD_DIR + "image_page_" + i + ".png"; // 각 페이지 마다 임시이미지 파일 생성. 
				System.out.println("Image directory" + imagePath); 
				
				try (PDDocument document = PDDocument.load(new File(fullPath))) { 
					PDFRenderer pdfRenderer = new PDFRenderer(document);
					BufferedImage image = pdfRenderer.renderImageWithDPI(i-1, 300); // 페이지, 300은 이미지 렌더링의 수준. 300은 높은수준.
					ImageIO.write(image, "png", new File(imagePath)); // 이미지를 imagePath의 디렉토리에 image_page 이름으로 저장.
					System.out.println("Saving image done");
				} 
				catch (IOException e) { //Handle the exception appropriately 
					e.printStackTrace(); 
				}
				
				File imgFile = new File(imagePath);
				pageText = OcrTesseract.ocrTess(imgFile.getName(), language, UPLOAD_DIR);
				result = result + pageText + "\n";
				
				imgFile.delete(); 
			}
		}
		System.out.println("debug1");
		prompt = "FIX_TYPO_" + language.toUpperCase(); // FIX_TYPO_KOR, FIX_TYPO_ENG
		preprocessingResult = UseGPT.useGPT(Prompts.getPrompt(prompt), result); // text after using ChatGPT to fix typos
		fileName = file.getOriginalFilename().replaceAll(" ", "_"); // replace all spaces with _ to prevent file name
																	// being lost
		System.out.println(result);
		System.out.println(preprocessingResult);
		System.out.println(fileName);
		System.out.println(language);
		/* Saves results to webpage model */
		model.addAttribute("scan", result);
		model.addAttribute("result", preprocessingResult);
		model.addAttribute("fileName", fileName);
		model.addAttribute("lang", language);
		System.out.println(model.toString());
		if (fullPath != null) { // remove temporary file
			File doDelete = new File(fullPath);
			if (doDelete.exists()) {
				doDelete.delete();
			}
		}

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
		String prompt = "SUMMARY_" + lang.toUpperCase(); // SUMMARY_KOR, SUMMARY_ENG등 언어에 맞는 요약 요청 프롬포트
		String fileTrim = fileName; // .png등 파일 포멧을 떼고 저장하기 위함
		String summaryText = ""; // 요약 텍스트를 보관

		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex != -1) {
			fileTrim = fileName.substring(0, dotIndex); // sample.png, sample.jpg와 같이 .을 기준으로 점 이전의 텍스트만 보관
		} else {
			System.out.println("파일에 . 이 존재하지 않습니다");
		}
		System.out.println("scanResult: " + scanResult);
		summaryText = UseGPT.useGPT(Prompts.getPrompt(prompt), scanResult);
		summaryText = summaryText.replaceAll("\\.", ".\n"); // .뒤에 엔터키를 적용"
		summaryText = summaryText.replaceAll("(?m)^[\\s&&[^\\n]]+|^[\n]", ""); // 엔터키로 인해 생긴 스페이스를 지워줌

		/* 결과들을 웹페이지 모델에 요소들로 추가해줌 */
		model.addAttribute("fileTrim", fileTrim);
		model.addAttribute("summary", summaryText);

		return "ocr/ocrSummary";
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
		String prompt = "TAG_" + lang.toUpperCase(); // TAG_KOR, TAG_ENG등 언어에 맞는 요약 요청 프롬포트
		String jsonTag = ""; // json 형식의 요약 태그를 보관

		System.out.println("prompt: " + prompt);
		System.out.println("getPrompt: " + Prompts.getPrompt(prompt));
		System.out.println("scanResult: " + scanResult);

		jsonTag = UseGPT.useGPT(Prompts.getPrompt(prompt), scanResult);

		/* 결과들을 웹페이지 모델에 요소들로 추가해줌 */
		model.addAttribute("result", scanResult);
		model.addAttribute("lang", lang);
		model.addAttribute("jsonTag", jsonTag);
		model.addAttribute("imgLink", convertToLink(jsonTag));

		return "ocr/ocrTag";
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
		String prompt = "TOP_TAG_" + lang.toUpperCase(); // TAG_KOR, TAG_ENG등 언어에 맞는 요약 요청 프롬포트
		String topTags = ""; // 태그들중 가장 빈도수가 높은 태그 5가지
		String purpose = ""; // 태그를 기반으로 한 텍스트의 의도 추출
		String tagAndText = "";

		System.out.println("prompt: " + prompt);
		System.out.println("getPrompt: " + Prompts.getPrompt(prompt));
		System.out.println("tags: " + jsonTag);

		topTags = UseGPT.useGPT(Prompts.getPrompt(prompt), jsonTag);

		prompt = "PUR_" + lang.toUpperCase();
		tagAndText = topTags + "\n" + scanResult;

		System.out.println("prompt: " + prompt);
		System.out.println("getPrompt: " + Prompts.getPrompt(prompt));
		System.out.println("Top 5 tags: " + topTags);
		System.out.println("Tag and Text: " + tagAndText);

		purpose = UseGPT.useGPT(Prompts.getPrompt(prompt), tagAndText);

		/* 결과들을 웹페이지 모델에 요소들로 추가해줌 */
		model.addAttribute("result", scanResult);
		model.addAttribute("lang", lang);
		model.addAttribute("jsonTag", jsonTag);
		model.addAttribute("purpose", purpose);
		model.addAttribute("imgLink", convertToLink(jsonTag));

		return "ocr/ocrTag";
	}

	private static String convertToLink(String jsonString) {
		// JSON 문자열을 중괄호를 기준으로 나누어 배열로 변환
		String[] keyValuePairs = jsonString.substring(1, jsonString.length() - 1).split(",");

		// 원하는 형식의 문자열로 변환
		StringBuilder queryString = new StringBuilder();

		for (String pair : keyValuePairs) {
			// 각 키-값 쌍을 콜론을 기준으로 나누기
			String[] entry = pair.split(":");

			// 특수 문자 처리를 위해 key를 변환
			String key = entry[0].trim().replace("\"", "").replace(" ", "%20").replace("_", "%5F");

			// 쿼리 스트링에 추가
			queryString.append(key).append(":").append(entry[1].trim()).append(",");
		}

		// 마지막 쉼표 제거
		if (queryString.length() > 0) {
			queryString.deleteCharAt(queryString.length() - 1);
		}

		return "https://quickchart.io/wordcloud?text=" + queryString.toString() + "&useWordList=true";
	}
}

//Custom implementation of MultipartFile for in-memory file representation
class InMemoryMultipartFile implements MultipartFile {
 private final byte[] content;
 private final String name;
 private final String originalFilename;
 private final String contentType;

 public InMemoryMultipartFile(byte[] content, String name) {
     this.content = content;
     this.name = name;
     this.originalFilename = name;
     this.contentType = "image/png"; // Set the appropriate content type
 }

 @Override
 public String getName() {
     return name;
 }

 @Override
 public String getOriginalFilename() {
     return originalFilename;
 }

 @Override
 public String getContentType() {
     return contentType;
 }

 @Override
 public boolean isEmpty() {
     return content.length == 0;
 }

 @Override
 public long getSize() {
     return content.length;
 }

 @Override
 public byte[] getBytes() throws IOException {
     return content;
 }

 @Override
 public InputStream getInputStream() throws IOException {
     return new ByteArrayInputStream(content);
 }

 @Override
 public void transferTo(File dest) throws IOException, IllegalStateException {
     Files.write(dest.toPath(), content);
 }
 
 // Implement other methods of the MultipartFile interface as needed
}