package kg.arbocdi.learning.backtracking;

import java.util.LinkedList;
import java.util.List;

public class DigitsToChars {
    public static void main(String[] args) {
        System.out.println(minTasksToCancelForNoConflict("79"));
    }

    public static List<String> minTasksToCancelForNoConflict(String digits) {
        List<String> out = new LinkedList<>();
        dfs(digits.toCharArray(), 0, new StringBuilder(), out);
        return out;
    }

    private static void dfs(char[] digits, int index, StringBuilder sol, List<String> out) {
        if (index == digits.length) {
            out.add(sol.toString());
            return;
        }
        char[] current = getChars(digits[index]);
        for (char c : current) {
            sol.append(c);
            dfs(digits, index + 1, sol, out);
            sol.deleteCharAt(sol.length() - 1);
        }
    }

    private static char[] getChars(char digit) {
        char[] out;
        if (digit == '0' || digit == '1') {
            out = new char[]{digit};
        } else {

            int n = 3;
            int start = 'a' + 3 * (digit - '2');
            if (digit > '7') start++;
            if (digit == '7' || digit == '9') n++;
            out = new char[n];
            for (int i = 0; i < n; i++) {
                int num = start + i;
                out[i] = (char) (num);
            }
        }
        return out;
    }
}
