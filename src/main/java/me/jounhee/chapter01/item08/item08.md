# Item 08. finalizer와 cleaner 사용을 피하라

### 참고

- [https://m.blog.naver.com/PostView.naver?isHttpsRedirect=true&blogId=kbh3983&logNo=220908731253](https://m.blog.naver.com/PostView.naver?isHttpsRedirect=true&blogId=kbh3983&logNo=220908731253)

## Finalizer와 cleaner 사용을 피하라

---

- finalizer와 cleaner는 즉시 수행된다는 보장이 없다.
    - 따라서 finalizer와 cleaner는 실행되지 않을 수도 있다.
- finalizer 동작 중에 예외가 발생하면 정리 작업이 처리되지 않을 수도 있다.
- finalizer와 cleaner는 심각한 성능 문제가 있다.
    - finalizer가 GC의 성능 효율을 떨어뜨리기 때문이다.
    - cleaner 또한, 클래스의 모든 인스턴스를 수거하는 형태로 사용하면 성은 저하가 있다.
- finalizer는 보안 문제가 있다.
- **반납할 자원이 있는 클래스는 AutoCloseable을 구현하고 클라이언트에서 close()를 호출하거나 try-with-resource를 사용해야 한다.**

### Finalizer

- 자바9부터 deprecated
- Tardy Finalization (느린 실행) 문제가 있다.
    - 바로 실행된다는 보장이 없으므로, finalize 문구에 시간적으로 바로 실행되야하는 코드를 넣으면 안된다.
    - FD등 리소스의 사용 한계가 있는 자원을 정리하는 로직이 들어가 있으면, 원치않는 동작을 수행할 수 있다. (ex. FD 고갈)
- catch되지 않은 예외들이 발생할 수 있다.
    - Finalize에서 점검하지 않는 예외가 발생하면 해당 예외는 무시되며 종료 과정은 중단된다.
    - Stack trace가 발생하지만, 종료자 안에서는 아니므로, 경고 문구조차 출력되지 않을 수 있다.
- 성능 저하
    - 프로그램의 성능이 떨어진다.

**예제- Finalizer 살펴보기**

```java
public static void main(String[] args) throws InterruptedException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
    int i = 0;
    while(true) {
        i++;
        new FinalizerIsBad();

        if ((i % 1_000_000) == 0) {
            Class<?> finalizerClass = Class.forName("java.lang.ref.Finalizer");
            Field queueStaticField = finalizerClass.getDeclaredField("queue");
            queueStaticField.setAccessible(true);
            ReferenceQueue<Object> referenceQueue = (ReferenceQueue) queueStaticField.get(null);

            Field queueLengthField = ReferenceQueue.class.getDeclaredField("queueLength");
            queueLengthField.setAccessible(true);
            long queueLength = (long) queueLengthField.get(referenceQueue);
            System.out.format("There are %d references in the queue%n", queueLength);
        }
    }
}
```

- finalize는 Object 클래스에 선언되어 있는 메서드다.
    - 주석을 살펴보면, AutoCloseable을 사용하라고 추천하고 있다.
- 객체 참조없이 new FinalizerIsBad()를 통해 객체를 지속적으로 생성하고 있다.
    - GC의 대상이 된다.
- GC의 대상이 되는 객체들은 Finalizer 내부에 관리하고 있는 queue에 삽입되어 관리된다. Phantom reference와 비슷하다.
- 실행해보면, 간간히 GC가 되지 않은 Status가 찍히는데, 객체를 생성하느라 Resource를 많이 쓰기 때문에 GC가 원할히 이뤄지지 않아서이다.

### Cleaner

- 호출된다는 보장이 없지만, 자신을 수행할 스레드를 제어할 수 있다는 면에서 나을 수 있다.
- AutoClosable의 자원 반납용 안전망으로 사용할 수 있다.
- 네이티브 피어 자원을 회수한다.
    - 단 성능 저하를 감당할 수 있고, 네이티브 피어가 심각한 자원을 가지고 있지 않을 때에만 해당한다.
    - 네이티브 피어가 사용하는 자원을 즉시 회수해야 한다면 close() 메서드를 호출해야 한다.

**예제**

```java
public class BigObject {

  private List<Object> resource;

  public BigObject(List<Object> resource) {
      this.resource = resource;
  }

  // Runnable 구현체 안에는 BigObject에 대한 참조가 없어야한다. 그래야 확실하게 GC의 대상이 되기 때문.
  public static class ResourceCleaner implements Runnable {

      private List<Object> resourceToClean;

      public ResourceCleaner(List<Object> resourceToClean) {
          this.resourceToClean = resourceToClean;
      }

      @Override
      public void run() {
          resourceToClean = null;
          System.out.println("cleaned up.");
      }
  }
}

public class CleanerIsNotGood {

    public static void main(String[] args) throws InterruptedException {
        Cleaner cleaner = Cleaner.create();

        List<Object> resourceToCleanUp = new ArrayList<>();
        BigObject bigObject = new BigObject(resourceToCleanUp);
        cleaner.register(bigObject, new BigObject.ResourceCleaner(resourceToCleanUp));

        bigObject = null;
        System.gc();
        Thread.sleep(3000L);
    }

}
```

- Finalizer에서 하던 일을 별도의 내부 Runnable static class로 구현함
    - 해당 클래스에서 정리해야하는 Resource를 static 내부 클래스에서 run method로 정리한다.
- 이후, client에서 Cleaner를 만들고, 객체를 사용한 뒤 clear에 등록한다.
    - cleaner에 등록할 때, 정리 작업을 수행할 method를 지정해준다.
- 이후 gc를 호출하게한다.

### AutoClosable

- try-with-resources를 사용하여 다 쓰고 난 자산들을 회수하는데 특화
- close()라는 메서드를 구현하여, 객체를 모두 사용한 뒤, close()호출 하여 명시적으로 자원을 수거해줘야한다.
- 부수적으로 각 인스턴스는 자신이 닫혔는지를 추적하는 것이 좋다.
    - 즉, close 메서드에서 이 객체는 더이상 유효하지 않음을 필드에 기록하고, 다른 메서드에는 이 필드를 검사해서 객체가 닫힌 후에 불렸다면 Exception을 던지자.

**예제**

```java
public class AutoClosableIsGood implements Closeable {

    private BufferedReader reader;

    public AutoClosableIsGood(String path) {
        try {
            this.reader = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(path);
        }
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

public static void main(String[] args) {
    try(AutoClosableIsGood good = new AutoClosableIsGood("")) {
        // TODO 자원 반납 처리가 됨.

    }
}
```

- Closable을 구현하며, close() 메서드를 overriding 한다.
    - Closable은 AutoClosable을 extends하고 있다.
- try - with - resources에서 close 메서드를 호출하며 자연스럽게 사용했던 resource들을 정리하게끔한다.
- 또한, 구현을 해놓았더라도 try - with - resources를 사용하지 않을 수 있으므로, Cleaner를 사용하여 해당 기회를 제공해주자.

### Native peer

- 자바 객체가 네이티브 메서드를 통해 기능 수행을 위임할 때, 네이티브 피어라고 한다.
- 네이티브 메서드는 C, C++과 같은 네이티브 언어로 작성한 메서드를 말한다.
- 만약 Native peer에서 성능에 critical한 resource를 사용하고 있다면, AutoClosable, Cleaner를 활용하여 직접 회수 하도록하자.

## Finalizer 공격

---

- 직렬화나 생성자 과정에서 예외가 발생한 경우, 생성되다만 객체에서 하위 클래스의 finalize()가 호출될 수 있다.

### 예제

```java
public class Account {

    private String accountId;

    public Account(String accountId) {
        this.accountId = accountId;

        if (accountId.equals("푸틴")) {
            throw new IllegalArgumentException("푸틴은 계정을 막습니다.");
        }
    }

    public void transfer(BigDecimal amount, String to) {
        System.out.printf("transfer %f from %s to %s\n", amount, accountId, to);
    }

}

public class BrokenAccount extends Account {

    public BrokenAccount(String accountId) {
        super(accountId);
    }

    @Override
    protected void finalize() throws Throwable {
        this.transfer(BigDecimal.valueOf(100), "keesun");
    }
}
```

- Accout를 상속한 뒤, finalize()를 구현한다.
- finalize() 메서드 안에서 transfer를 호출한다.
- BrokenAccount() 객체를 생성할 때, “푸틴”을 넘겨주며 try-catch로 exception을 잡은 뒤 gc를 수행해보자
- BrokenAccount의 finalize가 GC에서 실행된다. → 즉, transfer 메서드가 실행된다.

### 막는 방법

- Account 클래스를 final을 주어 상속받지 못하게 한다.
- (상속 구조가 필요하다면) Account 클래스에 빈 finalize메서드를 final로 오버라이딩 한다.
    - final method는 더이상 오버라이딩 할 수 없으므로

## AuotoClosable

---

- try-with-resource를 지원하는 인터페이스
- 해당 인터페이스는 close() 하나만의 API를 가지고 있다.

**void close() throws Exception**

- 인터페이스에 정의된 메서드에서 Exception 타입으로 예외를 던진다.
- 실제 구현체에서는 구체적인 예외를 던지는 것이 좋다.

    ```java
    @Override
    public void close() throws IOException {
    		reader.close();
    }
    ```

- 가능하다면 예외를 던지지 않는 것도 권장한다.

    ```java
    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    ```

    - 직접 잡아서 처리하는 것이 기선님의 추천
    - 대신, RuntimeException을 던져 close를 호출 실패한 Client의 Thread를 종료시킨다.
    - idempotent (멱등성)이 성립하면 좋다. 항상 지키도록 노력하자.
        - Closable은 무조건 멱등성이 성립해야한다.
        - AutoClosable은 추천 조건이다.

**Closeable 클래스와 차이점**

- IOException을 던지며
- 반드시 idempotent 해야 한다.

## 정적이 아닌 중첩 클래스는 자동으로 바깥 객체의 참조를 갖는다.

---

### 예제

```java
public class OuterClass {

    class InnerClass {
    }

    public static void main(String[] args) {
        OuterClass outerClass = new OuterClass();
        InnerClass innerClass = outerClass.new InnerClass();

        System.out.println(innerClass);

        outerClass.printFiled();
    }

    private void printFiled() {
        Field[] declaredFields = InnerClass.class.getDeclaredFields();
        for(Field field : declaredFields) {
            System.out.println("field type:" + field.getType());
            System.out.println("field name:" + field.getName());
        }
    }
}
```

- 동작을 시켜보면, Inner Class에서 outerClass에 대한 참조를 갖고 있는 것을 확인할 수 있다.
- InnerClass에서 OuterClass.this.hi()식으로 메서드를 참조할 수 있다.
- 만약 참조가 존재하게 되면, 서로 순환참조하게 되어 GC의 기회가 박탈된다.
- 따라서 Cleaner등을 구현할 때, 해당 참조가 발생하지 않도록 Runnable을 구현하여 Static으로 Inner class를 만들어야한다.

## 람다 역시 바깥 객체의 참조를 갖기 쉽다.

---

### 예제

```java
public class LambdaExample {

    private int value = 10;

    private Runnable instanceLambda = () -> {
        System.out.println(value);
    };

    public static void main(String[] args) {
        LambdaExample example = new LambdaExample();
        Field[] declaredFields = example.instanceLambda.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            System.out.println("field type: " + field.getType());
            System.out.println("field name: " + field.getName());
        }
    }

}
```

- 람다 표현식도 Inner Class에서 OuterClass에 대한 참조가 생기는 것처럼, 람다 표현식 내부에서 바깥 객체의 변수에 접근하고 있다면 OuterClass에 대한 참조가 생긴다.
    - value를 static으로 만들거나, 람다식에서 value를 참조하지 않도록 수정해야한다.
- 만약 참조가 존재하게 되면, 서로 순환참조하게 되어 GC의 기회가 박탈된다.
- 즉, 외부 클래스의 변수를 캡처링 하냐 안하냐를 잘 따져봐야한다.