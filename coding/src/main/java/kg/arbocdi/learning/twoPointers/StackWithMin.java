package kg.arbocdi.learning.twoPointers;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class StackWithMin {

    public static void main(String[] args) {
        List<String> operations = Arrays.asList("push 2", "push 0", "push 3", "push 0", "getMin", "pop", "getMin", "pop", "top", "getMin");
        List<Integer> result = processCouponStackOperations(operations);
        System.out.println(result);
    }

    public static List<Integer> processCouponStackOperations(List<String> operations) {
        List<Integer> result = new LinkedList<>();
        for (String data : operations) {
            Operation operation = new Operation(data);
            if (operation.isPush()) {
                push(operation.value);
            }
            if (operation.isPop()) {
                pop();
            }
            if (operation.isTop()) {
                result.add(top());
            }
            if (operation.isMin()) {
                result.add(min());
            }
        }
        return result;
    }

    private static Deque<Integer> data = new LinkedList<>();
    private static Deque<Counter> mins = new LinkedList<>();

    public static void push(int i) {
        data.push(i);
        if (mins.isEmpty()) {
            mins.push(new Counter(i));
        } else {
            Counter min = mins.peek();
            if (i == min.value) min.count++;
            else if (i < min.value) mins.push(new Counter(i));
        }
    }

    public static int pop() {
        int i = data.pop();
        Counter min = mins.peek();
        if (min.value == i) {
            min.count--;
            if (min.count <= 0) mins.pop();
        }
        return i;
    }

    public static int top() {
        return data.peek();
    }

    public static int min() {
        return mins.peek().value;
    }

    public static class Operation {
        String operation;
        int value;

        public Operation(String data) {
            String[] op = data.split(" ");
            operation = op[0];
            if (op.length > 1) {
                value = Integer.parseInt(op[1]);
            }
        }

        boolean isPush() {
            return operation.equals("push");
        }

        boolean isPop() {
            return operation.equals("pop");
        }

        boolean isMin() {
            return operation.equals("getMin");
        }

        boolean isTop() {
            return operation.equals("top");
        }
    }

    public static class Counter {
        int value;
        int count;

        public Counter(int value) {
            this.value = value;
            count = 1;
        }
    }

}
