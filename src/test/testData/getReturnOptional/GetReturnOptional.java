import java.util.Optional;

public class GetReturnOptional {
    public static void main(String[] args) {

        Optional<String> side = getSide();
        System.out.println(side);

    }

    private static Optional<String> <weak_warning descr="Name 'getSide' should start with find">getSide</weak_warning>() {
        return Optional.empty();
    }

    public static class Inner {
        private Optional<String> <weak_warning descr="Name 'getSide' should start with find">getSide</weak_warning>() {
            return Optional.empty();
        }
    }
}
