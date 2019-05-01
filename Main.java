import syntaxtree.*;

import java.io.*;

class Main {
    public static void main (String [] args){
        if(args.length != 1){
            System.err.println("Usage: java Driver <inputFile>");
            System.exit(1);
        }
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(args[0]);
            MiniJavaParser parser = new MiniJavaParser(fis);
            System.err.println("Program parsed successfully.");
            STVisitor eval = new STVisitor();
            Goal root = parser.Goal();
            root.accept(eval, null);
            TCVisitor eval2 = new TCVisitor();
            Goal root2 = parser.Goal();
            root2.accept(eval2, STVisitor.getClassMap());
//            System.out.println(root.accept(eval, null));
        }
        catch(ParseException ex){
            System.out.println(ex.getMessage());
        }
        catch(FileNotFoundException ex){
            System.err.println(ex.getMessage());
        }
        finally{
            try{
                if(fis != null) fis.close();
            }
            catch(IOException ex){
                System.err.println(ex.getMessage());
            }
        }
    }
}
