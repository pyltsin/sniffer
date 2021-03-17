import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HashCodeOverride {
    public static void main(String[] args) {
        Map<Clazz1, Integer> clazz1IntegerMap = Map.of(new Clazz1(), 1);
        Map<Clazz2, Integer> clazz2IntegerMap = Map.of(new Clazz2(), 1);
        Map<Clazz3, Integer> clazz3IntegerMap = <weak_warning descr="Class 'Clazz3' does not contain overrided hashCode() or equals(..) methods">Map.of(new Clazz3(), 1)</weak_warning>;

        Set<Clazz1> clazz1s = Set.of(new Clazz1());
        Set<Clazz2> clazz2s = Set.of(new Clazz2());
        Set<Clazz3> clazz3s = <weak_warning descr="Class 'Clazz3' does not contain overrided hashCode() or equals(..) methods">Set.of(new Clazz3())</weak_warning>;

        HashMap<Clazz1, String> map1 = new HashMap<>();
        Map<Clazz2, String> map2 = new HashMap<>();
        Map<Clazz3, String> map3 = <weak_warning descr="Class 'Clazz3' does not contain overrided hashCode() or equals(..) methods">new HashMap<>()</weak_warning>;

        Set<Clazz1> set1 = new HashSet<>();
        Set<Clazz2> set2 = new HashSet<>();
        Set<Clazz3> set3 = <weak_warning descr="Class 'Clazz3' does not contain overrided hashCode() or equals(..) methods">new HashSet<>()</weak_warning>;

        Set<Clazz1> collectSet1 = Stream.of(new Clazz1(), new Clazz1())
                .collect(Collectors.toSet());
        Set<Clazz2> collectSet2 = Stream.of(new Clazz2(), new Clazz2())
                .collect(Collectors.toSet());
        Set<Clazz3> collectSet3 = <weak_warning descr="Class 'Clazz3' does not contain overrided hashCode() or equals(..) methods">Stream.of(new Clazz3(), new Clazz3())
                .collect(Collectors.toSet())</weak_warning>;

        Set<Clazz1> collectSet4 = Stream.of(new Clazz1(), new Clazz1())
                .collect(Collectors.toUnmodifiableSet());
        Set<Clazz2> collectSet5 = Stream.of(new Clazz2(), new Clazz2())
                .collect(Collectors.toUnmodifiableSet());
        Set<Clazz3> collectSet6 = <weak_warning descr="Class 'Clazz3' does not contain overrided hashCode() or equals(..) methods">Stream.of(new Clazz3(), new Clazz3())
                .collect(Collectors.toUnmodifiableSet())</weak_warning>;

        Map<Clazz2, Clazz2> collect1 = Stream.of(new Clazz2(), new Clazz2())
                .collect(Collectors.toMap(t -> t, t -> t));
        Map<Clazz3, Clazz3> collect2 = <weak_warning descr="Class 'Clazz3' does not contain overrided hashCode() or equals(..) methods">Stream.of(new Clazz3(), new Clazz3())
                .collect(Collectors.toMap(t -> t, t -> t))</weak_warning>;

        Map<Clazz3, Clazz3> collect3 = <weak_warning descr="Class 'Clazz3' does not contain overrided hashCode() or equals(..) methods">Stream.of(new Clazz3(), new Clazz3())
                .collect(Collectors.toConcurrentMap(t -> t, t -> t))</weak_warning>;
        Map<Clazz3, Clazz3> collect4 = <weak_warning descr="Class 'Clazz3' does not contain overrided hashCode() or equals(..) methods">Stream.of(new Clazz3(), new Clazz3())
                .collect(Collectors.toUnmodifiableMap(t -> t, t -> t))</weak_warning>;
        Map<Clazz3, List<Clazz3>> collect5 = <weak_warning descr="Class 'Clazz3' does not contain overrided hashCode() or equals(..) methods">Stream.of(new Clazz3(), new Clazz3())
                .collect(Collectors.groupingBy(t -> t))</weak_warning>;
        Map<Clazz3, List<Clazz3>> collect6 = <weak_warning descr="Class 'Clazz3' does not contain overrided hashCode() or equals(..) methods">Stream.of(new Clazz3(), new Clazz3())
                .collect(Collectors.groupingByConcurrent(t -> t))</weak_warning>;

        Map<Clazz3, Clazz3> collect7 = <weak_warning descr="Class 'Clazz3' does not contain overrided hashCode() or equals(..) methods">Stream.of(new Clazz3(), new Clazz3())
                .collect(HashMap::new,
                        (clazz3Clazz3HashMap, clazz3) -> clazz3Clazz3HashMap.put(clazz3, clazz3),
                        (clazz3Clazz3HashMap, mapNext) -> {
                            mapNext.forEach(clazz3Clazz3HashMap::put);
                        }
                )</weak_warning>;
    }
}

class Clazz1 {
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}

class Clazz2 extends Clazz1 {
}

class Clazz3 {
}
