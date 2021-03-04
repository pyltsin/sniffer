import java.util.Map;

public class MapOfHashMap {
    public static void main(String[] args) {
        Map<Clazz1, Integer> clazz1IntegerMap = Map.of(new Clazz1(), 1);
        Map<Clazz2, Integer> clazz2IntegerMap = Map.of(new Clazz2(), 1);
        Map<Clazz3, Integer> clazz3IntegerMap = <weak_warning descr="Class 'Clazz3' doesnt contain overrided hashCode()">Map.of(new Clazz3(), 1)</weak_warning>;
    }
}

class Clazz1 {
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
class Clazz2 extends Clazz1 {
}
class Clazz3 {
}
