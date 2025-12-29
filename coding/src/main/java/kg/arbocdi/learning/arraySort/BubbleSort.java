package kg.arbocdi.learning.arraySort;

import kg.arbocdi.learning.Utils;

public class BubbleSort {
    public static void main(String[] args) {
        int[]x = {2,9,8,4,7,9};
        sort(x);
        Utils.printIntArray(x);
    }
    public static void sort(int[] x) {
        for (int a = 1; a < x.length; a++) {
            boolean sorted = true;
            for (int b = 0; b < x.length - a; b++) {
                if (x[b] > x[b + 1]) {
                    Utils.swap(x, b, b + 1);
                    sorted = false;
                }
            }
            if(sorted) break;
        }
    }
}
