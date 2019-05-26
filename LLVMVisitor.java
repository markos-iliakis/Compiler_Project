import syntaxtree.*;
import visitor.GJNoArguDepthFirst;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class LLVMVisitor extends GJNoArguDepthFirst<String> {

    private enum Type{
        BOOLEAN ("i1"),
        CHAR ("i8"),
        CHARP ("i8*"),
        INT ("i32"),
        INTP ("i32*");

        String name;

        Type(String s){
            this.name = s;
        }

        static String getName(String s){
            try {
                return Type.valueOf(s.toUpperCase()).name;
            }catch (IllegalArgumentException e){
                return CHARP.name;
            }
        }
    }

    private static ArrayList<String> state = new ArrayList<>();
    private static FileWriter fw;
    private static int regCounter=-1;
    private static HashMap<String, String> varMap;

    static public String getNewVar(){
        regCounter++;
        return "%_" + regCounter;
    }

    static public void setFw(String str) throws Exception{
        fw=new FileWriter(str.replace(".java", ".ll"));
    }

    static public void unsetFw() throws Exception{
        fw.close();
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
    @Override
    public String visit(MainClass n) throws Exception {
        regCounter = 0;
        state.add("main");
        fw.write("declare i8* @calloc(i32, i32)\n" +
                "declare i32 @printf(i8*, ...)\n" +
                "declare void @exit(i32)\n" +
                "\n" +
                "@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n" +
                "@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n" +
                "define void @print_int(i32 %i) {\n" +
                "    %_str = bitcast [4 x i8]* @_cint to i8*\n" +
                "    call i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n" +
                "    ret void\n" +
                "}\n" +
                "\n" +
                "define void @throw_oob() {\n" +
                "    %_str = bitcast [15 x i8]* @_cOOB to i8*\n" +
                "    call i32 (i8*, ...) @printf(i8* %_str)\n" +
                "    call void @exit(i32 1)\n" +
                "    ret void\n" +
                "}\n" +
                "\n" +
                "define i32 @main() {\n"
        );

//        var declarations
        n.f14.accept(this);
//        statements
        n.f15.accept(this);

        fw.write("    ret i32 0\n" +
                "}\n"
        );
        state.remove("main");
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
    public String visit(ClassDeclaration n) throws Exception{
        System.err.println("in class");
        state.add(n.f1.f0.tokenImage);
        varMap = new HashMap<>();
//        go only to method declarations
        n.f4.accept(this);

        state.remove(n.f1.f0.tokenImage);
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
    @Override
    public String visit(ClassExtendsDeclaration n) throws Exception{
        System.err.println("in class extends");
        state.add(n.f1.f0.tokenImage);
        varMap = new HashMap<>();
//        go only to method declarations
        n.f6.accept(this);

        state.remove(n.f1.f0.tokenImage);
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
    @Override
    public String visit(MethodDeclaration n) throws Exception{
        System.err.println("in method");
        state.add(n.f2.f0.tokenImage);
        String methType = Type.getName(STVisitor.getMethType(state.get(0), state.get(1)));

        fw.write("define " +
                methType +
                "@"+ state.get(0) + "." + state.get(1) + "(i8* %this"
        );

//        write the arguments
        n.f4.accept(this);
        fw.write(") {\n");

//        parameter allocas
        ArrayList<String> argsName = STVisitor.getArgsName(state.get(0), state.get(1));
        ArrayList<String> argsType = STVisitor.getArgsType(state.get(0), state.get(1));
        Iterator i1 = argsName.iterator(), i2 = argsType.iterator();

        while (i1.hasNext() && i2.hasNext()){
            String argName = (String) i1.next(), argType = Type.getName((String) i2.next());
            varMap.put(argName, "%".concat(argName));
            fw.write("    %" + argName + " = alloca " + argType + "\n" +
                    "    store " + argType + " %." + argName + ", " + argType + "* %" + argName + "\n"
            );
        }

//        var declarations
        n.f7.accept(this);
//        statements
        n.f8.accept(this);

        fw.write("    ret " + methType + " ");

//        return value
        n.f10.accept(this);

        fw.write("\n}\n");

        state.remove(n.f2.f0.tokenImage);
        System.err.println("leaving method");
        return "null";
    }

    /**
     * Grammar production:
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n) throws Exception{
        System.err.println("in parameters");
        fw.write(", " + Type.getName(STVisitor.getVarType(state.get(0), state.get(1), n.f1.f0.tokenImage)) + " %." + n.f1.f0.tokenImage);
        System.err.println("leaving parameters");
        return "null";
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    @Override
    public String visit(VarDeclaration n) throws Exception {
        System.err.println("in vars");
        String varType = Type.getName(STVisitor.getVarType(state.get(0), state.get(1), n.f1.f0.tokenImage));
        fw.write("    %" + n.f1.f0.tokenImage + " = alloca " + varType + "\n");
        varMap.put(n.f1.f0.tokenImage, "%".concat(n.f1.f0.tokenImage));
        return "null";
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    @Override
    public String visit(AssignmentStatement n) throws Exception {

    }
//
//    /**
//     * f0 -> "if"
//     * f1 -> "("
//     * f2 -> Expression()
//     * f3 -> ")"
//     * f4 -> Statement()
//     * f5 -> "else"
//     * f6 -> Statement()
//     */
//    @Override
//    public String visit(IfStatement n) throws Exception {
//        return super.visit(n);
//    }

//    /**
//     * f0 -> "while"
//     * f1 -> "("
//     * f2 -> Expression()
//     * f3 -> ")"
//     * f4 -> Statement()
//     */
//    @Override
//    public String visit(WhileStatement n) throws Exception {
//        return super.visit(n);
//    }
//
//    /**
//     * f0 -> "System.out.println"
//     * f1 -> "("
//     * f2 -> Expression()
//     * f3 -> ")"
//     * f4 -> ";"
//     */
//    @Override
//    public String visit(PrintStatement n) throws Exception {
//        return super.visit(n);
//    }
//
//    /**
//     * f0 -> Clause()
//     * f1 -> "&&"
//     * f2 -> Clause()
//     */
//    @Override
//    public String visit(AndExpression n) throws Exception {
//        return super.visit(n);
//    }
//
//    /**
//     * f0 -> PrimaryExpression()
//     * f1 -> "<"
//     * f2 -> PrimaryExpression()
//     */
//    @Override
//    public String visit(CompareExpression n) throws Exception {
//        return super.visit(n);
//    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(PlusExpression n) throws Exception {
        String var = getNewVar();
        fw.write(var + " = add i32 " + varMap.get(n.f0.accept(this)) + ", " + varMap.get(n.f2.accept(this)));
        return var;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(MinusExpression n) throws Exception {
        String var = getNewVar();
        fw.write(var + " = sub i32 " + varMap.get(n.f0.accept(this)) + ", " + varMap.get(n.f2.accept(this)));
        return var;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(TimesExpression n) throws Exception {
        String var = getNewVar();
        fw.write(var + " = mul i32 " + varMap.get(n.f0.accept(this)) + ", " + varMap.get(n.f1.accept(this)));
        return var;
    }

//    /**
//     * f0 -> PrimaryExpression()
//     * f1 -> "["
//     * f2 -> PrimaryExpression()
//     * f3 -> "]"
//     */
//    @Override
//    public String visit(ArrayLookup n) throws Exception {
//        return super.visit(n);
//    }
//
//    /**
//     * f0 -> PrimaryExpression()
//     * f1 -> "."
//     * f2 -> "length"
//     */
//    @Override
//    public String visit(ArrayLength n) throws Exception {
//        return super.visit(n);
//    }
//
//    /**
//     * f0 -> PrimaryExpression()
//     * f1 -> "."
//     * f2 -> Identifier()
//     * f3 -> "("
//     * f4 -> ( ExpressionList() )?
//     * f5 -> ")"
//     */
//    @Override
//    public String visit(MessageSend n) throws Exception {
//        return super.visit(n);
//    }
//
    /**
     * f0 -> "true"
     */
    @Override
    public String visit(TrueLiteral n) throws Exception {
        fw.write("1");
        return "null";
    }

    /**
     * f0 -> "false"
     */
    @Override
    public String visit(FalseLiteral n) throws Exception {
        fw.write("0");
        return "null";
    }

//    /**
//     * f0 -> <IDENTIFIER>
//     */
//    @Override
//    public String visit(Identifier n) throws Exception {
//        return super.visit(n);
//    }
//
//    /**
//     * f0 -> "this"
//     */
//    @Override
//    public String visit(ThisExpression n) throws Exception {
//        return super.visit(n);
//    }
//
//    /**
//     * f0 -> "new"
//     * f1 -> "int"
//     * f2 -> "["
//     * f3 -> Expression()
//     * f4 -> "]"
//     */
//    @Override
//    public String visit(ArrayAllocationExpression n) throws Exception {
//        return super.visit(n);
//    }
//
//    /**
//     * f0 -> "!"
//     * f1 -> Clause()
//     */
//    @Override
//    public String visit(NotExpression n) throws Exception {
//        return super.visit(n);
//    }
//
//    /**
//     * f0 -> Identifier()
//     * f1 -> "["
//     * f2 -> Expression()
//     * f3 -> "]"
//     * f4 -> "="
//     * f5 -> Expression()
//     * f6 -> ";"
//     */
//    @Override
//    public String visit(ArrayAssignmentStatement n) throws Exception {
//        return super.visit(n);
//    }
//
//    /**
//     * f0 -> "new"
//     * f1 -> Identifier()
//     * f2 -> "("
//     * f3 -> ")"
//     */
//    @Override
//    public String visit(AllocationExpression n) throws Exception {
//        return super.visit(n);
//    }
}
