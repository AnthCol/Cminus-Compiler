import absyn.*; 
import java.util.*; 


public class CodeGenerator implements AbsynVisitor
{
    public int emitLoc; 
    public int highEmitLoc; 
    public int mainEntry; 

    //public int offset; 

    public static final int AC = 0; 
    public static final int AC1 = 1; 
    public static final int FP = 5; 
    public static final int GP = 6; 
    public static final int PC = 7; 

    private String currentFunction; 

    public HashMap<String, Integer> functionLocations; 
    public HashMap<String, HashMap<String, Integer>> functionVariables; 

    public CodeGenerator()
    {
        //offset = 0; 
        emitLoc = 0; 
        highEmitLoc = 0; 
        mainEntry = -1; 
        currentFunction = ""; 
        functionLocations = new HashMap<String, Integer>(); 
        functionVariables = new HashMap<String, HashMap<String, Integer>>(); 

        functionLocations.put("input", 4); 
        functionLocations.put("output", 7); 
    }

    /*
        Emit functions below are copied from Dr. Song's slide deck:
        11-TMSimulator-ext
    */
   
    public void emitRM(String op, int r, int d, int s, String c) 
    {
        if (op.equals("IN") || op.equals("OUT"))
        {
            System.out.printf("%3d: %5s %d,%d,%d \t%s\n", emitLoc, op, r, d, s, c); 
            ++emitLoc; 
        }
        else
        {
            System.out.printf("%3d: %5s %d,%d(%d)", emitLoc, op, r, d, s); 
            System.out.printf("\t%s\n", c); 
            ++emitLoc;
            if(highEmitLoc < emitLoc)
                highEmitLoc = emitLoc;
        }
    }
    
    
    public void emitRM_Abs(String op, int r, int a, String c)
    {
        System.out.printf("%3d: %5s %d,%d(%d) ", emitLoc, op, r, a - (emitLoc + 1), PC); 
        System.out.printf("\t%s\n", c);
        ++emitLoc;
        if(highEmitLoc < emitLoc)
            highEmitLoc = emitLoc;
    }

    
    public void emitRO(String op, int r, int s, int t, String c)
    {
        System.out.printf("%3d: %5s %d,%d,%d", emitLoc, op, r, s, t); 
        System.out.printf("\t%s\n", c);
        ++emitLoc;
        if(highEmitLoc < emitLoc)
            highEmitLoc = emitLoc;
    }

    public int emitSkip(int distance)
    {
        int i = emitLoc;
        emitLoc += distance;
        if(highEmitLoc < emitLoc)
            highEmitLoc = emitLoc;
        return i;
    }

    public void emitBackup(int loc)
    {
        if(loc > highEmitLoc)
            emitComment("BUG in emitBackup");
        emitLoc = loc;
    }

    public void emitRestore()
    {
        emitLoc = highEmitLoc;
    }

    public void emitComment(String comment)
    {
        System.out.println("* " + comment);
    }


    /*
        HELPER FUNCTIONS
    */
    private String getOperationSymbol(int op)
    {
        String[] operationSymbols = {"+", "-", "-", "*", "/", "==", "!=", "<", "<=", ">", ">=", "~", "&&", "||", "=="}; 
        return operationSymbols[op];  
    }

    private String getOperationDescription(int op)
    {
        // FIXME TESTEQ
        String[] operationWords = {"ADD", "SUB", "UMINUS", "MUL", "DIV", "EQ", "NE", "LT", "LE", "GT", "GE", "NOT", "AND", "OR", "EQ"}; 
        return operationWords[op]; 
    }

    private boolean isLogicalOp(int op)
    {
        String symbol = getOperationSymbol(op); 
        return (symbol != "+" && symbol != "-" && symbol != "*" && symbol != "/");  
    }
/*
    private void simpleVar(SimpleVar var, boolean isMemAddress)
    {
        emitComment("-> looking up id: " + var.name);  
        //offset = offset + functionVariables.get(currentFunction).get(var.name); 
        int tempOffset = functionVariables.get(currentFunction).get(var.name); 
        // FIXME if we want to do global variables here we need to change something. 
        // This will look in local scope only. 
        if (isMemAddress)
            emitRM("LDA", AC, tempOffset, FP, "load id address"); 
        else 
            emitRM("LD", AC, tempOffset, FP, "load id value"); 

        emitComment("<- id"); 
    }


    private void assignExp(AssignExp exp)
    {
        emitComment("-> op (assign)"); 
        // we alreayd know lhs is a var exp, get the offset for it
        VarExp vexp = exp.lhs; 
        SimpleVar svar = (SimpleVar)vexp.variable; 
        String variableName = svar.name; 

        simpleVar(svar, true); 

        emitRM("ST", AC, offset, FP, "op: push left"); 
        
        if (exp.rhs instanceof IntExp)
            intExp((IntExp)exp.rhs); 
        else if (exp.rhs instanceof BoolExp)
            boolExp((BoolExp)exp.rhs); 
        else if (exp.rhs instanceof OpExp)
            opExp((OpExp)exp.rhs); 
        else if (exp.rhs instanceof VarExp)
            varExp((VarExp)exp.rhs); 

        evaluateExp(exp.rhs);  

        emitRM("LD", AC1, offset, FP, "op: load left"); 
        emitRM("ST", AC, 0, AC1, "assign: store value"); 
        emitComment("<- op"); 
    }

    private void intExp(IntExp exp)
    {
        int value = exp.value; 
        emitComment("-> constant"); 
        emitRM("LDC", AC, value, AC, "load const"); 
        emitComment("<- constant"); 
    }

    private void boolExp(BoolExp exp)
    {
        int value = (exp.value) ? 1 : 0; 
        emitComment("-> constant"); 
        emitRM("LDC", AC, value, AC, "load const"); 
        emitComment("<- constant"); 
    }

    private void callExp(CallExp exp)
    {
        emitComment("-> call of function: " + exp.func); 
        int functionLocation = -emitLoc; 

        if (exp.args != null)
        {
            functionLocation += 1; 
            if (exp.args instanceof ExpList)
            {
                ExpList list = (ExpList)exp.args; 
                while (list != null)
                {
                    if (list.head != null)
                        evaluateExp(list.head); 

                    list = list.tail; 
                }
            }
        } 

        emitRM("ST", FP, offset, FP, "push ofp");  
        emitRM("LDA", FP, offset, FP, "push frame"); 
        emitRM("LDA", AC, 1, PC, "load ac with ret ptr"); 
        emitRM("LDA", PC, functionLocation, PC, "jump to fun loc"); 
        emitRM("LD", FP, 0, FP, "pop frame"); 
        emitComment("<- call"); 
    }

    private void opExp(OpExp exp)
    {

    }

    private void varExp(VarExp exp)
    {   
        if (exp.variable instanceof SimpleVar)
        {
            SimpleVar var = (SimpleVar)exp.variable;
            emitComment("-> id"); 
            emitComment("looking up id: " + var.name); 
            // FIXME potential problem here since we don't check if the variable is in the map 
            // assuming we are trying to get the value  of the variable. 
            offset = functionVariables.get(currentFunction).get(var.name); 
            emitRM("LD", AC, offset, FP, "load id value"); 
            emitComment("<- id"); 
        }
    }

    private void evaluateExp(Exp exp)
    {
        if (exp instanceof AssignExp)
            assignExp((AssignExp)exp); 
        else if (exp instanceof CallExp)
            callExp((CallExp)exp); 
        else if (exp instanceof OpExp)
            opExp((OpExp)exp); 
        else if (exp instanceof VarExp)
            varExp((VarExp)exp); 
    }
*/
    /* 
    public void visit(FunctionDec dec, int OFFSET, boolean isMemAddress)
	{
        emitComment("processing function: " + dec.func); 
        emitComment("jump around function body here"); 

        if (dec.func.equals("main"))
            mainEntry = emitLoc; 

        // insert function into table
        functionLocations.put(dec.func, emitLoc); 
        functionVariables.put(dec.func, new HashMap<String, Integer>()); 
 
        offset = -2; 
        currentFunction = dec.func; 
         
        // done with dec params
        if (dec.params != null)
        {
            VarDecList list = dec.params; 
            while (list != null)
            {
                if (list.head != null && list.head instanceof SimpleDec)
                {
                    functionVariables.get(dec.func).put(((SimpleDec)list.head).name, offset--); 
                }
                list = list.tail; 
            }
        }

        // now move to body  
        if (dec.body != null)
        {
            if (dec.body instanceof CompoundExp)
            {
                emitComment("-> compound statement"); 

                CompoundExp exp = (CompoundExp)dec.body;    
                VarDecList decs = exp.decs;
                ExpList exps = exp.exps; 
                // go through declarations first

                while (decs != null)
                {
                    if (decs.head != null && decs.head instanceof SimpleDec)
                    {
                        emitComment("processing local var: " + ((SimpleDec)decs.head).name); 
                        functionVariables.get(dec.func).put(((SimpleDec)decs.head).name, offset--); 
                    }
                    decs = decs.tail; 
                }

                while (exps != null)
                {  
                    if (exps.head != null)
                    {
                        evaluateExp(exps.head); 
                    }
                    exps = exps.tail; 
                }
                emitComment("<- compound statement"); 
            }
        }

        int functionDeclarationLocation = functionLocations.get(dec.func); 
        int afterBodyProcessing = emitSkip(0); 
        emitRM("LD", PC, -1, FP, "return to caller"); 
        emitBackup(functionDeclarationLocation); 
        // Calculation here is the size of the function effectively, so you move that into the
        // program counter to jump ahead. - calculation results in positive integer
        emitRM("LDA", PC, afterBodyProcessing - functionDeclarationLocation, PC, "jump around fn body"); 
        emitComment("<- fundecl"); 
        emitRestore(); 
	} 

    */

    public void visit(DecList list, int OFFSET, boolean isMemAddress)
	{ 
        while (list != null)
        {
            if (list.head != null)
                list.head.accept(this, OFFSET--, false); 
            list = list.tail; 
        }
	} 

    public void visit(FunctionDec dec, int OFFSET, boolean isMemAddress)
    {
        // Goes to function dec
        emitComment("processing function: " + dec.func); 
        emitComment("jump around function body here"); 

        if (dec.func.equals("main"))
            mainEntry = emitLoc; 

        int functionStartLocation = emitSkip(1); 
        
        // save current function name
        currentFunction = dec.func; 

        // Save information about the function. 
        functionLocations.put(dec.func, functionStartLocation); 
        functionVariables.put(dec.func, new HashMap<String, Integer>()); 
 
        // minus one because you have to go back up one instruction as your end value
        // in this case, it would be instruction 11 as the return line (for fac.cm)
        emitRM("ST", AC, -1, FP, "store return");         

        OFFSET = -2; 

        // Iterate through function parameters and save them on the stack. 
        if (dec.params != null)
        {
            VarDecList params = dec.params; 
            while (params != null)
            {
                // ASSUME SIMPLEDEC 
                functionVariables.get(currentFunction).put(((SimpleDec)params.head).name, OFFSET--); 
                params = params.tail; 
            }
        }

        // get rid of function variables? FIXME


        // After handling params, go to function body. 
        // CompoundExp
        if (dec.body != null)
            dec.body.accept(this, OFFSET, false);     

        int functionEndLocation = emitSkip(0); 

        // Evaluate the return part of the function. 
        emitRM("LD", PC, -1, FP, "return to caller"); 
        emitBackup(functionStartLocation); 
        emitRM("LDA", PC, functionEndLocation - functionStartLocation, PC, "jump around fn body"); 
        emitComment("<- fundecl"); 
        emitRestore(); 
    }



    public void visit(CompoundExp exp, int OFFSET, boolean isMemAddress)
    {
        emitComment("-> compound statement"); 

        // go through variable declarations
        if (exp.decs != null)
        {
            VarDecList decs = exp.decs; 
            while (decs != null)
            {
                // SIMPLEDEC
                decs.head.accept(this, OFFSET--, false); 
                decs = decs.tail; 
            }
        }

        // go through expressions
        if (exp.exps != null)
        {
            ExpList exps = exp.exps; 
            while (exps != null)
            {
                exps.head.accept(this, OFFSET, false); 
                exps = exps.tail;  
            }         
        }
        emitComment("<- compound statement"); 
    }

    public void visit (AssignExp exp, int OFFSET, boolean isMemAddress)
    {
        emitComment("-> op (assign)"); 

        if (exp.lhs != null)
            exp.rhs.accept(this, OFFSET, true); 

        emitRM("ST", AC, OFFSET, FP, "op: push left"); 

        if (exp.rhs != null)
            exp.rhs.accept(this, OFFSET, false); 

        emitRM("LD", AC1, OFFSET, FP, "op: load left"); 
        emitRM("ST", AC, 0, 1, "assign: store value"); 
        emitComment("<- op"); 
    }

    public void visit (SimpleDec dec, int OFFSET, boolean isGlobalVar)
    {
        if (isGlobalVar)
        {

        }
        else
        {
            emitComment("processing local var: " + dec.name); 
            functionVariables.get(currentFunction).put(dec.name, OFFSET); 
        }
    }

    public void visit (VarExp exp, int OFFSET, boolean isMemAddress)
    {
        exp.variable.accept(this, OFFSET, isMemAddress); 
    }

    public void visit (SimpleVar var, int OFFSET, boolean isMemAddress)
    {
        emitComment("-> id"); 
        emitComment("looking up id: " + var.name); 

        int varOffset = functionVariables.get(currentFunction).get(var.name); 

        // FIXME global variables later. 

        if (isMemAddress)
            emitRM("LDA", AC, varOffset + OFFSET, FP, "load id address"); 
        else
            emitRM("LD", AC, varOffset + OFFSET, FP, "load id value"); 

        emitComment("<- id"); 
    }

    public void visit (IntExp exp, int OFFSET, boolean isMemAddress)
    {
        int value = ((IntExp)exp).value; 
        emitComment("-> constant"); 
        emitRM("LDC", AC, value, AC, "load const"); 
        emitComment("<- constant"); 
    }

    public void visit (BoolExp exp, int OFFSET, boolean isMemAddress)
    {
        int value = ((BoolExp)exp).value ? 1 : 0;  
        emitComment("-> constant"); 
        emitRM("LDC", AC, value, AC, "load const"); 
        emitComment("<- constant"); 
    }


    public void visit (CallExp exp, int OFFSET, boolean isMemAddress)
    {
        emitComment("-> call of function: " + exp.func); 

        int functionLocation = functionLocations.get(exp.func); 

        /* System.out.println(); 
        System.out.println("CURRENT LOCATION: " + emitSkip(0)); 
        System.out.println("CURRENT OFFSET: " + OFFSET); 
        System.out.println(); 
        */

        if (exp.args != null)
        {
            ExpList args = exp.args; 
            while (args != null)
            {

                args.head.accept(this, OFFSET, false);  
                emitRM("ST", AC, OFFSET--, FP, "store arg val"); 
                //OFFSET--; 
                /*if (args.head instanceof VarExp)
                {
                    // ASSUME SIMPLE VAR AND PASS BY VALUE
                    VarExp variable = (VarExp)args.head; 
                    SimpleVar sexp = (SimpleVar)variable.variable; 
                    String variableName = sexp.name; 
                    emitComment("-> id"); 
                    emitComment("looking up id: " + variableName); 
                    int variableLocation = functionVariables.get(currentFunction).get(variableName);  
                    emitRM("LD", AC, variableLocation, FP, "load id value"); 
                    emitComment("<- id"); 
                    emitRM("ST", AC, variableLocation + OFFSET--, FP, "store arg val");  
                }
                else if (args.head instanceof OpExp) 
                    emitRM("ST", AC, OFFSET--, FP, "store arg val");  
                else if (args.head instanceof IntExp)
                    emitRM("ST", AC, OFFSET--, FP, "store arg val");  
                else if (args.head instanceof BoolExp)
                    emitRM("ST", AC, OFFSET--, FP, "store arg val");  
                */ 
                args = args.tail; 
            }
        }

        emitRM("ST", FP, OFFSET, FP, "push ofp");  
        emitRM("LDA", FP, OFFSET, FP, "push frame"); 
        emitRM("LDA", AC, 1, PC, "load ac with ret ptr"); 
        emitRM("LDA", PC, functionLocation - emitSkip(0), PC, "jump to fun loc"); 
        emitRM("LD", FP, 0, FP, "pop frame"); 
        emitComment("<- call"); 
    }









    /*
    private boolean isExpWithPossibleVar(Exp exp)
    {
        return (exp instanceof VarExp || /*exp instanceof IfExp || exp instanceof WhileExp || exp instanceof OpExp); 
    }
    */

    public void visit(NameTy type, int OFFSET, boolean isMemAddress)
    {
        /* Nothing to do */
    }

    public void visit(IndexVar var, int OFFSET, boolean isMemAddress)
	{

	} 

    public void visit(NilExp exp, int OFFSET, boolean isMemAddress)
	{
        /* Nothing to do */
	} 


   // public void visit(CallExp exp, int OFFSET, boolean isMemAddress)
	//{
        /*
        emitComment("-> call of function: " + exp.func); 
        int functionLocation = -emitLoc; 
        if (exp.args != null)
        {
            functionLocation += 1; 
            exp.args.accept(this, offset, false); 
            if (exp.args instanceof ExpList)
            {
                emitRM("ST", AC, offset--, FP, "store arg val"); 
            }
        }

        emitRM("ST", FP, offset, FP, "push ofp");  
        emitRM("LDA", FP, offset, FP, "push frame"); 
        emitRM("LDA", AC, 1, PC, "load ac with ret ptr"); 
        // Fp here why?
        emitRM("LDA", PC, functionLocation, PC, "jump to fun loc"); 
        emitRM("LD", FP, 0, FP, "pop frame"); 
        emitComment("<- call"); 
        */
	//} 


    public void visit(OpExp exp, int OFFSET, boolean isMemAddress)
	{
        /*
        emitComment("-> op (exp)"); 
        exp.left.accept(this, offset, isMemAddress); 
        emitRM("ST", AC, offset, FP, "op: push left"); 
        exp.right.accept(this, offset, isMemAddress); 
        emitRM("LD", AC1, offset, FP, "op: load left");  

        if (isLogicalOp(exp.op))
        {
            emitRO("SUB", AC, 1, AC, "op " + getOperationSymbol(exp.op)); 
            emitRM("J" + getOperationDescription(exp.op), AC, 2, PC, "br if true"); 
            emitRM("LDC", AC, 0, AC, "false case"); 
            emitRM("LDA", PC, 1, PC, "uncondtional jmp"); 
            emitRM("LDC", AC, 1, AC, "true case"); 
        }
        else
        {
            emitRO(getOperationDescription(exp.op), AC, 1, 0, "op " + getOperationSymbol(exp.op)); 
        }
        emitComment("<- op"); 
        */
	} 

    public void visit(IfExp exp, int OFFSET, boolean isMemAddress)
	{
        /*
        int ifBodyLocation = 0; 
        int elseBodyLocation = 0; 

        emitComment("-> if"); 
        exp.test.accept(this, offset, isMemAddress); 
        if (exp.thenpart != null)
        {
            emitComment("if: jump to else belongs here"); 
            ifBodyLocation = emitSkip(1); 
            exp.thenpart.accept(this, offset, isMemAddress); 
        }
        if (exp.elsepart != null)
        {
            emitComment("if: jump to end belongs here"); 
            elseBodyLocation = emitSkip(1); 
            emitBackup(ifBodyLocation); 
            emitRM("JEQ", AC, elseBodyLocation - ifBodyLocation, PC, "if: jmp to else"); 
            emitRestore(); 
            exp.elsepart.accept(this, offset, isMemAddress); 
        }
        emitBackup(ifBodyLocation); 
        emitRM("LDA", PC, ifBodyLocation, PC, "jmp to end"); 
        emitRestore(); 
        emitComment("<- if"); 
        */
	} 

    public void visit(WhileExp exp, int OFFSET, boolean isMemAddress)
	{
        /*
        emitComment("-> while"); 
        emitComment("while: jump after body comes back here"); 
        exp.test.accept(this, offset, false); 
        emitComment("while: jump to end belongs here"); 
        int whileBodyStart = emitSkip(1); 
        exp.body.accept(this, offset, false); 
        int whileBodyEnd = emitLoc;        
        emitRM("LDA", PC, -whileBodyStart, PC, "while: absolute jmp to test"); 
        emitBackup(whileBodyStart); 
        emitRM("JEQ", AC, whileBodyEnd - whileBodyStart, PC, "while: jmp to end"); 
        emitRestore(); 
        emitComment("<- while"); 
        */
	} 

    public void visit(ReturnExp exp, int OFFSET, boolean isMemAddress)
	{
        /*
        emitComment("-> return"); 
        exp.exp.accept(this, offset, isMemAddress); 
        emitRM("LD", PC, -1, FP, "return to caller"); 
        emitComment("<- return"); 
        */
	} 

    
    public void visit(ArrayDec dec, int OFFSET, boolean isMemAddress)
	{

	} 
    

    public void visit(VarDecList list, int OFFSET, boolean isMemAddress)
	{
        /*
        while(list != null)
        {
            //System.out.println("\n\n\n RIGHT BEFORE VAR DEC ACCEPT\n\n\n"); 
            if (list.head != null)
                list.head.accept(this, offset, false); 
            //System.out.println("\n\n\n RIGHT AFTER VARDEC ACCEPT\n\n\n"); 
            list = list.tail; 
        }
        */
	}     


    public void visit(ExpList list, int OFFSET, boolean isMemAddress)
	{
        /*
        while(list != null)
        {
            //System.out.println("\n\n\nprinting offset for variable: " + offset + "\n\n\n"); 

            if (list.head != null)
                list.head.accept(this, offset, false); 

            //if (isExpWithPossibleVar(list.head))
            //emitRM("ST", AC, offset, FP, "store arg val"); 
            list = list.tail; 
        }
        */
	} 
}
