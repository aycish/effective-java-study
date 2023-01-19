# Item 03. 생성자나 열거 타입으로 싱글턴임을 보증하라.

## 첫번째 방법. private 생성자 + public static final 필드

---

### 예제 코드

```java
public class Elvis implements IElvis, Serializable {

    /**
     * 싱글톤 오브젝트
     */
    public static final Elvis INSTANCE = new Elvis();
    private static boolean created;

    private Elvis() {
        if (created) {
            throw new UnsupportedOperationException("can't be created by constructor.");
        }

        created = true;
    }
}
```

- 생성자를 private으로 선언
- public static final 필드를 통해 인스턴스를 제공한다.

### 장점

**간결하고 싱글턴임을 API에 드러낼 수 있다.**

### 단점

**싱글톤을 사용하는 클라이언트를 테스트하기 어려워진다.**

- Client 코드 예시

```java
/* Client */
public class Concert {

    private boolean lightsOn;
    private boolean mainStateOpen;
    private IElvis elvis;

...
    public void perform() {
        mainStateOpen = true;
        lightsOn = true;
        elvis.sing();
    }
}

/* Test Code */
@Test
void testPerform() {
	...
	concert.perform();
	assert();
}

```

- 실제 Elvis 객체가 그대로 사용되기 때문에, 테스트 비용이 비싸진다.
- Interface를 제공한다면 mocking을 사용할 수 있다.
- 내지는 mockito에서 제공하는 기능을 통해 static을 mocking하여 우회할 수 있지만, 매우 번거롭다.

**리플렉션으로 private 생성자를 호출할 수 있다.**

- 예제 코드

```java
try {
    Constructor<Elvis> defaultConstructor = Elvis.class.getDeclaredConstructor();
    defaultConstructor.setAccessible(true);
    Elvis elvis1 = defaultConstructor.newInstance();
    Elvis elvis2 = defaultConstructor.newInstance();
    Elvis.INSTANCE.sing();
} catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
    e.printStackTrace();
}
```

- setAccesible을 true로 지정하게 되면, private 생성자를 호출할 수 있다.
- 매번 새로운 인스턴스를 생성할 수 있다.
- 이를 방지하고자, private 생성자에 예외를 던지면 상기 상황을 방지할 수 있다.

**역직렬화 할 때, 새로운 인스턴스가 생길 수 있다.**

- 예제 코드

```java
try (ObjectOutput out = new ObjectOutputStream(new FileOutputStream("elvis.obj"))) {
    out.writeObject(Elvis.INSTANCE);
} catch (IOException e) {
    e.printStackTrace();
}

try (ObjectInput in = new ObjectInputStream(new FileInputStream("elvis.obj"))) {
    Elvis elvis3 = (Elvis) in.readObject();
    System.out.println(elvis3 == Elvis.INSTANCE);
} catch (IOException | ClassNotFoundException e) {
    e.printStackTrace();
}
```

- 역직렬화 시도 시, 생성자가 호출된다.
    - 직렬화는 JVM에서 다루는 형태로 객체를 저장
    - 역직렬화는 readResolve()를 통해 직렬화된 데이터를 담을 객체를 반환받아 갈아낀다.
- 막는 방법
    - readResolve() 메서드가 호출되기 때문에, readResolve를 재정의하여 기존의 INSTANCE를 반환하도록 수정하면 막을 수 있다.
- 따라서 싱글턴을 보증할 수 없다.
- 직렬화 관련 링크 - [https://catsbi.oopy.io/a45ea6f0-c9e7-4426-8782-4aa24d4d3b06](https://catsbi.oopy.io/a45ea6f0-c9e7-4426-8782-4aa24d4d3b06)

## 두 번째 방법. private 생성자 + 정적 팩터리 메서드

---

### 예제 코드

```java
public class Elvis implements IElvis, Serializable {

    private static final Elvis INSTANCE = new Elvis();
    private static boolean created;
		public static Elvis getInstance() { return INSTANCE; }
    private Elvis() {
        if (created) {
            throw new UnsupportedOperationException("can't be created by constructor.");
        }

        created = true;
    }
}
```

### 장점

**API를 바꾸지 않고도 싱글턴이 아니게 변경할 수 있다.**

- getInstance() 메서드의 내부 구현만 바꾸면, 손쉽게 수정 가능

**정적 팩토리를 제네릭 싱글턴 팩토리로 만들 수 있다.**

- 예제 코드

```java
public static <T> MetaElvis<T> getInstance() { return (MetaElvis<T>) INSTANCE; }
```

- 우리가 원하는 타입으로 형변환을 손쉽게 수행 가능
- 하지만, 다른 타입이기 때문에 “==” 비교는 수행할 수 없다.
- static <T> 는 Class <T>와 scope가 다르므로 주의

**정적 팩토리의 메서드 참조를 공급자(Supplier)로 사용할 수 있다.**

- 예제 코드

```java
public Concert {
	public void start(Supplier<Singer> singerSupplier) {
		Singer singer = singerSupplier.get();
		singer.sing();
	}

	public static void main() {
		Concert concert = new Concert();
		concert.start(Elvis::getInstance);
	}
}
```

### 단점

- 첫 번째 방법과 동일

## 세 번째 방법. 열거타입

---

### 예제 코드

```java
public enum Elvis {
    INSTANCE;

    public void leaveTheBuilding() {
        System.out.println("기다려 자기야, 지금 나갈께!");
    }
}

// 이 메서드는 보통 클래스 바깥(다른 클래스)에 작성해야 한다!
public static void main(String[] args) {
    Elvis elvis = Elvis.INSTANCE;
    elvis.leaveTheBuilding();
}
```

### 장점

**다른 방법들에 비해 가장 간결하다.**

- Test하는 경우에도 비슷하게 인터페이스를 만들어 Mocking하여 Test를 간소화 시킬 수 있다.



**직렬화와 리플렉션에도 안전하다.**

- enum은 열거형만을 제공해야하기 때문에, 애초에 생성자가 있어도, 접근할 수 없도록 막아져 있다.

!!!! 대부분의 상황에서는 원소가 하나뿐인 열거 타입이 싱글턴을 만드는 가장 좋은 방법이다.