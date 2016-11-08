package vorgu2.prax1.node.handler;

import static vorgu2.prax1.common.Util.getRequestBody;
import static vorgu2.prax1.common.Util.getRequestAddress;
import static vorgu2.prax1.common.Util.sendOk;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import vorgu2.prax1.node.Node;
import vorgu2.prax1.node.request.FileRequest;

public class FileHandler implements HttpHandler {

	private Node node;

	public FileHandler(Node node) {
		this.node = node;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		if(exchange.getRequestMethod().equals("POST")) {
			sendOk(exchange);
		} else {
			exchange.sendResponseHeaders(400, 0);
			return;
		}
		String requestAddress = getRequestAddress(exchange);
		System.out.printf("Received POST from %s: %s%n", requestAddress, exchange.getRequestURI());
		
		String id = exchange.getRequestHeaders().getFirst("id");
		
		if(receivedFirstTime(id)) {
			if(isMyRequest(id)) {
				if(node.getMyDownloadRequests().stream()
											   .filter(request -> request.getId().equals(id))
											   .anyMatch(request -> !request.isReceived())) {
					
					node.getMyDownloadRequests().stream()
												.filter(request -> request.getId().equals(id))
												.findFirst().get().setReceived(true);
					System.out.println("My file!");
					String resp = getRequestBody(exchange);
					int end = Math.min(100, resp.length());
					System.out.printf("%s...%n", resp.substring(0, end));
				} 
			} else {
				FileRequest fileRequest = new FileRequest(id, null, requestAddress);
				node.getRouteTable().add(fileRequest);
				System.out.printf("Added to route table: %s%n", fileRequest);
				
				FileHelper.forwardFileRequestToAll(id, node, requestAddress, exchange);
			}
		} else if(receivedOnlyDownloadRequest(id)) {
			node.getRouteTable().stream()
								.filter(req -> req.getId().equals(id))
								.findFirst().get().setFileIp(requestAddress);
			
			FileHelper.forwardFileRequest(id, node, requestAddress, exchange);
		} else {
			System.out.println("Ignore");
		} 
	}
	
	private boolean isMyRequest(String id) {
		return node.getMyDownloadRequests().stream().anyMatch(r -> r.getId().equals(id));
	}

	private boolean receivedFirstTime(String id) {
		return node.getRouteTable().stream().noneMatch(r -> r.getId().equals(id));
	}

	private boolean receivedOnlyDownloadRequest(String id) {
		return node.getRouteTable().stream().anyMatch(r -> r.getId().equals(id)
														&& r.getDownloadIp() != null);
	}

}
