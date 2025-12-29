package kg.arbocdi.learning.tasks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProperBrackets {
    private static Set<Character> openBrackets = new HashSet<>();
    {
        openBrackets.add('(');
        openBrackets.add('{');
        openBrackets.add('[');
    }
    private static Map<Character,Character> pairBrackets = new HashMap<>();
    static {
        pairBrackets.put(')', '(');
        pairBrackets.put('}', '{');
        pairBrackets.put(']', '[');
    }

    public static boolean areBracketsProperlyMatched(String code_snippet) {
        char[] brackets=new char[code_snippet.length()];
        int bIndx = -1;
        for(char a:code_snippet.toCharArray()){
            if(openBrackets.contains(a)){
                bIndx++;
                brackets[bIndx]=a;
                System.out.println(bIndx);
            }
            Character openBracket = pairBrackets.get(a);
            if(openBracket!=null){
                if(bIndx<0||brackets[bIndx]!=openBracket){
                    return false;
                }else{
                    bIndx--;
                }
                System.out.println(bIndx);
            }

        }
        return bIndx==-1;

    }
}
