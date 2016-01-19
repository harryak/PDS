package org.utils;

import java.net.InetAddress;

import java.util.ArrayList;
import java.util.List;

/**
 * The class Node represents one node of an XML-RPC network.
 */
public class Node implements Comparable<Node> {
	private static final String OLD_CHAR = ".";
	private static final String NEW_CHAR = "";
	
	private InetAddress address;
	private int port;
	private boolean isMaster;
	
	/**
	 * Empty node without address or port.
	 */
	public Node() { }
	
	/**
	 * New node that cannot be master.
	 * 
	 * @param address
	 * @param port
	 */
	public Node(InetAddress address, int port) {
		this.address = address;
		this.port = port;
		this.isMaster = false;
	}
	
	/**
	 * New node that might be the master node.
	 * 
	 * @param address
	 * @param port
	 * @param isMaster
	 */
	public Node(InetAddress address, int port, boolean isMaster) {
		this.address = address;
		this.port = port;
		this.isMaster = isMaster;
	}
	
	public InetAddress getAddress() {
		return this.address;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public boolean getIsMaster() {
		return isMaster;
	}
	
	public void setAddress(InetAddress address) {
		this.address = address;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public void setIsMaster(boolean isMaster) {
		this.isMaster = isMaster;
	}
	
	public String toString() {
		return "(" + address.getHostAddress() + ", " + port + ")";
	}
	
	public static List<String[]> asArray(List<Node> listOfNodes) {
		List<String[]> arrayOfNodes = new ArrayList<String[]>();
		for(Node node : listOfNodes) {
			arrayOfNodes.add(new String[] {
					node.getAddress().getHostAddress(),
					Integer.toString(node.getPort())
			});
		}
		
		return arrayOfNodes;
	}
	
	public int canonicalForm() {
		return Integer.parseInt(address.getHostAddress().replace(OLD_CHAR, NEW_CHAR) + port);
	}
	
	@Override
	public boolean equals(Object obj) {
		Node node = (Node) obj;
		return (this.getAddress().equals(node.getAddress()) &&
				this.getPort() == node.getPort());
	}
	
	@Override
	public int hashCode() {
		String hashString = address.getHostAddress() + port;
		return hashString.hashCode();
	}
	
	@Override
	public int compareTo(Node node) {
		if(this.canonicalForm() < node.canonicalForm())	return -1;
		else if(this.canonicalForm() > node.canonicalForm()) return 1;
		return 0;
	}

}
