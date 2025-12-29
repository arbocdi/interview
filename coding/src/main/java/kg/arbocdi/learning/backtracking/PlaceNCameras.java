package kg.arbocdi.learning.backtracking;

import java.util.LinkedList;
import java.util.List;

/**
 * Given an NxN grid where 0 is empty and 1 is blocked,
 * return true if N cameras can be placed on empty cells such that no two share the same row, column, or diagonal.
 */
public class PlaceNCameras {
    public static void main(String[] args) {
        int N = 4;
        List<List<Integer>> grid = new LinkedList<>();
        for(int row=0;row<N;row++){
            List<Integer> ro = new LinkedList<>();
            for(int col=0;col<N;col++){
                ro.add(0);
            }
            grid.add(ro);
        }
        grid.get(0).set(1,1);
        grid.get(0).set(2,1);
        printGrid(grid);
        System.out.println(canPlaceSecurityCameras(N,grid));
    }

    public static boolean canPlaceSecurityCameras(int N, List<List<Integer>> grid) {
        if (N <= 0) return true;
        boolean[] usedCol = new boolean[N];
        boolean[] usedD1 = new boolean[2 * N - 1];
        boolean[] usedD2 = new boolean[2 * N - 1];
        return dfs(N,0,usedCol,usedD1,usedD2,grid,0);
    }

    public static boolean dfs(int N, int row, boolean[] usedCol, boolean[] usedD1, boolean[] usedD2, List<List<Integer>> grid,int count) {
        if (count == N){
            printGrid(grid);
            return true;
        }
        for (int col = 0; col < N; col++) {
            if (grid.get(row).get(col) == 1) continue;
            int d1 = row - col + (N - 1);
            int d2 = row + col;
            if (usedCol[col] || usedD1[d1] || usedD2[d2]) continue;
            grid.get(row).set(col, 2);
            usedCol[col] = true;
            usedD1[d1] = true;
            usedD2[d2] = true;
            boolean result = dfs(N, row + 1, usedCol, usedD1, usedD2, grid,count+1);
            if(result) return true;
            grid.get(row).set(col, 0);
            usedCol[col] = false;
            usedD1[d1] = false;
            usedD2[d2] = false;
        }
        return false;
    }
    public static void printGrid(List<List<Integer>> grid){
        System.out.println();
        for(List<Integer> row:grid){
            for(Integer cell:row){
                System.out.print(cell);
                System.out.print(" ");
            }
            System.out.println();
        }
    }
}
