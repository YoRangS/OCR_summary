package egovframework.ocr.sample.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import egovframework.ocr.sample.service.OcrVO;

public class OcrRowMapper implements RowMapper<OcrVO>{
	
	@Override
	public OcrVO mapRow(ResultSet rs, int rowNum) throws SQLException {
		OcrVO sample = new OcrVO();
		sample.setId(rs.getInt("ID"));
		sample.setTitle(rs.getString("TITLE"));
		sample.setRegUser(rs.getString("REG_USER"));
		sample.setDescription(rs.getString("DESCRIPTION"));
		sample.setRegDate(rs.getDate("REG_DATE"));
		return sample;
	}
}
