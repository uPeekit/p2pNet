package vorgu2.prax1.node.handler;

import static java.util.Arrays.asList;
import static vorgu2.prax1.common.Util.getContent;
import static vorgu2.prax1.common.Util.getRequestBody;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.goebl.david.Response;
import com.sun.net.httpserver.HttpExchange;

import vorgu2.prax1.node.Node;
import vorgu2.prax1.node.request.FileRequest;
import vorgu2.prax1.node.requestsender.PostRequestSender;

public class FileHelper {
	
	public static void downloadFromUrlAndForwardFileRequest(String id, Node receivedNode, String requestAddress, String url) 
			throws MalformedURLException, IOException {
		FileRequest fileRequest = receivedNode.getRouteTable().stream()
															  .filter(req -> req.getId().equals(id))
															  .findFirst().get();
		fileRequest.setFileIp(receivedNode.getFullAddress());
		System.out.printf("Request in route table changed: %s%n", fileRequest);
		
		System.out.printf("Downloading from %s%n", url);
		String content = getContent(url);
		
		FileHelper.forwardFileRequest(id, receivedNode, requestAddress, content);
	}

	public static void forwardFileRequest(String id, Node receivedNode, String requestAddress, HttpExchange exchange) {
		String content = getRequestBody(exchange);
		FileHelper.forwardFileRequest(id, receivedNode, requestAddress, content);
	}

	public static void forwardFileRequest(String id, Node receivedNode, String requestAddress, String content) {
		Optional<FileRequest> addressOpt = receivedNode.getRouteTable().stream()
				.filter(req -> req.getId().equals(id))
				.findFirst();
		
		if(addressOpt.isPresent()) {
			String address = addressOpt.get().getDownloadIp() + "/file";
			Response<?> response;
			try {
				response = PostRequestSender.post(address, id, content, receivedNode.getFullAddress()).get();
				if(response.getStatusCode() == 200) {
					return;
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		FileHelper.forwardFileRequestToAll(id, receivedNode, requestAddress, content);
	}
	
	public static void forwardFileRequestToAll(String id, Node receivedNode, String requestAddress, HttpExchange exchange) {
		String content = getRequestBody(exchange);
		FileHelper.forwardFileRequestToAll(id, receivedNode, requestAddress, content);
	}
	
	public static void forwardFileRequestToAll(String id, Node receivedNode, String requestAddress, String content) {
		List<String> addresses = asList(receivedNode.getNeighbourAddresses()).stream()
				.filter(adr -> !adr.equals(requestAddress))
				.map(adr -> adr + "/file")
				.collect(Collectors.toList());
		PostRequestSender.postToAll(addresses, id, content, receivedNode.getFullAddress());
	}

}
