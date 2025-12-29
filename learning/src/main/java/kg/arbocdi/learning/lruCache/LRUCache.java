package kg.arbocdi.learning.lruCache;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class LRUCache {
    private final int cap;
    private final Map<Integer, Integer> map = new HashMap<>();
    private final LinkedHashSet<Integer> list = new LinkedHashSet<>();

    public LRUCache(int capacity) {
        cap = capacity;
    }

    // returns -1 if not found
    public synchronized int get(int key) {
        Integer val = map.get(key);
        if (val == null) return -1;
        list.remove(key);
        list.add(key);
        return val;
    }

    public synchronized void put(int key, int value) {
        if (list.size() == cap) {
            int lruKey = list.iterator().next();
            list.remove(lruKey);
            map.remove(lruKey);
        }
        list.add(key);
        map.put(key, value);
    }
}
