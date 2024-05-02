package egovframework.ocr.sample.web;
import java.util.*;

public class Splitter {
    public final ArrayList<String> returns = new ArrayList<String>();
    private String input;
    private int maxToken;

    public Splitter(String inp, int maxToken) {
        this.input = inp;
        this.maxToken = maxToken;
    }
    
    public void split() {
        if (input == null || input.isEmpty()) {
            return;
        }

        int length = input.length();
        int startIndex = 0;
        while (startIndex < length) {
            int endIndex = Math.min(startIndex + maxToken, length);
            String token = input.substring(startIndex, endIndex);
            returns.add(token);
            startIndex += maxToken;
        }
    }
    
    public ArrayList<String> getList() {
        return this.returns;
    }
}
