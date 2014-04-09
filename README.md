* You need maven to build this project
	To build project without test

		mvn clean install -DskipTests

	To build project with test, you must start zookeeper (default address: 127.0.0.1:2181):
	
		mvn clean install -DskipTests
		
* Guide

1. Create class: MockApplicationListener implements ApplicationListener, overide method onChange()

2. Create Application, call start() method

	MockApplicationListener mockApplicationListener = new MockApplicationListener();
	Application app = new Application();
	app.registListener(mockApplicationListener);
	app.start();

Whenever app becomes master or slave, mockApplicationListener will be called by onChange method

	