package egovframework.ocr.sample.web;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.aspose.cells.Shape;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.aspose.slides.Presentation;
import com.aspose.slides.SaveFormat;

import egovframework.rte.fdl.property.EgovPropertyService;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;

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
        fullPath = convertToPdf(fullPath, originalFilename);

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
		result = checkTessType(language, tessType, UPLOAD_DIR, fullPath, result, start, end);
		
		System.out.println("result: " + result);
		
		language = languageFirst(language).toUpperCase();
		prompt = "FIX_TYPO_" + language;
		preprocessingResult = blockRequest(language, prompt, result, maxOutputToken);
		System.out.println("prompt: " + Prompts.getPrompt(prompt));
		System.out.println("preprocessingResult: " + preprocessingResult);
		prompt = "DETECT_SEN_" + language; // DETECT_SEN_KOR, DETECT_SEN_ENG
		afterDetectResult = blockRequest(language, prompt, preprocessingResult, maxOutputToken);
		fileName = file.getOriginalFilename().replaceAll(" ", "_"); // replace all spaces with _ to prevent file name
		
		addTextExtract(fileName, language, model, result, afterDetectResult);
		
		removeFile(fullPath);

		return "ocr/01_ocr/ocrTessResult";
	}

	private String convertToPdf(String fullPath, String originalFilename) {
		if (originalFilename != null && !originalFilename.isEmpty()) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
            
            if (extension.equals("docx") || extension.equals("doc")) {
            	fullPath = docToPdf(fullPath);
            }
            else if (extension.equals("pptx") || extension.equals("ppt")) {
            	fullPath = pptToPdf(fullPath);
            }
            else if (extension.equals("xlsx") || extension.equals("xls")) {
            	try {
					fullPath = xslToPdf(fullPath);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            else if (extension.equals("hwp")) {
            	
            }
        }
		return fullPath;
	}
	
	private String checkTessType(String language, String tessType, String UPLOAD_DIR, String fullPath, String result,
			int start, int end) {
		String pageText; // tessLimit 옵션을 사용할 경우의 각 페이지 마다의 텍스트.
		String imagePath; // 이미지 경로
		if ("tess".equals(tessType)) { // 기본값으로 행동할 경우
			System.out.println("Doing tess!");
			System.out.println(fullPath.substring(fullPath.lastIndexOf("//") + 1));
			result = OcrTesseract.ocrTess(fullPath.substring(fullPath.lastIndexOf("\\") + 1), language, UPLOAD_DIR);
//			result = OcrTesseract.ocrTess(file.getOriginalFilename(), language, UPLOAD_DIR);
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
		return result;
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
		
		language = languageFirst(language).toUpperCase(); // kor+eng의 경우 prompt를 kor로 하기 위함
		prompt = "FIX_TYPO_" + language;
		preprocessingResult = blockRequest(language, prompt, result, maxOutputToken);
		prompt = "DETECT_SEN_" + language.toUpperCase(); // DETECT_SEN_KOR, DETECT_SEN_ENG
		afterDetectResult = blockRequest(language, prompt, preprocessingResult, maxOutputToken);
		fileName = fileName.replaceAll(" ", "_"); // replace all spaces with _ to prevent file name being lost
		
		System.out.println(result);
		System.out.println(preprocessingResult);
		System.out.println(fileName);
		System.out.println(language);
		
		addTextExtract(fileName, language, model, result, afterDetectResult);
		
		System.out.println(model.toString());
		
		removeFile(fullPath);
		
		return "ocr/ocrSampleList";
	}

	private void addTextExtract(String fileName, String language, Model model, String result,
			String preprocessingResult) {
		/* Saves results to webpage model */
		model.addAttribute("scan", result);
		model.addAttribute("result", preprocessingResult);
		model.addAttribute("fileName", fileName);
		model.addAttribute("lang", language);
	}
	
	private void removeFile(String fullPath) {
		if (fullPath != null) { // remove temporary file
			File doDelete = new File(fullPath);
			if (doDelete.exists()) {
				doDelete.delete();
			}
		}
	}
	
	private String languageFirst(String language) {
		System.out.println(language);
		int plusIndex = language.indexOf('+');
		if (plusIndex != -1) { // '+'가 발견된 경우
			language = language.substring(0, plusIndex); // kor+eng의 경우 kor만 고려. prompt를 사용할 언어와의 상호작용을 위함
		}
		return language;
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
		lang = languageFirst(lang).toUpperCase(); // kor+eng의 경우 prompt를 kor로 하기 위함
		String prompt = "SUMMARY_" + lang; // SUMMARY_KOR, SUMMARY_ENG등 언어에 맞는 요약 요청 프롬포트
		String fileTrim = fileName; // .png등 파일 포멧을 떼고 저장하기 위함
		String summaryText = ""; // 요약 텍스트를 보관
		
		
		int dotIndex = fileName.lastIndexOf('.');
		
		if (dotIndex != -1) {
			fileTrim = fileName.substring(0, dotIndex); // sample.png, sample.jpg와 같이 .을 기준으로 점 이전의 텍스트만 보관
		} else {
			System.out.println("파일에 . 이 존재하지 않습니다");
		}
		
		System.out.println("scanResult: " + scanResult);
		summaryText = blockRequest(lang, prompt, scanResult, maxInputToken);
		summaryText = summaryText.replaceAll("\\.", ".\n"); // .뒤에 엔터키를 적용"
		summaryText = summaryText.replaceAll("(?m)^[\\s&&[^\\n]]+|^[\n]", ""); // 엔터키로 인해 생긴 스페이스를 지워줌

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
		lang = languageFirst(lang).toUpperCase(); // kor+eng의 경우 prompt를 kor로 하기 위함
		String prompt = "TAG_" + lang; // TAG_KOR, TAG_ENG등 언어에 맞는 요약 요청 프롬포트
		String jsonTag = ""; // json 형식의 요약 태그를 보관

		System.out.println("prompt: " + prompt);
		System.out.println("getPrompt: " + Prompts.getPrompt(prompt));
		System.out.println("scanResult: " + scanResult);

		jsonTag = blockRequest(lang, prompt, scanResult, maxInputToken);
		jsonTag = concatJson(jsonTag);
		/* 결과들을 웹페이지 모델에 요소들로 추가해줌 */
		model.addAttribute("result", scanResult);
		model.addAttribute("lang", lang);
		model.addAttribute("jsonTag", jsonTag);
		model.addAttribute("imgLink", convertToLink(jsonTag));

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
		lang = languageFirst(lang).toUpperCase(); // kor+eng의 경우 prompt를 kor로 하기 위함
		String prompt = "TOP_TAG_" + lang; // TAG_KOR, TAG_ENG등 언어에 맞는 요약 요청 프롬포트
		String topTags = ""; // 태그들중 가장 빈도수가 높은 태그 5가지
		String purpose = ""; // 태그를 기반으로 한 텍스트의 의도 추출
		String tagAndText = "";

		System.out.println("prompt: " + prompt);
		System.out.println("getPrompt: " + Prompts.getPrompt(prompt));
		System.out.println("tags: " + jsonTag);

		topTags = blockRequest(lang, prompt, scanResult, maxInputToken);
		topTags = concatJson(topTags);
		
		prompt = "PUR_" + lang;
		tagAndText = topTags + "\n" + scanResult;

		System.out.println("prompt: " + prompt);
		System.out.println("getPrompt: " + Prompts.getPrompt(prompt));
		System.out.println("Top 5 tags: " + topTags);
		System.out.println("Tag and Text: " + tagAndText);

		purpose = blockRequest(lang, prompt, tagAndText, maxInputToken);

		/* 결과들을 웹페이지 모델에 요소들로 추가해줌 */
		model.addAttribute("result", scanResult);
		model.addAttribute("lang", lang);
		model.addAttribute("jsonTag", jsonTag);
		model.addAttribute("purpose", purpose);
		model.addAttribute("imgLink", convertToLink(jsonTag));

		return "ocr/01_ocr/ocrTag";
	}
	
	private String concatJson(String jsonString) {
		jsonString = jsonString.replace("}{", ", ");
		jsonString = jsonString.replace("} {", ", ");
		return jsonString;
	}
	
	private String blockRequest(String language, String prompt, String result, int tokenNum) { // 언어, 프롬프트, 결과, 사용 토큰 숫자
		String blockText, blockOutput; // 각 블력의 텍스트, 호출된 결과를 받기 위한 함수
		String mergeResult = ""; // 전체 텍스트. 각 블럭당 호출의 결과물을 합침
		int charNum, blockNum = 0, i, endIndex;
		
		charNum = result.length();
		System.out.println("charNum: " + charNum);
		blockNum = (charNum/tokenNum) + 1; // 텍스트 글자수 / 전체 입력 토큰의 반올림을 블럭의 갯수로 정함
		System.out.println("blockNum: " + blockNum);
		
		for (i = 0; i < blockNum; i++) { // 블럭 갯수 만큼 반복
			System.out.println("Start");
			endIndex = Math.min((i + 1) * tokenNum, charNum); // 인덱스의 끝부분 표시. 최대를 넘지 않도록 비교함
			blockText = result.substring(i * tokenNum, endIndex); // 현재 인덱스에서 끝 인덱스까지
			System.out.println("blockText: " + blockText);
			System.out.println("Promt: " + Prompts.getPrompt(prompt));
			UseGPT gpt = new UseGPT(GPTPropertiesService.getString("GPT_KEY"),
	    			GPTPropertiesService.getString("GPT_MODEL"),
	    			Integer.parseInt(GPTPropertiesService.getString("GPT_MAXINPUTTOKEN")),
	    			Integer.parseInt(GPTPropertiesService.getString("GPT_MAXOUTPUTTOKEN")));
			blockOutput = gpt.useGPT(Prompts.getPrompt(prompt), blockText); // 블럭에 대한 요청을 받기
			System.out.println("End");
			mergeResult = mergeResult.concat(blockOutput); // GPT 호출 내용 합치기
		}
		
		return mergeResult;
	}
	
	private static String convertToLink(String jsonString) {
		// JSON 문자열을 중괄호를 기준으로 나누어 배열로 변환
		System.out.println("Json to deal with: " + jsonString);
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
	
	public String docToPdf(String docPath) {
		String pdfPath = null;
	    try {
	        InputStream doc = new FileInputStream(new File(docPath));
	        XWPFDocument document = new XWPFDocument(doc);
	        PdfOptions options = PdfOptions.create();
	        
	        pdfPath = docPath.substring(0, docPath.lastIndexOf('.')) + ".pdf";
	        
	        OutputStream out = new FileOutputStream(new File(pdfPath));
	        PdfConverter.getInstance().convert(document, out, options);
	    } catch (IOException ex) {
	        System.out.println(ex.getMessage());
	    }
	    return pdfPath;
	}
	
	private String xslToPdf(String xslPath) {
		String pdfPath = null;
		Workbook workbook;
		try {
			System.out.println("start");
			workbook = new Workbook(xslPath);
			pdfPath = xslPath.substring(0, xslPath.lastIndexOf('.')) + ".pdf";
			System.out.println("pdfPath : " + pdfPath);
//			Worksheet ws = workbook.getWorksheets().get(0);
//			Shape sh = ws.getShapes().get(0);
//			sh.getFill().getTextureFill().setTiling(true);
			workbook.save(pdfPath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return pdfPath;
	}
	private String pptToPdf(String pptPath) {
		String pdfPath = null;
		Presentation presentation = new Presentation(pptPath);
		pdfPath = pptPath.substring(0, pptPath.lastIndexOf('.')) + ".pdf";
		presentation.save(pdfPath, SaveFormat.Pdf);
		
		return pdfPath;
	}
}