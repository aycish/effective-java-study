package me.jounhee.chapter01.item04;

public class UtilityClass {

    /**
     * 이 클래스는 인스턴스를 만들 수 없습니다.
     */
    private UtilityClass() {
        throw new AssertionError();
    }
    public static void utilityMethod() {
        System.out.println("Utility method");
    }
    public static void main(String[] args) {
        UtilityClass.utilityMethod();
        UtilityClass utilityClass = new UtilityClass();
        utilityClass.utilityMethod();
    }
}
