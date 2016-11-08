package vorgu2.prax1.node.request;

public class DownloadRequest {
	
	private String id;
	private String url;
	private long timestamp;
	private boolean received;
	
	public DownloadRequest(String id, String url, long timestamp) {
		this.id = id;
		this.url = url;
		this.timestamp = timestamp;
	}
	
	public String getId() {
		return id;
	}
	
	public String getUrl() {
		return url;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public boolean isReceived() {
		return received;
	}

	public void setReceived(boolean received) {
		this.received = received;
	}

	@Override
	public String toString() {
		return String.format("%s: %s, time %s", id, url, timestamp);
	}

}
