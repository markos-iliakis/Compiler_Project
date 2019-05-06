import syntaxtree.*;

import visitor.GJNoArguDepthFirst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class TCVisitor extends GJNoArguDepthFirst<String> {
    private ArrayList<String> h = new ArrayList<>();

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
    public String visit(MainClass c) throws Exception {
        h.add(c.f1.f0.tokenImage);
        h.add(c.f6.tokenImage);
        c.f15.accept(this);
        h.clear();
        return "null";
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    public String visit(ClassDeclaration c) throws Exception {
        h.add(c.f1.f0.tokenImage);
        c.f4.accept(this);
        h.clear();
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
    public String visit(ClassExtendsDeclaration c) throws Exception {
        h.add(c.f1.f0.tokenImage);
        c.f6.accept(this);
        h.clear();
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
    public String visit(MethodDeclaration m) throws Exception {
        h.add(m.f2.f0.tokenImage);
        m.f8.accept(this);
        String temp = m.f10.accept(this);
        String temp2 = getMethType();
        if(!temp.equals(temp2)){
            throw new MyException("Invalid Return Type in "+m.f2.f0.tokenImage);
        }
        h.remove(m.f2.f0.tokenImage);
        return temp2;
    }

    /**
     * Grammar production:
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration c) throws Exception {
        String type = c.f0.accept(this);
        if(!STVisitor.checkClassExists(type)){
            throw new MyException("Undefined type "+type);
        }
        return "null";
    }

    /**
     * Grammar production:
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public String visit(AssignmentStatement a) throws Exception {
//        check if types are different
        String temp = a.f0.accept(this);
        String temp2 = a.f2.accept(this);
        if((temp == null || !temp.equals(temp2)) && !STVisitor.isSubclassOf(temp2, temp)){
            String error = "Invalid Assignment in "+a.f0.f0.tokenImage;
            if(temp == null)
                error += " Variable does not exist";
            throw new MyException(error);
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
    public String visit(ArrayAssignmentStatement a) throws Exception {
//        check if identifier type is int array
        String id1 = a.f0.accept(this);
        if(!id1.equals("int[]")){
            throw new MyException("Invalid Type "+a.f0.f0.tokenImage);
        }

//        check if f2 and f5 are int
        String index = a.f2.accept(this);
        String id2 = a.f5.accept(this);
        if(!index.equals("int") || !id2.equals("int")){
            throw new MyException("Invalid Array Assignment in "+a.f0.f0.tokenImage);
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
    public String visit(IfStatement c) throws Exception {
//        check if f2 is bool
        String stmt = c.f2.accept(this);
        if(!stmt.equals("boolean")){
            throw new MyException("Invalid If statement");
        }

        c.f4.accept(this);
        c.f6.accept(this);

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
    public String visit(WhileStatement w) throws Exception {
//        check if f2 is bool
        String stmt = w.f2.accept(this);
        if(!stmt.equals("boolean")){
            throw new MyException("Invalid while statement");
        }
        w.f4.accept(this);

        return "null";
    }

    /**
     * Grammar production:
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
    public String visit(AndExpression a) throws Exception {
//        check if f0 anf f2 are bools
        String id1 = a.f2.accept(this);
        String id2 = a.f0.accept(this);
        if(!id1.equals("boolean") || !id2.equals("boolean")){
            throw new MyException("Invalid and statement");
        }

        return "boolean";
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression c) throws Exception {
//        check if f0 anf f2 are ints

        String s = c.f2.accept(this);
        String s1 = c.f0.accept(this);
        if(!s.equals("int") || !s1.equals("int")){
            throw new MyException("Invalid compare statement");
        }

        return "boolean";
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression c) throws Exception {
//        check if f0 anf f2 are ints
        String s = c.f2.accept(this);
        String s1 = c.f0.accept(this);
        if(!s.equals("int") || !s1.equals("int")){
            throw new MyException("Invalid Plus statement");
        }

        return "int";
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression c) throws Exception {
//        check if f0 anf f2 are ints
        String s = c.f2.accept(this);
        String s1 = c.f0.accept(this);
        if(!s.equals("int") || !s1.equals("int")){
            throw new MyException("Invalid Minus statement");
        }

        return "int";
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression c) throws Exception {
//        check if f0 anf f2 are ints
        String s = c.f2.accept(this);
        String s1 = c.f0.accept(this);
        if(!s.equals("int") || !s1.equals("int")){
            throw new MyException("Invalid Times statement");
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
    public String visit(ArrayLookup c) throws Exception {
//        check if f0 is of type array int
        String s = c.f0.accept(this);
        if(!s.equals("int[]")){
            throw new MyException("Invalid type, not an array type");
        }

//        check if f2 is of type int
        String s1 = c.f2.accept(this);
        if(!s1.equals("int")){
            throw new MyException("Invalid Type, not an int");
        }

        return "int";
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength c) throws Exception {
//        check if f0 is of type array int
        String s = c.f0.accept(this);
        if(!s.equals("int[]")){
            throw new MyException("Invalid type, not an int");
        }
        return "int";
    }

    /**
     * Grammar production:
     * f0 -> "!"
     * f1 -> Clause()
     */
    public String visit(NotExpression c) throws Exception {
//        check if f1 is bool
        String s = c.f1.accept(this);
        if(!s.equals("boolean")){
            throw new MyException("Invalid type, not a boolean");
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
    public String visit(ArrayAllocationExpression c) throws Exception {
//        check if f3 is int
        String s = c.f3.accept(this);
        if(!s.equals("int")){
            throw new MyException("Invalid type, not an int");
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
    public String visit(AllocationExpression c)throws Exception{

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
    public String visit(MessageSend c) throws Exception {
        String methodType;
        String functionObjectType = c.f0.accept(this);
        String functionName = c.f2.f0.tokenImage;

//        check if f0 has method f2
        if((methodType = checkMethod(functionObjectType, functionName)) == null){
            throw new MyException("Method "+functionName+" not found in class "+functionObjectType);
        }

//        check if arguments are correct(number - type)
        int argNum = STVisitor.getArgNum(functionObjectType, functionName);
        if(argNum > 0 && !c.f4.present()){
            throw new MyException("Method "+functionName+" need more arguments");
        }

//        check for arguments
        if(c.f4.present()) {
            h.add(functionObjectType);
            h.add(functionName);

            c.f4.accept(this);
        }

//        return the type of the function
        return methodType;
    }

    /**
     * Grammar production:
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public String visit(ExpressionList c) throws Exception {
        int argNum = STVisitor.getArgNum(h.get(2), h.get(3));
        ArrayList<String> correctArgs = STVisitor.getArgs(h.get(2), h.get(3));
        String methodName = h.get(3);
        h.remove(h.size()-1);
        h.remove(h.size()-1);

//        check the number of the arguments
        if(argNum != c.f1.f0.size() + 1){
            throw new MyException("Method "+methodName+" need more or less arguments, should be "+argNum +" and you have: "+ c.f1.f0.size()+1);
        }

//        check the type of the first argument
        String firstArgType = c.f0.accept(this);
        if(!correctArgs.get(0).equals(firstArgType) && ! STVisitor.isSubclassOf(firstArgType, correctArgs.get(0))){
            throw new MyException("Method "+methodName+" need first argument of type: "+correctArgs.get(0)+" but you have type: "+c.f0.accept(this));
        }

//        check the types of the rest arguments
        for (int i = 0; i<c.f1.f0.size(); i++){
            String type = c.f1.f0.elementAt(i).accept(this);
            if(!type.equals(correctArgs.get(i+1)) && !STVisitor.isSubclassOf(type, correctArgs.get(i+1))){
                throw new MyException("Method "+methodName+" need other argument types \n"+"Argument found: "+c.f1.f0.elementAt(i).accept(this)+" Argument needed: "+correctArgs.get(i+1));
            }
        }

        return "null";
    }

    /**
     * Grammar production:
     * f0 -> ( ExpressionTerm() )*
     */
    public String visit(ExpressionTail c) throws Exception {
        if(c.f0.present())
            return c.f0.accept(this);
        else
            return "null";
    }

    /**
     * Grammar production:
     * f0 -> ","
     * f1 -> Expression()
     */
    public String visit(ExpressionTerm c) throws Exception {
        return c.f1.accept(this);
    }

    /**
     * Grammar production:
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    public String visit(PrintStatement c) throws Exception {
        String s = c.f2.accept(this);
        if(!s.equals("boolean") && !s.equals("int")){
            throw new MyException("Can only print boolean or int");
        }

        return "null";
    }

    /**
     * Grammar production:
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier c) throws Exception{
        h.add(c.f0.tokenImage);
        String str = getVarType();
        h.remove(c.f0.tokenImage);
        return str;
    }

    public String visit(IntegerLiteral c)throws Exception{
        return "int";
    }

    public String visit(TrueLiteral c)throws Exception{
        return "boolean";
    }

    public String visit(FalseLiteral c)throws Exception{
        return "boolean";
    }

    public String visit(ThisExpression c)throws Exception{
        return h.get(0);
    }

    public String visit(BracketExpression c)throws Exception{
        return c.f1.accept(this);
    }

    private String getVarType(){
        if(h.size() == 3) return STVisitor.getVarType(h.get(0), h.get(1), h.get(2));
        else {
//            System.out.println("getVarType: wrong list");
            return null;
        }


    }

    private String getMethType(){
        return (String) STVisitor.getMethArray(h.get(0), h.get(1)).get(0);
    }

    private String checkMethod(String className, String method){
//        check if method exists in object's class or any of the parent classes
        if(STVisitor.getMethArray(className, method)!=null){
            return (String) STVisitor.getMethArray(className, method).get(0);
        }
        else if(STVisitor.getSuperClass(className) != null){
            return checkMethod(STVisitor.getSuperClass(className), method);
        }
        return null;
    }
}
