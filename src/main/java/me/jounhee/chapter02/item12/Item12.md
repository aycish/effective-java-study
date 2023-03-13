# Item 12. toString을 항상 재정의 하라

## toString을 항상 재정의하라

---

### Default 동작

- Object에서 기본적으로 제공
- 클래스이름@16진수로 표현한 해시코드

### 강의 및 책 내용

- Object에서 제공하는 toString은 사실 사용자에게 그닥 유익하지 않다.
- **toString은 간결하면서 사람이 읽기 쉬운 형태의 유익한 정보를 반환해야 한다.**
- **객체가 가진 모든 정보를 보여주는 것이 좋다.**
- 값 클래스라면 포맷을 문서에 명시하는 것을 고려한다.
    - 해당 포맷으로 객체를 생성할 수 있는 정적 팩터리나 생성자를 제공하는 것이 좋다.
    - 장점 : 포맷이 명시되어 있어 데이터의 가공이나 저장, 사람이 읽기 편하다.
    - 단점 : 포맷이 변경되었을 때, 데이터 가공 로직등 수정해야하는 부분들이 많아진다.
- **toString이 반환한 값에 포함된 정보를 얻어올 수 있는 API를 제공하는 것이 좋다.**
    - 그렇지 않으면, 사용자들은 toString으로 얻어온 데이터를 파싱해야하는 번거로움이 있으며, 성능상에 문제가 될 수 있다.
    - toString으로 반환해주는 정보들은 공개되었다고 생각하자.
        - 해당 정보들만 API를 제공하자.
    - (백기선님 의견) 즉, 외부에 공개해야하는 정보만을 선택해서 보여주는것이 좋아보인다.
        - 아마존에서는 고객 정보등 매우 민감하기 때문에, logging도 안한다.
        - 따라서 외부에 노출해서는 안되는 데이터들에 대해서는 노출시키지 않는다.
- 경우에 따라 AutoValue, 롬복 또는 IDE를 사용하지 않는게 적절할 수 있다.

### 예시

```java
/**
 * 이 전화번호의 문자열 표현을 반환한다.
 * 이 문자열은 "XXX-YYY-ZZZZ" 형태의 12글자로 구성된다.
 * XXX는 지역 코드, YYY는 프리픽스, ZZZZ는 가입자 번호다.
 * 각각의 대문자는 10진수 숫자 하나를 나타낸다.
 *
 * 전화번호의 각 부분의 값이 너무 작아서 자릿수를 채울 수 없다면,
 * 앞에서부터 0으로 채워나간다. 예컨대 가입자 번호가 123이라면
 * 전화번호의 마지막 네 문자는 "0123"이 된다.
 */
@Override
public String toString() {
    return String.format("%03d-%03d-%04d",
            areaCode, prefix, lineNum);
}

public short getAreaCode() {
    return areaCode;
}

public short getPrefix() {
    return prefix;
}

public short getLineNum() {
    return lineNum;
}
```

- 공개하고자 하는 Data만 ToString에 정해진 형식으로 출력한다.
    - areaCode, prefix, lineNum
- 각각의 데이터별로 접근할 수 있도록 getter 설정