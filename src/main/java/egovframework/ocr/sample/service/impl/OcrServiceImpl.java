package egovframework.ocr.sample.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import egovframework.ocr.sample.service.OcrDAO;
import egovframework.ocr.sample.service.OcrService;
import egovframework.ocr.sample.service.OcrVO;

//import egovframework.rte.fdl.cmmn.EgovAbstractServiceImpl;
//import egovframework.rte.fdl.idgnr.EgovIdGnrService;

@Service("ocrService")
public class OcrServiceImpl /*extends EgovAbstractServiceImpl*/ implements OcrService {
//	private static final Logger LOGGER = LoggerFactory.getLogger(OcrServiceImpl.class);
	
//	@Resource(name="daoSpring")
//	private OcrDAO ocrDAO;
	
//	@Resource(name="egovIdGnrService")
//	private EgovIdGnrService egovIdGnrService;

	public void insertSample(OcrVO vo) throws Exception {
//		System.out.println("SampleService---Sample 등록");
//		LOGGER.trace("TRACE Level Logging");
//		LOGGER.debug("DEBUG Level Logging");
//		LOGGER.info("INFO Level Logging");
//		LOGGER.warn("WARN Level Logging");
//		LOGGER.error("ERROR Level Logging");
		
		/** ID Generation Service */
//		String id = egovIdGnrService.getNextStringId();
//		vo.setId(id);
		
//		ocrDAO.insertSample(vo);
	}
   
	public void updateSample(OcrVO vo) throws Exception {
//		System.out.println("SampleService---Sample 수정");
		
//		ocrDAO.updateSample(vo);
	}
   
	public void deleteSample(OcrVO vo) throws Exception {
//		System.out.println("SampleService---Sample 삭제");
		
//		ocrDAO.deleteSample(vo);
	}
   
	public OcrVO selectSample(OcrVO vo) throws Exception {
//		System.out.println("SampleService---Sample 상세 조회");
		
		return null;
//		return ocrDAO.selectSample(vo);
	}
   
	public List<OcrVO> selectSampleList(OcrVO vo) throws Exception {
//		System.out.println("SampleService---Sample 목록 검색");
		
		return null;
//		return ocrDAO.selectSampleList(vo);
	}
}