# Item 09. try-finally 보다 try-with-resources를 사용하라.

## try-finally보다 try-with-resources를 사용하라

---

- try-finally는 더이상 최선의 방법이 아니다. (자바7부터)
- try-with-resources를 사용하면 코드가 더 짧고 분명하다.
- 만들어지는 예외 정보도 훨씬 유용하다.

### 예제 -  before

```java
static void copy(String src, String dst) throws IOException {
    InputStream in = new FileInputStream(src);
    try {
        OutputStream out = new FileOutputStream(dst);
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            int n;
            while ((n = in.read(buf)) >= 0)
                out.write(buf, 0, n);
        } finally {
            out.close();
        }
    } finally {
        in.close();
    }
}

## Leak이 생길 수 있다.
static void copy(String src, String dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            byte[] buf = new byte[BUFFER_SIZE];
            int n;
            while ((n = in.read(buf)) >= 0)
                out.write(buf, 0, n);
        } finally {
            in.close();
            out.close();
        }
    }
```

- 가독성이 떨어진다.
- 아래의 메서드는 out의 close 호출이 보장되지 않음
- 또한, close 시에도 만약에 exception이 발생하면, 가장 처음에 발생한 예외를 Client 입장에서는 잡을 수 없다.

### 예제 - after

```java
static void copy(String src, String dst) throws IOException {
    try (InputStream   in = new FileInputStream(src);
         OutputStream out = new FileOutputStream(dst)) {
        byte[] buf = new byte[BUFFER_SIZE];
        int n;
        while ((n = in.read(buf)) >= 0)
            out.write(buf, 0, n);
    }
}
```

- in, out의 close가 호출됨을 보장한다.
- 또한, close시에도 exception이 발생하면, 후속으로 발생한 예외, 젤 처음에 발생했던 예외 모두를 보여준다.

## 자바 퍼즐러 예외 처리 코드의 실수

---

### 예시

```java
static void copy(String src, String dst) throws IOException {
    InputStream in = new FileInputStream(src);
    OutputStream out = new FileOutputStream(dst);
    try {
        byte[] buf = new byte[BUFFER_SIZE];
        int n;
        while ((n = in.read(buf)) >= 0)
            out.write(buf, 0, n);
    } finally {
        try {
            out.close();
        } catch (IOException e) {
            // TODO 이렇게 하면 되는거 아닌가?
        }

        try {
            in.close();
        } catch (IOException e) {
            // TODO 안전한가?
        }
    }
}
```

- close 시도 시, IOException 이외의 Exception이 발생하는 경우, Exception 핸들링은 되지 않고, 해당 함수를 호출한 객체의 Thread를 종료시킨다.

## try-with-resources 바이트 코드

---

- 어떻게 처음에 발생한 Exception이 처음으로 찍힐 까?
    - 내부적으로 addSuppressed를 호출하여 추가로 내보내기 때문
- 항상 멱등성을 생각하면서 개발하자.
- 또한, 내부적으로 finally를 사용하여 기능을 제공하는 것이 아닌, 중첩된 try-with-finalliy를 통해 해당 기능을 제공한다.