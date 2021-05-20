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

    public static class AImpl implements A{
        public void a<weak_warning descr="Arguments may be mixed, check their order, suggest: '[argument = bX, parameter = bX],[argument = aX, parameter = aX]'">(int bX, int aX)</weak_warning>{};
    }

    public interface A{
        void a(int aX, int bX);
    }
}
