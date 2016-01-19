package org.net;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import org.utils.Node;

/**
 * This class handles incoming XMLRPC requests.
 */
public class NetworkHandler {
	/**
	 * A node wants to join this network.
	 * 
	 * @param host	Host address.
	 * @param port	Host port.
	 * @return
	 * @throws MalformedURLException
	 * @throws XmlRpcException
	 * @throws UnknownHostException
	 */
	public List<String[]> join(String host, int port) throws MalformedURLException, XmlRpcException, UnknownHostException {
		System.out.println("\n(" + host + "," + port + ") joining...");
		
		// Get the list of old nodes in this network before the new node joined.
		List<Node> oldNodes = new ArrayList<>(Network.listOfNodes);
		// Add the new node to the local node list.
		Network.listOfNodes.add(new Node(InetAddress.getByName(host), port));
		
		System.out.println("Operation: Other nodes are being informed of the new node joining the network.");
		Object[] params = new Object[] { host, port };
		
		// Loop through all old nodes and inform them that we have a new node.
		for(Node node : oldNodes) {
			Network.getXmlRpcClient(node).execute("network.join", params);
			System.out.println("Result: Node " + node.toString() + " informed.");
		}
		System.out.println("Result: Joining completed.");
		
		return Node.asArray(oldNodes);		
	}
	
	/**
	 * A node wants to leave this network.
	 * 
	 * @param host
	 * @param port
	 * @return
	 * @throws UnknownHostException
	 */
	public boolean signOff(String host, int port) throws UnknownHostException {
		// Simply remove the node from our local list.
		return Network.listOfNodes.remove(new Node(InetAddress.getByName(host), port));
	}

}
