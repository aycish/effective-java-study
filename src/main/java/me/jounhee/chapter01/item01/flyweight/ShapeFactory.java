package me.jounhee.chapter01.item01.flyweight;

import java.util.HashMap;

public class ShapeFactory {
	private static final HashMap<String, Circle> circleMap = new HashMap<>();

	public static Shape getCircle(String color, int radius) {
		Circle circle = (Circle)circleMap.get(color);

		if (circle == null) {
			circle = new Circle(color, radius);
			circleMap.put(color, circle);
		}

		return circle;
	}
}
