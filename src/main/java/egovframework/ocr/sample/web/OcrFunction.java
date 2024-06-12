package egovframework.ocr.sample.web;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import com.aspose.cells.Workbook;
import com.aspose.slides.Presentation;
import com.aspose.slides.SaveFormat;

import egovframework.rte.fdl.property.EgovPropertyService;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;

@Component
public class OcrFunction {

	@Resource(name="GPTPropertiesService")
    protected EgovPropertyService GPTPropertiesService;
	
	private String UPLOAD_DIR;
	
	@Autowired
    private ServletContext servletContext;
	
    public String convertToPdf(String fullPath, String originalFilename) {
        if (originalFilename != null && !originalFilename.isEmpty()) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();

            if (extension.equals("docx") || extension.equals("doc")) {
                fullPath = docToPdf(fullPath);
            } else if (extension.equals("pptx") || extension.equals("ppt")) {
                fullPath = pptToPdf(fullPath);
            } else if (extension.equals("xlsx") || extension.equals("xls")) {
                try {
                    fullPath = xslToPdf(fullPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (extension.equals("hwp")) {

            }
        }
        return fullPath;
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
	
	public String xslToPdf(String xslPath) {
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
			e.printStackTrace();
		}
		
		return pdfPath;
	}
	public String pptToPdf(String pptPath) {
		String pdfPath = null;
		Presentation presentation = new Presentation(pptPath);
		pdfPath = pptPath.substring(0, pptPath.lastIndexOf('.')) + ".pdf";
		presentation.save(pdfPath, SaveFormat.Pdf);
		
		return pdfPath;
	}
    
    public String transferFile(MultipartFile file) throws IOException {
  	  UPLOAD_DIR = servletContext.getRealPath("/WEB-INF/classes/saveImage/");
      String fullPath = null; // path to upload image file
      if(!file.isEmpty()) {
          fullPath = UPLOAD_DIR + file.getOriginalFilename(); // set path if file is not empty
          System.out.println("File Save fullPath = " + fullPath);
          file.transferTo(new File(fullPath));
      } else {
          System.out.println("isEmpty!");
      }
		return fullPath;
	}
	
    public void removeFile(String fullPath) {
		if (fullPath != null) { // remove temporary file
			File doDelete = new File(fullPath);
			if (doDelete.exists()) {
				doDelete.delete();
			}
		}
	}
	
	public String languageFirst(String language) {
		System.out.println(language);
		int plusIndex = language.indexOf('+');
		if (plusIndex != -1) { // '+'가 발견된 경우
			language = language.substring(0, plusIndex); // kor+eng의 경우 kor만 고려. prompt를 사용할 언어와의 상호작용을 위함
		}
		return language;
	}
    
    public String checkTessType(String language, String tessType, String UPLOAD_DIR, String fullPath, String result,
            int start, int end) {
        String pageText;
        String imagePath;
        if ("tess".equals(tessType)) {
            System.out.println("Doing tess!");
            System.out.println(fullPath.substring(fullPath.lastIndexOf("//") + 1));
            result = OcrTesseract.ocrTess(fullPath.substring(fullPath.lastIndexOf("\\") + 1), language, UPLOAD_DIR);
        } else if ("tessLimit".equals(tessType)) {
            System.out.println("Not doing tess!");

            for (int i = start; i <= end; i++) {
                System.out.println("Checking for " + i);
                imagePath = UPLOAD_DIR + "image_page_" + i + ".png";
                System.out.println("Image directory" + imagePath);

                try (PDDocument document = PDDocument.load(new File(fullPath))) {
                    PDFRenderer pdfRenderer = new PDFRenderer(document);
                    BufferedImage image = pdfRenderer.renderImageWithDPI(i - 1, 300);
                    ImageIO.write(image, "png", new File(imagePath));
                    System.out.println("Saving image done");
                } catch (IOException e) {
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
    
 	public String pageSpecific(String language, String startPage, String endPage, String fullPath, String result,
			int start, int end) {
		String pageText = ""; // PDF의 각 이미지 마다의 텍스트
		String imagePath = ""; // 새롭게 만들어질 각 이미지의 위치
		
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
		return result;
	}
    
    public String summary(String text, String language) {
        String prompt = "SUMMARY_" + language.toUpperCase(); // SUMMARY_KOR, SUMMARY_ENG등 언어에 맞는 요약 요청 프롬포트
        String summaryText = ""; // 요약 텍스트를 보관
        
        summaryText = blockRequest(language, prompt, text, Integer.parseInt(GPTPropertiesService.getString("GPT_MAXOUTPUTTOKEN")));
        summaryText = summaryText.replaceAll("\\.", ".\n"); // .뒤에 엔터키를 적용"
        summaryText = summaryText.replaceAll("(?m)^[\\s&&[^\\n]]+|^[\n]", ""); // 엔터키로 인해 생긴 스페이스를 지워줌

        return summaryText;
    }
    
    public String getPurpose(String scanResult, String language, String topTags) {
   	String prompt;
		String purpose;
		String tagAndText;
		
		prompt = "PUR_" + language.toUpperCase();
        tagAndText = topTags + "\n" + scanResult;
        purpose = blockRequest(language, prompt, tagAndText, Integer.parseInt(GPTPropertiesService.getString("GPT_MAXINPUTTOKEN"))); // topTags 기반으로 텍스트의 의도 추출
         
        System.out.println("[rest] prompt: " + prompt);
        System.out.println("[rest] getPrompt: " + Prompts.getPrompt(prompt));
        System.out.println("[rest] Top 5 tags: " + topTags);
        System.out.println("[rest] Tag and Text: " + tagAndText);
        System.out.println("[rest] Purpose" + purpose);
		return purpose;
	}
	
    public String getTopTags(String language, String jsonTag) {
		String prompt;
		String topTags;
		
		prompt = "TOP_TAG_" + language.toUpperCase(); // TAG_KOR, TAG_ENG등 언어에 맞는 요약 요청 프롬포트
         
        System.out.println("[rest] prompt: " + prompt);
        System.out.println("[rest] getPrompt: " + Prompts.getPrompt(prompt));
        System.out.println("[rest] tags: " + jsonTag);
         
        topTags = blockRequest(language, prompt, jsonTag, Integer.parseInt(GPTPropertiesService.getString("GPT_MAXINPUTTOKEN"))); // 가장 빈도수 높은 태그 5개로 추리기
		return topTags;
	}
	
	public String getTags(String language, String scanResult, String prompt) {
		String jsonTag;
		
		System.out.println("[rest] prompt: " + prompt);
        System.out.println("[rest] getPrompt: " + Prompts.getPrompt(prompt));
        System.out.println("[rest] scanResult: " + scanResult);
         
        jsonTag = blockRequest(language, prompt, scanResult, Integer.parseInt(GPTPropertiesService.getString("GPT_MAXINPUTTOKEN")));
        jsonTag = concatJson(jsonTag);
		return jsonTag;
	}
    
    public String blockRequest(String language, String prompt, String result, int tokenNum) { // 언어, 프롬프트, 결과, 사용 토큰 숫자
		String blockText, blockOutput; // 각 블력의 텍스트, 호출된 결과를 받기 위한 함수
		String mergeResult = ""; // 전체 텍스트. 각 블럭당 호출의 결과물을 합침
		int charNum, blockNum = 0, i, endIndex;
		
		if (GPTPropertiesService == null) {
	        throw new IllegalStateException("GPTPropertiesService is not initialized.");
	    }
		
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
	
	public String concatJson(String jsonString) {
		jsonString = jsonString.replace("}{", ", ");
		jsonString = jsonString.replace("} {", ", ");
		return jsonString;
	}
	
	public String convertToLink(String jsonString) {
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
	
	public void addTextExtract(String fileName, String language, Model model, String result,
			String preprocessingResult) {
		/* Saves results to webpage model */
		model.addAttribute("scan", result);
		model.addAttribute("result", preprocessingResult);
		model.addAttribute("fileName", fileName);
		model.addAttribute("lang", language);
	}
	
}
