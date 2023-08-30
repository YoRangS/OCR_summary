package egovframework.ocr.example.sample.service;

import java.util.List;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import egovframework.ocr.sample.service.OcrService;
import egovframework.ocr.sample.service.OcrVO;

public class OcrServiceClient {
	public static void main(String[] args) throws Exception {
		// 1. Spring 컨테이너를 구동한다.
		AbstractApplicationContext container = new GenericXmlApplicationContext(
				"egovframework/spring/context-*.xml");
		
		// 2. Spring 컨테이너로부터 SampleServiceImpl 객체를 Lookup한다.
		OcrService sampleService = (OcrService) container.getBean("ocrService");
		OcrVO vo = new OcrVO();
		vo.setTitle("생성 테스트");
		vo.setRegUser("테스터");
		vo.setDescription("테스트 중 .....");
		sampleService.insertSample(vo);
		
		List<OcrVO> sampleList = sampleService.selectSampleList(vo);
		System.out.println("[ Sample LIST ]");
		for (OcrVO sample : sampleList) {
			System.out.println("---> " + sample.toString());
		}
		
//		vo.setId(4);
//		sampleService.deleteSample(vo);
		
		// 3. Spring 컨테이너를 종료한다.
		container.close();
	}
}
