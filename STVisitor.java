import jdk.nashorn.internal.runtime.arrays.ArrayIndex;
import syntaxtree.*;
import visitor.GJDepthFirst;

import java.lang.reflect.Array;
import java.util.*;

public class STVisitor extends GJDepthFirst<String, HashMap<String, ArrayList<Object>>> {

    static private HashMap<String, ArrayList<Object>> ClassMap;
    static private ArrayList<ArrayList<String>> order = new ArrayList<>(Arrays.asList(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
    private static boolean flag = false;
    private static ArrayList<String> args;

    static private void orderAddclass(String className){
        order.get(0).add(className);
    }

    static private void orderAddvar(String varName){
        if(flag) order.get(1).add(varName);
    }

    static private void orderAddmeth(String methName){
        order.get(2).add(methName);
    }

    static public void deleteClassMap(){
        ClassMap = null;
    }

    static public void newClassMap(){
        ClassMap = new HashMap<>();
    }

    public static int getArgNum(String className, String methName){
        if(getMethArray(className, methName) != null)
            return ((ArrayList<String>) getMethArray(className, methName).get(2)).size();
        else
            return getArgNum(getSuperClass(className), methName);
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

    public static boolean checkBaseClassFunction(String function, String className){
        String base;
        if(((HashMap<String, ArrayList<Object>>) ClassMap.get(className).get(2)).containsKey(function))
            return true;
        else if((base = getSuperClass(className)) != null)
            return checkBaseClassFunction(function, base);
        else
            return false;
    }

    public static String checkClassVars(String className, String var){
        String upper;
        ArrayList<Object> str;
        if((str = (((HashMap<String, ArrayList<Object>>) ClassMap.get(className).get(1)).get(var))) != null)
            return (String) str.get(0);

        else if((upper = getSuperClass(className))!=null)
            return checkClassVars(upper, var);

        else
            return null;
    }

    public static ArrayList<String> getArgs(String className, String method){
        return (ArrayList<String>) getMethArray(className, method).get(2);
    }

    private static void checkVirtualization(HashMap<String, ArrayList<Object>> methMap, String baseClass){
        Iterator it = methMap.entrySet().iterator();
        String base;

//        for every method
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();

//            if number of arguments are different
            if(((HashMap<String, ArrayList<Object>>) ClassMap.get(baseClass).get(2)).containsKey(pair.getKey())){
                if(((ArrayList<String>) methMap.get(pair.getKey()).get(2)).size() != ((ArrayList<String>) getMethArray(baseClass, (String) pair.getKey()).get(2)).size()){
                    System.out.println("Virtualization failed");
                    System.exit(-1);
                }

//                for every argument check types
                Iterator it2 = ((ArrayList<String>) methMap.get(pair.getKey()).get(2)).iterator();
                Iterator it3 = getArgs(baseClass, (String) pair.getKey()).iterator();
                while(it2.hasNext() && it3.hasNext()){
                    String type1 = (String) it2.next();
                    String type2 = (String) it3.next();
                    if(!type1.equals(type2)){
                        System.out.println("Virtualization failed");
                        System.exit(-1);
                    }
//                    it2.remove();
                }

            }else if((base = getSuperClass(baseClass))!=null){
                checkVirtualization(methMap, base);
            }

        }
    }

    public static String getVarType(String className, String methName, String varName){
        ArrayList<Object> methList;
        if((methList = getMethArray(className, methName)) == null) return null;
        if(((HashMap<String, ArrayList<Object>>) methList.get(1)).get(varName) == null) {
            return checkClassVars(className, varName);
        }
        return (String) ((HashMap<String, ArrayList<Object>>) methList.get(1)).get(varName).get(0);
    }

    public static boolean checkClassExists(String className){
        return ClassMap.containsKey(className);
    }

    public static boolean isSubclassOf(String subClassName, String superClassName){
        if(ClassMap.get(subClassName) == null || ClassMap.get(subClassName).get(0) == null)
            return false;
        else if(!getSuperClass(subClassName).equals(superClassName))
            return isSubclassOf(getSuperClass(subClassName), superClassName);
        else
            return true;
    }

    public static void makeIndexes(){
        HashMap<String, ArrayList<Integer>> map = new HashMap<>();

        Iterator it = order.get(0).iterator();
        while (it.hasNext()) {

            String className = (String) it.next();

//            Initialize new entry
            map.put(className, new ArrayList<>(Arrays.asList(0,0)));

//            Find base class var and method offsets
            int baseVarOffset = 0, baseMethodOffset = 0;;
            String base;
            if((base = (String) ClassMap.get(className).get(0)) != null){
                baseVarOffset = map.get(base).get(0);
                baseMethodOffset = map.get(base).get(1);
                map.get(className).set(0, baseVarOffset);
                map.get(className).set(1, baseMethodOffset);
            }

//            Find var offsets
            Iterator it2 = order.get(1).iterator();
            while(it2.hasNext()){

                String varName = (String) it2.next();
                if (varName.equals("null")){
                    it2.remove();
                    break;
                }

//                bytes to raise
                String type = (String) ((HashMap<String, ArrayList<Object>>) ClassMap.get(className).get(1)).get(varName).get(0);
                int bytes = baseVarOffset;
                if(type.equals("int")) bytes += 4;
                else if(type.equals("boolean")) bytes += 1;
                else bytes += 8;

//                print offsets
                System.out.println(className + "." + varName + " : " + map.get(className).get(0));

//                raise the VarIndex counter
                map.get(className).set(0, map.get(className).get(0) + bytes);

                it2.remove();
            }

//            Find method offsets
            Iterator it3 = order.get(2).iterator();
            while(it3.hasNext()){

                String methName = (String) it3.next();
                if(methName.equals("null")){
                    it3.remove();
                    break;
                }

//                check if the method already exists in base class and skip it
                if(base != null && checkBaseClassFunction(methName, base)) continue;
//                bytes to raise
                int bytes = baseMethodOffset + 8;

//                print offsets
                System.out.println(className + "." + methName + " : " + map.get(className).get(1));

//                raise the MethodIndex counter
                map.get(className).set(1, map.get(className).get(1) + bytes);

                it3.remove();
            }

            it.remove();
        }
    }

    /**
     * Grammar production:
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    public String visit(MainClass c, HashMap<String, ArrayList<Object>> h) throws Exception {

        HashMap<String, ArrayList<Object>>methMap = new HashMap<>();

        flag = true;
        orderAddclass(c.f1.f0.tokenImage);
        orderAddvar("null");
        orderAddmeth("null");
        flag = false;

//        take the map of the children
        HashMap<String, ArrayList<Object>> VarMap = new HashMap<>();
        args = new ArrayList<>();
        ArrayList<Object> temp = new ArrayList<>();
        temp.add(c.f1.f0.tokenImage);
        VarMap.put(c.f11.f0.tokenImage, temp);
        c.f14.accept(this, VarMap);

//        make the array list
        ArrayList<Object> a = new ArrayList<>();

        a.add("void");
        a.add(VarMap);

//        insert
        methMap.put("main", a);

//        make the array list
        ArrayList<Object> a2 = new ArrayList<>();
        a2.add(null);
        a2.add(null);
        a2.add(methMap);

//        insert
        ClassMap.put(c.f1.f0.tokenImage, a2);

        return "null";
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
    public String visit(ClassExtendsDeclaration c, HashMap<String, ArrayList<Object>> h) throws Exception {

        if(ClassMap.containsKey(c.f1.f0.tokenImage)){
            throw new MyException("Class "+c.f1.f0.tokenImage+" already exists");
        }

        if(!ClassMap.containsKey(c.f3.f0.tokenImage)){
            throw new MyException("Base Class "+c.f3.f0.tokenImage+" not exists");
        }

//        take the maps from children
        HashMap<String, ArrayList<Object>> VarMap = new HashMap<>();
        HashMap<String, ArrayList<Object>> MethMap = new HashMap<>();
        flag = true;
        c.f5.accept(this, VarMap);
        orderAddvar("null");
        flag = false;
        c.f6.accept(this, MethMap);
        checkVirtualization(MethMap, c.f3.f0.tokenImage);

//        make the array list
        ArrayList<Object> a = new ArrayList<>();
        a.add(c.f3.f0.tokenImage);
        a.add(VarMap);
        a.add(MethMap);

//        insert
        ClassMap.put(c.f1.f0.tokenImage, a);
        orderAddclass(c.f1.f0.tokenImage);
        orderAddmeth("null");
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
    public String visit(ClassDeclaration c, HashMap<String, ArrayList<Object>> h) throws Exception{
        if(ClassMap.containsKey(c.f1.f0.tokenImage)){
            throw new MyException("Class "+c.f1.f0.tokenImage+" already exists");
        }

//        take the maps from children
        HashMap<String, ArrayList<Object>> VarMap = new HashMap<>();
        HashMap<String, ArrayList<Object>> MethMap = new HashMap<>();
        flag = true;
        c.f3.accept(this, VarMap);
        orderAddvar("null");
        flag = false;
        c.f4.accept(this, MethMap);

//        make the array list
        ArrayList<Object> a = new ArrayList<>();
        a.add(null);
        a.add(VarMap);
        a.add(MethMap);

//        insert
        ClassMap.put(c.f1.f0.tokenImage, a);
        orderAddclass(c.f1.f0.tokenImage);
        orderAddmeth("null");
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
    public String visit(MethodDeclaration m, HashMap<String, ArrayList<Object>> h) throws Exception{

        if(h.containsKey(m.f2.f0.tokenImage)){
            throw new MyException("Method "+m.f2.f0.tokenImage+" already exists");
        }

//        take the map of the children
        HashMap<String, ArrayList<Object>> VarMap = new HashMap<>();
        args = new ArrayList<>();
        m.f4.accept(this, VarMap);
        int count = VarMap.size();
        m.f7.accept(this, VarMap);

//        make the array list
        ArrayList<Object> a = new ArrayList<>();


        a.add(m.f1.accept(this, h));
        a.add(VarMap);
        a.add(args);

//        insert
        h.put(m.f2.f0.tokenImage, a);
        orderAddmeth(m.f2.f0.tokenImage);
        return "";
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration v, HashMap<String, ArrayList<Object>> h) throws Exception {
        if(h.containsKey(v.f1.f0.tokenImage)){
            throw new MyException("Variable "+v.f1.f0.tokenImage+" already exist");
        }

        ArrayList<Object> a = new ArrayList<>();
        a.add(v.f0.accept(this, h));
        h.put(v.f1.f0.tokenImage, a);
        orderAddvar(v.f1.f0.tokenImage);
        return "";
    }

    /**
     * Grammar production:
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public String visit(FormalParameter f, HashMap<String, ArrayList<Object>> h) throws Exception{
        if(h.containsKey(f.f1.f0.tokenImage)){
            throw new MyException("Argument "+f.f1.f0.tokenImage+" already exist");
        }

        ArrayList<Object> a = new ArrayList<>();
        a.add(f.f0.accept(this, h));
        args.add((String) a.get(0));
        h.put(f.f1.f0.tokenImage, a);
        return "";
    }

    /**
     * Grammar production:
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(ArrayType c, HashMap<String, ArrayList<Object>> h) throws Exception{
        return "int[]";
    }

    /**
     * Grammar production:
     * f0 -> "int"
     */
    public String visit(IntegerType c, HashMap<String, ArrayList<Object>> h) throws Exception{
        return "int";
    }

    /**
     * Grammar production:
     * f0 -> "boolean"
     */
    public String visit(BooleanType c, HashMap<String, ArrayList<Object>> h) throws Exception{
        return "boolean";
    }

    /**
     * Grammar production:
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier c, HashMap<String, ArrayList<Object>> h) throws Exception{
        return c.f0.tokenImage;
    }
}