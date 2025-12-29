package kg.arbocdi.learning.backtracking;

/**
 * Given an NxN grid place N queens without conflict
 * return all variants
 */
public class NQueens {
    public static void main(String[] args) {
        int n = 4;
        int[][] grid = new int[n][n];
        boolean[] usedCol = new boolean[n];
        boolean[] usedD1 = new boolean[2 * n - 1];
        boolean[] usedD2 = new boolean[2 * n - 1];
        dfs(n, 0, usedCol, usedD1, usedD2, grid, 0);
    }


    public static void dfs(int N, int row, boolean[] usedCol, boolean[] usedD1, boolean[] usedD2, int[][] grid, int count) {
        if (count == N) {
            printGrid(grid);
        }
        for (int col = 0; col < N; col++) {
            int d1 = row - col + (N - 1);
            int d2 = row + col;
            if (usedCol[col] || usedD1[d1] || usedD2[d2]) continue;
            grid[row][col] = 1;
            usedCol[col] = true;
            usedD1[d1] = true;
            usedD2[d2] = true;
            dfs(N, row + 1, usedCol, usedD1, usedD2, grid, count + 1);
            grid[row][col] = 0;
            usedCol[col] = false;
            usedD1[d1] = false;
            usedD2[d2] = false;
        }
    }

    public static void printGrid(int[][] grid) {
        System.out.println();
        for (int[] row : grid) {
            for (int cell : row) {
                System.out.print(cell);
                System.out.print(" ");
            }
            System.out.println();
        }
    }
}
