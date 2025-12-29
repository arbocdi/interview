package kg.arbocdi.learning.graphs;

import java.util.*;

public class CycleDetector {
    public static void main(String[] args) {
        CycleDetector detector = new CycleDetector();
        List<List<Integer>> nodes = new LinkedList<>();
        nodes.add(Arrays.asList(1, 2, 3));//0
        nodes.add(List.of());//1
        nodes.add(Arrays.asList(4));//2
        nodes.add(Arrays.asList(4));//3
        nodes.add(Arrays.asList(5, 6));//4
        nodes.add(Arrays.asList());//5
        nodes.add(Arrays.asList());//6

        System.out.println(detector.detectCycle(nodes));
    }

    public boolean detectCycle(List<List<Integer>> data) {
        Map<Integer, Node> nodes = new HashMap<>();
        for (int i = 0; i < data.size(); i++) {
            Node node = nodes.computeIfAbsent(i, Node::new);
            for (int n : data.get(i)) {
                node.addChild(nodes.computeIfAbsent(n, Node::new));
            }
        }
        //dfs time complexity O(n+m) < O(n^2)
        //visits node once
        //marking visited nodes
        for (Node r : nodes.values()) {
            if (r.visited) continue;
            if (dfs(r, new HashSet<>())) return true;
        }
        return false;
    }

    private boolean dfs(Node r, Set<Node> path) {
        if(path.contains(r)) return true;
        if (r.visited) return false;
        r.visited = true;
        path.add(r);
        for (Node n : r.getChildren()) {
            if(dfs(n, path)) return true;
        }
        path.remove(r);
        return false;
    }

    public static class Node {
        private final int index;
        public boolean visited;
        private final Set<Node> children = new HashSet<>();


        public Node(int index) {
            this.index = index;
        }

        public void addChild(Node c) {
            children.add(c);
        }

        public Collection<Node> getChildren() {
            return children;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return index == node.index;
        }

        @Override
        public int hashCode() {
            return index;
        }

        @Override
        public String toString() {
            return "Node{" +
                    ", index=" + index +
                    ", visited=" + visited +
                    '}';
        }
    }
}
