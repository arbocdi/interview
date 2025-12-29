package kg.arbocdi.theory;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.UUID;

public class Cache {
    private final IdentityHashMap<Object, String> VALS = new IdentityHashMap<>();
    private final HashMap<String, Object> KEYS = new HashMap<>();

    public String add(Object o) {
        if (o == null) return null;
        String key = VALS.get(o);
        if (key == null) {
            key = UUID.randomUUID().toString();
            VALS.put(o, key);
            KEYS.put(key, o);
        }
        return key;
    }

    public Object get(String id) {
        if (id == null) return null;
        return KEYS.get(id);
    }
}
