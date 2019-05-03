import syntaxtree.*;

import visitor.GJNoArguDepthFirst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class TCVisitor extends GJNoArguDepthFirst<String> {
    private ArrayList<String> h = new ArrayList<>();
    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    public String visit(ClassDeclaration c){
        System.out.println("Class");
        h.add(c.f1.f0.tokenImage);
        c.f4.accept(this);
        h.remove(c.f1.f0.tokenImage);
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
    public String visit(ClassExtendsDeclaration c){
        System.out.println("Class Extends");
        h.add(c.f1.f0.tokenImage);
        c.f6.accept(this);
        h.remove(c.f1.f0.tokenImage);
        return "null";
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
    public String visit(MethodDeclaration m) {
        System.out.println("Method");
        h.add(m.f2.f0.tokenImage);
        m.f8.accept(this);
        String temp = m.f10.accept(this);
        String temp2 = m.f2.accept(this);
        if(!temp.equals(temp2)){
            System.out.println("Invalid Return Type in "+m.f2.f0.tokenImage);
            System.exit(-2);
        }
        h.remove(m.f2.f0.tokenImage);
        return temp2;
    }

    /**
     * Grammar production:
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public String visit(AssignmentStatement a){
        System.out.println("Assignment Statement");
//        check if types are different
        String temp = a.f0.accept(this);
        if(!temp.equals(a.f2.accept(this))){
            System.out.println("Invalid Assignment in "+a.f0.f0.tokenImage);
            System.exit(-2);
        }
        return temp;
    }

    /**
     * Grammar production:
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */
    public String visit(ArrayAssignmentStatement a){
        System.out.println("ArrayAssignmentStatement");
//        check if identifier type is int array
        if(!a.f0.accept(this).equals("int[]")){
            System.out.println("Invalid Type "+a.f0.f0.tokenImage);
        }

//        check if f2 and f5 are int
        if(!a.f2.accept(this).equals("int") || !a.f5.accept(this).equals("int")){
            System.out.println("Invalid Array Assignment in "+a.f0.f0.tokenImage);
            System.exit(-2);
        }
        return "int";
    }

    /**
     * Grammar production:
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
    public String visit(IfStatement i){
//        check if f2 is bool
        if(!i.f2.accept(this).equals("boolean")){
            System.out.println("Invalid If statement");
            System.exit(-2);
        }
        return "null";
    }

    /**
     * Grammar production:
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public String visit(WhileStatement w){
//        check if f2 is bool
        if(!w.f2.accept(this).equals("boolean")){
            System.out.println("Invalid while statement");
            System.exit(-2);
        }
        return "null";
    }

    /**
     * Grammar production:
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
    public String visit(AndExpression a){
//        check if f0 anf f2 are bools
        if(!a.f2.accept(this).equals("boolean") || !a.f0.accept(this).equals("boolean")){
            System.out.println("Invalid and statement");
            System.exit(-2);
        }

        return "boolean";
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression c){
//        check if f0 anf f2 are ints
        if(!c.f2.accept(this).equals("int") || !c.f0.accept(this).equals("int")){
            System.out.println("Invalid compare statement");
            System.exit(-2);
        }
        return "boolean";
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression c){
//        check if f0 anf f2 are ints
        if(!c.f2.accept(this).equals("int") || !c.f0.accept(this).equals("int")){
            System.out.println("Invalid compare statement");
            System.exit(-2);
        }
        return "int";
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression c){
//        check if f0 anf f2 are ints
        if(!c.f2.accept(this).equals("int") || !c.f0.accept(this).equals("int")){
            System.out.println("Invalid compare statement");
            System.exit(-2);
        }
        return "int";
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression c){
//        check if f0 anf f2 are ints
        if(!c.f2.accept(this).equals("int") || !c.f0.accept(this).equals("int")){
            System.out.println("Invalid compare statement");
            System.exit(-2);
        }
        return "int";
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public String visit(ArrayLookup c){
//        check if f0 is of type array int
        if(!c.f0.accept(this).equals("int[]")){
            System.out.println("Invalid type");
            System.exit(-2);
        }

//        check if f2 is of type int
        if(!c.f2.accept(this).equals("int")){
            System.out.println("Invalid type");
            System.exit(-2);
        }
        return "int";
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength c){
//        check if f0 is of type array int
        if(!c.f0.accept(this).equals("int[]")){
            System.out.println("Invalid type");
            System.exit(-2);
        }
        return "int";
    }

    /**
     * Grammar production:
     * f0 -> "!"
     * f1 -> Clause()
     */
    public String visit(NotExpression c){
//        check if f1 is bool
        if(!c.f1.accept(this).equals("boolean")){
            System.out.println("Invalid type");
            System.exit(-2);
        }
        return "boolean";
    }

    /**
     * Grammar production:
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(ArrayAllocationExpression c){
        System.out.println("ArrayAllocationExpression");

//        check if f3 is int
        if(!c.f3.accept(this).equals("int")){
            System.out.println("Invalid type");
            System.exit(-2);
        }
        return "int[]";
    }

    /**
     * Grammar production:
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public String visit(AllocationExpression c){
        System.out.println("AllocationExpression");

//        just return type
        return c.f1.f0.tokenImage;
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    public String visit(MessageSend c){
        System.out.println("MessageSend");
//        check if f0 has method f2

    }

    /**
     * Grammar production:
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier c){
        h.add(c.f0.tokenImage);
        String str = getType();
        h.remove(c.f0.tokenImage);
        return str;
    }

    private String getType(){

        if(h.size()!=3) return "null";
        for (String s : h) {
            System.out.println(s);
        }
//        ArrayList<Object> map =((HashMap<String, ArrayList<Object>>) STVisitor.getClassMap().get(h.get(0)).get(2)).get(h.get(1));
        return ((String) (((HashMap<String, ArrayList<Object>>) ((HashMap<String, ArrayList<Object>>) STVisitor.getClassMap().get(h.get(0)).get(2)).get(h.get(1)).get(1)).get(h.get(2)).get(0)));
    }
}
