public class Ternary {
    double calc(int bx, int angle, double scale) {
        return Math.tan((<weak_warning descr="Equal then and else expressions">angle % 2 == 0 ?
                bx - 1 : bx/**/-1</weak_warning>) * 0.42) * scale;
    }
}
