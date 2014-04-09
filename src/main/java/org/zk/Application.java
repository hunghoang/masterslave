package org.zk;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.jboss.netty.handler.timeout.TimeoutException;

public class Application implements Watcher {

	private static final Logger log = Logger.getLogger(Application.class.getName());
	
	private String address = "127.0.0.1:2181";
	private String masterNode = "/masterNode";
	
	private int timeoutSessionMiliseconds = 3000; 
	private ApplicationListener listener;
	
	private boolean isMaster;
	
	private boolean isActive;
	
	private ZooKeeper zk;
	
	public Application() {
		
	}
	
	public void registListener(ApplicationListener listener) {
		this.listener = listener;
	}
	
	public Application(String address, String masterNode) {
		this.address = address;
		this.masterNode = masterNode;
	}
	
	public synchronized void start() {
		try {
			zk = new ZooKeeper(address, timeoutSessionMiliseconds, this);
			isActive = true;
			tryToBeMaster();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (KeeperException.ConnectionLossException e){
			// Error when make connection, throw exception
			log.warn("ConnectionLossException", e);
			throw new TimeoutException(e.getCause());
		} catch (KeeperException e){
			// Error when create master node, Application is going to be a slave;
			log.warn("KeeperException", e);
		}
	}

	public synchronized void stop() {
		try {
			isActive = false;
			changeState(false);
			if (zk != null) {
				zk.close();
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private void tryToBeMaster() throws KeeperException, InterruptedException {
		boolean canBeMaster = false;
		if (zk.exists(masterNode, this) == null) {
			try {
				zk.create(masterNode, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
				canBeMaster= true;
			} catch (KeeperException.NodeExistsException e) {
				log.warn("NodeExist, Application gonna be a slave");
			}
		}
		changeState(canBeMaster);
	}

	private void changeState(boolean isMaster) {
		this.isMaster = isMaster;
		if (listener != null) {
			listener.onChange(isMaster);
		}
	}
	
	public boolean isMaster() {
		return isMaster;
	}

	public boolean isSlave() {
		return !isMaster;
	}


	public boolean isActive() {
		return isActive;
	}

	@Override
	public void process(WatchedEvent we) {
		// Type of watched event = NONE means state of connection has changed
		if (EventType.None.equals(we.getType())) {
			if (KeeperState.SyncConnected.equals(we.getState())) {
				log.info("Connected to Zookeeper");
				return;
			}	
			changeState(false);
			return;
		} 
		
		if (EventType.NodeDeleted.equals(we.getType())) {
			if (!isActive()) return;
			try {
				log.debug("Node delete, retry to be master");
				tryToBeMaster();
			} catch (KeeperException e) {
				throw new RuntimeException(e);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		
	}
}