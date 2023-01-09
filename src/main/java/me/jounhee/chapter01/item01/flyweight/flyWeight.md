### [참조 링크](https://lee1535.tistory.com/106)

---

## 정의

- 어떤 클래스의 인스턴스 한 개만 가지고, 여러 개의 가상 인스턴스를 제공하고 싶을 때 사용하는 패턴
- new 연산자를 통한 메모리 낭비를 줄이기 위한 패턴이다.
- 싱글톤 패턴과 비슷한 느낌

## 구성

- Flyweight 역할
    - 공유에 사용할 클래스들의 인터페이스 선언

    ```java
    public interface Shape {
        public void draw();
    }
    ```

- ConcreteFlyweight 역할
    - Flyweight의 내용을 정의
    - 실제 공유될 객체

    ```java
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
    ```

- FlyweightFactory 역할
    - Factory를 활용하여 Flyweight의 인스턴스를 생성 또는 공유해주는 역할을 한다.

    ```java
    public class ShapeFactory {
        private static final HashMap<String, Circle> circleMap = new HashMap<>();
        
        public static Shape getCircle(String color, int radius) {
            Circle circle = (Circle)circleMap.get(color);
    
            if (circle == null) {
                circle = new Circle(color, radius);
                circleMap.put(color,circle);
            }
            return circle;
        }
    }
    ```

- Client 역할
    - 해당 패턴의 사용자

    ```java
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
    ```

## 장점

- 실행시, 생성하는 인스턴스의 수를 조절할 수 있어 메모리 절약 가능
- 여러 "가상"객체의 상태를 한 곳에서 집중시켜 놓을 수 있다. 즉, 제어에 용이하다.

## 단점

- 특정 인스턴스만 다른 인스턴스처럼 동작하도록 하는 것이 불가능 → 객체간 공유될 수 있으므로
- 즉, 객체의 값을 변경하면 공유받은 가상 객체를 사용하는 곳에 영향을 줄 수 있다.
