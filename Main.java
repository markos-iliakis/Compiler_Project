import syntaxtree.*;

import java.io.*;
import java.util.ArrayList;

class Main {
    public static void main (String [] args){
        if(args.length < 1){
            System.err.println("Usage: java Driver <inputFile1> <inputFile2> ...");
            System.exit(1);
        }
        for (String arg: args) {

            FileInputStream fis = null;
            FileInputStream fis2 = null;
            FileInputStream fis3 = null;
            try {
                //            Symbol Table
                fis = new FileInputStream(arg);
                MiniJavaParser parser = new MiniJavaParser(fis);
//                System.err.println("Program parsed successfully.");
                STVisitor.newClassMap();
                STVisitor eval = new STVisitor();
                Goal root = parser.Goal();
                root.accept(eval, null);

                //            Type Checking
                fis2 = new FileInputStream(arg);
                MiniJavaParser parser2 = new MiniJavaParser(fis2);
                TCVisitor eval2 = new TCVisitor();
                Goal root2 = parser2.Goal();
                root2.accept(eval2);

                //            Print Indexes
                STVisitor.makeIndexes();

                fis3 = new FileInputStream(arg);
                MiniJavaParser parser3 = new MiniJavaParser(fis3);
                LLVMVisitor eval3 = new LLVMVisitor();
                Goal root3 = parser3.Goal();
//                LLVMVisitor.setFw("~/Documents/Compilers/compilers2/minijava/test_files/my_results/"+arg+".ll");
                LLVMVisitor.setFw(arg);
                root3.accept(eval3);
                LLVMVisitor.unsetFw();
                System.err.println("LLVM Program written");

            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            } catch (FileNotFoundException ex) {
                System.err.println(ex.getMessage());
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
                ex.printStackTrace(System.err);
            } finally {
                try {
                    if (fis != null) fis.close();
                    if (fis2 != null) fis2.close();
                    STVisitor.deleteClassMap();
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }
            }
        }
    }
}
