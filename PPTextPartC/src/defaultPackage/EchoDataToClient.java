package defaultPackage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EchoDataToClient implements Serializable{
	
	List<BrokerObject> list = new ArrayList<>();
	private boolean reAndUnRe; //indicate that this data is confirming that successfully register or unregister;
	private String fileName;
	private String text;
	private boolean largerThanTwo;
	private boolean exists = true;
	
	
	public EchoDataToClient() {
		
	}
	
	public EchoDataToClient(List<BrokerObject> list, boolean reAndUnRe, String text) {
		this.list = list;
		this.reAndUnRe = reAndUnRe;
		this.text = text;
	}
	
	public boolean isExists() {
		return exists;
	}

	public void setExists(boolean exists) {
		this.exists = exists;
	}
	
	public boolean isLargerThanTwo() {
		return largerThanTwo;
	}

	public void setLargerThanTwo(boolean largerThanTwo) {
		this.largerThanTwo = largerThanTwo;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	
	public boolean isReAndUnRe() {
		return reAndUnRe;
	}

	public void setReAndUnRe(boolean reAndUnRe) {
		this.reAndUnRe = reAndUnRe;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public List<BrokerObject> getList() {
		return list;
	}

	public void setList(List<BrokerObject> list) {
		this.list = list;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
