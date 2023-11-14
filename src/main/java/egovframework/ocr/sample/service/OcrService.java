package egovframework.ocr.sample.service;

import java.util.List;

public interface OcrService {

	void insertSample(OcrVO vo) throws Exception;

	void updateSample(OcrVO vo) throws Exception;

	void deleteSample(OcrVO vo) throws Exception;

	OcrVO selectSample(OcrVO vo) throws Exception;

	List<OcrVO> selectSampleList(OcrVO vo) throws Exception;

}