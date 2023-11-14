package egovframework.ocr.sample.service;

import java.sql.Date;

public class OcrVO {
	private int id;
	private String title;
	private String regUser;
	private String description;
	private Date regDate;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getRegUser() {
		return regUser;
	}
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Date getRegDate() {
		return regDate;
	}
	public void setRegDate(Date regDate) {
		this.regDate = regDate;
	}
	
	@Override
	public String toString() {
		return "OcrVo [id=" + id + ", title=" + title + ", regUser=" + regUser + ", description=" + description
				+ ", regDate=" + regDate + "]";
	}
}
