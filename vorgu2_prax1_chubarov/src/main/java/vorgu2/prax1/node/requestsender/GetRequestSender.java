package vorgu2.prax1.node.requestsender;

import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.goebl.david.Response;
import com.goebl.david.Webb;

public class GetRequestSender implements Callable<Response<String>> {

	private String address;
	private String id;
	private String url;
	private String requester;
	
	public GetRequestSender(String address, String id, String url, String requester) {
		this.address = address;
		this.id = id;
		this.url = url;
		this.requester = requester;
	}

	public static Future<Response<String>> get(String address, String id, String url, String requester) {
		GetRequestSender sender = new GetRequestSender(address, id, url, requester);
		return Executors.newSingleThreadExecutor().submit(sender);
	}
	
	public static void getToAll(List<String> addresses, String id, String url, String requester) {
		for(String address : addresses) {
			GetRequestSender.get(address, id, url, requester);
		}
	}
	
	@Override
	public Response<String> call() {
		System.out.printf("Sending GET to %s%n", address);
		try {
			return Webb.create()
					 .get(address)
					 .param("id", id)
					 .header("Referer", requester)
					 .param("url", URLEncoder.encode(url, "UTF-8"))
					 .asString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
