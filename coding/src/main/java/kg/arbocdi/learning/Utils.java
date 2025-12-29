package kg.arbocdi.learning;

public class Utils {
    public static void printIntArray(int[] x) {
        for (int a = 0; a < x.length; a++) {
            System.out.print(x[a]);
        }
    }

    public static void swap(int[] x, int a, int b) {
        int aa = x[a];
        x[a] = x[b];
        x[b] = aa;
    }
}
