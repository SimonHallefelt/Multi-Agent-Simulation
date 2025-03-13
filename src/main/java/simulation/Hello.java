package simulation;

public class Hello {
    public String hello() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        Hello h = new Hello();
        System.out.println("main " + h.hello());
    }
}
