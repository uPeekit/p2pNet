package vorgu2.prax1.server;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class NewNodeHandler implements HttpHandler {

	private ServerDispatcher serverDispatcher;

	public NewNodeHandler(ServerDispatcher serverDispatcher) {
		this.serverDispatcher = serverDispatcher;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String host = serverDispatcher.getNextHost();
		String json = String.format("{ host: \"%s\" }", host);
		
		exchange.getResponseHeaders().set("Content-Type", "text/plain");
		exchange.sendResponseHeaders(200, json.length());
		exchange.getResponseBody().write(json.getBytes());
		exchange.getResponseBody().close();
		
		serverDispatcher.addNode(host);
	}

}
