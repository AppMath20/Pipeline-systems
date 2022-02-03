public class input {
    private int temp = 1;

    input() {
        ++this.temp;
    }

    void print() {
        System.out.println(this.temp);
    }

    void change() {
        for(int i = 0; i < 100; ++i) {
            if (i % 2 == 0) {
                ++this.temp;
            }

            if (i % 3 == 0 && i < 50) {
                this.temp *= this.temp;
            }
        }

    }

    boolean check() {
        return this.temp < 1000;
    }
}