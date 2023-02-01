# Item 05. 자원을 직접 명시하지 말고 의존 객체 주입을 사용하라

- 자원을 직접 명시하지 않고, 의존 객체 주입을 사용해야하는 인스턴스 or 클래스는 사용하는 자원에 따라 동작이 달라지는 클래스를 의미한다.
- 해당 경우에는 자원을 직접 명시하지 말고, 객체 주입을 통해 해결하도록 하자

## 의존 객체 주입을 사용하기

---

### 의존 객체 주입이란?

- 인스턴스를 생성할 때 필요한 자원을 넘겨주는 방식

### 예제 - before

```java
public class SpellChecker {

  private static final Dictionary dictionary = new DefaultDictionary();

  private SpellChecker() {}

  public static boolean isValid(String word) {
    return dictionary.contains(word);
  }
  public static List<String> suggestions(String typo) {
    return dictionary.closeWordsTo(typo);
  }
}
```

- Dictionary 멤버가 있는데, final 키워드를 사용했기 때문에, 한 종류의 사전만 사용 가능하다.
  - final 한정자를 제거하고, 교체 메서드를 구현하는 방법도 있지만, 해당 방법은 오류를 내기 쉬우며 멀티 스레드 환경에서는 사용할 수 없다.
  - 또한, 사용하는 자원에 따라 동작이 달라지는 클래스에는 정적 유틸리티 클래스나 싱글턴 방식이 적합하지 않다.
- 실생활에서는 사전이 언어별로 따로 있는 경우가 왕왕 있기 때문에 상기 코드는 유연성과 재사용성이 떨어진다.
  - 한글 사전을 사용해야하는 경우를 생각해보자.
- 또한, 항상 Dictionary를 생성해야한다.
  - 만약 Dictionary를 생성할 때 resource가 많이 필요하다면, Test Code를 작성할 때 매우 비효율적이게 된다.
- 의존성 주입을 통해 이를 극복할 수 있다.

### 예제 - after

```java
## SpellChecker
public class SpellChecker {

    private final Dictionary dictionary;

    public SpellChecker(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public boolean isValid(String word) {
        return dictionary.contains(word);
    }

    public List<String> suggestions(String typo) {
        return dictionary.closeWordsTo(typo);
    }
}
```

- 상기 SpellChecker는 모든 Dictionary에 대해서 본래 제공하던 기능을 재사용할 수 있다.
- 다만, Dictionary는 인터페이스여야하는 전제가 있다.

## 변형해보기 - factory 넘겨주기

---

- 쓸만한 변형으로 생성자에 자원 팩토리를 넘겨주는 방식이 있는데, 이 방식을 알아보자.

### 예제 - after

```java
## Factory
public class DictionaryFactory {
    public static DefaultDictionary get() {
        return new DefaultDictionary();
    }
}

## SpellChecker
public class SpellChecker {

    private final Dictionary dictionary;

    public SpellChecker(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

		## Factory 넘겨 받기
		public SpellChecker(DictionaryFactory dictionaryFactory) {
				this.dictionary = dictionaryFactory.get();
		}
	
		## Supplier를 통한 Java 8에서의 방식	
    public SpellChecker(Supplier<? extends Dictionary> dictionarySupplier) {
        this.dictionary = dictionarySupplier.get();
    }

    public boolean isValid(String word) {
        return dictionary.contains(word);
    }

    public List<String> suggestions(String typo) {
        return dictionary.closeWordsTo(typo);
    }
}
```

- 중간 단계를 추상화한 버전이라고 생각하자.
- 자바8에서는 Supplier를 통해 이를 아름답게 구현할 수 있다.
- Supplier에 특정 타입만을 지정하는것이 아닌, 확장성있게 적절한 타입을 지정하거나, 한정적 와일드카드 타입을 사용해야한다.

## 정리

---

- 클래스가 내부적으로 하나 이상의 자원에 의존하고, 그 자원이 클래스 동작에 영향을 준다면, 싱글턴과 정적 유틸리티 클래스는 사용하지 않는 것이 좋다.
- 또한, 해당 자원들을 클래스가 직접 만들게해서는 안된다.
- 대신, 필요한 자원을 생성자에게 넘겨주는 의존 객체 주입 방법을 사용하자
  - 이를 통해 클래스의 유연성, 재사용성, 테스트 용이성을 개선할 수 있다.

## 팩토리 메서드 패턴

---

- 구체적으로 어떤 인스턴스를 만들지는 서브 클래스가 정하는 패턴
- 새로운 Product를 제공하는 팩토리를 추가하더라도, 팩토리를 사용하는 클라이언트 코드는 변경할 필요가 없다.

### 예시

```java
## Creator
public interface DictionaryFactory {
  Dictionary getDictionary();
}

## ConcreteCreator -> Concrete Product를 Product 타입으로 반환한다.
public class DefaultDictionaryFactory implements DictionaryFactory {
  @Override
  public Dictionary getDictionary() {
    return new DefaultDictionary();
  }
}
```

- 객체 지향 원칙인 OCP를 지킬 수 있게 되므로, 숙지하자.

## Spring IoC

---

### IoC (Inversion Of Control)

- 자기 코드에 대한 제어권을 자기 자신이 가지고 있지 않고, 외부에서 제어하는 경우를 의미. 하기와 같은 경우가 해당한다.
  - 인스턴스 생성
  - 메서드 실행
  - 의존성 주입

### Spring IoC 컨테이너 사용 장점

- 수많은 개발자들에 의해 검증되었으며, 자바 표준 스펙인(@Inject)도 지원한다.
  - 내가 만든 컨테이너보다 훨씬 안정적이다.
- 손쉽게 싱글톤 Scope를 사용할 수 있다.
- 객체 생성 (Bean) 관련 라이프사이클 인터페이스를 제공한다.

### 예시

**SpellChecker, Dictionary**

```java
@Component
public class SpellChecker {

  private Dictionary dictionary;

  public SpellChecker(Dictionary dictionary) {
    this.dictionary = dictionary;
  }

  public boolean isValid(String word) {
    return dictionary.contains(word);
  }

  public List<String> suggestions(String typo) {
    return dictionary.closeWordsTo(typo);
  }
}

@Component
public class SpringDictionary implements Dictionary {

  @Override
  public boolean contains(String word) {
    System.out.println("contains " + word);
    return false;
  }

  @Override
  public List<String> closeWordsTo(String typo) {
    return null;
  }
}
```

- Spring은 POJO를 지향하므로, @Component 어노테이션을 제외하고는, 기존 코드들과 크게 다를게 없다.
- Configuration을 직접 명시하여 Bean 등록을 제어할 수 있다.

**Configuration**

```java
@Configuration
@ComponentScan(basePackageClasses = AppConfig.class)
public class AppConfig {

}
```

- @ComponentScan 어노테이션을 통해, @Component 어노테이션이 붙어있는 클래스로 빈을 생성하여 관리할 수 있다.
- basePackageClasses는 지정한 Class가 존재하는 package부터 component들을 찾겠다는 의미