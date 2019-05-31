import syntaxtree.*;
import visitor.GJNoArguDepthFirst;

import java.io.FileWriter;
import java.util.*;

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
    private static int regCounter=-1, ifCounter=-1, loopCounter=-1, arrCounter=-1, oobCounter=-1, andCounter;

    private static ArrayList<String> argRegs = new ArrayList<>();

    static public String getNewVar(){
        regCounter++;
        return "%_" + regCounter;
    }

    static public String getNewIf(){
        ifCounter++;
        return "if" + ifCounter;
    }

    static public String getNewLoop(){
        loopCounter++;
        return "loop" + loopCounter;
    }

    static public String getNewArr(){
        arrCounter++;
        return "arr_alloc" + arrCounter;
    }

    static public String getNewOob(){
        oobCounter++;
        return "oob" + oobCounter;
    }

    static public String getNewAnd(){
        andCounter++;
        return "andclause" + andCounter;
    }

    static public void setFw(String str) throws Exception{
        fw=new FileWriter(str.replace(".java", ".ll"));
//        fw = new FileWriter(str);
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
        regCounter = -1;
        ifCounter = -1;
        state.add(n.f1.f0.tokenImage);
        state.add("main");
        makeVtable();
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
        state.remove(n.f1.f0.tokenImage);
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
        //System.err.println("in class");
        state.add(n.f1.f0.tokenImage);
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
        //System.err.println("in class extends");
        state.add(n.f1.f0.tokenImage);
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
        regCounter = -1;
        //System.err.println("in method");
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
            fw.write("    %" + argName + " = alloca " + argType + "\n" +
                    "    store " + argType + " %." + argName + ", " + argType + "* %" + argName + "\n"
            );
        }

//        var declarations
        n.f7.accept(this);
        fw.write("\n");
//        statements
        n.f8.accept(this);

        //        return value
        String reg = n.f10.accept(this);

        fw.write("\n\n    ret " + methType + " " + reg + "\n}\n");

        state.remove(n.f2.f0.tokenImage);
        //System.err.println("leaving method");
        return "null";
    }

    /**
     * Grammar production:
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n) throws Exception{
        //System.err.println("in parameters");
        fw.write(", " + Type.getName(STVisitor.getVarType(state.get(0), state.get(1), n.f1.f0.tokenImage)) + " %." + n.f1.f0.tokenImage);
        return "null";
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    @Override
    public String visit(VarDeclaration n) throws Exception {
        //System.err.println("in vars");
        String varName = n.f1.f0.tokenImage;
        String varType = Type.getName(STVisitor.getVarType(state.get(0), state.get(1), varName));

        fw.write("    %" + n.f1.f0.tokenImage + " = alloca " + varType + "\n");

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
        String reg = n.f2.accept(this);
        String varName = n.f0.f0.tokenImage;
        String type = Type.getName(STVisitor.getVarType(state.get(0), state.get(1), varName));
        if(STVisitor.checkClassVars(state.get(0), varName) == null) {
            fw.write("      store " + type + " " + reg + ", " + type + "* %" + varName + "\n\n");
        }else{
            String reg1 = getNewVar();
            String reg2 = getNewVar();
            int off = STVisitor.offsets.get(state.get(0)).varOff.get(varName) + 8;

            fw.write("      " + reg1 + " = getelementptr i8, i8* %this, i32 " + off + "\n" +
                    "       " + reg2 + " = bitcast i8* " + reg1 + " to " + type + "*\n" +
                    "      store " + type + " " + reg + ", " + type + "* " + reg2 + "\n\n"
            );
        }
        return "null";
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    @Override
    public String visit(PrintStatement n) throws Exception {
        String reg = n.f2.accept(this);
        fw.write("      call void (i32) @print_int(i32 "+  reg +")\n\n");
        return "null";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(CompareExpression n) throws Exception {
        String reg1 = getNewVar();
        String ident1 = n.f0.accept(this);
        String ident2 = n.f2.accept(this);

        fw.write("      " + reg1 + " = icmp slt i32 "+  ident1 +", " + ident2 + "\n");
        return reg1;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(PlusExpression n) throws Exception {
        String var = getNewVar();
        String reg1 = n.f0.accept(this);
        String reg2 = n.f2.accept(this);
        fw.write("      " + var + " = add i32 " + reg1 + ", " + reg2 + "\n");
        return var;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(MinusExpression n) throws Exception {
        String reg1 = n.f0.accept(this);
        String reg2 = n.f2.accept(this);
        String var = getNewVar();
        fw.write("      " + var + " = sub i32 " + reg1 + ", " + reg2 + "\n");
        return var;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(TimesExpression n) throws Exception {
        String reg1 = n.f0.accept(this);
        String reg2 = n.f2.accept(this);
        String var = getNewVar();
        fw.write("      " + var + " = mul i32 " + reg1 + ", " + reg2 + "\n");
        return var;
    }

    /**
     * f0 -> "true"
     */
    @Override
    public String visit(TrueLiteral n) throws Exception {
        return "1";
    }

    /**
     * f0 -> "false"
     */
    @Override
    public String visit(FalseLiteral n) throws Exception {
        return "0";
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    @Override
    public String visit(Identifier n) throws Exception {
        //System.err.println("Identifier");

        String varName = n.f0.tokenImage;
//        System.err.println(state.get(0) + " " + state.get(1) + " " + ident);
        String type = Type.getName(STVisitor.getVarType(state.get(0), state.get(1), varName));
        if(state.size()>2){
            state.remove(2);
            state.add(STVisitor.getVarType(state.get(0), state.get(1), varName));
        }

        if(STVisitor.checkClassVars(state.get(0), varName) == null) {
            String reg1 = getNewVar();
            fw.write("      " + reg1 + " = load " + type + ", " + type + "* %" + varName + "\n");
            return reg1;
        }else{
            String reg1 = getNewVar();
            String reg2 = getNewVar();
            String reg3 = getNewVar();
            int off = STVisitor.offsets.get(state.get(0)).varOff.get(varName) + 8;

            fw.write("      " + reg1 + " = getelementptr i8, i8* %this, i32 " + off + "\n" +
                    "       " + reg2 + " = bitcast i8* " + reg1 + " to " + type + "*\n" +
                    "       " + reg3 + " = load " + type + ", " + type + "* " + reg2 + "\n"
            );
            return reg3;
        }

    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    @Override
    public String visit(IntegerLiteral n) throws Exception {
        return n.f0.tokenImage;
    }

    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
    @Override
    public String visit(IfStatement n) throws Exception {
        String reg1 = n.f2.accept(this);
        String if1 = getNewIf(), if2 = getNewIf(), if3 = getNewIf();
        fw.write("      br i1 " + reg1 + ", label %" + if1 + ", label %" + if2 + "\n\n" +
                "       " + if1 + ":\n"
        );
        n.f4.accept(this);
        fw.write("      br label %" + if3 + "\n" +
                "\n    " + if2 + ":\n");
        n.f6.accept(this);
        fw.write("      br label %" + if3 + "\n" +
                "\n    " + if3 + ":\n");
        return "null";
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    @Override
    public String visit(WhileStatement n) throws Exception {
        String loop1 = getNewLoop();
        String loop2 = getNewLoop();
        String loop3 = getNewLoop();

        fw.write("      br label %" + loop1 + "\n"+
                "       " + loop1 + ":\n"
        );

        String reg = n.f2.accept(this);

        fw.write("      br i1 " + reg + ", label %" + loop2 + ", label %" + loop3 + "\n"+
            "       " + loop2 + ":\n"
        );

        n.f4.accept(this);

        fw.write("      br label %" + loop1 + "\n" +
            "       " + loop3 + ":\n"
        );

        return "null";
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    @Override
    public String visit(AllocationExpression n) throws Exception {
        //System.err.println("Allocation");
        String reg1 = getNewVar();
        String reg2 = getNewVar();
        String reg3 = getNewVar();
        String className = n.f1.f0.tokenImage;
        if(state.size()>2){
            state.remove(2);
            state.add(className);
        }
        int objectStart = STVisitor.offsets.get(n.f1.f0.tokenImage).methOff.size();
        int objectSize = 8 + (STVisitor.offsets.get(className).varOff.isEmpty() ? 0 :Collections.max(STVisitor.offsets.get(className).varOff.values()));

        fw.write("      " + reg1 + " = call i8* @calloc(i32 1, i32 " + objectSize + ") \n" +
                "       " + reg2 + " = bitcast i8* " + reg1 + " to i8***\n" +
                "       " + reg3 + " = getelementptr [ " + objectStart + " x i8*], [ " + objectStart + " x i8*]* @." + n.f1.f0.tokenImage + "_vtable, i32 0 i32 0\n" +
                "       store i8** " + reg3 + ", i8*** " + reg2 + "\n"
        );
        return reg1;
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    @Override
    public String visit(ArrayAllocationExpression n) throws Exception {
        //System.err.println("ArrayAllocation");
        String reg1 = n.f3.accept(this);
        String reg2 = getNewVar();
        String reg3 = getNewVar();
        String reg4 = getNewVar();
        String reg5 = getNewVar();
        String arr_alloc1 = getNewArr();
        String arr_alloc2 = getNewArr();

        fw.write("      " + reg2 + " = icmp slt i32 " + reg1 + ", 0\n"+
            "       br i1 " + reg2 + ", label %" + arr_alloc1 + ", label %" + arr_alloc2 + "\n" +
            "\n       " + arr_alloc1 + ":\n" +
            "       call void @throw_oob()\n" +
            "       br label %" + arr_alloc2 + "\n" +
            "\n       " + arr_alloc2 + ":\n" +
            "       " + reg3 + " = add i32 " + reg1 + ", 1\n" +
            "       " + reg4 + " = call i8* @calloc(i32 4, i32 "+ reg3 +")\n" +
            "       " + reg5 + " = bitcast i8* " + reg4 + " to i32*\n" +
            "       store i32* " + reg1 + ", i32* " + reg5 + "\n"
        );
        return reg5;
    }

        /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */
    @Override
    public String visit(ArrayAssignmentStatement n) throws Exception {
        String reg1 = n.f0.accept(this);
        String reg2 = getNewVar();
        String reg3 = getNewVar();
        String reg4 = n.f2.accept(this);
        String reg5 = n.f5.accept(this);
        String reg6 = getNewVar();
        String reg7 = getNewVar();

        String label1 = getNewOob();
        String label2 = getNewOob();
        String label3 = getNewOob();

        fw.write("      " + reg2 + " = load i32, i32* " + reg1 + "\n" +
                "       " + reg3 + " = icmp ult i32 " + reg4 + ", " + reg2 + "\n" +
                "       br i1 " + reg3 + ", label %" + label1 + ", label %" + label2 + "\n" +
                "\n       " + label1 + ":\n" +
                "       " + reg6 + " = add i32" + reg1 + ", 1\n" +
                "       " + reg7 + " = getelementptr i32, i32* " + reg1 + ", i32 " + reg6 + "\n" +
                "       store i32 " + reg5 + ", i32* " + reg7 + "\n" +
                "       br label %" + label3 + "\n" +
                "       " + label2 + ":\n" +
                "       call void @throw_oob()\n" +
                "       br label %" + label3 + "\n" +
                "\n       " + label3 + ":\n"
        );
        return "null";
    }

        /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    @Override
    public String visit(ArrayLookup n) throws Exception {
        String reg1 = n.f0.accept(this);
        String reg2 = n.f2.accept(this);
        String reg3 = getNewVar();
        String reg4 = getNewVar();
        String reg5 = getNewVar();
        String reg6 = getNewVar();
        String reg7 = getNewVar();

        String oob1 = getNewOob();
        String oob2 = getNewOob();
        String oob3 = getNewOob();

        fw.write("      " + reg3 + " = load i32, i32* " + reg1 + "\n" +
                "       " + reg4 + " = icmp ult i32 " + reg2 + ", " + reg3 + "\n" +
                "       br i1 " + reg4 + ",  label %" + oob1 + ", label %" + oob2 + "\n" +
                "\n       " + oob1 + ":\n" +
                "       " + reg5 + " = add i32 " + reg2 + ", 1\n" +
                "       " + reg6 + " = getelementptr i32, i32* " + reg1 + ", i32 " + reg5 + "\n" +
                "       " + reg7 + " = load i32, i32* " + reg6 + "\n" +
                "       br label %" + oob3 + "\n" +
                "\n       " + oob2 + ":\n" +
                "       call void @throw_oob()\n" +
                "       br label %" + oob3 + "\n" +
                "\n       " + oob3 + ":\n"
        );
        return reg7;
    }

        /**
     * f0 -> "this"
     */
    @Override
    public String visit(ThisExpression n) throws Exception {
        //System.err.println("This Expression");

        if(state.size()>2){
            state.remove(2);
            state.add(state.get(0));
        }
        return "%this";
    }

    /**
     * f0 -> "!"
     * f1 -> Clause()
     */
    @Override
    public String visit(NotExpression n) throws Exception {
        String reg1 = n.f1.accept(this);
        String reg2 = getNewVar();

        fw.write("      " + reg2 + " = xor i1 1, " + reg1 + "\n");
        return reg2;
    }

    /**
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
    @Override
    public String visit(AndExpression n) throws Exception {
        String reg1 = n.f0.accept(this);
        String reg3 = getNewVar();

        String and1 = getNewAnd();
        String and2 = getNewAnd();
        String and3 = getNewAnd();
        String and4 = getNewAnd();

        fw.write("      br label %" + and1 + "\n\n" +
                "       " + and1 + ":\n" +
                "       br i1 " + reg1 + ", label %" + and1 + ", label %" + and4 + "\n" +
                "       " + and2 + ":\n"
        );

        String reg2 = n.f2.accept(this);

        fw.write("      br label %" + and3 + "\n\n" +
                "       " + and3 + ":\n" +
                "       br label %" + and4 + "\n\n" +
                "       " + and4 + ":\n" +
                "       " + reg3 + " = phi i1 [ 0, %" + and1 + " ], [ " + reg2 + ", %" + and3 + " ]\n"
        );
        return reg3;
    }
//%c = phi i32 [%a, %lb1], [%b, %lb2]


    /**
     * f0 -> NotExpression()
     * | PrimaryExpression()
     */
    @Override
    public String visit(Clause n) throws Exception {
        return n.f0.accept(this);
    }

    /**
     * f0 -> AndExpression()
     * | CompareExpression()
     * | PlusExpression()
     * | MinusExpression()
     * | TimesExpression()
     * | ArrayLookup()
     * | ArrayLength()
     * | MessageSend()
     * | Clause()
     *
     * @param n
     */
    @Override
    public String visit(Expression n) throws Exception {
        return n.f0.accept(this);
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    @Override
    public String visit(BracketExpression n) throws Exception {
        return n.f1.accept(this);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    @Override
    public String visit(ArrayLength n) throws Exception {
        String reg1 = n.f0.accept(this);
        String reg2 = getNewVar();

        fw.write("      " + reg2 + " = load i32, i32* " + reg1 + "\n");
        return reg2;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    @Override
    public String visit(MessageSend n) throws Exception {
        //System.err.println("MessageSend");
        //System.err.println(state.size());
        state.add("1");
        String reg1 = n.f0.accept(this);
        //System.err.println(state.get(2));
        String className = state.remove(2);

        String reg3 = getNewVar();
        String reg4 = getNewVar();
        String reg5 = getNewVar();
        String reg6 = getNewVar();
        String reg7 = getNewVar();
        String reg8 = getNewVar();

        n.f4.accept(this);

        String methName = n.f2.f0.tokenImage;
        //System.err.println(className + " " + methName + " ");
        int off = (STVisitor.offsets.get(className).methOff.isEmpty() ? 0 : STVisitor.offsets.get(className).methOff.get(methName) / 8);
        String methType = Type.getName(STVisitor.getMethType(className, methName));

        fw.write("      " + reg3 + " = bitcast i8* " + reg1 + " to i8***\n" +
                "       " + reg4 + " = load i8**, i8*** " + reg3 + "\n" +
                "       " + reg5 + " = getelementptr i8*, i8** " + reg4 + ", i32 " + (off<0?0:off) + "\n" +
                "       " + reg6 + " = load i8*, i8** " + reg5 + "\n" +
                "       " + reg7 + " = bitcast i8* " + reg6 + " to " + methType + " (i8*"
        );


        for (String type: STVisitor.getArgsType(className, methName)) {
            type = Type.getName(type);
            fw.write("," + type);
        }

        fw.write(")*\n" +
                "       " + reg8 + " = call " + methType + " " + reg7 + "(i8* " + reg1
        );

        for (String type: STVisitor.getArgsType(className, methName)) {
            type = Type.getName(type);
            String reg = argRegs.remove(0);
            fw.write(", " + type + " " + reg);
        }

        fw.write(")\n\n");
        return reg8;
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    @Override
    public String visit(ExpressionList n) throws Exception {
        argRegs.add(n.f0.accept(this));
        for (int i = 0; i < n.f1.f0.size(); i++) {
            argRegs.add(n.f1.f0.elementAt(i).accept(this));
        }
        return "null";
    }

    /**
     * f0 -> ( ExpressionTerm() )*
     */
    @Override
    public String visit(ExpressionTail n) throws Exception {
        return n.f0.accept(this);
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    @Override
    public String visit(ExpressionTerm n) throws Exception {
        return n.f1.accept(this);
    }

    /**
     * f0 -> IntegerLiteral()
     * | TrueLiteral()
     * | FalseLiteral()
     * | Identifier()
     * | ThisExpression()
     * | ArrayAllocationExpression()
     * | AllocationExpression()
     * | BracketExpression()
     */
    @Override
    public String visit(PrimaryExpression n) throws Exception {
        //System.err.println("PrimaryExpression");
        return n.f0.accept(this);
    }

    public void makeVtable() throws Exception{
        boolean first = true;
        for (Map.Entry<String, Offset> e: STVisitor.offsets.entrySet()) {

            fw.write("@." + e.getKey() + "_vtable = global [" + e.getValue().methOff.size() + " x i8*] [");

            for (Map.Entry<String, Integer> m: e.getValue().methOff.entrySet()) {
                String methName = m.getKey();
                String methType = Type.getName(STVisitor.getMethType(e.getValue().className, methName));

                if(!first)  fw.write(", ");
                fw.write("i8* bitcast (" + methType + " (i8*");
                //arguments
                for (String type: STVisitor.getArgsType(e.getValue().className, methName)) {
                    type = Type.getName(type);
                    fw.write("," + type);
                }
                first = false;
                fw.write(")* @" + e.getValue().className + "." + methName + " to i8*)");
            }
            first = true;
            fw.write("]\n");
        }
        fw.write("\n\n");
    }

}
