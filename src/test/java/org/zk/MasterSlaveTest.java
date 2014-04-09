package org.zk;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.handler.timeout.TimeoutException;
import org.junit.Assert;
import org.junit.Test;

public class MasterSlaveTest {

	@Test(expected=TimeoutException.class)
	public void testWithZookeeperDoesntExist() {
		Application app = null;
		try {
			String zooKeeperAddr = "127.0.0.1:1111";
			String masterNode = "/masterNode";
			app = new Application(zooKeeperAddr, masterNode);
			app.start();
		} finally {
			app.stop();
		}
	}
	
	@Test
	public void testSetUpMaster() throws InterruptedException {
		Application app = new Application();
		app.start();
		Assert.assertEquals(true, app.isMaster());
		app.stop();
	}

	@Test
	public void testStart2ApplicationThenOneMasterOneSlave() {
		Application app1 = new Application();
		app1.start();
		Assert.assertEquals(true, app1.isMaster());
		System.out.println("---");
		Application app2 = new Application();
		app2.start();
		Assert.assertEquals(true, app2.isSlave());
		app1.stop();
		app2.stop();
	}

	@Test
	public void testStart2ApplicationConcurrentlyThenCheckMasterSlave() throws InterruptedException {
		final Application app1 = new Application();
		new Thread(new Runnable() {
			@Override
			public void run() {
				app1.start();
			}
		}).start();

		final Application app2 = new Application();
		new Thread(new Runnable() {
			@Override
			public void run() {
				app2.start();
			}
		}).start();

		// Wait app start successfully
		Thread.sleep(2000);
		Assert.assertFalse("2 App cannot be master concurrently", app1.isMaster() && app2.isMaster());
		Assert.assertFalse("2 App cannot be slave concurrently", app1.isSlave() && app2.isSlave());
		Assert.assertTrue("Noone is master", (app1.isSlave() && app2.isMaster()) || (app1.isMaster() && app2.isSlave()));
		System.out.println("App1 is Master " + app1.isMaster());
		System.out.println("App2 is Master " + app2.isMaster());
		app1.stop();
		app2.stop();
	}

	@Test
	public void testStartManyApplicationsConcurrentlyThenCheckMasterSlave() throws InterruptedException {

		List<Application> appList = new ArrayList<Application>();
		for (int i = 0; i < 10; i++) {
			appList.add(new Application());
		}

		for (final Application app : appList) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					app.start();
				}
			}).start();
		}

		// Wait app start successfully
		Thread.sleep(2000);

		int numsOfMaster = 0;
		for (final Application app : appList) {
			if (app.isMaster()) {
				numsOfMaster++;
			}
		}

		Assert.assertEquals("Number of master is greater than 1", 1, numsOfMaster);
		
		// Stop all applications
		for (final Application app : appList) {
			app.stop();
		}
	}

	@Test
	public void testAutomaticallyBecomeMasterIfOneAppStop() throws InterruptedException {
		Application app1 = new Application();
		app1.start();
		Assert.assertTrue("App 1 is not master", app1.isMaster());
		Application app2 = new Application();
		app2.start();
		Assert.assertTrue("App 2 is not slave", app2.isSlave());

		// Stop App1
		app1.stop();
		Thread.sleep(2000);

		// Then after that, app 2 is going to be master, app 1 is slave
		Assert.assertTrue("App 1 is still a master", app1.isSlave());
		Assert.assertTrue("App 2 is not master", app2.isMaster());

		System.out.println("App1 is Master: " + app1.isMaster());
		System.out.println("App2 is Master: " + app2.isMaster());
		app2.stop();
	}

	@Test
	public void test2AppBecomeSlaveAfterStop() throws InterruptedException {
		Application app1 = new Application();
		app1.start();
		Assert.assertTrue("App 1 is not master", app1.isMaster());
		Application app2 = new Application();
		app2.start();
		Assert.assertTrue("App 2 is not slave", app2.isSlave());

		// Stop App1
		app1.stop();

		// Wait for other app become master
		Thread.sleep(2000);

		// Then after that, app 2 is going to be master, app 1 is slave
		Assert.assertTrue("App 1 is still a master", app1.isSlave());
		Assert.assertTrue("App 2 is not master", app2.isMaster());
		System.out.println("App1 is Master: " + app1.isMaster());
		System.out.println("App2 is Master: " + app2.isMaster());

		app2.stop();

		// Wait for other app become master
		Thread.sleep(2000);

		// Then after that,2 app are going to be slave
		Assert.assertTrue("App 1 is still a master", app1.isSlave());
		Assert.assertTrue("App 2 is not master", app2.isSlave());
		System.out.println("App1 is Master: " + app1.isMaster());
		System.out.println("App2 is Master: " + app2.isMaster());
	}
	
	
	@Test
	public void testAppNotifyWhenChangeStateFromMasterToSlaveAndViceVersa() throws InterruptedException {
		MockApplicationListener mockApplicationListener = new MockApplicationListener();
		Application app = new Application();
		app.registListener(mockApplicationListener);
		app.start();
		Assert.assertTrue("Application 1 must not be slave", app.isMaster());
		Assert.assertTrue("ApplicationListener 1 record state of application is slave", mockApplicationListener.isMaster());
		
		MockApplicationListener mockApplicationListener2 = new MockApplicationListener();
		Application app2 = new Application();
		app2.registListener(mockApplicationListener2);
		app2.start();
		
		Assert.assertFalse("Application 2 must not be master", app2.isMaster());
		Assert.assertFalse("ApplicationListener 2 record state of application is master", mockApplicationListener2.isMaster());
		
		app.stop();
		Assert.assertFalse("Application 1 must not be master", app.isMaster());
		Assert.assertFalse("ApplicationListener 1 record state of application is slave", mockApplicationListener.isMaster());
		
		// Thread sleep to wait application 2 become master
		Thread.sleep(5000);
		Assert.assertTrue("Application 2 must not be slave", app2.isMaster());
		Assert.assertTrue("ApplicationListener 2 record state of application is slave", mockApplicationListener2.isMaster());
		
		app2.stop();
		app.start();
		// Thread sleep to wait application 1 become master
		Thread.sleep(5000);
				
		Assert.assertTrue("Application 1 must not be slave", app.isMaster());
		Assert.assertTrue("ApplicationListener 1 record state of application is slave", mockApplicationListener.isMaster());
		
		app.stop();
		
	}
	
	
}






