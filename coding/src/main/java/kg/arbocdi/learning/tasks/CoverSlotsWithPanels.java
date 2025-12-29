package kg.arbocdi.learning.tasks;

import java.math.BigInteger;

public class CoverSlotsWithPanels {
    public static void main(String[] args) {
        System.out.println(countInstallationSequences(5));
    }

    public static String countInstallationSequences(int n) {
        return String.valueOf(fib(n));
    }

    private static BigInteger fib(int n) {
        BigInteger fn2 = BigInteger.valueOf(1);
        BigInteger fn1 = BigInteger.valueOf(1);
        if (n == 0 || n == 1) return fn1;
        BigInteger fn = null;
        for (int i = 2; i <= n; i++) {
            fn = fn1.add(fn2);
            fn2 = fn1;
            fn1 = fn;
        }
        return fn;
    }
}
