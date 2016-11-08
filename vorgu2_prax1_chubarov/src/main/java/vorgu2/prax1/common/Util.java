package vorgu2.prax1.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;

import vorgu2.prax1.node.Node;

public class Util {

	private static Gson gson = new Gson(); 
	
	public static String[] parseEndodedJsonToArray(String json) throws JsonSyntaxException, UnsupportedEncodingException {
		String[] array = gson.fromJson(URLDecoder.decode(json, "UTF-8").split("=")[1], String[].class);
		return array;
	}

	public static Node parseJsonToNode(String json) {
		return gson.fromJson(json, Node.class);
	}

	public static Map<String, String> parseEncodedQueryParameters(String query) throws UnsupportedEncodingException {
		Map<String, String> map = new HashMap<>();
		String[] pair;
		query = URLDecoder.decode(query, "UTF-8");
		for(String s : query.split("&")) {
			pair = s.split("=");
			map.put(pair[0], pair[1]);
		}
		return map;
	}
	
	public static String getRequestAddress(HttpExchange exchange) {
		String address;
		if(exchange.getRequestHeaders().containsKey("Referer")) {
			address = exchange.getRequestHeaders().getFirst("Referer");
		} else if(exchange.getRequestHeaders().containsKey("Host")) {
			address = exchange.getRequestHeaders().getFirst("Host");
		} else {
			address = getFullAddressForHost(exchange.getLocalAddress().getAddress().getHostAddress());
		}
		if(address.endsWith("/")) {
			address = address.substring(0, address.length()-1);
		}
		return address;
	}

	public static String getRequestBody(HttpExchange exchange) {
		return getContent(exchange.getRequestBody());
	}

	public static String getContent(String url) throws MalformedURLException, IOException {
		return getContent(new URL(url).openConnection().getInputStream());
	}
	
	public static String getContent(InputStream inputStream) {
		String res = new BufferedReader(new InputStreamReader(inputStream)).lines()
							.filter(line -> !line.trim().isEmpty())
							.reduce("", (s1, s2) -> s1 + "\n" + s2);
		return res;
	}
	
	public static long timestamp() {
		return LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
	}
	
	public static String getIpFromAddress(String address) {
		int i1 = address.indexOf("//") + 2;
		int i2 = address.indexOf(":", i1);
		return address.substring(i1, i2);
	}

	public static String getFullAddressForHost(String host) {
		return String.format("http://%s:%s", host, ConfigParser.getIntParameter("node_port"));
	}

	public static void sendOk(HttpExchange exchange) throws IOException {
		exchange.getResponseHeaders().add("Content-Type", "text/html");
		exchange.sendResponseHeaders(200, 2);
		exchange.getResponseBody().write("ok".getBytes());
	}
	
}
