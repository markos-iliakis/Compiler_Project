import syntaxtree.*;
import visitor.GJDepthFirst;

import java.util.*;

public class STVisitor extends GJDepthFirst<String, HashMap<String, ArrayList<Object>>> {

    static private HashMap<String, ArrayList<Object>> ClassMap = new HashMap<>();
    static private int varIndex=0, methIndex=0;

    public static HashMap<String, ArrayList<Object>> getClassMap() {
        return ClassMap;
    }

    public static ArrayList<Object> getMethArray(String className, String method){
        if(ClassMap.get(className) == null) return null;
        if(((HashMap<String, ArrayList<Object>>) ClassMap.get(className).get(2)).get(method) == null) return null;
        return ((HashMap<String, ArrayList<Object>>) ClassMap.get(className).get(2)).get(method);
    }

    public static String getSuperClass(String className){
        if(ClassMap.get(className) == null) return null;
        return (String) ClassMap.get(className).get(0);
    }

    public static String checkClassVars(String className, String var){
        String str, upper;
        if((str = (String) (((HashMap<String, ArrayList<Object>>) ClassMap.get(className).get(1)).get(var).get(0))) != null){
            return str;
        }
        else if((upper = getSuperClass(className))!=null){
            return checkClassVars(upper, var);
        }
        return null;
    }

//    public static String checkArgs(){
//
//    }

    public static String getVarType(ArrayList<String> list){
        ArrayList<Object> methList;
        if((methList = getMethArray(list.get(0), list.get(1))) == null) return null;
        if(((HashMap<String, ArrayList<Object>>) methList.get(1)).get(list.get(2)) == null) {
            return checkClassVars(list.get(0), list.get(2));
        }
        return (String) ((HashMap<String, ArrayList<Object>>) methList.get(1)).get(list.get(2)).get(0);
    }

    public static void makeIndexes(){
        HashMap<String, ArrayList<Integer>> map = new HashMap<>();

        Iterator it = ClassMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();

//            Initialize new entry
            map.put((String) pair.getKey(), new ArrayList<Integer>(Arrays.asList(0,0)));

//            Find var offsets

//            Find method offsets

            System.out.println(pair.getKey() + "." + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    public String visit(ClassExtendsDeclaration c, HashMap<String, ArrayList<Object>> h){
        System.out.println("ClassExt");

        if(ClassMap.containsKey(c.f1.f0.tokenImage)){
            System.out.println("Class "+c.f1.f0.tokenImage+" already exists");
            System.exit(-1);
        }

//        take the maps from children
        HashMap<String, ArrayList<Object>> VarMap = new HashMap<>();
        HashMap<String, ArrayList<Object>> MethMap = new HashMap<>();
        c.f5.accept(this, VarMap);
        c.f6.accept(this, MethMap);
//        checkVirtuals(MethMap, c.f3.f0.tokenImage);

//        make the array list
        ArrayList<Object> a = new ArrayList<>();
        a.add(c.f3.f0.tokenImage);
        a.add(VarMap);
        a.add(MethMap);

//        insert
        ClassMap.put(c.f1.f0.tokenImage, a);
        return "";
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    public String visit(ClassDeclaration c, HashMap<String, ArrayList<Object>> h){
        if(ClassMap.containsKey(c.f1.f0.tokenImage)){
            System.out.println("Class "+c.f1.f0.tokenImage+" already exists");
            System.exit(-1);
        }

//        take the maps from children
        HashMap<String, ArrayList<Object>> VarMap = new HashMap<>();
        HashMap<String, ArrayList<Object>> MethMap = new HashMap<>();
        c.f3.accept(this, VarMap);
        c.f4.accept(this, MethMap);

//        make the array list
        ArrayList<Object> a = new ArrayList<>();
        a.add(null);
        a.add(VarMap);
        a.add(MethMap);

//        insert
        ClassMap.put(c.f1.f0.tokenImage, a);
        return "";
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    public String visit(MethodDeclaration m, HashMap<String, ArrayList<Object>> h){
//        System.out.println("Method "+m.f2.f0.tokenImage+" "+m.f1.accept(this, h));
        if(h.containsKey(m.f2.f0.tokenImage)){
            System.out.println("Method "+m.f2.f0.tokenImage+" already exists");
            System.exit(-1);
        }

//        take the map of the children
        HashMap<String, ArrayList<Object>> VarMap = new HashMap<>();
        m.f4.accept(this, VarMap);
        int count = VarMap.size();
        m.f7.accept(this, VarMap);

//        make the array list
        ArrayList<Object> a = new ArrayList<>();
        a.add(m.f1.accept(this, h));
        a.add(VarMap);
        a.add(count);

//        insert
        h.put(m.f2.f0.tokenImage, a);
        return "";
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration v, HashMap<String, ArrayList<Object>> h){
//        System.out.println("Variable "+v.f1.f0.tokenImage+" "+v.f0.accept(this, h));
        if(h.containsKey(v.f1.f0.tokenImage)){
            System.out.println("Variable "+v.f1.f0.tokenImage+" already exists");
            System.exit(-1);
        }
        else {
            ArrayList<Object> a = new ArrayList<>();
            a.add(v.f0.accept(this, h));
            h.put(v.f1.f0.tokenImage, a);
        }
        return "";
    }

    /**
     * Grammar production:
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public String visit(FormalParameter f, HashMap<String, ArrayList<Object>> h){
//        System.out.println("Parameter Variable "+f.f1.f0.tokenImage+" "+f.f0.accept(this, h));
        if(h.containsKey(f.f1.f0.tokenImage)){
            System.out.println("Variable "+f.f1.f0.tokenImage+" already exists");
            System.exit(-1);
        }

        ArrayList<Object> a = new ArrayList<>();
        a.add(f.f0.accept(this, h));
        h.put(f.f1.f0.tokenImage, a);
        System.out.println(f.f1.f0.tokenImage +" "+ a.get(0));
        return "";
    }

    /**
     * Grammar production:
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(ArrayType c, HashMap<String, ArrayList<Object>> h){
        return "int[]";
    }

    /**
     * Grammar production:
     * f0 -> "int"
     */
    public String visit(IntegerType c, HashMap<String, ArrayList<Object>> h){
        return "int";
    }

    /**
     * Grammar production:
     * f0 -> "boolean"
     */
    public String visit(BooleanType c, HashMap<String, ArrayList<Object>> h){
        return "boolean";
    }

    /**
     * Grammar production:
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier c, HashMap<String, ArrayList<Object>> h){
        return c.f0.tokenImage;
    }

//    public int checkVirtuals(HashMap<String, ArrayList<Object>> MethMap, String superClass){
//        while(superClass != null){
//
//            superClass = (String) ClassMap.get(superClass).get(0);
//        }
//    }
}