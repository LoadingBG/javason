package loadingbg.javason;

public class Main {
    public static void main(String[] args) {
        System.out.println(JavaSON.toJSON(new A()));
    }

    static class A {
        @JSONField("key")
        public int a() {
            return 1;
        }

        @JSONField("key")
        public int b() {
            return 2;
        }

        @JSONField("key")
        public int c() {
            return 3;
        }

        @JSONField("k")
        public int d() {
            return 4;
        }
    }
}
