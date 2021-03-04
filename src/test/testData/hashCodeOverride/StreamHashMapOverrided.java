import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamHashMapOverrided {
    public static void main(String[] args) {
        Map<Clazz2, Clazz2> collect = <weak_warning descr="Class 'StreamHashMapOverrided.Clazz2' doesnt contain overrided hashCode()">java.util.stream.Stream.of(new Clazz2(), new Clazz2())
                .collect(Collectors.toMap(t -> t, t -> t))</weak_warning>;


        Map<Clazz3, Clazz3> collect3 = <weak_warning descr="Class 'StreamHashMapOverrided.Clazz3' doesnt contain overrided hashCode()">java.util.stream.Stream.of(new Clazz3(), new Clazz3())
                .collect(Collectors.toMap(t -> t, t -> t))</weak_warning>;

        Map<Clazz3, Clazz3> collect4 = <weak_warning descr="Class 'StreamHashMapOverrided.Clazz3' doesnt contain overrided hashCode()">java.util.stream.Stream.of(new Clazz3(), new Clazz3())
                .collect(HashMap::new,
                        (clazz3Clazz3HashMap, clazz3) -> clazz3Clazz3HashMap.put(clazz3, clazz3),
                        (clazz3Clazz3HashMap, map2) -> {
                            map2.forEach((key, value) -> {
                                clazz3Clazz3HashMap.put(key, value);
                            });
                        }
                )</weak_warning>;

    }

    public static class Clazz1 {
        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }

    public static class Clazz2 extends Clazz1 {
    }

    public static class Clazz3 {
    }
}