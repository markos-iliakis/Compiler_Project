import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class Offset {
    String className;
    Map<String, Integer> varOff;
    Map<String, Integer> methOff;

    public Offset(String className){
        this.className = className;
        varOff = new LinkedHashMap<>();
        methOff = new LinkedHashMap<>();
    }
}
