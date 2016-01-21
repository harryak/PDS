package org.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.net.LocalEndPoint;

public class RA implements Runnable {
	private List<RAHandler> requests;
	private List<RAHandler> pending;
	private Map<RAHandler, Integer> confirmRequest;
	private int lamportClock;
	
	public LocalEndPoint localNetwork;
	
	public RA(LocalEndPoint network) {
		this.requests = new ArrayList<>();
		this.pending = new ArrayList<>();
		this.confirmRequest = new HashMap<>();
		this.lamportClock = 0;
		
		this.localNetwork = network;
	}
	
	public List<RAHandler> getRequests() {
		return this.requests;
	}
	
	public void setRequests(List<RAHandler> requests) {
		this.requests = requests;
	}
	
	public List<RAHandler> getPending() {
		return this.pending;
	}
	
	public void setPending(List<RAHandler> pending) {
		this.pending = pending;
	}
	
	public Map<RAHandler, Integer> getConfirmRequest() {
		return this.confirmRequest;
	}
	
	public void setConfirmRequest(Map<RAHandler, Integer> confirmRequest) {
		this.confirmRequest = confirmRequest;
	}
	
	public int getLamportClock() {
		return this.lamportClock;
	}
	
	public void setLamportClock(int lamportClock) {
		this.lamportClock = lamportClock;
	}
	
	public void syncClocks(int otherLamportClock) {
		this.lamportClock = Math.max(lamportClock, otherLamportClock);
	}
	
	public int incrLamportClock() {
		this.lamportClock += 1;
		return getLamportClock();
	}
	
	public boolean isLocked(RAHandler raHandler) {
		Integer num = this.confirmRequest.get(raHandler);
		if(num == null) num = 0;
		
		return LocalEndPoint.listOfNodes.size() <= num;
	}
	
	public RAHandler firstRequest(RAHandler raHandler) throws Exception {
		List<RAHandler> listOfHandlers = new ArrayList<>();
		for(RAHandler rah : requests) {
			if(rah.equals(raHandler)) {
				listOfHandlers.add(rah);
			}
		}
		
		if(listOfHandlers.size() > 0) {
			Collections.sort(listOfHandlers);
			return listOfHandlers.get(0);
		}
		
		return null;
	}
	
	@Override
	public void run() {
		boolean stop = false;
		while(!stop) {
			try {
				Thread.sleep(5000);
				//XmlRpcClient remoteServer = Network.getXmlRpcClient(Network.localNode);
				//remoteServer.execute("network.pending_requests", new Object[]{});
			} catch(InterruptedException exception) {
				// Interrupt. We want the server to shut down.
				stop = true;
			} catch(Exception exception) {
				// True Exception. Most likely we didn't want the server to shut down, so print stack trace.
				exception.printStackTrace();
				stop = true;
			}
		}
		
		try {
			this.localNetwork.signOffNetwork();
		} catch(Exception exception) {
			exception.printStackTrace();
			stop = true;
		}
	}

}
