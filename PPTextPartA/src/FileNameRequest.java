import java.io.Serializable;

public class FileNameRequest implements Serializable {
	private String fileName;
	
	public FileNameRequest(String fileName) {
		this.fileName = fileName;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
