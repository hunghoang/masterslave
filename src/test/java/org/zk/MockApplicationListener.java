package org.zk;

public class MockApplicationListener implements ApplicationListener {

	private boolean isMaster;
	
	@Override
	public void onChange(boolean isMaster) {
		this.isMaster = isMaster;
	}

	public boolean isMaster() {
		return isMaster;
	}

}
