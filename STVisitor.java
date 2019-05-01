import syntaxtree.*;
import visitor.GJVoidDepthFirst;

import java.util.ArrayList;
import java.util.HashMap;

public class STVisitor extends GJVoidDepthFirst<HashMap<String, ArrayList<Object>>> {

    static private HashMap<String, ArrayList<Object>> ClassMap = new HashMap<>();

    public static HashMap<String, ArrayList<Object>> getClassMap() {
        return ClassMap;
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
    public void visit(ClassExtendsDeclaration c, HashMap<String, ArrayList<Object>> h){
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
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    public void visit(ClassDeclaration c, HashMap<String, ArrayList<Object>> h){
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
    public void visit(MethodDeclaration m, HashMap<String, ArrayList<Object>> h){
        if(h.containsKey(m.f2.f0.tokenImage) && h.get(m.f2.f0.tokenImage).contains(m.f1.f0.which)){
            System.out.println("Method "+m.f2.f0.tokenImage+" already exists");
            System.exit(-1);
        }

//        take the map of the children
        HashMap<String, ArrayList<Object>> VarMap = new HashMap<>();
        m.f4.accept(this, VarMap);
        m.f7.accept(this, VarMap);

//        make the array list
        ArrayList<Object> a = new ArrayList<>();
        a.add(m.f1.f0.which);
        a.add(VarMap);

//        insert
        h.put(m.f2.f0.tokenImage, a);
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public void visit(VarDeclaration v, HashMap<String, ArrayList<Object>> h){
        if(h.containsKey(v.f1.f0.tokenImage)){
            System.out.println("Variable "+v.f1.f0.tokenImage+" already exists");
            System.exit(-1);
        }
        else {
            ArrayList<Object> a = new ArrayList<>();
            a.add(v.f0.f0.which);
            h.put(v.f1.f0.tokenImage, a);
        }
    }

    public void visit(FormalParameter f, HashMap<String, ArrayList<Object>> h){
        if(h.containsKey(f.f1.f0.tokenImage)){
            System.out.println("Variable "+f.f1.f0.tokenImage+" already exists");
            System.exit(-1);
        }
        else {
            ArrayList<Object> a = new ArrayList<>();
            a.add(f.f0.f0.which);
            h.put(f.f1.f0.tokenImage, a);
        }
    }

//    public int checkVirtuals(HashMap<String, ArrayList<Object>> MethMap, String superClass){
//        while(superClass != null){
//
//            superClass = (String) ClassMap.get(superClass).get(0);
//        }
//    }
}