import java.util.HashMap;
import java.util.Map;

public class NewHashMap {
    public static void main(String[] args) {
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        Map<Runnable, String> runnableStringHashMap = <weak_warning descr="Class 'java.lang.Runnable' doesnt contain overrided hashCode()">new HashMap<>()</weak_warning>;
        Map<Clazz2, String> clazzStringHashMap = new HashMap<>();
    }

    public static class Clazz1{
        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }

    public static class Clazz2 extends Clazz1{
    }
}