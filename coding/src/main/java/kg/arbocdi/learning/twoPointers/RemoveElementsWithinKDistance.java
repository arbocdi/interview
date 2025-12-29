package kg.arbocdi.learning.twoPointers;

import java.util.Arrays;
import java.util.List;

public class RemoveElementsWithinKDistance {
    public static void main(String[] args) {
        List<Integer> taskDurations = Arrays.asList(1, 1, 5, 1, 4);
        System.out.println(findTaskPairForSlot(taskDurations, 5));
    }

    public static List<Integer> findTaskPairForSlot(List<Integer> taskDurations, int slotLength) {
        List<Integer> result = Arrays.asList(-1, -1);
        if (taskDurations.size() < 2) return result;
        for (int a = 0; a < taskDurations.size() - 1; a++) {
            for (int b = a + 1; b < taskDurations.size(); b++) {
                if (taskDurations.get(a) + taskDurations.get(b) == slotLength) {
                    return Arrays.asList(a, b);
                }
            }
        }
        return result;

    }
}
