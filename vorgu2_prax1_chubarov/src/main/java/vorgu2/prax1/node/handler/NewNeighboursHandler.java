package vorgu2.prax1.node.handler;

import static vorgu2.prax1.common.Util.getRequestBody;
import static vorgu2.prax1.common.Util.parseEndodedJsonToArray;

import java.io.IOException;
import java.util.Arrays;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import vorgu2.prax1.common.Util;
import vorgu2.prax1.node.Node;

public class NewNeighboursHandler implements HttpHandler {

	private Node node;

	public NewNeighboursHandler(Node node) {
		this.node = node;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		if(exchange.getRequestMethod().equals("POST")) {
			Util.sendOk(exchange);
		} else {
			exchange.sendResponseHeaders(400, 0);
			return;
		}
		String request = getRequestBody(exchange);
		node.setNeighbourHosts(parseEndodedJsonToArray(request));
		System.out.println("new neighbours: " + Arrays.toString(node.getNeighbourAddresses()));
	}

}
