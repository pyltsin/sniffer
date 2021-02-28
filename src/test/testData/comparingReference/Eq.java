public class Eq {
    public boolean compare2Strings(java.lang.String s1, java.lang.String s2) {
        return (<warning descr="SDK Suspicious comparison s1 == s2">s1 == s2</warning>);
    }
}