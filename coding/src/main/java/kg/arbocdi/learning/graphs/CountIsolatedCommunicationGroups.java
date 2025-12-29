package kg.arbocdi.learning.graphs;

import java.util.*;

public class CountIsolatedCommunicationGroups {
    public static void main(String[] args) {
        List<List<Integer>> links = new LinkedList<>();
        links.add(Arrays.asList(0, 1));
        links.add(Arrays.asList(3, 4));
        System.out.println(countIsolatedCommunicationGroups(links, 5));
    }

    public static int countIsolatedCommunicationGroups(List<List<Integer>> links, int n) {
        //build connections
        List<Node> nodes = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            nodes.add(new Node(i));
        }
        for (List<Integer> link : links) {
            Node left = nodes.get(link.get(0));
            Node right = nodes.get(link.get(1));
            left.addLink(right);
            right.addLink(left);
        }
        int count = 0;
        for (Node node : nodes) {
            if (node.visited) continue;
            count++;
            //bfs
            Queue<Node> q = new LinkedList<>();
            q.add(node);
            while (!q.isEmpty()) {
                Node nn = q.remove();
                if (nn.visited) {
                    continue;
                }
                nn.visited = true;
                q.addAll(nn.links);
            }
        }
        return count;
    }

    private static class Node {
        final int id;
        boolean visited;
        final List<Node> links = new LinkedList<>();

        private Node(int id) {
            this.id = id;
        }

        public void addLink(Node node) {
            links.add(node);
        }
    }
}
