package kg.arbocdi.learning.tasks;

public class CustomFibonacchi {
    public static void main(String[] args) {
        System.out.println(getAutoSaveInterval(3));
    }
    public static long getAutoSaveInterval(int n) {
        int n2=1;
        int n1=2;
        if(n==0) return n2;
        if(n==1) return n1;
        return dfs(n2,n1,0,n-2);
    }
    private static long dfs(long n2,long n1,int i,int n){
        long val = n1+n2;
        if(i==n) return val;
        return dfs(n1,val,i+1,n);
    }
}
