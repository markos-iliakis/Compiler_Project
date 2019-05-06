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

//                System.err.println("Program ok");

            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            } catch (FileNotFoundException ex) {
                System.err.println(ex.getMessage());
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
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
