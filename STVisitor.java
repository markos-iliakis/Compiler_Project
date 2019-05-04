import jdk.nashorn.internal.runtime.arrays.ArrayIndex;
import syntaxtree.*;
import visitor.GJDepthFirst;

import java.lang.reflect.Array;
import java.util.*;

public class STVisitor extends GJDepthFirst<String, HashMap<String, ArrayList<Object>>> {

    static private HashMap<String, ArrayList<Object>> ClassMap = new HashMap<>();
    static private ArrayList<ArrayList<String>> order = new ArrayList<>(Arrays.asList(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
    static boolean flag = true;

    static private void orderAddclass(String className){
        order.get(0).add(className);
    }

    static private void orderAddvar(String varName){
        if(flag) order.get(1).add(varName);
    }

    static private void orderAddmeth(String methName){
        order.get(2).add(methName);
    }

//    static private void orderAddmethvar(String methvarName){
//        order.get(3).add(methvarName);
//    }

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

    public static boolean checkBaseClassFunction(String function, String className){
        String base;
        if(((HashMap<String, ArrayList<Object>>) ClassMap.get(className).get(2)).containsKey(function)){
            return true;
        }
        else if((base = getSuperClass(className)) != null){
            return checkBaseClassFunction(function, base);
        }
        return false;
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
//        System.out.println("ClassExt");

        if(ClassMap.containsKey(c.f1.f0.tokenImage)){
            System.out.println("Class "+c.f1.f0.tokenImage+" already exists");
            System.exit(-1);
        }

//        take the maps from children
        HashMap<String, ArrayList<Object>> VarMap = new HashMap<>();
        HashMap<String, ArrayList<Object>> MethMap = new HashMap<>();
        flag = true;
        c.f5.accept(this, VarMap);
        orderAddvar("null");
        flag = false;
        c.f6.accept(this, MethMap);
//        checkVirtuals(MethMap, c.f3.f0.tokenImage);

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
    public String visit(ClassDeclaration c, HashMap<String, ArrayList<Object>> h){
        if(ClassMap.containsKey(c.f1.f0.tokenImage)){
            System.out.println("Class "+c.f1.f0.tokenImage+" already exists");
            System.exit(-1);
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
        orderAddmeth(m.f2.f0.tokenImage);
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
    public String visit(FormalParameter f, HashMap<String, ArrayList<Object>> h){
//        System.out.println("Parameter Variable "+f.f1.f0.tokenImage+" "+f.f0.accept(this, h));
        if(h.containsKey(f.f1.f0.tokenImage)){
            System.out.println("Variable "+f.f1.f0.tokenImage+" already exists");
            System.exit(-1);
        }

        ArrayList<Object> a = new ArrayList<>();
        a.add(f.f0.accept(this, h));
        h.put(f.f1.f0.tokenImage, a);
//        System.out.println(f.f1.f0.tokenImage +" "+ a.get(0));
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