package egovframework.ocr.sample.web;

import java.io.File;

import org.springframework.stereotype.Component;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.lang3.SystemUtils;

@Component("OcrTess")
public class OcrTesseract {

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
	
	 public static String ocrTess(String imgName, String lang, String filePath) {
	     Tesseract tesseract = new Tesseract();  // JNA Interface Mapping
	     
	     if(SystemUtils.IS_OS_WINDOWS) // windows OS
	     {
	    	 tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata"); // replace with your tessdata path
	     }
	     else if(SystemUtils.IS_OS_MAC) // macintosh OS
	     {
		     tesseract.setDatapath("/usr/local/Cellar/tesseract"); // replace with your tesseract path
	     }

	     tesseract.setTessVariable("user_defined_dpi", "300"); // sets dpi to avoid warning message
	     String imageLocation = filePath + imgName;
	     
	     /* TODO: 언어 인식 기능 넣기 */
	     String language = lang;
	     
	     
	     return showText(tesseract, imageLocation, language);
	   }
}
