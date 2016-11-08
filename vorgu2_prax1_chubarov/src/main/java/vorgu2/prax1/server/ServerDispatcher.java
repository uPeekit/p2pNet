package vorgu2.prax1.server;

import static vorgu2.prax1.common.ConfigParser.getIntParameter;
import static vorgu2.prax1.common.ConfigParser.getParameter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.sun.net.httpserver.HttpServer;

import vorgu2.prax1.node.Node;

public class ServerDispatcher {
	
	private static final String HOST_BASE = getParameter("host_base");
	public static final String SERVER_DISPATCHER_HOST = HOST_BASE + 0;
	
	private final HttpServer httpServer;
	
	private List<Node> nodes = new LinkedList<>();
	private long nodesCounter = 1;
	
	public ServerDispatcher(int serverPort) throws UnknownHostException, IOException {
		httpServer = HttpServer.create(new InetSocketAddress(SERVER_DISPATCHER_HOST, serverPort), 0);
		createContexts();
		httpServer.start();
		
		System.out.println(httpServer.getAddress());
	}
	
    private void createContexts() {
    	httpServer.createContext("/newnode", new NewNodeHandler(this));
	}

	String getNextHost() {
		return HOST_BASE + (nodesCounter++);
	}

	void addNode(String host) throws UnknownHostException, IOException {
		Node newNode = new Node();
		newNode.setHost(host);
		newNode.setPort(getIntParameter("node_port"));
		nodes.add(newNode);
		
		System.out.println("new node on " + host + ", reallocating neighbours");
		reallocateNeighbours();
		System.out.println("created " + newNode);
	}

	synchronized private void reallocateNeighbours() throws IOException {
		int size = nodes.size();
		if(size <= 1) {
			return;
		} else if(size <= getIntParameter("neighbours_count") + 1) {
			allocateEveryoneToEveryone();
		} else {
			allocateByAlgorhitm();
		}
	}

	private void allocateEveryoneToEveryone() throws IOException {
		String[] newNeighbours;
		int neighboursCount = getIntParameter("neighbours_count");
		for(Node n1 : nodes) {
			int counter = 0;
			newNeighbours = new String[neighboursCount];
			for(Node n2 : nodes) {
				if(!n1.getHost().equals(n2.getHost())) {
					newNeighbours[counter++] = n2.getHost();
				}
			}
			updateNodeIfNeeded(n1, newNeighbours);
		}
	}

	private void allocateByAlgorhitm() throws IOException {
		Node node;
		String[] newNeighbours;
		int neighboursCount = getIntParameter("neighbours_count");
		for(int i = 0; i < nodes.size(); ++i) {
			node = nodes.get(i);
			newNeighbours = new String[neighboursCount];
			for(int j = 0; j < neighboursCount; ++j) {
				newNeighbours[j] = nodes.get(getModIndex(i + j + 1)).getHost();
			}
			updateNodeIfNeeded(node, newNeighbours);
		}
	}
	
	private void updateNodeIfNeeded(Node node, String[] neighbours) throws IOException {
		if(!Arrays.equals(node.getNeighbourAddresses(), neighbours)) {
			node.setNeighbourHosts(neighbours);
			System.out.println("Node updated: " + node);
			ServerDataSender.postTextToNodePage(node, "newneighbours", Arrays.toString(neighbours));
		}
	}

	private int getModIndex(int i) {
		return i < nodes.size() ? i : (i - nodes.size());
	}

	private void startPingingThread() {
		new Thread(() -> {
    		Iterator<Node> iter;
    		Node node;
    		while(true) {
				synchronized(this) {
					try {
						this.wait(3000);
						if(nodes.isEmpty()) {
							continue;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
    			iter = nodes.iterator();
    			while(iter.hasNext()) {
    				node = iter.next();
    				if( !ping(node, 2000) ) {
    					System.out.println(node.getHost() + " timeout");
    					iter.remove();
    					try {
							reallocateNeighbours();
						} catch (IOException e) {
							e.printStackTrace();
						}
    				}
    			}
    		}
    	}, "ping_thread").start();
	}
	
	private boolean ping(Node node, int timeout) {
		try (Socket socket = new Socket()) {
	        socket.connect(new InetSocketAddress(node.getHost(), node.getPort()), timeout);
	        return true;
	    } catch (IOException e) {
	        return false;
	    }
	}

	public static void main(String[] args) throws UnknownHostException, IOException {
    	ServerDispatcher serverDispatcher = new ServerDispatcher(getIntParameter("server_port"));
    	serverDispatcher.startPingingThread();
    }
	
}
