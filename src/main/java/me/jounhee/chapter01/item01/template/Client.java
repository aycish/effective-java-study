package me.jounhee.chapter01.item01.template;

public class Client {
	public static void main(String[] args) {
		ChildA childA = new ChildA();
		childA.doSomething();

		System.out.println("------------------");

		ChildB childB = new ChildB();
		childB.doSomething();
	}
}
