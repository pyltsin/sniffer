public class MixedParameters {
    public static void main(String[] args) {
        int aX = 7;
        new MixedParameters().test<weak_warning descr="Arguments may be mixed, check their order, suggest: '[argument = aX, parameter = aX],[argument = bx, parameter = bX]'">(getBx(), aX)</weak_warning>;
        test2<weak_warning descr="Arguments may be mixed, check their order, suggest: '[argument = aX, parameter = aX],[argument = bx, parameter = bX]'">(getBx(), aX)</weak_warning>;
    }

    private static int getBx() {
        return 0;
    }

    public void test(int aX, int bX) {

    }

    public static void test2(int aX, int bX) {

    }
}
