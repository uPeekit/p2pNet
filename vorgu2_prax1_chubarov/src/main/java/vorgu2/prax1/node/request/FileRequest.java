package vorgu2.prax1.node.request;

public class FileRequest {

	private String id;
	private String downloadIp;
	private String fileIp;
	
	public FileRequest(String id, String downloadIp, String fileIp) {
		this.id = id;
		this.downloadIp = downloadIp;
		this.fileIp = fileIp;
	}

	public String getId() {
		return id;
	}
	
	public String getDownloadIp() {
		return downloadIp;
	}
	
	public String getFileIp() {
		return fileIp;
	}
	
	public void setDownloadIp(String downloadIp) {
		this.downloadIp = downloadIp;
	}
	
	public void setFileIp(String fileIp) {
		this.fileIp = fileIp;
	}
	
	@Override
	public String toString() {
		return String.format("%s: download %s, file %s", id, downloadIp, fileIp);
	}
	
}
