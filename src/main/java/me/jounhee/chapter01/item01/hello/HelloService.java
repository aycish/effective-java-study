package me.jounhee.chapter01.item01.hello;

public interface HelloService {

    String hello();

    static String hi() {
        prepareMessage();
        return "hi";
    }

    static private void prepareMessage() {
    }

    static String hi1() {
        prepareMessage();
        return "hi";
    }

    static String hi2() {
        prepareMessage();
        return "hi";
    }

    default String bye() {
        return "bye";
    }
    static HelloService of(String lang) {
        if (lang.equals("ko")) {
            return new HelloService() {
                @Override
                public String hello() {
                    return "하이";
                }
            };
        } else {
            return new ChineseHelloService();
        }
    }
}
