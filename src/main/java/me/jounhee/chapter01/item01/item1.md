# 책 내용 정리

### 찾아봤던 내용들

1. [팩토리 메서드 패턴](https://github.com/aycish/effective-java-study/blob/main/src/main/java/me/jounhee/chapter01/item01/factory/factoryMethod.md) - 본문에서 사용되는 팩터리 메서드와 유사하지만, 동일한 것은 아님
2. [템플릿 메서드 패턴](https://github.com/aycish/effective-java-study/blob/main/src/main/java/me/jounhee/chapter01/item01/template/templateMethod.md)
3. [플라이 웨이트](https://github.com/aycish/effective-java-study/blob/main/src/main/java/me/jounhee/chapter01/item01/flyweight/flyWeight.md)

---

클라이언트가 클래스의 인스턴스를 얻는 수단은 public 생성자지만, 본 책에서는 정적 팩터리 메서드를 활용하여 인스턴스를 얻는 것을 권장하고 있다.

추천하는 이유로 5가지의 장점을 말하고 있는데, 이를 정리해보자.

## 장점

---

### 첫 번째, 이름을 가질 수 있다.

- 명확한 이름을 가진 생성자를 정의할 수 있어, 가독성을 높일 수 있다.
- 동일한 시그니처의 생성자를 두개 가질 수 없기 때문에, 정적 팩토리 메서드를 통해 우회할 수 있다.

```java
public class Order {

    private boolean prime;
    private boolean urgent;
    private Product product;
    private OrderStatus orderStatus;

/* 불가능
	public Order(boolean prime, Product product) {
		this.prime = prime;
		this.product = product;
	}

	public Order(boolean urgent, Product product) {
		this.urgent = urgent;
		this.product = product;
	}

*/
    public static Order primeOrder(Product product) {
        Order order = new Order();
        order.prime = true;
        order.product = product;

        return order;
    }

    public static Order urgentOrder(Product product) {
        Order order = new Order();
        order.urgent = true;
        order.product = product;
        return order;
    }

    public static void main(String[] args) {

        Order order = new Order();
        if (order.orderStatus == OrderStatus.DELIVERED) {
            System.out.println("delivered");
        }
    }

}
```

### 두 번째, 호출될 때마다 인스턴스를 새로 생성하지 않아도 된다.

- 객체 생성 비용이 큰 경우, 생성이 자주 요청된다면 플라이 웨이트 기법과 비슷하게 구현하여 해결할 수 있다.
- 이를 인스턴스 통제 클래스라고 하는데, 인스턴스 통제 클래스를 통해 싱글턴으로 만들거나 인스턴스화 불가로 만들 수 있다.
- Boolean.valueOf()와 같이, 상수를 반환하거나 정해진 인스턴스를 반환하도록 제어할 수 있다.
- 생성자가 존재한다면, 매번 인스턴스가 생성될 수 있는 여지를 열어두는 것이다. 그에 따라 관리가 힘들며, 원치 않는 동작을 유발시킬 수 있다.
- 결국 private으로 생성자를 만든다.
- 플라이웨이트 패턴과 통용되는 의미를 갖는다.

```java
/**
 * 이 클래스의 인스턴스는 #getInstance()를 통해 사용한다.
 * @see #getInstance()
 */
public class Settings {

    private boolean useAutoSteering;

    private boolean useABS;

    private Difficulty difficulty;

    private Settings() {}

    private static final Settings SETTINGS = new Settings();

    public static Settings getInstance() {
        return SETTINGS;
    }

}
```


### 세 번째, 반환 타입의 하위 타입 객체를 반환할 수 있는 능력이 있다.

- 정적 팩터리 메서드를 통해 반환할 객체를 선택할 수 있으므로, 상황에 따라 하위 타입의 객체 또한 반환할 수 있다. → 유연성 증가
- 그에 따라, 구현 클래스를 공개하지 않아도 되며, API를 작게 유지할 수 있게 된다.
- 인터페이스 기반 프레임워크, 인터페이스에 정적 메서드를 위치시킬 수 있게 되므로 매우 유용
- 특정 argument등에 따라 내가 원하는 하위 클래스를 반환할 수 있게된다.

```java
public interface HelloService {
    static HelloService of(String lang) {
        if (lang.equals("ko")) {
            return new HelloService() {
                @Override
                public String hello() {
                    return "하이";
                }
            };
        } else {
            return new ChineseHelloService();
        }
    }
}
```

### 네 번째, 입력 매개변수에 따라 매번 다른 클래스의 객체를 반환할 수 있다.

- 이를 통해, 클라이언트는 팩터리가 건네주는 객체가 어떤 클래스의 인스턴스인지 알 수도 없고, 알 필요도 없다.
- 단지 반환받는 클래스가 하위 클래스이기만 하면 된다.
- EnumSet 클래스는 정적 팩터리 메서드만을 제공한다.
- 반환 타입의 하위 타입이기만 하면 어떤 클래스의 객체를 반환해도 상관없다.


### 다섯 번째, 정적 팩터리 메서드를 작성하는 시점에는 반환할 객체의 클래스가 존재하지 않아도 된다.

- 이를 통해, 서비스 제공자 프레임워크를 만들 수 있게된다. (예시 JDBC)
- 서비스 제공자 프레임 워크란?
    - 구성
        - 서비스 인터페이스 : 구현체의 동작 정의
        - 제공자 등록 API : 제공자가 구현체를 등록할 때 사용
        - 서비스 접근 API : 클라이언트가 서비스의 인스턴스를 얻을 때 사용
        - (서비스 제공자 인터페이스) : 서비스 인터페이스의 인스턴스를 생성하는 팩터리 객체 → 만약 이를 사용하지 않는다면 리플렉션을 사용해야한다.
- 인터페이스만 존재하고 구현체가 없을때를 가정한다.

```java
public interface HelloService {
	String hello();
}
```

- 해당 인터페이스의 구현체의 Hello를 호출해보자

```java
public class HelloServiceFactory {
	public static void main(String[] args) {
		ServiceLoader<HelloService> loader = ServiceLoader.load(HelloService.class);
		Optional<HelloService> helloServiceOptional = loader.findFirst();
		helloServiceOptional.ifPresent(h -> System.out.println(h.hello()));
	}
}
```

- ServiceLoader에 HelloService 제네릭을 지정했기 때문에, HelloService를 구현한 모든 구현체 (라이브러리 포함)를 loader에 올린다.
- 즉, 프로젝트 구성 레벨에서 라이브러리등에 대한 의존성이 사라진다.
- JDBC를 사용해 특정 DB를 상정하지 않은 Application 개발 시, 응용한다면 매우 도움이 된다.


## 단점

---

### 첫 번째, 상속을 하기 위해선 public | protected 생성자가 필요하니 정적 팩터리 메서드만 제공하면 하위 클래스를 만들 수 없다.

- 이 제약은 상속보다 컴포지션을 사용하도록 유도한다.
- 어떻게 보면 컴포지션을 사용하기를 유도하기 때문에 장점일 수도 있다.

### 두 번째, 정적 팩터리 메서드는 프로그래머가 찾기 어렵다.

- 생성자처럼 API가 설명에 명확히 드러나지 않으니, 사용자는 정적 팩터리 메서드 방식 클래스를 인스턴스화할 방법을 알아내야 한다.
- javaDoc을 사용한다고 가정해보면, Constructor 섹션이 아닌, method 섹션에서 찾아야함
- 따라서 남들이 자주 사용하는 네이밍 패턴을 사용해야한다.
- javaDoc을 잘 활용하여 문서를 자세히 써놓으면, 극복할 수 있다.
- 따라서 API 문서를 잘 작성해놓고 메서드 이름 또한 규약을 따라 짓는 방식을 취해 문제를 해결해야 한다.
    - 규약 예시
        - from : 매개변수를 하나 받아서 해당 타입의 인스턴스를 반환하는 형변환 메서드

            ```
            예) Date d = Date.from(instant);
            ```

        - of : 여러 매개변수를 받아 적합한 타입의 인스턴스를 반환하는 집계 메서드

            ```
            예) Set<Rank> faceCards = Enum,Set.of(JACK, QUEEN, KING);
            ```

        - valueOf : from 과 of의 더 자세한 버전

            ```
            예) BingInteger prime = BigInteger.valueOf(Integer.MAX_VALUE);
            ```

        - instance | getInstance : 매개변수로 명시한 인스턴스를 반환하지만, 같은 인스턴스임을 보장하지는 않는다.

            ```
            예) StackWalker luke = StackWalker.getInstance(options);
            ```

        - create | newInstance : instance 혹은 getInstance와 같지만, 매번 새로운 인스턴스를 생성해 반환함을 보장한다.

            ```
            예) Object newArray = Array.newInstance(classObject, arrayLen);
            ```

        - getType : getInstance와 같으나, 생성할 클래스가 아닌 다른 클래스에 팩터리 메서드를 정의할 때 쓴다. "Type"은 팩터리 메서드가 반환할 객체의 타입이다.

            ```
            예) FileStore fs = Files.getFileStore(path);
            ```

        - newType : newInstance와 같으나, 생성할 클래스가 아닌, 다른 클래스에 펙터리 메서드를 정의할 때 쓴다. "Type"은 팩터리 메서드가 반환할 객체의 타입이다.

            ```
            예) BufferedReader br = Files.newBufferedReader(path);
            ```

        - type : getType과 newType의 간결한 버전

            ```
            예) List<Complaint> litany = Coolections.list(legacyLitany);
            ```

### 강의 중 깨알 Tip

- 메서드 시그니처를 규정할 때, 파라미터의 형식만 보므로 유의한다.
- Full Name은 보통 package 경로까지 포함한 이름을 의미한다.


## 정리

---

- 정적 팩터리 메서드와 public 생성자는 각자의 쓰임새가 존재하므로 상대적인 장단점을 이해해야한다.
- 정적 팩터리 메서드를 사용하는게 유리한 경우가 빈번하므로 public 생성자를 제공하던 습관이 있다면 고치자.
- 무조건 정적 팩토리 메서드를 사용하는것이 아니라, 고려를 하는것임을 명심


## 완벽 정리 part

---

### 열거 타입

*정의*

- 상수 목록을 담을 수 있는 데이터 타입

*특징*

- 특정한 변수가 가질 수 있는 값을 제한할 수 있다.
- Type Safety를 보장할 수 있다.

*사용처*

- 싱글톤 패턴을 구현할 때 사용하기도 한다.

*질문*

1. 특정 enum 타입이 가질 수 있는 모든 값을 순회하며 출력하라.

    ```java
    public enum OrderStatus {
        PREPARING, SHIPPED, DELIVERING, DELIVERED;
    }
    
    ...
    
    OrderStatus.values().forEach(System.out::println);
    ```

2. enum은 자바의 클래스처럼 생성자, 메서드, 필드를 가질 수 있는가?

    ```java
    public enum OrderStatus {
    
        PREPARING(0), SHIPPED(1), DELIVERING(2), DELIVERED(3);
        
    		private int number;
    
        OrderStatus(int number) {
            this.number = number;
        }
    }
    ```

3. enum의 값은 == 연산자로 동일성을 비교할 수 있는가?
    - enum은 JVM에서 하나의 인스턴스만 존재함을 보장하기 때문에, 성능상의 이점을 고려하여 ==을 사용하자
4. enum을 key로 사용하는 Map을 정의하고, enum을 담고 있는 Set을 생성해보자
    - EnumMap의 경우, 구현체가 이미 존재
    - EnumSet의 경우, noneOf등, 정적 팩터리 메서드를 통해서 생성할 수 있움

    ```java
    EnumMap<OrderStatus, Integer> enumMap = new EnumMap<>();
    EnumSet enumSet = EnumSet.allOf(OrderStatus.class);
    ```


참고 링크

[https://johnmarc.tistory.com/152](https://johnmarc.tistory.com/152)

### 플라이웨이트 패턴

*정의*
- 객체를 가볍게 만들어 메모리 사용을 줄이는 패턴
- 자주 변하는 속성(또는 외적인 속성, extrinsit)과 변하지 않는 속성(또는 내적인 속성, intrinsit)을 부리하고 재사용하여 메모리 사용을 줄일 수 있다.

### 인터페이스, 정적 메서드

*정의*

- 자바8, 9에서 추가된 기능으로, 인터페이스에 기본 메서드와 정적 메서드를 가질 수 있다.

*기본 메서드*

- 인터페이스에서 메서드 선언 뿐 아니라, 기본적인 구현체까지 제공가능
- 기존의 인터페이스를 구현하는 클래스에 새로운 기능을 추가할 수 있다.

*정적 메서드*

- 자바 9부터 private static 메서드도 가질 수 있다.
- 단 private 필드는 아직도 선언할 수 없다.

*질문*

1. 내림차순으로 정렬하는 Comparator를 만들고, List<Integer>를 정렬하라.

    ```java
    public class ComEx implements Comparator<Integer> {
        @Override
        public int compare(Integer i1, Integer i2) {
            return i2 - i1;
        }
    
        @Override
        public Comparator reversed() {
            return Comparator.super.reversed();
        }
    }
    
    public static void main() {
        
        List<Integer> numbers = new ArrayList();
        numbers.add(100);
        numbers.add(20);
        numbers.add(44);
        numbers.add(3);
        
        Comparator<Integer> desc = (o1, o2) -> {o2 - o1};
        numbers.sort(desc);
    }
    ```

1. 질문1에서 만든 Comparator를 사용해서 오름차순으로 정렬하라

    ```java
    	# 뒤집기
    	numbers.sort(desc.reversed());
    ```


### 서비스 제공자 프레임워크

*주요 구성 요소*

- 서비스 제공자 인터페이스 (SPI)와 서비스 제공자(서비스 구현체)
    - 그냥 서비스의 인터페이스구나 라는 정도로 생각하자.
- 서비스 제공자 등록 API (서비스 인터페이스의 구현체를 등록하는 방법)
    - 서비스 구현체를 Bean으로 등록하는 일련의 과정을 의미
- 서비스 접근 API(서비스의 클라이언트가 서비스 인터페이스의 인스턴스를 가져올 때 사용하는 API)
    - Application context 등을 통해 서비스 구현체 Bean을 참조하는 것을 의미

*다양한 변형*

- 브릿지 패턴
- 의존 객체 주입 프레임워크
- java.utilServiceLoader

### 리플렉션

*정의*

- 클래스로더를 통해 읽어온 클래스 정보를 사용하는 기술

*특징*

- 클래스를 읽어 올 수 있고, 인스턴스 생성, 메서드 실행, 필드의 값 참조 또는 변경이 가능하다.

*활용 사례*

- 특정 어노테이션이 붙어있는 필드 또는 메서드 읽어오기
- 특정 이름 패턴에 해당하는 메서드 목록 가져와 호출하기

