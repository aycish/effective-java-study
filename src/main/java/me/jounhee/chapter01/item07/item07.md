# Item 07. 다 쓴 객체 참조를 해제하라

## 다 쓴 객체 참조를 해제하라

---

- 어떤 객체에 대한 레퍼런스가 남아있다면 해당 객체는 가비지 컬렉션의 대상이 되지 않는다.
- 자기 메모리를 직접 관리하는 클래스라면 메모리 누수에 주의해야 한다.
    - 예) 스택, 캐시, 리스너 또는 콜백
- 참조 객체를 null 처리하는 일은 예외적인 경우이며 가장 좋은 방법은 유효 범위 밖으로 밀어내는 것이다.

### 예제 (Stack) - before

```java
public class Stack {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack() {
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(Object e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public Object pop() {
        if (size == 0)
            throw new EmptyStackException();
        return elements[--size];
    }
}
```

- pop할 때, null로 반환하지 않기 때문에, 메모리에 계속 쌓이게 되며, 메모리 누수가 생긴다.
- 직접 구현한 Cache도 동일한 문제를 만날 수 있다.

### 예제 (Stack)- after

```java
public Object pop() {
    if (size == 0)
        throw new EmptyStackException();
    Object result = elements[--size];
    elements[size] = null; // 다 쓴 참조 해제
    return result;
}
```

- null 처리를 통해 기존에 할당했었던 객체가 GC의 대상이 되도록 한다.
- 마찬가지로, 다른 메모리를 직접 관리하게 되는 객체들 (Collection들과 같이 객체를 쌓아두는 공간)에 대해서는 이런 null 처리를 고려해야한다.

### 해결 방안

- 객체를 쌓아놓는 공간에서 하나를 빼낼 때, null 처리
- Cache는 하기와 같은 방법들 중 선택할 수 있다.
    - null 처리
        - 하지만, 메모리를 직접 관리하는 클래스 (ex . stack)에만 해당한다.
    - weakReference를 사용하는 Collection을 사용하여 관리한다.
    - 직접 관리하는 로직을 구현하는 방법
        - background thread로 구현

## NullPointerException

---

### NullPointerException을 만나는 이유

- 메서드에서 null을 리턴하고, null 체크를 하지 않았기 때문
- 보통 해당 에러가 발생한 경우, if문을 넣어서 해결한다.

### 메서드에서 적절한 값을 리턴할 수 없는 경우에 선택할 수 있는 대안

- 예외를 던진다.
- null을 리턴한다.
- Optional을 리턴한다.
    - if문을 Optional에서 제공하는 ifPresent등으로 대체할 수 있다.

### Optional 사용 시 주의 점

- 리턴 값으로 쓰기를 권장
    - 메서드 매개변수 타입, 맵의 키 타입, 인스턴스 필드 타입으로 사용하지 말자
- Optional을 리턴하는 메서드에서 null을 리턴하지 말자.
- 프리미티브 타입용 Optional은 따로 있다.
    - OptionalInt, OptionalLong
- Collection, Map, Stream Array, Optional은 Optional로 감싸지 말것
    - Collection들은 이미 Size check등 비어있는지 확인할 수 있기 때문
- Optional 반환은 신중히 하라

## WeakHashMap

---

- Key가 더이상 강하게 레퍼런스되는 곳이 없다면, 해당 엔트리를 제거한다.
- 맵의 엔트리를 맵의 Value가 아니라 Key에 의존해야하는 경우에 사용할 수 있다.
- 캐시를 구현하는데 사용할 수 있지만, 캐시를 직접 구현하는 것은 권장하지 않는다.
- WeakHashMap을 사용할 때, Key를 primitive 변수를 사용하고 싶다면, wrapper 객체를 사용해야한다.
- [https://mangkyu.tistory.com/119](https://mangkyu.tistory.com/119)

### 레퍼런스 종류

- Stong reference
    - 일반적으로 사용하는 꼴
    - 객체를 할당한 경우
- Soft
    - 자바에서 제공하는 SoftReference로 만든 참조
    - Strong하게 reference하는 곳이 없고, Soft하게 Reference만 한다면, GC의 대상이된다.
    - 메모리가 부족한 상황이 아니라면 GC하지 않음
- Weak
    - 자바에서 제공하는 WeakReference로 만든 참조
    - Strong하게 reference하는 곳이 없다면, GC 발생 시, 무조건 없어진다.
- Phantom
    - Phantom reference가 Strong reference가 없어진 이후에 남는다.
    - Queue를 사용한다. (ReferenceQueue)
    - Strong reference가 없고, phantom reference가 남은 상황에서 GC가 일어나면, ReferenceQueue에 넣어준다.
    - 자원 정리시 사용한다. → 강의를 다시 들어보기 ..
        - 근데 사용하지 말자.
    - 메모리 할당 시점을 알고 있고, 해제하는 시점 또한 알고 있는 상황에서 꼼꼼하게 개발자가 자원 관리를 해야할 때, 또는 자원 해제 시점을 알아야할 때 사용할 수 있다..

### 약한 참조

- 객체의 할당 해제를 GC에 의존적이기 때문에, 불확실하다.
- WeakReference<Integer> soft = new WeakReference<Integer>(prime); 와 같이 WeakReference Class를 이용하여 생성이 가능하다.
- Strong reference가 없어지는 경우, GC 수행 시 사라진다.
- SoftReference와 차이점은 메모리가 부족하지 않더라도 GC 대상이 된다. 다음 GC가 발생하는 시점에 무조건 없어진다.

## 백그라운드 쓰레드

---

- Thread, Runnable, ExecutorService

### ScheduledThreadPoolExecutor

- 쓰레드풀의 개수를 정할 때 주의할 것
    - CPU에 의존적인지, I/O에 의존적인지 잘 판단할 것
    - CPU 사용량이 많은 작업이 요구된다면, CPU 갯수만큼만 할당해야한다.
    - I/O 관련된 작업이 요구되는 경우에는, CPU 갯수보다는 많은 정도의 Thread 갯수를 할당하는 것이 좋다. 언제 다시 작업이 실행될지 모르거나 언제 블록될지 모르기 때문
- 쓰레드풀의 종류
    - Single
        - 단 하나의 쓰레드만 존재하는 쓰레드 풀
        - 보통 예외처리를 위한 Thread를 생성하기위해 사용
    - Fixed
        - 지정한 갯수만큼 스레드를 생성해놓고 작업 대기
    - Cached
        - 갯수를 지정하지 않는다.
        - 매 작업 요청마다 쓰레드를 생성해두고, 작업이 종료된 쓰레드는 바로 사라지지 않고 약 1분동안 유지되다가 사라진다.
    - Scheduled
        - Timer class 사용하지 않고, 주기적으로 무엇인가를 수행해야할 때 사용하는 Thread Pool
- Runnable, Callable, Future
    - Runnable
        - 함수형 인터페이스
        - Thread에 어떤 작업을 할당할지 지정하는 용도로 사용
    - Callable
        - 한 작업의 결과를 받고 싶을 때 사용
    - Future
        - 비동기적 연산, 작업을 수행한 후, 도출된 결과를 나타냄
        - 타 Thread에서 return한 값을 메인 Thread에서 받고 싶을 때 사용
- CompletableFuture, ForkJoinPool
    - CompletableFuture
        - Future의 한계점을 극복하기 위해 추가됨
            - 외부에서 작업을 완료시킬 수 없고, get의 타임아웃 설정만으로만 완료 가능
            - 블로킹 코드(get)을 통해서만 이후의 결과를 처리할 수 있음
            - 여러 Future를 조합할 수 없음
            - 여러 작업을 조합하거나 예외 처리할 수 없음
        - Future를 기반으로 외부에서 완료시킬 수 있기 때문에 “Completable”이 붙음
            - 몇 초 이내에 응답이 오지 않으면 기본 값을 반환한다 등을 설정핤 ㅜ 있음
        - 또한, 콜백 등록 및 Future 조합등이 가능해짐
    - ForkJoinPool
        - 자바 Concurrency를 위한 툴
        - 동일한 작업을 여러개의 SubTask로 분리(Fork)하여 각각 처리하고, 이를 최종적으로 합쳐서(Join) 결과를 만들어내는 방식
        - 하나의 작업 큐를 가지고 있으며, 작업 큐에 있는 작업들을 Fork Join Pool에서 관리하는 여러 스레드에서 접근하여 처리한다.
        - 각 스레드들은 본인 스스로의 작업 처리를 위한 내부 큐를 가지고 있으며, 서로 각자의 큐에 접근하여 WorkLoad를 배분한다.