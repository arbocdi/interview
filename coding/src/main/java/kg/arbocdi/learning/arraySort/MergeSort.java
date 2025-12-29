package kg.arbocdi.learning.arraySort;

import kg.arbocdi.learning.Utils;

public class MergeSort {
    public static void main(String[] args) {
        int[] x = {5, 6, 7, 8, 9, 1, 2, 3};
        Utils.printIntArray(x);
        System.out.println();
        MergeSort mergeSort = new MergeSort();
        mergeSort.sort(x);
        Utils.printIntArray(x);
    }

    public void sort(int[] x) {
        if (x.length < 2) return;
        int mid = x.length / 2;
        int[] left = subarray(x, 0, mid);
        int[] right = subarray(x, mid, x.length);

        sort(left);
        sort(right);
        print(left, right);
        merge(x, left, right);
        Utils.printIntArray(x);
        System.out.println();
    }

    public void merge(int[] x, int[] left, int[] right) {
        int s = 0, l = 0, r = 0;
        while (l < left.length && r < right.length) {
            if (left[l] <= right[r]) {
                x[s++] = left[l++];
            } else {
                x[s++] = right[r++];
            }
        }
        while (l < left.length) {
            x[s++] = left[l++];
        }
        while (r < right.length) {
            x[s++] = right[r++];
        }
    }

    private int[] subarray (int[] x, int start, int end) {
        int[] result = new int[end - start];
        int i = 0;
        for (int a = start; a < end; a++) {
            result[i++] = x[a];
        }
        return result;
    }

    private void print(int[] left, int[] right) {
        Utils.printIntArray(left);
        System.out.print(" # ");
        Utils.printIntArray(right);
        System.out.println();
    }
}
