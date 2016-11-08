package vorgu2.prax1.server;

import static java.lang.String.format;

import java.io.IOException;

import com.goebl.david.Webb;

import vorgu2.prax1.node.Node;

public class ServerDataSender extends Thread {
	
	private Node node;
	private String page;
	private String text;

	public static void postTextToNodePage(Node node, String page, String text) throws IOException {
		ServerDataSender sender = new ServerDataSender();
		sender.node = node;
		sender.page = page;
		sender.text = text;
		sender.start();
	}
	
	@Override
	public void run() {
		String address = format("http://%s:%d/%s", node.getHost(), 
												   node.getPort(), 
												   page);
		System.out.println("POST to " + address);
		Webb webb = Webb.create();
		webb.post(address)
			.header("Content-Type", "text/html")
			.header("Content-Length", String.valueOf(text.length()))
			.param("neighbours", text)
			.asVoid();
	}

}
