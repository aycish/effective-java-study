# Item 13. clone 재정의는 주의해서 진행하라

## clone() 재정의는 주의해서 진행하라.

---

### 책의 요약

- 배열을 제외한 모든 새로 생성할 클래스, 인터페이스등은 clone()을 사용하지 말자.
- 대신 복사한 객체를 제공할 수 있는 팩토리를 사용하자.

### Cloneable 인터페이스

- 메서드가 하나도 존재하지 않는 인터페이스
- 태초의 목적은 복제해도 되는 클래스임을 명시하는 용도였지만, clone() 메서드의 선언부가 Object 클래스이기 때문에 의도한 목적을 이루지 못함
- 다만, Object의 clone() 메서드의 동작 방식을 결정한다.
  - Cloneable 인터페이스를 구현하지 않은 객체에서 clone()메서드를 호출할 경우, CloneNotSupportedException이 발생한다.
  - Colneable 인터페이스를 구현한 경우, 해당 객체의 필드들을 하나하나 복사한 객체를 반환한다.
- 실무에서 Cloneable을 구현한 클래스는 clone() 메서드를 public으로 제공하며, 사용자는 당연히 복제가 제대로 이뤄지리라 기대하므로 정확하게 구현해야한다.
  - 그렇게 하기 위해서는 clone 규약을 지키도록 노력해야한다.
- Cloneable 인터페이스는 가변 객체를 참조하는 필드는 final로 선언하라라는 일반 용법과 충돌한다.
  - final 키워드를 사용하면 배열의 clone을 사용하여 깊은 복사를 수행하더라도, clone된 객체에 set하지 못하기 때문

### clone() 메서드

- clone 메서드는 사실상 생성자와 같은 효과를 낸다.
- 즉, clone은 원본 객체에 아무런 해를 끼치지 않는 동시에, 복제된 객체의 불변식을 보장해야한다.

## Clone 규약

---

- x.clone() ≠ x는 반드시 참이어야 한다.
    - clone() 메서드는 객체의 내용을 복사해서 새로운 객체를 생성함을 의미한다.
- x.clone().getClass() == x.getClass()는 반드시 참이어야 한다.
- x.clone().equals(x)는 true가 아닐 수 있다.

### 불변 객체인 경우

- Clonable 인터페이스로 구현
- 이후, clone 메서드를 재정의한다. 이때, super.clone()을 사용한다.

### 예제 코드

```java
### Client

...

public static void main(String[] args) {
    PhoneNumber pn = new PhoneNumber(707, 867, 5309);
    Map<PhoneNumber, String> m = new HashMap<>();
    m.put(pn, "제니");
    PhoneNumber clone = pn.clone();
    System.out.println(m.get(clone));

    System.out.println(clone != pn);                       // 반드시 true
    System.out.println(clone.getClass() == pn.getClass()); // 반드시 true
    System.out.println(clone.equals(pn));                  // true가 아닐 수도 있다.
}

### PhoneNumber의 clone() 메서드
@Override
public PhoneNumber clone() {
    try {
        return (PhoneNumber) super.clone();
    } catch (CloneNotSupportedException e) {
        throw new AssertionError();  // 일어날 수 없는 일이다.
    }
}
```

- 결국 PhoneNumber의 clone() 메서드는 super의 clone()을 사용하고 있으므로, 최종적으로는 Object의 clone()메서드를 호출하게 된다.
- Object의 clone()메서드에서 해당 객체의 멤버를 복사하여 새로운 객체를 생성하여 반환한다.
    - 내부적으로 Object에서는 @IntrinsicCandidate 어노테이션을 통해 해당 함수를 JVM 내장 함수로 지원하고 있다.

### 꼭 super.clone()을 호출해야하는 이유

```java
public class Item implements Cloneable {

    private String name;

    /**
     * 이렇게 구현하면 하위 클래스의 clone()이 깨질 수 있다. p78
     * @return
     */
    @Override
    public Item clone() {
        Item item = new Item();
        item.name = this.name;
        return item;
    }
}

public class SubItem extends Item implements Cloneable {

    private String name;

    @Override
    public SubItem clone() {
        return (SubItem)super.clone();
    }

    public static void main(String[] args) {
        SubItem item = new SubItem();
        SubItem clone = item.clone();

        System.out.println(clone != item);
        System.out.println(clone.getClass() == item.getClass());
        System.out.println(clone.equals(item));
    }
}
```

- 상기처럼 Item에서 clone을 임의로 구현한 경우 (super.clone() 없이), 규약이 깨질 수 있다.
    - clone.getClass() == item.getClass()에서 규약이 깨진다.
    - item.clone()의 경우, SubItem으로 타입 캐스팅을 할 수 없기 때문. 상위 타입은 하위 타입으로 타입 캐스팅할 수 없기 때문이다.
- 다른 방면으로 SubItem에서 clone()을 오버라이드 하지 않은 경우
    - 결국 내부적으로 Object의 clone 호출 → Item의 clone 호출되며 CastingException이 발생하게된다.
- 결국, Item에서 clone 구현 시, Item을 반환하면 안된다. (비 결정적)

### 규약을 지키며 구현해보기

```java
@Override
public Item clone() {
    Item result = null;
    try {
        result = (Item) super.clone();
        return result;
    } catch (CloneNotSupportedException e) {
        throw new AssertionError();
    }
}
```

- super.clone()을 사용해야 한다.
- Cloneable 인터페이스를 구현해야한다.

## 가변 객체의 clone 구현하는 방법

---

### 구현 방법

- 접근 제한자는 public, 반환 타입은 자신의 클래스로 변경한다.
- super.clone을 호출한 뒤, 필요한 필드를 적절히 수정한다.
    - 배열 복제 시, 배열의 clone 메서드 사용
    - 경우에 따라 final을 사용할 수 없을수도 있다.
    - 필요한 경우 deep copy를 해야한다.
    - super.clone으로 객체를 만든 뒤, 고수준 메서드를 호출하는 방법도 있다.
    - 오버라이딩 할 수 있는 메서드는 참조하지 않도록 조심해야 한다.
    - 상속용 클래스는 Cloneable을 구현하지 않는것이 좋다.
    - Cloneable을 구현한 스레드 안전 클래스를 작성할 때는 동기화를 해야한다.

### 예제 코드 - clone 을 정상적으로 구현하기 이전

```java
public class Stack implements Cloneable {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack() {
        this.elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }
    public void push(Object e) {
        ensureCapacity();
        elements[size++] = e;
    }
    
    public Object pop() {
        if (size == 0)
            throw new EmptyStackException();
        Object result = elements[--size];
        elements[size] = null; // 다 쓴 참조 해제
        return result;
    }

    public boolean isEmpty() {
        return size ==0;
    }

    // 코드 13-2 가변 상태를 참조하는 클래스용 clone 메서드
    @Override public Stack clone() {
        try {
            Stack result = (Stack) super.clone();
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    // 원소를 위한 공간을 적어도 하나 이상 확보한다.
    private void ensureCapacity() {
        if (elements.length == size)
            elements = Arrays.copyOf(elements, 2 * size + 1);
    }
}
```

- Stack에 들어가는 Object형 배열과 size를 나타내는 필드 존재
- 만약, 배열의 clone()을 사용하지 않고, copy를 사용하게 된다면?
    - copy된 stack과 origin stack은 동일한 인스턴스 배열 (Object형 배열)을 바라보게 된다.

### 확인해보자!

```java
// clone이 동작하는 모습을 보려면 명령줄 인수를 몇 개 덧붙여서 호출해야 한다.
public static void main(String[] args) {
    Object[] values = new Object[2];
    values[0] = new PhoneNumber(123, 456, 7890);
    values[1] = new PhoneNumber(321, 764, 2341);

    Stack stack = new Stack();
    for (Object arg : values)
        stack.push(arg);

    Stack copy = stack.clone();

    System.out.println("pop from stack");
    while (!stack.isEmpty())
        System.out.println(stack.pop() + " ");

    System.out.println("pop from copy");
    while (!copy.isEmpty())
        System.out.println(copy.pop() + " ");

    System.out.println(stack.elements[0] == copy.elements[0]);
}
```

- copy에서는 pop할 수 없다.
    - 두 Stack에서 바라보고 있는 배열이 동일하기 때문
- 따라서, 해당 배열을 복사해서 사용해줘야 한다.

```java
@Override public Stack clone() {
    try {
        Stack result = (Stack) super.clone();
        result.elements = elements.clone();
        return result;
    } catch (CloneNotSupportedException e) {
        throw new AssertionError();
    }
}
```

- 하지만, 배열은 다르지만 배열 내부에서 참조하고 있는 element들은 동일한 객체이다.
    - 얇은 카피이기 때문
- 결국, origin에서 특정 객체에 대해서 필드를 변경하게 되면, clone한 stack 내부의 객체 또한 변하게 되어 영향을 주게된다.
- 결국 deep copy가 필요하다.

### clone에서의 Deep copy 예제

```java
public class HashTable implements Cloneable {

    private Entry[] buckets = new Entry[10];

    private static class Entry {
        final Object key;
        Object value;
        Entry next;

        Entry(Object key, Object value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public void add(Object key, Object value) {
            this.next = new Entry(key, value, null);
        }

        public Entry deepCopy() {
            Entry result = new Entry(key, value, next);
            for (Entry p = result ; p.next != null ; p = p.next) {
                p.next = new Entry(p.next.key, p.next.value, p.next.next);
            }
            return result;
        }
    }

    @Override
    public HashTable clone() {
        HashTable result = null;
        try {
            result = (HashTable)super.clone();
            result.buckets = new Entry[this.buckets.length];

            for (int i = 0 ; i < this.buckets.length; i++) {
                if (buckets[i] != null) {
                    result.buckets[i] = this.buckets[i].deepCopy();
                }
            }
            return result;
        } catch (CloneNotSupportedException e) {
            throw  new AssertionError();
        }
    }
}
```

- 재귀적으로 구현하게 되면 Stack Overflow가 발생할 수 있으므로, 최대한 재귀를 지양한다.

### 주의점

- 내부 필드 ( 상기 예제에서는 Object 배열 또는 Entry)를 새로 생성하거나 복사할 수 있는 메서드를 만들면 안된다.
    - 상속한 클래스 또는 Entry 자체가 호출하게 되는 경우, 내부 동작이 변할 수 있다.
- 상속용 클래스에는 Cloneable 인터페이스 사용을 지양하자.
    - 상속용 클래스를 상속받기 위해서, clone()을 올바르게 구현하기 위해서 개발자들에게 엄청난 부담이된다.
    - 상속용 클래스에서 clone을 구현하는 방법 또는 final을 붙여 재정의를 못하게해서 부담을 줄여줄 순 있다.
- 쓰레드 동기화를 생각해야한다면, synchronized 키워드를 사용해주자.

## 그럼 clone의 대안을 알아보자

---

### Copy 생성자 / 팩토리

```java
public final class PhoneNumber implements Cloneable {
    private final short areaCode, prefix, lineNum;
	
		// 생성자를 통해 clone() 대체
		public PhoneNumber(PhoneNumber phoneNumber) {
        this(phoneNumber.areaCode, phoneNumber.prefix, phoneNumber.lineNum);
    }
}
```

- 생성자 내부에서 copy를 해주는 방식
- clone을 사용하지 않기 때문에, 부담이 많이 준다.
    - equlas 등등에 관한 규약에서 자유로워 진다.
    - 또한, 각각의 필드들에 대한 검증으로부터 자유로워 진다.
    - clone을 사용했을 때의 제약점이 사라진다. (final 키워드 사용 불가 등)
    - 인터페이스 타입의 인스턴스를 인수로 받을 수 있다.


## UncheckedException

---

### Unchecked Exception

- RuntimeException을 상속하여 명시적인 처리를 강제하지 않는 익셉션들
- Error(StackOverFlow 등 JVM에서 발생하는 것들)를 상속받아 구현한 Exception들도 포함된다.
- try-catch로 잡아도되고 안잡아도 된다.
    - 잡는다고 하더라도, Runtime에서 발생하는 예외이기 때문에 직접적인 처리를 수행하기는 어렵다.
- 예) NullPointerExcetpion, ArthmeticException

### CheckedException

- Runtime Exception을 상속하지 않은 Exception으로, 명시적으로 처리해줘야하는 Exception들
- Compile 시점에 try-catch로 잡아야하는 Exception들이 포함된다.
- try-catch로 처리를 하던, 밖으로 또 던지던 처리가 필수적이다.
- 자체로 API로 생각하자. Client는 해당 메서드를 사용하기 전에, 이를 확인해야한다.
- 예) IOException, FileNotFoundExcetpion, SQLException, AWTException이 이에 해당한다.

### 왜 우리는 Unchecked Exception을 선호하는가?

- 컴파일 에러를 신경쓰지 않아도 되고, 작성이 매우 쉽다.
- 메서드 선언부에 선언하지 않아도 된다.
- 사용자에게 해당 Exception이 발생했을 때, Client가 예외처리할 수 있는 기회를 주고 싶을 때 try-catch 또는 throws를 붙여서 Checked Exception처럼 다루게 하자.

### Checked Exception은 왜 존재하는 것일 까?

- 왜 잡지 않은 예외를 메서드에 선언해야 하는가?
    - 메서드에 선언한 예외는 프로그래밍 인터페이스의 일부다. 즉 , 해당 메서드를 사용하는 코드가 반드시 알아야하는 정보다.
    - 그래야 해당 예외가 발생했을 상황에 대처하는 코드를 작성 가능하다.
- Unchecked Exception은 그럼 왜 메서드에 선언하지 않아도 되는가?
    - Unchecked Exception은 어떤 식으로든 처리하거나 복구할 수 없는 경우에 사용하는 예외다. 가령, 숫자를 0으로 나누거나, null 레퍼런스에 메서드를 호출하는 등.
        - 그렇기 때문에, try- catch로 잡아도 따로 처리할 수 있는 방법이 없다. 단순히 메세지 출력 외에는 …
        - 따라서 CloneNotSupportedException은 사실 Checked가 아닌, Unchecked Exception이어야 한다.
    - 이런 예외는 프로그램 전반에 걸쳐 어디서든 발생할 수 있기 때문에 이 모든 Unchecked Exception을 메서드에 선언하도록 강제한다면 프로그램의 명확도(API 가 흐려진다.)가 떨어진다.


### 언제 예외를 사용해야하는가?

- 단순히 처리하기 쉽고 편하다는 이유만으로 RuntimeException을 선택하지는 말자.
- 클라이언트가 해당 예외 상황을 복구할 수 있다면 CheckedException을 사용하고, 해당 예외가 발생했을 때 아무것도 할 수 없다면, UncheckedException으로 만든다.
- [https://docs.oracle.com/javase/tutorial/essential/exceptions/runtime.html](https://docs.oracle.com/javase/tutorial/essential/exceptions/runtime.html)

### 예외 처리 방법

- 예외를 처리하는 방법에는 크게 예외 복구, 예외 처리 회피, 예외 전환이 있다.
- 예외 복구 : 예외를 파악하고, 문제를 해결해서 정상 상태로 돌려놓는 것
- 예외 처리 회피 : 예외 처리를 직접 담당하지 않고 호출한 쪽으로 던져 회피하는 방법
- 예외 전환 : 예외 회피와 비슷하게 메서드 밖으로 예외를 던지지만, 적절한 예외로 전환하여 던지는 방법. 가령, 명확한 의미로 전달하기 위해 변경한다. 또는 단순 포장한다.

### 올바른 예외 처리 방식

- 예외 복구 전략이 명확하고, 복구가 가능하다면 CheckedException을 try-catch로 잡아서 예외를 복구하자.
- 복구가 불가능한 CheckedException이 발생하면 더 구체적인 UncheckedException을 발생시키고, 예외에 대한 메시지를 명확하게 전달하는 것이 좋다.
- 무책임하게 상위 메서드에 throw로 예외를 던지는 것은 상위 메서드의 책임이 증가하기 때문에 좋지 않다.

## TreeSet

---

### AbstractSet을 확장한 정렬된 컬렉션

- 엘리먼트를 추가한 순서는 중요하지 않다.
- 엘리먼트가 지닌 자연적인 순서(natural order)에 따라 정렬한다.
    - Comparable을 구현한 객체들만 삽입 가능
    - Comparable을 구현하지 않은 객체를 삽입하려고 한다면, Casting 에러가 난다.
        - 비교를 해야하니까!
    - 또는 사용할 비교 연산자를 넣어주면 된다.
- 오름차순으로 정렬한다.
- 스레드 안전하지 않다.