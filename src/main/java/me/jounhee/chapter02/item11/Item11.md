# Item 11. equals를 재정의 하려거든 hashCode도 재정의하라

## equals를 재정의하려거든 hashCode도 재정의하라

---

### 만약 정의하지 않는다면 .. ?

- hashCode를 정의하지 않는다면, HashCode 관련 규약을 어기게 되어, HashMap등의 컬렉션에 원소로 사용할 때 문제가 발생할 수 있다.
    - ex ) equals의 결과가 같은 서로 다른 두 객체를 HashMap에 넣고나서 get()으로 가져올 때, get()은 null을 반환한다.

### hashCode 규약

- equals 비교에 사용하는 정보가 변경되지 않았다면, hashCode는 매번 같은 값을 리턴해야한다.
    - 변경되거나, 어플리케이션을 다시 실행했다면 달라질 수 있다.
- 두 객체에 대한 equals가 같다면, hashCode의 값도 같아야 한다.
    - 보통 equals를 구현했을 때, 위반하는 규약은 이 규약이다.
- 두 객체에 대한 equals가 다르더라도, hashCode의 값은 같을 수 있지만 해시 테이블 성능을 고려해 다른 값을 리턴하는 것이 좋다.
    - 같은 해쉬값을 반환하기 때문에, 한 서랍에 담기게된다.
    - 그에 따라 hash 테이블의 속도가 O(1) → O(n)으로 변경되며, 연결 리스트처럼 사용된다.

### hashCode 구현 방법

```java
@Override
public int hashCode() {
	int result = Short.hashCode(areaCode); // 기본 타입
	result = 31 * result + Short.hashCode(prefix);
	result = 31 * result + Short.hashCode(lineNUm);
	return result;
}
```

1. 핵심 필드 하나의 값의 해쉬값을 계산해서 result 값을 초기화 한다.
    1. 핵심 필드란, equals에서 동등 비교할 때 사용하는 필드를 의미한다.
2. 반환 Type을 지정한다.
    1. 기본 타입은 Type.hashCode를 사용한다.
    2. 참조 타입은 해당 필드의 hashCode를 사용한다.
        1. 만약, equals 메서드가 해당 필드의 메서드인 equals를 재귀적으로 사용한다면, 해당 필드의 hashCode를 재귀적으로 호출하도록 구현한다.
        2. 계산이 더 복잡해질것 같다면, 이 필드의 표준형을 만들어 그 표준형이 hashCode를 호출한다.
        3. 필드의 값이  null이라면 0을 사용한다.
    3. 배열은 모든 원소를 재귀적으로 위의 로직을 적용하거나 Arrays.hashCode
        1. 핵심 원소 각각을 별도의 필드처람 다루도록 하자.
        2. 이후, 재귀적으로 적용하여 각 핵심 원소의 해시 코드를 계산한 다음, 2.d의 방식으로 갱신한다.
        3. 만약 배열에 원소가 없다면 단순히 상수를 사용한다.
        4. 모든 원소가 핵심 원소라면 Arrays.hashCode를 사용한다.
    4. result = 31 * result + 해당 필드의 hashCode 계산값
        1. 31은 통계적인 값으로, 특정 영어사전을 대상으로 했을 때, 해쉬 충돌이 가장 적었던 수이다.
        2. 특수한 동작이 필요하지 않다면, 굳이 짝수나 다른 홀수를 사용하지 말자.
3. result를 반환한다.

### hashCode 구현 대안

- 구글 구아바의 com.google.common.hash.Hashing
- Objects 클래스의 hash메서드
    - 넘겨준 매개변수들을 배열로 만들기도 하고, 언박싱 과정을 거치기도 하기 때문에 성능에 민감하다면 사용하면 안된다.
- 캐싱을 사용해 불변 클래스의 해시 코드 계산 비용을 줄일 수 있다.

### 주의 사항

- 지연 초기화 기법을 사용할 때 스레드 안전성을 신경써야 한다.
    - 성능을 위해 지연 초기화법을 도입하고자 한다면, 스레드 안정성을 신경써야한다.
- 성능 때문에 핵심 필드를 해시코드 계산할 때 빼면 안된다.
- 해시코드 계산 규칙을 API에 노출하지 않는 것이 좋다.

## 연결 리스트

---

내부 구현은 언제든지 바뀔 수도 있으므로 주의

- 자바 8에서 해시 충돌시 성능 개선을 위해 내부적으로 동일한 버켓에 일정 개수 이상의 엔트리가 추가되면, 연결 리스트 대신 이진 트리를 사용하도록 바뀌었다.
    - https://dzone.com/articles/hashmap-performance
- 연결 리스트에서 어떤 값을 찾는데 걸리는 시간은?
- 이진 트리에서 어떤 값을 찾는데 걸리는 시간은?

## 클래스를 스레드 안전하게 만들도록 신경 써야 한다.

---

멀티 스레드 환경에서 안전한 코드를 Thread-safety하다 라고 한다.

- 가장 안전한 방법은 여러 스레드 간에 공유하는 데이터가 없는 것!
- 공유하는 데이터가 있다면
    - Synchronization
    - ThreadLocal
    - 불변 객체 사용
    - Synchronized 데이터 사용
    - Concurrent 데이터 사용
- 아니면, 공유한 데이터를 해시 테이블을 사용하여 초기부터 Thread Safe하게 데이터를 접근하는 방법이 있다.

### 예시

```java
private volatile int hashCode; // 자동으로 0으로 초기화된다.

@Override
public int hashCode() {
    int result = hashCode;
    if (result == 0) {
        result = Short.hashCode(areaCode);
        result = 31 * result + Short.hashCode(prefix);
        result = 31 * result + Short.hashCode(lineNum);
        this.hashCode = result;
    }
    return result;
}
```

- result가 0인 경우, hashCode를 Update하는 로직에서 Thread-safe하지 않다.
- 이를 방지하기 위해서는 상기에 언급했듯, 키워드들을 사용해주자.

### 예시 - after

```java
private volatile int hashCode; // 자동으로 0으로 초기화된다.

@Override
public int hashCode() {
    if (this.hashCode != 0) {
        return hashCode;
    }

    synchronized (this) {   //Object lock을 설정
        int result = hashCode;
        if (result == 0) {
            result = Short.hashCode(areaCode);
            result = 31 * result + Short.hashCode(prefix);
            result = 31 * result + Short.hashCode(lineNum);
            this.hashCode = result;
        }
        return result;
    }
}
```

- hashCode가 0인지 판단하면서 1차적으로 Blocking
- 그래도 만약 초기 설정 단계에서 여러 스레드가 통과한 경우, Object Lock을 통해서 Race Condition.을 방지한다.
- 또한, volatile를 공유 데이터에 선언해두어 Main Memory에 직접적으로 Access하게 한다.
    - 가장 최근에 Update한 값에 대해서 접근하기 때문에 경쟁이 의심된다면 이를 사용한다.
    - 정합성을 보장한다.