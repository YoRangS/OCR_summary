package egovframework.ocr.sample.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import egovframework.ocr.sample.service.OcrDAO;
import egovframework.ocr.sample.service.OcrVO;

@Repository("daoSpring")
public class OcrDAOSpring implements OcrDAO {
	@Resource(name="jdbcTemplate")
	private JdbcTemplate spring;
	
	// SQL 명령어들
	private final String SAMPLE_INSERT = "INSERT INTO SAMPLE (ID, TITLE, REG_USER, DESCRIPTION, REG_DATE) "
			+ "SELECT IFNULL(MAX(ID), 0), ?, ?, ?, NOW() FROM SAMPLE";
	private final String SAMPLE_UPDATE = "UPDATE SAMPLE SET TITLE=?, REG_USER=?, DESCRIPTION=? WHERE ID=?";
	private final String SAMPLE_DELETE = "DELETE FROM SAMPLE WHERE ID=?";
	private final String SAMPLE_GET = "SELECT ID, TITLE, REG_USER, DESCRIPTION, REG_DATE FROM SAMPLE WHERE ID=?";
	private final String SAMPLE_LIST = "SELECT ID, TITLE, REG_USER, DESCRIPTION, REG_DATE FROM SAMPLE "
			+ "ORDER BY REG_DATE DESC";
	
	public OcrDAOSpring() {
		System.out.println("===> OcrDAOSpring 생성");
	}
	
	public void insertSample(OcrVO vo) throws Exception {
		System.out.println("===> JDBC로 insertSample() 기능 처리");
		Object[] args = {vo.getId(), vo.getTitle(), vo.getRegUser(), vo.getDescription()};
		spring.update(SAMPLE_INSERT, args);
	}

	public void updateSample(OcrVO vo) throws Exception {
		System.out.println("===> JDBC로 updateSample() 기능 처리");
		Object[] args = {vo.getId(), vo.getTitle(), vo.getRegUser(), vo.getDescription(), vo.getId()};
		spring.update(SAMPLE_UPDATE, args);
	}

	public void deleteSample(OcrVO vo) throws Exception {
		System.out.println("===> JDBC로 deleteSample() 기능 처리");
		spring.update(SAMPLE_DELETE, vo.getId());
	}

	public OcrVO selectSample(OcrVO vo) throws Exception {
		System.out.println("===> JDBC로 selectSample() 기능 처리");
		Object[] args = {vo.getId()};
		return spring.queryForObject(SAMPLE_GET, args, new OcrRowMapper());
	}

	public List<OcrVO> selectSampleList(OcrVO vo) throws Exception {
		System.out.println("===> JDBC로 selectSampleList() 기능 처리");
		return spring.query(SAMPLE_LIST, new OcrRowMapper());
	}
}
