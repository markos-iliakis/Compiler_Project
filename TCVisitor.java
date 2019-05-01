import syntaxtree.ClassDeclaration;
import syntaxtree.Identifier;
import syntaxtree.MethodDeclaration;

import syntaxtree.VarDeclaration;
import visitor.GJVoidDepthFirst;

import java.util.ArrayList;
import java.util.HashMap;

public class TCVisitor extends GJVoidDepthFirst<HashMap<String, ArrayList<Object>>> {

    public void visit(ClassDeclaration c, HashMap<String, ArrayList<Object>> h){
        ArrayList<Object> currentAr = h.get(c.f1.f0.tokenImage);

    }

    public void visit(MethodDeclaration m, HashMap<String, ArrayList<Object>> h) {
        System.out.println("Method");

    }
    public void visit(VarDeclaration v, HashMap<String, ArrayList<Object>> h) {
        System.out.println("Variable");
        ArrayList<Object> a = new ArrayList<>();
        a.add(v.f0);
        h.put(v.f1, a);
    }
}