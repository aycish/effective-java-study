# Item 06. 불필요한 객체 생성을 피하라

## 불필요한 객체 생성을 피하라

---

“객체 생성은 비싸니 피하라.”는 뜻으로 오해하면 안된다.

### 문자열

- 사실상 동일한 객체라서 매번 새로 만들 필요가 없다.
    - “”로 선언하여 대입한 경우, 문자열 풀에서 해당 String을 참조해서 사용할 수 있다.
    - 만약 new를 선언하여 생성한 경우, 억지로 새로운 객체를 만드는 꼴이 되므로 지양해야한다.

**예시**

```java
public static void main(String[] args) {
    String hello = "hello";

    String hello2 = new String("hello");

    String hello3 = "hello";

    System.out.println(hello == hello2); // false
    System.out.println(hello.equals(hello2)); // true
    System.out.println(hello == hello3); // true
    System.out.println(hello.equals(hello3)); // true
}
```

### 정규식, Pattern

- 생성 비용이 비싼 개체이기 때문에, 반복해서 생성하는 것 보다는 캐싱하여 재사용하는 것이 좋다.

**예시**

```java
static boolean isRomanNumeralSlow(Strings) {
	return s.matches(""); // 유한 상태 머신 알고리즘을 사용
}

private static final Pattern ROMAN = pattern.compile("");

static boolean isRomanNumeralFast(String s) { return ROMAN.matcher(s).matches(); }

public static void main(Stringp[ args) {
	boolean result - false;
	long start = System.nanoTime();
	for (int j = 0; j < 1; j++) {
		result = isRomanNumeralFast("TESTSTRING");
		//result = isRomanNumeralSlow("TESTSTRING");
	}
}
```

- 새로 생성하는 것과 캐싱하는 경우, 성능이 유의미하게 차이난다.

### 오토 박싱 (auto boxing)

- 기본 타입(int)을 그에 상응하는 박싱된 객체 타입(Integer)로 변환해주는 기술
- 기본 타입과 박싱된 기본 타입을 섞어서 사용하면, 변환하는 과정에서 불필요한 객체가 생성될 수 있다.

**예시**

```java
private static long sum() {
    Long sum = 0L;
    for (long i = 0; i <= Integer.MAX_VALUE; i++)
        sum += i;
    return sum;
}

public static void main(String[] args) {
    long start = System.nanoTime();
    long x = sum();
    long end = System.nanoTime();
    System.out.println((end - start) / 1_000_000. + " ms.");
    System.out.println(x);
}
```

- 원시형 타입을 사용하는 경우와 오토 박싱을 사용하는 경우, 유의미한 성능 차이가난다.
- 또한, 불필요한 객체를 지속적으로 생성하기 때문에, 지양해야한다.

### 정리

- 너무 집착하게 된다면, static을 엄청 쓰게 되므로 어떤 경우가 본 상황에 맞는지 잘 판단해보자

## Deprecation

---

클라이언트가 사용하지 않길 바라는 코드가 중간에 있다면, 표시해줘야한다. 그 경우, 사용 자제를 권장하고 대안을 제시하는 방법이 있다.

### @Deprecated

- 컴파일시 경고 메세지를 통해 사용 자제를 권장하는 API라는 것을 클라이언트에 알려줄 수 있다.

### @deprecated

- javaDoc에 사용하여 왜 해당 API사용을 지양해야하는지, 그 대신 권장하는 API가 어떤 것인지 표기할 수 있다.

### 예시

```java
public class Deprecation {

/**
 * @deprecated in favor of
 * {@link #Deprecation(String)}
 */
@Deprecated(forRemoval = true, since = "1.2")
public Deprecation() {
}

private String name;

public Deprecation(String name) {
    this.name = name;
}
```

- forRemoval : JAVA9부터 사용 가능한 것으로, 앞으로 삭제될 여지가 있음을 나타내는 Property
- since : JAVA9부터 사용 가능, 지정한 버전 부터 deprecated 될것이라고 표기하는 용도

## 정규 표현식

---

내부적으로 Pattern이 쓰이는 곳에서 사용할 수 있는데, 여러 곳에서 사용될 수 있다.

- String.matches
- String.split
- String.replaceAll
- String.replaceFirst

### 예시

```java
public class RegularExpression {

    private static final Pattern SPLIT_PATTERN = Pattern.compile(",");

    public static void main(String[] args) {
        long start = System.nanoTime();
        for (int j = 0; j < 10000; j++) {
            String name = "keesun,whiteship";
            name.split(",");
//          SPLIT_PATTERN.split(name);
        }
        System.out.println(System.nanoTime() - start);
    }
}
```

- 한 글자의 String으로 Split 하는 경우는 내부적인 코딩 지원으로 굳이 캐싱하지 않아도 빠르게 사용할 수 있다.

### 과제

- 자바 정규표현식 Pattern 문법 학습하기

## 가비지 컬렉션

---

가비지 컬렉션의 기본 개념을 공부할때 참고할 키워드들에 대해서 알아보자.

### Mark, Sweep, Compact algorithm

- Mark : 해당 Object가 GC의 대상이 되는 것인지 체크하는 용도
- Sweep : 필요 없는 Object를 실제 Heap에서 날리는 것
- Compact : 필요없는 객체들을 지우고, 살아 있는 객체들을 한 곳으로 모은다. 파편화를 방지

### Young Generation(Eden, S0, S1), Old Generation

- Young Generation
    - 최초에는 Eden에 객체가 놓여진다.
    - Eden 공간이 부족해지는 상황이 오면, S1으로 옮긴다.
    - 그런식으로 Eden의 공간을 S1으로 객체들을 보내며 확보한다.
    - S1도 마찬가지로 가득차는 경우가 오면, S0로 보낸다.
- Old Generation
    - 늙은 개체들만 모이는곳 ㅋㅋ

### Minor GC, Full GC

- Minor GC : Young Generation만
- Full GC : Old Generation까지 전부

### Throughput, Latency (Stop - The - World), Footprint

GC를 공부할 때, 3가지 관점을 가지고 GC 로직을 살펴봐야한다. 그것이 하기의 것들

- Throughput : 처리량. 결국 Resource와 관련있으며, resoucre와 동일하게 생각하면 될것 같다.
- Latency : 지연성. 어떻게하면 StopTheWorld를 안할 수 있을까
- Footprint : 프로세스에 의해 할당된 메모리의 양을 의미. Footprint를 최적화한다는 것은 적은 양을 더 적은 횟수로 메모리를 사용하는 것을 의미한다.

### Serial, Parallel, CMS, g1, ZGC, Shenandoah

- Serial
- Parallel : java 8
- CMS
- g1 : java 11
- ZGC :
- Shenandoah :