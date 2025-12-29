package kg.arbocdi.learning;

import java.util.HashMap;
import java.util.Map;

public class FindUniqueSymbolIndex {
    public int firstUniqChar(String s) {
        Map<Character, Integer> map = new HashMap<>();
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            Integer cnt = map.get(chars[i]);
            if (cnt == null) cnt = 0;
            cnt++;
            map.put(chars[i], cnt);
        }
        for (int i = 0; i < chars.length; i++) {
            Integer cnt = map.get(chars[i]);
            if (cnt == 1) return i;
        }
        return -1;
    }
}
