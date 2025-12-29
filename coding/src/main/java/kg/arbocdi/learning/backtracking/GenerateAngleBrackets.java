package kg.arbocdi.learning.backtracking;

import java.util.LinkedList;
import java.util.List;

/**
 * Generate Valid Angle Bracket Sequences
 * Given n, return all valid sequences of n pairs of '<' and '>' with proper nesting.
 * int n = 2
 * Output:
 * <><>
 * <<>>
 */
public class GenerateAngleBrackets {
    public static void main(String[] args) {
        for (int n = 1; n <= 4; n++) {
            System.out.println("SEQUENCE OF "+n);
            for(String result:generateAngleBracketSequences(n)){
                System.out.println(result);
            }
        }
    }

    public static List<String> generateAngleBracketSequences(int n) {
        List<String> result = new LinkedList<>();
        if (n <= 0) return result;
        dfs(n, 0, 0, new StringBuilder(), result);
        return result;
    }

    public static void dfs(int n, int openBr, int closeBr, StringBuilder partialSolution, List<String> result) {
        if (partialSolution.length() == 2 * n) {
            result.add(partialSolution.toString());
            return;
        }
        if (openBr < n) {
            partialSolution.append("<");
            dfs(n, openBr + 1, closeBr, partialSolution, result);
            partialSolution.deleteCharAt(partialSolution.length() - 1);
        }
        if (closeBr < openBr) {
            partialSolution.append(">");
            dfs(n, openBr, closeBr + 1, partialSolution, result);
            partialSolution.deleteCharAt(partialSolution.length() - 1);
        }
    }
}
