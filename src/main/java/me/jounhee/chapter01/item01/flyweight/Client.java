package me.jounhee.chapter01.item01.flyweight;

import me.jounhee.chapter01.item01.util.Console;

public class Client {
	private static final int RADIUS = 1;
	private static final int MAX_COUNT = 10;

	public static void main(String[] args) {
		int trial = Integer.parseInt(Console.readLine());
		String[] colors = new String[trial];

		System.out.println("Trial = " + trial);

		for (int i = 0; i < trial; i++) {
			colors[i] = Console.readLine();
		}

		for (int i = 0; i < MAX_COUNT; i++) {
			Circle circle = (Circle)ShapeFactory.getCircle(colors[i % trial], RADIUS);
			circle.draw();
		}
	}
}
