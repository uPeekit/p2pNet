package vorgu2.prax1.node.handler;

import static java.lang.String.format;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RootHandler implements HttpHandler {
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String html = format("<!Doctype HTML>\n"
				+ "<html>\n"
				+ "<head><title>Get url</title></head>\n"
				+ "<body>\n"
				+ "<form action=\"download/\" method=\"GET\">\n"
				+ "URL<br>\n"
				+ "<input type=\"text\" name=\"url\" value=\"\">\n"
				+ "<input type=\"hidden\" name=\"id\" value=\"%s\">\n"
				+ "<input type=\"submit\" value=\"Get\">\n"
				+ "</form>\n"
				+ "</body>\n</html>", 
					LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) + (long)(Math.random() * 1000000));
		exchange.getResponseHeaders().add("Content-Type", "text/html");
		exchange.sendResponseHeaders(200, html.length());
		exchange.getResponseBody().write(html.getBytes());
	}

}
