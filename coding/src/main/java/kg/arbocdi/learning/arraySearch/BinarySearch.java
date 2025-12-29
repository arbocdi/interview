package kg.arbocdi.learning.arraySearch;

public class BinarySearch {
    public static void main(String[] args) {
        int[] src = {4, 6, 7, 9, 10, 11, 12};
        int target = 13;
        System.out.println(search(src, target, 0, src.length - 1));
    }

    public static int search(int[] src, int target, int start, int end) {
        if (end < start) {
            return -1;
        }
        int mInd = start + (end - start) / 2;
        int mVal = src[mInd];
        if (mVal == target) {
            return mInd;
        } else if (target < mVal) {
            return search(src, target, start, mInd - 1);
        } else {
            return search(src, target, mInd + 1, end);
        }
    }
}
