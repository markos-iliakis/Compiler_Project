import syntaxtree.*;

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

    }

    /**
     * Grammar production:
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public void visit(AssignmentStatement a, ){
//        check if types are different
        if(getType(a.f0.f0.tokenImage) != a.f2.accept(this, )){
            System.out.println("Invalid Assignment in "+a.f0.f0.tokenImage);
            System.exit(-2);
        }
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
    public void visit(ArrayAssignmentStatement a, ){
//        check if identifier type is int array
        if(getType(a.f0.f0.tokenImage) != ){
            System.out.println("Invalid Type "+a.f0.f0.tokenImage);
        }

//        check if f2 and f5 are int
        if((a.f2.accept(this, ) != 1) || (a.f5.accept(this, ) != 1)){
            System.out.println("Invalid Array Assignment in "+a.f0.f0.tokenImage);
            System.exit(-2);
        }

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
    public void visit(IfStatement i, ){
//        check if f2 is bool
        if(i.f2.accept(this, ) != ){
            System.out.println("Invalid If statement");
            System.exit(-2);
        }
    }

    /**
     * Grammar production:
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public void visit(WhileStatement w, ){
//        check if f2 is bool
        if(w.f2.accept(this, ) != ){
            System.out.println("Invalid while statement");
            System.exit(-2);
        }
    }

    /**
     * Grammar production:
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
    public void visit(AndExpression a, ){
//        check if f0 anf f2 are bools
        if(a.f2.accept(this, ) !=  || a.f0.accept(this, ) != ){
            System.out.println("Invalid and statement");
            System.exit(-2);
        }

        return ;
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public void visit(CompareExpression c, ){
//        check if f0 anf f2 are ints
        if(c.f2.accept(this, ) !=  || c.f0.accept(this, ) != ){
            System.out.println("Invalid compare statement");
            System.exit(-2);
        }
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public void visit(PlusExpression c, ){
//        check if f0 anf f2 are ints
        if(c.f2.accept(this, ) !=  || c.f0.accept(this, ) != ){
            System.out.println("Invalid compare statement");
            System.exit(-2);
        }
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public void visit(MinusExpression c, ){
//        check if f0 anf f2 are ints
        if(c.f2.accept(this, ) !=  || c.f0.accept(this, ) != ){
            System.out.println("Invalid compare statement");
            System.exit(-2);
        }
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public void visit(TimesExpression c, ){
//        check if f0 anf f2 are ints
        if(c.f2.accept(this, ) !=  || c.f0.accept(this, ) != ){
            System.out.println("Invalid compare statement");
            System.exit(-2);
        }
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public void visit(ArrayLookup c, ){
//        check if f0 is of type array int
        if(c.f0.accept(this, ) != ){
            System.out.println("Invalid type");
            System.exit(-2);
        }

//        check if f2 is of type int
        if(c.f2.accept(this, ) != ){
            System.out.println("Invalid type");
            System.exit(-2);
        }
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public void visit(ArrayLength c, ){
//        check if f0 is of type array int
        if(c.f0.accept(this, ) != ){
            System.out.println("Invalid type");
            System.exit(-2);
        }

//        check if f2 is of type int
        if(c.f2.accept(this, ) != ){
            System.out.println("Invalid type");
            System.exit(-2);
        }
    }

    /**
     * Grammar production:
     * f0 -> "!"
     * f1 -> Clause()
     */
    public void visit(NotExpression c, ){
//        check if f1 is bool
        if(c.f1.accept(this, ) != ){
            System.out.println("Invalid type");
            System.exit(-2);
        }
    }

    /**
     * Grammar production:
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public void visit(ArrayAllocationExpression c, ){
//        check if f1 is int
        if(c.f3.accept(this, ) != ){
            System.out.println("Invalid type");
            System.exit(-2);
        }
    }

    /**
     * Grammar production:
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public void visit(AllocationExpression c, ){
//        just return type
    }
}
