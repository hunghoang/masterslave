* You need maven to build this project
	To build project without test

		mvn clean install -DskipTests

	To build project with test, you must start zookeeper (default address: 127.0.0.1:2181):
	
		mvn clean install
		
* Guide

To integrate Master - Slave Application to your app, you need declare and register a ApplicationListener. Ex:
  
1. Create class: MockApplicationListener implements ApplicationListener, override method onChange()


2. Create Application, call start() method

	
	MockApplicationListener mockApplicationListener = new MockApplicationListener();
	
	Application app = new Application();
	
	app.registListener(mockApplicationListener);
	
	app.start();


Whenever application becomes master or slave, mockApplicationListener will be called by onChange method
