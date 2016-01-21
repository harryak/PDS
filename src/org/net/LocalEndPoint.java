package org.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import java.util.ArrayList;
import java.util.List;

import org.algorithms.RA;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import org.utils.Node;

/**
 * The local representation of the XML-RPC network with different nodes. 
 */
public class LocalEndPoint {
	public static final int MUTUAL_EXCLUSION_MODE = 1;	// 0: Centralized Mutual Exclusion, 1: Ricart & Agrawala
	public static final String default_port = "8000"; // Default.
	
	public static List<Node> listOfNodes;
	public static Node localNode;
	
	public static String masterstring = "";
	
	private Node remoteNode;
	private WebServer webServer;
	private boolean isServerRunning = false;
	
	/**
	 * Instantiate Network with a remote node.
	 * 
	 * @param remoteNode	A remote node object.
	 * @throws NumberFormatException
	 * @throws IOException
	 * @throws XmlRpcException
	 */
	public LocalEndPoint(Node remoteNode) throws NumberFormatException, IOException, XmlRpcException {
		this.remoteNode = remoteNode;
		LocalEndPoint.listOfNodes = new ArrayList<Node>();
		
		// Do we have a remote node?
		if(remoteNode != null) {
			// Then add it to our list of nodes.
			LocalEndPoint.listOfNodes.add(remoteNode);
		}
		
		// Create a new node to represent the local node.
		LocalEndPoint.localNode = new Node(InetAddress.getLocalHost(), Integer.parseInt(LocalEndPoint.default_port));
		
		joinNetwork();
		startLocalXmlRpcServer();
	}
	
	/**
	 * Join an XMLRPC network using our remote node, if we have one.
	 * 
	 * @throws MalformedURLException
	 * @throws XmlRpcException
	 * @throws NumberFormatException
	 * @throws UnknownHostException
	 */
	private void joinNetwork() throws MalformedURLException, XmlRpcException, NumberFormatException, UnknownHostException {
		if(this.remoteNode == null) return;
		
		System.out.println("Operation: Joining the network.");
		Object[] neighborAddress;
		
		// Get client for talking with our remote node (which is an XMLRPC server).
		XmlRpcClient remoteNodeClient = LocalEndPoint.getXmlRpcClient(this.remoteNode);
		
		// Ask the remote node to join its network. It will return a list of the network's other nodes.
		Object[] neighbors = 	(	Object[])remoteNodeClient.execute("network.join", 
									new Object[] {
										LocalEndPoint.localNode.getAddressString(),
										Integer.parseInt(default_port)
									}
								);
		
		// For all other nodes get their address and store it in the node list.
		for(Object neighbor : neighbors) {
			neighborAddress = (Object[])neighbor;
			LocalEndPoint.listOfNodes.add(new Node(InetAddress.getByName((String)neighborAddress[0]), (Integer)neighborAddress[1]));
		}
		
		System.out.println("Result: Node joined the network successfully.");
	}
	
	/**
	 * Start an XMLRPC server to handle incoming XMLRPC requests.
	 * 
	 * @throws IOException
	 * @throws NumberFormatException
	 * @throws XmlRpcException
	 */
	public void startLocalXmlRpcServer() throws IOException, NumberFormatException, XmlRpcException {
		System.out.println("Operation: Starting the server.");
		
		// Define the local web server.
		this.webServer = new WebServer(Integer.parseInt(default_port), LocalEndPoint.localNode.getAddress());
		XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
		
		// Handle incoming requests with the NetworkHandler class.
		PropertyHandlerMapping phm = new PropertyHandlerMapping();
		phm.addHandler("network", org.net.NetworkHandler.class);
		xmlRpcServer.setHandlerMapping(phm);
		
		// Set some options for XMLRPC.
		XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl)xmlRpcServer.getConfig();
		serverConfig.setEnabledForExtensions(true);
		serverConfig.setContentLengthOptional(false);
		
		// Start the server.
		this.webServer.start();
		System.out.println("Result: The server started at port " + LocalEndPoint.localNode.getPort() + " .");
		this.isServerRunning = true;
	}
	
	/**
	 * Get an XMLRPC client to talk to a (remote) node of our network.
	 * 
	 * @param node	The node we want to talk to.
	 * @return		The XMLRPC client.
	 * @throws MalformedURLException
	 */
	public static XmlRpcClient getXmlRpcClient(Node node) throws MalformedURLException {
		XmlRpcClientConfigImpl rpcConfig = new XmlRpcClientConfigImpl();
		rpcConfig.setServerURL(new URL("http://" + node.getAddress().getHostAddress() + ":" + 
				Integer.toString(node.getPort())));
		
		XmlRpcClient client = new XmlRpcClient();
		client.setConfig(rpcConfig);
		
		return client;		
	}
	
	/**
	 * Sign of the joined network and message all nodes we are leaving.
	 * 
	 * @throws MalformedURLException
	 * @throws XmlRpcException
	 */
	public void signOffNetwork() throws MalformedURLException, XmlRpcException {
		System.out.println("Operation: Signing off.");
		// We can only sign of if we are online.
		if(isOnline()) {
			System.out.println("Operation: Other nodes are being informed of the node leaving the network.");
			
			// For all neighbors: Message them that we are leaving.
			for(Node node : listOfNodes) {
				if((boolean)LocalEndPoint.getXmlRpcClient(node).execute(
							"network.signOff",
							new Object[] {
								LocalEndPoint.localNode.getAddressString(),
								LocalEndPoint.localNode.getPort()
							})) {
					System.out.println("Result: Node " + node.toString() + " informed.");
				}
			}
		}
		
		// If our local server is running, shut it down.
		if(isServerRunning) {
			webServer.shutdown();
			isServerRunning = false;
		}
	}
	
	/**
	 * Returns whether we have a running servers and a network to talk to.
	 * 
	 * @return	Boolean
	 */
	public boolean isOnline() {
		return this.isServerRunning && listOfNodes.size() > 0;
	}
	
	/**
	 * The main server function. Connects to other nodes and then starts a server thread.
	 * 
	 * @param args	Awaits two optional parameters to connect to another node (address and port).
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		Node node = null;
		
		// Do we have a node to connect to via arguments?
		if(args.length == 2) {
			node = new Node(InetAddress.getByName(args[0]), Integer.parseInt(args[1]));
		} else {
			// If not, either do nothing or read them in from stdin.
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String commandString, ipString, portString;
			
			System.out.println("Should this node connect to an existing network? (y/n)");
			while ((commandString = in.readLine()) == null || commandString.length() < 1) {
				System.out.println("Error while reading the input. Please try again.");
			}
			
			if (commandString == "y") {
				// Read in IP and port.
				System.out.println("Please write a node's IP to connect to.");
				while ((ipString = in.readLine()) == null || ipString.length() < 1) {
					System.out.println("Error while reading the input. Please try again.");
				}
				System.out.println("Please write those node's port number.");
				while ((portString = in.readLine()) == null || portString.length() < 1) {
					System.out.println("Error while reading the input. Please try again.");
				}
				
				node = new Node(InetAddress.getByName(ipString), Integer.parseInt(portString));
			}
		}
		
		try {
			LocalEndPoint network = new LocalEndPoint(node);
			
			// New runnable thread as daemon.
			RA ra = new RA(network);
			Thread thread = new Thread(ra);
			thread.setDaemon(true);
			thread.start();
		} catch (Exception exception) {
			System.out.println("Exception: " + exception.getMessage());
		}
	}
}