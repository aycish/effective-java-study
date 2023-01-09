### [참고 링크](https://victorydntmd.tistory.com/299)

---

## 정의

- 조건에 따라 다른 객체를 한 클래스에서 생성해야할 필요가 있을 때, 객체의 생성을 직접하지 않고 팩토리라는 클래스에 위임하는 것
- 즉, 팩토리는 말 그대로 객체를 찍어내는 공장을 의미

## 왜 사용할까?

분기에 따라 객체를 생성하는 코드가 중복되었을 때, 유지보수가 어려워지므로 사용한다. 즉, 객체간 결합도를 떨구는 작업이다.

- 예시 코드

```java
public abstract class Person {
}
```

```java
public class PersonA extends Person {
	public PersonA() {
		System.out.println("Person A created.");
	}
}
```

```java
public class PersonB extends Person {
	public PersonB() {
		System.out.println("Person B created.");
	}
}
```

```java
public class ClassA {
	public Person createPerson(String type) {
		Person returnPerson = null;
		switch (type) {
			case "A":
				returnPerson = new PersonA();
				break;
			case "B":
				returnPerson = new PersonB();
				break;
		}
		return returnPerson;
	}
}
```

```java
public class Client {
	public static void main(String args[]) {
		ClassA classA = new ClassA();
		classA.createPerson("A");
		classA.createPerson("B");
	}
}
```

상기와 같이 조건에 따라 객체를 다르게 생성하는 상황에서, ClassA 뿐만아니라 ClassB, ClassC 도 존재한다고 가정해보자. 위와 같은 switch 로직이 중복되게 존재해야 하고, 객체를 직접 생성함으로써
객체간의 결합도가 높아진다. 즉 유지보수가 어려워진다.

## 팩토리 메서드를 적용해보자

팩토리 메서드를 적용하는 방법은 다음과 같다.

1. **팩토리 클래스 정의**
2. **객체 생성이 필요한 클래스(ClassA)에서 팩토리 객체를 생성하여 분기에 따라 객체 생성 메서드를 호출하도록 한다.**

PersonA, PersonB, Client는 그대로 두고, Factory 를 만들고, ClassA를 수정해보자.

- 예시 코드

```java
public class PersonFactory {
	public Person createPerson(String type) {
		Person returnPerson = null;
		switch (type) {
			case "A":
				returnPerson = new PersonA();
				break;
			case "B":
				returnPerson = new PersonB();
				break;
		}
		return returnPerson;
	}
}
```

```java
public class ClassA {
	public Person createPerson(String type) {
		PersonFactory factory = new PersonFactory();
		Person returnPerson = factory.createPerson(type);
		return returnPerson;
	}
}
```

기존의 ClassA가 하던 일을 인간 공장(?) 클래스가 대신 하고 있다. 즉, 조건에 따른 객체 생성 부분을 Factory 클래스에 위임하여 객체를 생성하게끔 수정이 되었고, 이를 통해 Class와 Person
간의 객체 결합도가 낮아지게 되어 유지보수에 용이하게 되었다. 만약 조건이 추가되거나 반환해야 하는 객체 타입이 변경된다면, PersonFactory 클래스만 수정하면 되므로 아주 유지보수에 용이해졌다.
