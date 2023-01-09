package me.jounhee.chapter01.item01.flyweight;

public class Circle implements Shape {
	private String color;
	private int radius;

	public Circle(String color, int radius) {
		this.color = color;
		this.radius = radius;
		System.out.println("[" + color + "]" + "created with " + this.radius);
	}

	@Override
	public void draw() {
		System.out.println(toString());
	}

	@Override
	public String toString() {
		return "[" + this.color + "]  radius : " + radius;
	}
}
