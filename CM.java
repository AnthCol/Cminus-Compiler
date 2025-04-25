import java.io.*; 
import java.nio.file.Path; 
import java.nio.file.*; 
import absyn.*; 

class CM
{
    static public void main(String args[])
    {
        boolean abstract_flag = false; 
        boolean semantic_flag = false; 
        boolean compiler_flag = false; 


        for (String s : args)
        {
            if (s.equals("-a"))
                abstract_flag = true; 
            else if (s.equals("-s"))
                semantic_flag = true; 
            else if (s.equals("-c"))
                compiler_flag = true; 
        }

        try
        {
            parser p = new parser(new Lexer(new FileReader(args[0]))); 

            Absyn result = (Absyn)(p.parse().value); 

            Path path = Paths.get(args[0]); 
            Path file = path.getFileName(); 
            String filename = file.toString(); 
            filename = filename.substring(0, filename.lastIndexOf(".")); 

            if (abstract_flag && result != null)
            {
                PrintStream out = new PrintStream(new FileOutputStream(filename + ".abs")); 
                System.setOut(out); 
                System.out.println("The abstract syntax tree is:"); 
                AbsynVisitor visitor = new ShowTreeVisitor(); 
                result.accept(visitor, 0, false); 
            }

            if (semantic_flag && result != null)
            {
                PrintStream out = new PrintStream(new FileOutputStream(filename + ".sym")); 
                System.setOut(out); 
                System.out.println("Entering the global scope:"); 
                SemanticAnalyzer visitor = new SemanticAnalyzer(); 
                result.accept(visitor, 0, false); 
                visitor.hasMain(); 
                visitor.printTable(0); 
                System.out.println("Leaving the global scope"); 
            }

            if (compiler_flag && result != null)
            {
                PrintStream out = new PrintStream(new FileOutputStream(filename + ".tm")); 
                System.setOut(out); 
                System.out.println("* C-Minus Compilation to TM Code");
                System.out.println("* For file: " + filename + ".tm"); 
                CodeGenerator generator = new CodeGenerator(); 


                // prelude
                generator.emitComment("Prelude:"); 
                generator.emitRM("LD", generator.GP, 0, generator.AC, "load GP with maxaddr"); 
                generator.emitRM("LDA", generator.FP, 0, generator.GP, "copy GP to FP"); 
                generator.emitRM("ST", generator.AC, 0, generator.AC, "clear content at loc 0"); 

                // Code for IO functions 
                int inputStart = generator.emitSkip(1); 
                generator.emitComment("Jump around i/o routines"); 
                generator.emitComment("Input Routine:"); 
                generator.emitRM("ST", generator.AC, -1, generator.FP, "store return"); 
                generator.emitRM("IN", generator.AC, 0, generator.AC, "input");
                generator.emitRM("LD", generator.PC, -1, generator.FP, "return to caller"); 

                generator.emitComment("Output Routine:"); 
                generator.emitRM("ST", generator.AC, -1, generator.FP, "store return"); 
                generator.emitRM("LD", generator.AC, -2, generator.FP, "load output value"); 
                generator.emitRM("OUT", 0, 0, 0, "output"); 
                generator.emitRM("LD", generator.PC, -1, generator.FP, "return to caller"); 
                int outputEnd = generator.emitSkip(0); 
                
                generator.emitBackup(inputStart);
                generator.emitRM_Abs("LDA", generator.PC, outputEnd, "jump around i/o code"); 
                generator.emitRestore(); 
                generator.emitComment("End of standard prelude"); 
                
                result.accept(generator, 0, false);  

                if (generator.mainEntry == -1)
                {
                    System.err.println("ERROR Missing main - stopping code generation process."); 
                    System.out.println("ERROR Missing main - stopping code generation process."); 
                }
                else
                {
                    // finale  - check something here 
                    generator.emitRM("ST", generator.FP, 0, generator.FP, "push ofp");
                    generator.emitRM("LDA", generator.FP, 0, generator.FP, "push frame");
                    generator.emitRM("LDA", generator.AC, 1, generator.PC, "load ac with ret addr"); 
                    generator.emitRM_Abs("LDA", generator.PC, generator.mainEntry + 1, "jump to main loc"); 
                    generator.emitRM("LD", generator.FP,  0, generator.FP, "pop frame"); 
                    generator.emitRO("HALT", 0, 0, 0, ""); 

                }


            }

        }
        catch (Exception e)
        {
            e.printStackTrace(); 
        }
    }
}
