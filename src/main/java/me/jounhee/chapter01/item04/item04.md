# Item 04. 인스턴스화를 막으려거든 private 생성자를 사용하라

인스턴스화를 하지 않는 것이 권장되는 경우가 있는데, static 메서드만 제공하는 유틸리티성 클래스가 해당된다. 정적 메서드만 담은 유틸리티 클래스는 인스턴스로 만들어 쓰려고 설계한 클래스가 아니다.

여러가지 방법이 있는데, 그 방법들은 하기와 같다.

- 추상 클래스로 만들기
- private 생성자를 사용하기

어떤 방법으로 인스턴스화를 막을 수 있을지 살펴보자.

## 방법 1. abstract class로 만들기 → 못막음

---

추상 클래스로 만든다면, 겉으로 보기에는 인스턴스화를 막히는 것 처럼 보인다.

### 예시 - abstract class

```java
public abstract class AbstractUtilityClass {

    public static void utilityMethod() {
        System.out.println("Utility method");
    }
}
```

- 위 코드를 보면 생성자가 존재하지 않기 때문에 인스턴스화가 막히는것 처럼 보인다.
- 하지만, 자바 컴파일러는 생성자를 명시하지 않으면 기본 생성자를 만들어 준다.
- 상기 예제를 파훼하는 케이스인 subclass를 두는 경우를 생각해보자

### 예시 - subclass

```java
public class ExtendedUtilityClass extends AbstractUtilityClass {
}
```

- 상속을 받게 되면, 자식 클래스를 인스턴스화할 때, 기본 생성자를 사용한다면, 부모의 생성자를 호출하기 때문에 인스턴스화를 막을 수 없다.
- 또한, 상속을 받아서 사용해야하는 건가? 라고 사용자가 착각할 수 있다.

## 방법 2. private 생성자를 사용하는 방법

---

- private 생성자를 추가하면 클래스의 인스턴스화를 막을 수 있다.

### 예시

```java
public class UtilityClass {

    private UtilityClass() {}

    public static void utilityMethod() {
        System.out.println("Utility method");
    }
}
```

- 생성자를 private으로 막아놨기 때문에, 외부에서 호출할 수 없게되고 인스턴스화를 막을 수 있다.
- 하지만, 내부에서는 인스턴스화를 할 수 있다.

### 예시 - 내부에서 생성자 호출

```java
public class UtilityClass {

    private UtilityClass() {}
    public static void utilityMethod() {
        System.out.println("Utility method");
    }
    public static void main(String[] args) {
        UtilityClass.utilityMethod();
        UtilityClass utilityClass = new UtilityClass();
        utilityClass.utilityMethod();
    }
}
```

- 상기와 같은 경우, 내부에서 객체화를 할 수 있다.
- private 생성자 내부에 Exception을 던져 막는 방법이 있다.

```java
public class UtilityClass {

    private UtilityClass() {
				throw new AssertionError();
		}
    
		public static void utilityMethod() {
        System.out.println("Utility method");
    }
    public static void main(String[] args) {
        UtilityClass.utilityMethod();
        UtilityClass utilityClass = new UtilityClass();
        utilityClass.utilityMethod();
    }
}
```

- 상기의 경우, 인스턴스화할 때 Exception이 던져지면서 내부에서도 생성자를 호출할 수 없게된다.
- AssertionError를 꼭 던질 필요는 없지만, 클래스 안에서 실수로라도 생성자를 호출하지 않도록 해준다.
    - AssertionError는 try-catch하도록 만든 익셉션이 아니므로, 해당 익셉션이 발생한다면 잘못된 경우이므로 수정하자.
- 생성자가 보이는데 생성자를 못쓰게되는것이 아이러니하다. 의문을 최대한 해소시키기 위해 주석을 꼭 달도록 하자.
- 이 방법은 상속을 불가능하게 하는 효과도 있다.
    - 모든 생성자는 명시적이든 묵시적이든 상위 클래스의 생성자를 호출하게 된다.
    - 이를 private으로 선언함으로써, 하위 클래스가 상위 클래스의 생성자에 접근할 수 없게된다.

## Spring은 어떨까?

---

- Spring은 빈 생성을 막기 위해, abstract class화가 적용되어 있다. (ActiveProfilesUtils, AnnotationConfigUtils… 등등등)
- 하지만 이는, 상속받으면 이를 파훼할 수 있다.