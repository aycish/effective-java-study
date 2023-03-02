# Item 10. equals는 일반 규약을 지켜 재정의하라

태그: 모든 객체의 공통 메서드

## Equals는 일반 규약을 지켜 재정의 하라

---

- 책에서는 만들지 않아도 되는 경우라면, 만들지 않는 것이 최선이다라고 말하고 있다.

### 재정의가 필요하지 않는 경우

- 각 인스턴스가 본질적으로 고유한 경우
    - 싱글톤 객체, enum
- 인스턴스의 논리적 등치성을 검사할 필요가 없는 경우
    - 멤버의 특정 값이 같다면 같은 객체로 보는 식
    - 객체별로 구분이 필요하지 않은 경우를 의미
- 상위 클래스에서 재정의한 equals가 하위 클래스에도 적절한 경우
    - List, Set과 같은것을 상속받아 구현하는 경우등이 해당
- 클래스가 private이거나 package-private이고, equals 메서드를 호출할 일이 없는 경우
    - ↔ public class는 equals를 호출하지 않음을 보장할 수 없기 때문

## equals 규약

---

### **반사성**

- A.equals(A) == true
- 본인과 본인을 비교했을 때, 같아야함

### **대칭성**

- A.equals(B) == B.equals(A)
- CaseInsensitiveString
- 예제

```java
public final class CaseInsensitiveString {
    private final String s;

    public CaseInsensitiveString(String s) {
        this.s = Objects.requireNonNull(s);
    }

    // 대칭성 위배!
    @Override
		public boolean equals(Object o) {
        if (o instanceof CaseInsensitiveString)
            return s.equalsIgnoreCase(((CaseInsensitiveString) o).s);
        if (o instanceof String)  // 한 방향으로만 작동한다!
            return s.equalsIgnoreCase((String) o);
        return false;
    }

		public static void main(String[] args) {
        CaseInsensitiveString cis = new CaseInsensitiveString("Polish");
        CaseInsensitiveString cis2 = new CaseInsensitiveString("polish");
        
				String polish = "polish";
        System.out.println(cis.equals(polish));  // true
        System.out.println(cis2.equals(cis));    // true

        List<CaseInsensitiveString> list = new ArrayList<>();
        list.add(cis);

        System.out.println(list.contains(polish)); // false : String Class에서는 polish와 Polish를 구분할 수 있기 때문
    }

}
```

### **추이성**

- A.equals(B) && B.equals(C), A.equals(C)
    - Point, ColorPoint(inherit), CounterPointer, ColorPoint(comp)
- A와 B가 같고, B가 C와 같다면, A는 C와 같아야함
- 예제

    ```java
    public class Point {
    
        private final int x;
        private final int y;
    
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    
        @Override
    		public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
    
            if (!(o instanceof Point)) {
                return false;
            }
    
            Point p = (Point) o;
            return p.x == x && p.y == y;
        }
    }
    
    public class ColorPoint extends Point {
        private final Color color;
    
        public ColorPoint(int x, int y, Color color) {
            super(x, y);
            this.color = color;
        }
    
    		@Override
        public boolean equals(Object o) {
            if (!(o instanceof Point))
                return false;
    
            // o가 일반 Point면 색상을 무시하고 비교한다. -> stack overflow, 서로 계속해서 equals를 호출할 수 있음
            if (!(o instanceof ColorPoint))
                return o.equals(this);
    
            // o가 ColorPoint면 색상까지 비교한다.
            return super.equals(o) && ((ColorPoint) o).color == color;
        }
    }
    ```

- Color까지 비교하기 때문에, Point와 ColorPoint는 같을 수 없다.
    - 따라서 추이성이 위배된다.
- 요약하자면, 구현체가 기존 상위 클래스에 새로운 멤버를 추가하는 경우, 대칭성과 추이성을 만족하도록 equals를 작성할 수 없다.
- Composition 패턴을 사용하면 만족시킬 수 있다.

```java
public class ColorPoint {
    private final Color color;
		private final Point point;

		public ColorPoint(int x, int y, Color color) {
        point = new Point(x, y);
        this.color = Objects.requireNonNull(color);
    }

		// Color Point를 Point로 취급할 수 있는 인터페이스를 제공
		public Point asPoint() {
        return point;
    }

		@Override
		public boolean equals(Object o) {
        if (!(o instanceof ColorPoint))
            return false;
        ColorPoint cp = (ColorPoint) o;
        return cp.point.equals(point) && cp.color.equals(color);
    }
}
```

### **일관성**

- A.equals(B) == A.equals(B)
- 어떤 시점에 호출하여도 값은 같아야한다
- 예시

    ```java
    public class EqualsInJava extends Object {
    
        public static void main(String[] args) throws MalformedURLException {
            long time = System.currentTimeMillis();
            Timestamp timestamp = new Timestamp(time);
            Date date = new Date(time);
    
            // 대칭성 위배! P60
            System.out.println(date.equals(timestamp));
            System.out.println(timestamp.equals(date));
    
            // 일관성 위배 가능성 있음. P61
            URL google1 = new URL("https", "about.google", "/products/");   // Virtual Hosting을 하는 URL인 경우, 일관성이 보장되지 않을 수 있다.
            URL google2 = new URL("https", "about.google", "/products/");   // 따라서, 그 상황에서는 URL을 까서 IP주소를 얻어와 이를 비교한다.
            System.out.println(google1.equals(google2));
        }
    }
    ```


### **null-아님**

- A.equals(null) == false
- equals에 NULL 객체를 넣었을 때, false가 반환되어야한다.

## Equals 구현 방법

---

### 내가 구현 할 때

- == 연산자를 사용해 자기 자신의 참조인지 확인한다.
- instanceof 연산자로 올바른 타입인지 확인한다.
- 입력된 값을 올바른 타입으로 형변환 한다.
- 입력 객체와 자기 자신의 대응되는 핵심 필드가 일치하는지 확인한다.
    - Lock과 같은 필드들은 사용하면 안된다. 자기 자신과 대응되는 객체가 아니기 때문
    - 만약 핵심 필드 중, Null값이 허용되는 경우, Objects의 equals를 사용한다.
- 예제

```java
@Override
public boolean equals(Object o) {
    // 자기 자신의 참조인지 확인
		if (this == o) {
        return true;
    }

		// 올바른 타입인지 확인
    if (!(o instanceof Point)) {
        return false;
    }

		// 입력된 값을 올바른 타입으로 형변환
    Point p = (Point) o;

		// 입력 객체와 자기 자신의 대응되는 핵심 필드가 일치하는지 확인
    return p.x == x && p.y == y;
}
```

### 복잡하니까 툴을 쓰자!

- 구글의 AutoValue를 사용한다.
    - com.google.auto.value package 사용
    - @AutoValue 어노테이션을 통해 equals를 생성해서 사용하자.
- Lombok을 사용한다.
    - 기존의 코드를 변경하기 때문에, 주의해야한다.
- 자바의 Record를 사용한다.
    - Value Object를 만드는 경우에는 굳이 Lombok이나, AutoValue를 사용하지 않아도 된다.
    - 과제) 자바의 Record를 공부하세요
- IDE의 코드 생성 기능을 사용한다.

### 주의 사항

- equals를 재정의 할 때 hashCode도 반드시 재정의하자. (아이템 11)
- 너무 복잡하게 해결하지 말자.
- Object가 아닌 타입의 매개변수를 받는 equals 메서드는 선언하지 말자.

## Value 기반의 클래스

---

### 정의

- 클래스처럼 생겼지만, int처럼 동작하는 클래스

### 특징

- 식별자가 없고 불변이다.
- 식별자가 아니라 인스턴스가 가지고 있는 상태를 기반으로 equals, hashCode, toString을 구현한다.
- == 오퍼레이션이 아니라 equals를 사용해서 동등성을 비교한다.
- 동일한(equals) 객체는 상호 교환 가능하다.

### 자바의 record 말고 만드는 방법!

- final Class로 생성하여 emutable하게 만든다.
- equals, hashCode를 만들어 준다.
- 유일하게 식별할 수 있는 필드가 존재하면 안된다.

## StackOverflowError

---

### Stack 정의

- Stack에는 Stack Frame이 쌓이는 구조
- Stack Frame은 한 스레드가 사용하는 공간
- 메서드에 전달하는 매개변수, 메서드 실행 끝내고 돌아갈 곳, 힙에 들어있는 객체에 대한 레퍼런스

### StackOverFlowError

- 운영체제, JVM 마다 정해져있는 Stack의 최대 크기 (1MB)보다 Frame들의 용량이 커지는 경우에 생기는 Error

## 리스코프 치환 원칙

---

### 정의

- 하위 클래스의 객체가 상위 클래스 객체를 대체하더라도 소프트웨어의 기능을 깨트리지 않아야 한다.
- semantic over syntactic, 구문 보다는 의미