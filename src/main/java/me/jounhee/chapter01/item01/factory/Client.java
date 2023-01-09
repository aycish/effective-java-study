package me.jounhee.chapter01.item01.factory;

import me.jounhee.chapter01.item01.util.Console;

public class Client {
	public static void main(String[] args) {
		int trial = 0;

		ClassA classA = new ClassA();
		ClassB classB = new ClassB();

		trial = Integer.parseInt(Console.readLine());

		for (int i = 0; i < trial; i++) {
			String userInput = Console.readLine();
			classA.createPerson(userInput);
			classB.createPerson(userInput);
		}
	}
}
