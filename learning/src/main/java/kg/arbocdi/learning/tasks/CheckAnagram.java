package kg.arbocdi.learning.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CheckAnagram {
    public static void main(String[] args) {
        System.out.println(isAnagram("abbac", "cbbaa"));
    }

    public static int isAnagram(String s, String t) {
        if (s == null || t == null) return 0;
        if (s.length() != t.length()) return 0;
        Map<Character, AtomicInteger> chars = new HashMap<>();
        for (char c : s.toCharArray()) {
            put(chars, c);
        }
        for (char c : t.toCharArray()) {
            if (!take(chars, c)) return 0;
        }
        return 1;
    }

    private static void put(Map<Character, AtomicInteger> chars, char c) {
        AtomicInteger count = chars.get(c);
        if (count == null) {
            chars.put(c, new AtomicInteger(1));
        } else {
            count.incrementAndGet();
        }
    }

    private static boolean take(Map<Character, AtomicInteger> chars, char c) {
        AtomicInteger count = chars.get(c);
        if (count == null) {
            return false;
        } else {
            int val  = count.decrementAndGet();
            if (val > 0) {
                return true;
            } else if (val == 0) {
                chars.remove(c);
                return true;
            } else {
                throw new RuntimeException("Algo error");
            }
        }
    }
}
