package egovframework.ocr.sample.web;

public class TestDataRequest {
    private String fileName;
    private String lang;
    private String textExtract;
    // Getters and setters
    
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getLang() {
        return lang;
    }
    public void setLang(String lang) {
        this.lang = lang;
    }
    public String getTextExtract() {
        return textExtract;
    }
    public void setTextExtract(String textExtract) {
        this.textExtract = textExtract;
    }
}