package vorgu2.prax1.node.requestsender;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.goebl.david.Response;
import com.goebl.david.Webb;

public class PostRequestSender implements Callable<Response<String>> {

	private String address;
	private String id;
	private String content;
	private String requester;
	
	public PostRequestSender(String address, String id, String content, String requester) {
		this.address = address;
		this.id = id;
		this.content = content;
		this.requester = requester;
	}
	
	public static Future<Response<String>> post(String address, String id, String content, String requester) {
		PostRequestSender sender = new PostRequestSender(address, id, content, requester);
		return Executors.newSingleThreadExecutor().submit(sender);
	}
	
	public static void postToAll(List<String> addresses, String id, String content, String requester) {
		for(String address : addresses) {
			PostRequestSender.post(address, id, content, requester);
		}
	}
	
	@Override
	public Response<String> call() {
		System.out.printf("Sending POST to %s%n", address);
		try {
			return Webb.create().post(address)
					 .header("Referer", requester)
					 .header("id", id)
					 .body(content)
					 .asString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
