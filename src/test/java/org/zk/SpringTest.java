package org.zk;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringTest {
	public static void main(String[] args) {
		System.out.println("Start spring container");
		System.out.println("=======================");
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
		System.out.println("Stop spring container");
		System.out.println("=======================");
		context.stop();
		context.close();
		context.destroy();
		System.out.println("Start spring container");
		System.out.println("=======================");
		context = new ClassPathXmlApplicationContext("beans.xml");
		context.close();
		context.destroy();
	}
}
