package vorgu2.prax1.node.handler;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static vorgu2.prax1.common.Util.getRequestAddress;
import static vorgu2.prax1.common.Util.parseEncodedQueryParameters;

import java.io.IOException;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import vorgu2.prax1.common.Util;
import vorgu2.prax1.node.Node;
import vorgu2.prax1.node.request.DownloadRequest;
import vorgu2.prax1.node.request.FileRequest;
import vorgu2.prax1.node.requestsender.GetRequestSender;

public class DownloadHandler implements HttpHandler {

	private Node node;

	public DownloadHandler(Node node) {
		this.node = node;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		if(exchange.getRequestMethod().equals("GET")) {
			Util.sendOk(exchange);
		} else {
			exchange.sendResponseHeaders(400, 0);
			return;
		}
		String requestAddress = getRequestAddress(exchange);
		System.out.printf("Received GET from %s: %s%n", requestAddress, exchange.getRequestURI());
		
		Map<String, String> params = parseEncodedQueryParameters(exchange.getRequestURI().getQuery());
		String id = params.get("id");
		String url = params.get("url");
		
		if(isMyRequest(requestAddress)) {
			if(notInDownloadTable(id)) {
				DownloadRequest downloadRequest = new DownloadRequest(id, url, Util.timestamp());
				node.getMyDownloadRequests().add(downloadRequest);
				System.out.printf("Added to download table: %s%n", downloadRequest);
			}
			GetRequestSender.getToAll(asList(node.getNeighbourFullAddresses()).stream()
										.filter(adr -> !adr.equals(requestAddress))
										.map(adr -> adr + "/download")
										.collect(toList()), 
									  id, url, node.getFullAddress());
		} else {
			if(receivedFirstTime(id)) {
				if(notInDownloadTable(id)) {
					FileRequest fileRequest = new FileRequest(id, requestAddress, null);
					node.getRouteTable().add(fileRequest);
					System.out.printf("Added to route table: %s%n", fileRequest);
					
					if(asList(node.getNeighbourFullAddresses()).stream()
							.filter(adr -> !adr.equals(requestAddress))
							.count() == 0) {
						FileHelper.downloadFromUrlAndForwardFileRequest(id, node, requestAddress, url);
					} else {
						if(shouldDownload()) {
							FileHelper.downloadFromUrlAndForwardFileRequest(id, node, requestAddress, url);
						} else {
							GetRequestSender.getToAll(asList(node.getNeighbourFullAddresses()).stream()
														.filter(adr -> !adr.equals(requestAddress))
														.map(adr -> adr + "/download")
														.collect(toList()), 
													  id, url, node.getFullAddress());
						}
					}
				} else {
					System.out.println("Own request. Ignore");
				}
			} else if(receivedOnlyFileRequest(id)) {
				node.getRouteTable().stream()
									.filter(req -> req.getId().equals(id))
									.findFirst().get().setDownloadIp(requestAddress);
			} else {
				System.out.println("Repeating request. Ignore");
			}
		}
	}
	
	private boolean shouldDownload() {
		return Math.random() > node.getLazyFactor();
	}

	private boolean notInDownloadTable(String id) {
		return node.getMyDownloadRequests().stream().noneMatch(r -> r.getId().equals(id));
	}

	private boolean receivedFirstTime(String id) {
		return node.getRouteTable().stream().noneMatch(r -> r.getId().equals(id));
	}

	private boolean receivedOnlyFileRequest(String id) {
		return node.getRouteTable().stream().anyMatch(r -> r.getId().equals(id)
														&& r.getFileIp() != null);
	}

	private boolean isMyRequest(String requestAddress) {
		return requestAddress.equals(node.getFullAddress());
	}

}
