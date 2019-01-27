package defaultPackage;
import java.io.Serializable;

public class Sentence implements Serializable{
	private String sentence;
	private boolean end;
	private boolean fileExist;
	
	public Sentence() {
		
	}
	
	public Sentence (String sentence) {
		this.sentence = sentence;
	}
	
	public boolean isFileExist() {
		return fileExist;
	}
	public void setFileExist(boolean fileExist) {
		this.fileExist = fileExist;
	}
	
	
	public String getSentence() {
		return sentence;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	public boolean isEnd() {
		return end;
	}

	public void setEnd(boolean end) {
		this.end = end;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
