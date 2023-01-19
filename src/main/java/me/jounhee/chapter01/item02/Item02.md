# Item 02. 생성자에 매개변수가 많다면 빌더를 고려하라.

정적 팩터리와 생성자에 선택적 매개변수가 많을 때 고려할 수 있는 방안에 대해서 알아보자.

## **대안1: 점층적 생성자 패턴 또는 생성자 체이닝**

---

### 예제 코드

```java
public class NutritionFacts {
	private final int servingSize;
	private final int servings;
	private final int calories;
	private final int fat;
	private final int soduim;
	private final int carbohydrate;
	
	public NutritionFacts(int servingSize)
	public NutritionFacts(int servingSize, int servings)
	public NutritionFacts(int servingSize, int servings, int calories)
	public NutritionFacts(int servingSize, int servings, int calories, int fat)
	
	public NutritionFacts(int servingSize, int servings, int calories, int fat, int sodium)
	
	public NutritionFacts(int servingSize, int servings, int calories, int fat, int sodium, int carbohydrate)
}
```

### 단점

- 매개변수가 늘어나면 클라이언트 코드를 작성하거나 읽기 어렵다.
- 이런 경우, 보통 사용자가 원하지 않는 매개 변수를 설정하는 경우가 더러 있다는 단점이 있다.

## **대안2: 자바빈즈 패턴**

---

### 예제 코드

```java

public class NutritionFacts {
	private int servingSize;
	private int servings;
	private int calories;
	private int fat;
	private int soduim;
	private int carbohydrate;

	public NutritionFacts() {}

	public void setServingSize(int servingSize) {
	    this.servingSize = servingSize;
	}
	
	public void setServings(int servings) {
	    this.servings = servings;
	}
	
	public void setCalories(int calories) {
	    this.calories = calories;
	}
	
	public void setFat(int fat) {
	    this.fat = fat;
	}
	
	public void setSodium(int sodium) {
	    this.sodium = sodium;
	}
	
	public void setCarbohydrate(int carbohydrate) {
	    this.carbohydrate = carbohydrate;
	}
```

### 단점

- 완전한 객체를 만들려면 메서드를 여러번 호출해야 한다. (일관성이 무너진 상태가 될 수도 있다.)
- 클래스를 불변으로 만들 수 없다.

## 권장하는 방법: 빌더 패턴

---

### 예제 코드

```java
...
public static class Builder {
	// 필수 매개변수
	private final int servingSize;
	private final int servings;
	
	// 선택 매개변수 - 기본값으로 초기화한다.
	private int calories      = 0;
	private int fat           = 0;
	private int sodium        = 0;
	private int carbohydrate  = 0;
	
	public Builder(int servingSize, int servings) {
	    this.servingSize = servingSize;
	    this.servings    = servings;
	}
	
	public Builder calories(int val) {
		calories = val;
		return this;
	}

	public Builder fat(int val) {
		fat = val;
    return this;
	}
	
	public Builder sodium(int val) {
		sodium = val;
		return this;
	}
	
	public Builder carbohydrate(int val) {
		carbohydrate = val;
		return this;
	}
	
	public NutritionFacts build() {
	    return new NutritionFacts(this);
	}
```

- 플루언트 API 또는 메서드 체이닝을 한다.

### 장점

- 계층적으로 설계된 클래스와 함께 사용하기 좋다.
- 점층적 생성자보다 클라이언트 코드를 읽고 쓰기가 훨씬 간결하고, 자바빈즈보다 훨씬 안전하다

## 자바빈즈

---

### 정의

- java.beans 패키지 안에 있는 모든것

### 특징

- 그 중에서도 자바빈이 지켜야할 규약 존재
    - argument없는 기본 생성자
        - 객체를 만들기 편하기 위함
        - 직렬화, 역직렬화를 하기 위해서는 아무것도 없는 생성자가 있어야 동작하기 때문
    - getter와 setter 메소드 이름 규약
    - Serializable 인터페이스 구현
        - 직렬화, 역직렬화 기능을 제공하기 위함
        - 타 시스템과의 연동을 위해서는 데이터를 주고받아야만 하는데, 그러기 위해서는 모든 시스템이 공통으로 사용하는 언어인 바이트형태의 코드로 변환할 수 있어야하기 때문
        - ex) JDBC를 사용하여 DB랑 통신을 할 때도 바이트 코드로 구동이 된다 ,.. ?

하지만 실제로 오늘날 자바빈 스팩 중에서도 getter와 setter가 주로 쓰는 이유는?

- JPA나 스프링과 같은 여러 프레임워크에서 리플렉션을 통해 특정 객체의 값을 조회하거나 설정하기 때문

### 예제 코드

```java
package me.jounhee.chapter01.item02.javabeans;

// 코드 2-2 자바빈즈 패턴 - 일관성이 깨지고, 불변으로 만들 수 없다. (16쪽)
public class NutritionFacts {
    // 필드 (기본값이 있다면) 기본값으로 초기화된다.
    private int servingSize  = -1; // 필수; 기본값 없음
    private int servings     = -1; // 필수; 기본값 없음
    private int calories     = 0;
    private int fat          = 0;
    private int sodium       = 0;
    private int carbohydrate = 0;
    private boolean healthy;

    public NutritionFacts() { }

    public static void main(String[] args) {
        NutritionFacts cocaCola = new NutritionFacts();
    }
}
```

## 객체 얼리기

---

### 정의

- 임의의 객체를 불변 객체로 만들어주는 기능

### 특징

- Object.freeze()에 전달한 객체는 그 뒤로 변경될 수 없다.
    - 새 프로퍼티를 추가하지 못함
    - 기존 프로퍼티를 제거하지 못함
    - 기존 프로퍼티 값을 변경하지 못함
    - 프로토타입을 변경하지 못함
- strict 모드에서만 동작함
- 비슷한 류의 Function으로 Object.seal()과 Object.preverntExtensions()가 있다.

### 예제 코드

- 자바 스크립트에서는 동적으로 멤버를 추가하거나 제거할 수 있다.

```jsx
var keesun= {
    'name': 'Keesun',
    'age': 40
};

keesun.kids = ["서연"];
console.info(keesun.name);
```

- const로 선언해놔도 변경이 가능하다. 마치 자바의 final 필드의 필드의 값을 변경할 수 있는 것과 비슷한 느낌

```jsx
final Person person = new Person();
person.setName("Test"); //이와 비슷
```

- Object의 freeze() 메서드를 사용하면, 상기 동작들은 동작하지 않고, 에러를 발생시킨다.

```jsx
public class Person {

    private final String name;

    private final int birthYear;

    private final List<String> kids;

    public Person(String name, int birthYear) {
        this.name = name;
        this.birthYear = birthYear;
        this.kids = new ArrayList<>();
    }

		public void setName(String name) {
			checkIfObjectIsFrozen();
			this.name = name;
		}

		public checkIfObjectIsFrozen() {
			if (this.frozen()) {
				throw new IllegarArgumentException();
			}
		}

    public static void main(String[] args) {
        Person person = new Person("keesun", 1982);
    }
}
```

## 빌더 패턴

---

### 정의

- 동일한 프로세스를 거쳐 다양한 구성의 인스턴스를 생성하는 방법

### 특징

- 복잡한 객체를 만드는 프로세스를 독립적으로 분리할 수 있다.

### 예제 코드

```jsx
public static class Builder {
        // 필수 매개변수
        private final int servingSize;
        private final int servings;

        // 선택 매개변수 - 기본값으로 초기화한다.
        private int calories      = 0;
        private int fat           = 0;
        private int sodium        = 0;
        private int carbohydrate  = 0;

        public Builder(int servingSize, int servings) {
            this.servingSize = servingSize;
            this.servings    = servings;
        }

        public Builder calories(int val) { calories = val;      return this; }
        public Builder fat(int val) { fat = val;           return this; }
        public Builder sodium(int val) { sodium = val;        return this; }
        public Builder carbohydrate(int val) { carbohydrate = val;  return this; }

        public NutritionFacts build() {
            return new NutritionFacts(this);
        }
    }
```

## IllegalArgumentException

---

### 정의

- 잘못된 인자를 넘겨 받았을 때, 사용
- 최소한 어떤 필드가 잘못되었는지 알려줄 수 있도록 구현해야함
- Runtime Exception을 시그니처에 선언함으로써, 어떤 Exception이 해당 로직에서 나올 수 있는지 알리기 위함.
- 최대한 기존에서 제공하고 있는 것을 사용하고, 없다면 그제서야 상속받아서 사용하는걸 지향하자.

### 질문

- checked exception과 unchecked exception의 차이는?
    - checked exception : 반드시 try-catch로 잡아야하는 Exception. Runtime exception을 상속받진 않았지만, Exception 하위의 Exception이다.
    - unchecked excetpion : 굳이 try-catch를 잡지 않아도 되고, thorw를 다시 안해도 된다. Runtime Exception는 개발자들에 의해 실수로 발생하는 것들이기 때문에 에러를 강제하지 않음
- 간혹 메서드 선언부에 unchecked exception을 선언하는 이유는?
- checked exception은 왜 사용할까?

### 예시

```jsx
public class Order {

    public void updateDeliveryDate(LocalDate deliveryDate) {
        if (deliveryDate == null) {
            throw new NullPointerException("deliveryDate can't be null");
        }

        if (deliveryDate.isBefore(LocalDate.now())) {
            //TODO 과거로 배송 해달라고??
            throw new IllegalArgumentException("deliveryDate can't be earlier than " + LocalDate.now());
        }

        // 배송 날짜 업데이트
    }

}
```

## 가변 인수

---

### 정의

- 여러 인자를 받을 수 있는 가변적인 argument (Var + args)
- 배열 형태로 취급
- 빌더패턴을 사용하면, 가변 인수를 사용하는것 처럼 사용할 수 있다는 뜻~

### 특징

- 가변인수는 메서드에 오직 하나만 선언할 수 있다.
- 가변 인수는 메서드의 가장 마지막 매겨변수가 되어야한다.

## 기타

---

### Tip

- boolean은 is로 getter를 대신한다.
- 클래스에 final을 선언한다고 해도, 레퍼런스에 대한것만 고정이지, 내용은 변경 가능하다.

### 숙제

- 자바의 모든 RuntimeException 클래스 이름 한번씩 읽어보자
- 이 [링크](https://docs.oracle.com/javase/tutorial/essential/exceptions/runtime.html)에 있는 글을 꼭 읽어보자