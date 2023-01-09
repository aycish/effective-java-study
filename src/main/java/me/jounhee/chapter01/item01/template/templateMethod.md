### [참고 링크](https://victorydntmd.tistory.com/298?category=719467)

---

## 정의

- 여러 클래스에서 공통으로 사용하는 메서드를 상위 클래스에서 정의 (템플릿 메서드)
- 하위 클래스마다 다르게 구현해야 하는 세부적인 사항을 하위 클래스에서 구현하는 패턴 (훅)
- 우리가 일반적으로 알고있는 리팩터링 기법

## 예시 코드

```java
public class Parent {
	// 자식에서 공통적으로 사용하는 부분을 템플릿 메서드라 한다.
	public void doSomthing() {
		System.out.println("부모에서 실행되는 부분");

		// 자식에서 구현해야 할 부분을 훅 메서드라 한다.
		hook();
	}

	public void hook() {
	}

	;
}
```

```java
public class ChildA extends Parent {
	@Override
	public void hook() {
		System.out.println("Child A 에서 hook 구현");
	}
}
```

```java
public class ChildB extends Parent {
	@Override
	public void hook() {
		System.out.println("Child B 에서 hook 구현");
	}
}
```

```java
public class Client {
	public static void main(String args[]) {
		ChildA childA = new ChildA();
		childA.someMethod();

		System.out.println("--------");

		ChildB childB = new ChildB();
		childB.someMethod();
	}
}
```

- Parent에서 정의된 someMethod()는 자식 클래스에서 공통으로 사용하는 부분
- hook() 메서드만 자식 클래스들에서 따로 구현해줘야 하는 부분이다.
- 중복 로직등을 제거하여 유지 보수에 용이하게끔 하려는 용도로 사용하는것 같다.
