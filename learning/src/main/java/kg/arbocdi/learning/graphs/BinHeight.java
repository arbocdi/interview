package kg.arbocdi.learning.graphs;

import java.util.Arrays;
import java.util.List;

public class BinHeight {
    public static void main(String[] args) {
        List<Integer> values = Arrays.asList(4, 2, 6, 1, 3, 5, 7);
        List<Integer> leftChild = Arrays.asList(1, 3, 5, -1, -1, -1, -1);
        List<Integer> rightChild = Arrays.asList(2, 4, 6, -1, -1, -1, -1);
        System.out.println(getBinarySearchTreeHeight(values,leftChild,rightChild));
    }
    public static int getBinarySearchTreeHeight(List<Integer> values, List<Integer> leftChild, List<Integer> rightChild) {
        return h(0,  leftChild, rightChild);

    }

    private static int h(int i, List<Integer> leftChild, List<Integer> rightChild) {
        if (i == -1) return 0;
        int left = leftChild.get(i);
        int right = rightChild.get(i);
        int h = Math.max(h(left, leftChild, rightChild), h( right, leftChild, rightChild));
        return h + 1;
    }
}
