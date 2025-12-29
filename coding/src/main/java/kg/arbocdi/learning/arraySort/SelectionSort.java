package kg.arbocdi.learning.arraySort;

import kg.arbocdi.learning.Utils;

public class SelectionSort {
    public static void main(String[] args) {
        int[] src = {9, 5, 7, 1, 0};
        sort(src);
        Utils.printIntArray(src);
    }

    public static void sort(int[] src) {
        for (int a = 0; a < src.length - 1; a++) {
            int minIndex = a;
            for (int b = a + 1; b < src.length; b++) {
                if (src[b] < src[minIndex]) minIndex = b;
            }
            if (minIndex != a) {
                Utils.swap(src, a, minIndex);
            }
        }
    }
}
