package kg.arbocdi.learning.twoPointers;

import java.util.Arrays;
import java.util.List;

public class BudgetCounter {
    public static void main(String[] args) {
        List<Integer> prices = Arrays.asList(1, 2, 3, 4, 5);
        int budget = 7;
        System.out.println(countAffordablePairs(prices, budget));
    }

    public static int countAffordablePairs(List<Integer> prices, int budget) {
        int counter = 0;
        if (prices.size() < 2) return counter;
        int l = 0, r = prices.size() - 1;
        while (l < r) {
            if (prices.get(l) + prices.get(r) <= budget) {
                counter += r - l;
                l++;
            } else {
                r--;
            }
        }
        return counter;
    }
}
