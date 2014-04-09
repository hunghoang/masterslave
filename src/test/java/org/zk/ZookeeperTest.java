package org.zk;

import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class ZookeeperTest {
	
	private ZooKeeper zk;
	
	private String masterPath = "/masterNode";
	
	@Before
	public void setUp() {
		if (zk != null) {
			try {
				zk.close();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private boolean createConnection() throws IOException, InterruptedException {
		String address = "127.0.0.1:2181";
		zk = new ZooKeeper(address, 3000, null);
		Thread.sleep(200);
		return zk.getState().isConnected();
		
	}
	@Test
	public void testConnectZookeeper() throws IOException, InterruptedException {
		boolean isConnected = createConnection();
		Assert.assertTrue("Cannot connect ZKServer", isConnected);
		
	}
	
	@Test
	public void testCreatePathZookeeper() throws IOException, KeeperException, InterruptedException {
		if (createConnection()) {
			zk.create(masterPath, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		}
		Assert.assertTrue("Create master node fail", zk.exists(masterPath, null) != null);
	}
	
	@Test(expected = KeeperException.NodeExistsException.class)
	public void testCreateDuplicatePathFail() throws IOException, KeeperException, InterruptedException {
		if (createConnection()) {
			zk.create(masterPath, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		}
		zk.create(masterPath, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
	}
	
	@Test
	public void testNodeAutoDeleteAfterDisconnect() throws IOException, KeeperException, InterruptedException {
		if (createConnection()) {
			zk.create(masterPath, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
			Assert.assertTrue("Master Node is not created", zk.exists(masterPath, null) != null);
		}
		zk.close();
		Thread.sleep(5000);
		if (createConnection()) {
			Assert.assertTrue("Master Node is still existed", zk.exists(masterPath, null) == null);
		}
	}
}
