package vorgu2.prax1.node;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static vorgu2.prax1.common.ConfigParser.getIntParameter;
import static vorgu2.prax1.server.ServerDispatcher.SERVER_DISPATCHER_HOST;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.goebl.david.Webb;
import com.sun.net.httpserver.HttpServer;

import vorgu2.prax1.common.Util;
import vorgu2.prax1.node.handler.DownloadHandler;
import vorgu2.prax1.node.handler.FileHandler;
import vorgu2.prax1.node.handler.NewNeighboursHandler;
import vorgu2.prax1.node.handler.RootHandler;
import vorgu2.prax1.node.request.DownloadRequest;
import vorgu2.prax1.node.request.FileRequest;

public class Node {

	private String host;
	private int port;
	private HttpServer httpServer;
	private String[] neighbourHosts = new String[getIntParameter("neighbours_count")];
	private double lazyFactor = Math.random();
	
	private List<DownloadRequest> myDownloadRequests = new ArrayList<>();
	private List<FileRequest> routeTable = new ArrayList<>();
	
	public Node() {
	}
	
	Node(int port) throws UnknownHostException, IOException {
		Node responseNode = Util.parseJsonToNode(getResponseFromServerPage("newnode"));
		this.host = String.valueOf(responseNode.getHost());
		this.port = port;
		this.httpServer = HttpServer.create(new InetSocketAddress(host, port), 0);
		createContexts();
		httpServer.start();
		
		System.out.println(this);
	}
	
	private void createContexts() {
		httpServer.createContext("/", new RootHandler());
		httpServer.createContext("/newneighbours", new NewNeighboursHandler(this));
		httpServer.createContext("/download", new DownloadHandler(this));
		httpServer.createContext("/file", new FileHandler(this));
	}

	private String getResponseFromServerPage(String page) throws IOException {
		String address = format("http://%s:%d/%s", SERVER_DISPATCHER_HOST, 
				 								   getIntParameter("server_port"), 
				 								   page);
		return Webb.create().get(address)
							.asString()
							.getBody();
	}
	
	public String getHost() {
		return host;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public String[] getNeighbourAddresses() {
		return neighbourHosts;
	}
	
	public String[] getNeighbourFullAddresses() {
		return Arrays.asList(neighbourHosts).stream()
					.filter(adr -> adr != null)
					.map(adr -> format("http://%s:%s", adr, getIntParameter("node_port")))
					.collect(toList()).toArray(new String[0]);
	}
	
	public void setNeighbourHosts(String[] neighbourHosts) {
		this.neighbourHosts = neighbourHosts;
	}
	
	public List<DownloadRequest> getMyDownloadRequests() {
		return myDownloadRequests;
	}
	
	public List<FileRequest> getRouteTable() {
		return routeTable ;
	}

	public double getLazyFactor() {
		return lazyFactor;
	}
	
	public String getFullAddress() {
		return format("http://%s:%s", host, port);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(!(obj instanceof Node))
			return false;
		Node node = (Node)obj;
		return node.getHost().equals(this.getHost());
	}
	
	@Override
	public int hashCode() {
		return httpServer.getAddress().hashCode();
	}

	@Override
	public String toString() {
		return format("%s:%d %s", host, port, Arrays.toString(neighbourHosts));
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		new Node(getIntParameter("node_port"));
	}

}
