package org.algorithms;

import java.util.ArrayList;
import java.util.List;

import org.utils.Node;

public class RAHandler implements Comparable<RAHandler> {
	private Node node;
	private String process;
	private String processId;
	private int timestamp;
	
	public RAHandler() { }
	
	public RAHandler(Node node, String process, String processId, int timestamp) {
		this.node = node;
		this.process = process;
		this.processId = processId;
		this.timestamp = timestamp;
	}
		
	public Node getNode() {
		return this.node;
	}
	
	public String getProcess() {
		return this.process;
	}
	
	public String getProcessId() {
		return this.processId;
	}
	
	public int getTimestamp() {
		return this.timestamp;
	}
	
	public void setNode(Node node) {
		this.node = node;
	}
	
	public void setProcess(String process) {
		this.process = process;
	}
	
	public void setProcessId(String processId) {
		this.processId = processId;
	}
	
	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof RAHandler))	return false;
		RAHandler secondObj = (RAHandler)obj;
		
		return (node.equals(secondObj.getNode()) && process.equals(secondObj.getProcess()) &&
				processId.equals(secondObj.getProcessId()) && timestamp == secondObj.getTimestamp());
	}
	
	@Override
	public int hashCode() {
		String hashString = String.valueOf(node.hashCode()) + process + processId + timestamp;
		return hashString.hashCode();
	}
	
	@Override
	public int compareTo(RAHandler obj) {
		if(this.timestamp > obj.timestamp) return  1;
		else if(this.timestamp < obj.timestamp)	return -1;
		return 0;
	}
}
