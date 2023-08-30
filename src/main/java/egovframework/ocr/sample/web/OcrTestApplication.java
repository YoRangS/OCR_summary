package egovframework.ocr.sample.web;

import java.io.File;

import org.springframework.stereotype.Component;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Component("OcrTest")
public class OcrTestApplication {

	private static String showText(Tesseract tesseract, String imagelocation, String language) {
		tesseract.setLanguage(language);
		File imageFile = new File(imagelocation);
		System.out.println(imagelocation);
		System.out.println("-----------------");
		String result = "";
		try {
			String _result = tesseract.doOCR(imageFile);
			//System.out.println(result);
			result = _result.replaceAll("\\s+", " "); // ‘\s‘ to match a whitespace character. Remove unnecessary enters white spaces
			result = result.replaceAll("\"", ""); // remove double quotes in string.
//			System.out.println(result);
			return result;
		} catch (TesseractException e) {
	         System.err.println(e.getMessage());
	         return "";
		}
	}
	
	 public static String OcrTest(String imgName, String lang) {
	     Tesseract tesseract = new Tesseract();  // JNA Interface Mapping
	     tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata"); // replace with your tessdata path

	     tesseract.setTessVariable("user_defined_dpi", "300"); // sets dpi to avoid warning message
	     String filePath = OcrSampleController.UPLOAD_DIR;
	     String imageLocation = filePath + imgName;
	     
	     /* TODO: 언어 인식 기능 넣기 */
	     String language = lang;
	     
	     
	     return showText(tesseract, imageLocation, language);
	   }
}
