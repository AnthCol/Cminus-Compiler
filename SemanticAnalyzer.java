import absyn.*; 
import java.util.*; 


public class SemanticAnalyzer implements AbsynVisitor
{
    private HashMap <Integer, ArrayList<TableElement>> table; 
    private Stack <Integer> scopes; 

    private static boolean hasError; 
    private static boolean validMain; 

    private static final String ANSI_RED = "\u001B[31m"; 
    private static final String ANSI_RESET = "\u001B[0m"; 

    private ArrayList<Integer> paramTypeList; 
    private boolean inParams = false; 

    final static int SPACES = 4;
    public int scopeCounter; 

    public SemanticAnalyzer()
    {
        scopeCounter = 0;
        inParams = false; 
        hasError = false; 
        validMain = false; 
        table = new HashMap<Integer, ArrayList<TableElement>>(); 
        insertScopeToTable(scopeCounter); 
        scopes = new Stack<Integer>(); 
    }

    public void hasMain()
    {
        ArrayList<TableElement> list = table.get(0); 
        boolean mainFound = false; 

        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i).name.equals("main"))
            {
                mainFound = true; 
                break; 
            }
        }

        if (!mainFound)
        {
            indent(1); 
            System.out.println(ANSI_RED + "ERROR [ Missing main ] No main function found in program" + ANSI_RESET); 
        }
    }

    public void printTable(int scopeNumber)
    {
        for (int i = 0; i < table.get(scopeNumber).size(); i++)
            table.get(scopeNumber).get(i).print(); 
    }

    private void insertScopeToTable(int scopeNumber)
    {
        ArrayList<TableElement> list = new ArrayList<TableElement>(); 
        table.put(scopeNumber, list); 
    }

    private void insertElementToTable(int scopeNumber, TableElement element, int level)
    {
        if (!existsInTable(element))
            table.get(scopeNumber).add(element); 
        else
        {
            indent(level);  
            System.out.println(ANSI_RED + "ERROR [ Redeclaration ] For variable " + element.name + " on line " + (element.pos + 1) + ANSI_RESET); 
        }
    }

    private void insertParamsToCurrentFunction(String name, ArrayList<Integer> paramList)
    {
        ArrayList<TableElement> list = table.get(0); 

        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i).name.equals(name) && list.get(i).variableTypes.size() == 1 && list.get(i).variableTypes.get(0) == -1)
            {
                list.get(i).variableTypes = paramList; 
                break; 
            }
        }
    }


    private ArrayList<Integer> getFunctionParamsByName(String name)
    {
        ArrayList<TableElement> list = table.get(0); 

        for (int i = 0; i < list.size(); i++)
            if (list.get(i).name == name)
                return list.get(i).variableTypes; 

        return null; 
    }

    private boolean existsInTable(TableElement element)
    {
        Iterator<Integer> iterator = scopes.iterator(); 
        while (iterator.hasNext())
        {
            ArrayList<TableElement> list = table.get(iterator.next()); 
            for (int i = 0; i < list.size(); i++)
                if (list.get(i).name.equals(element.name))
                    return true; 
        }
        return false; 
    }

    private int getTypeByName(String name)
    {

        System.out.println("printing name: " + name); 
        Iterator<Integer> iterator = scopes.iterator(); 

        while (iterator.hasNext())
        {
            ArrayList<TableElement> list = table.get(iterator.next()); 
            for (int i = 0; i < list.size(); i++)
                if (list.get(i).name.equals(name))
                    return list.get(i).type; 
        }

        return -1; 
    }

    private int getFunctionTypeByName(String name)
    {
        ArrayList<TableElement> list = table.get(0); 
        for (int i = 0; i < list.size(); i++)
            if (list.get(i).name.equals(name))
                return list.get(i).type;  

        return -1; 
    }

    
    private int determineExpType(Exp exp, int level)
    {

        if (exp instanceof OpExp)
        {
            int leftType = determineExpType(((OpExp)exp).left, level); 
            int rightType = determineExpType(((OpExp)exp).right, level); 

            // Less than five here is the following opeations: PLUS, MINUS, UMINUS, MUL, DIV            
            if (((OpExp)exp).op < 5 && ((!(((OpExp)exp).left instanceof OpExp) && leftType != NameTy.INT) || (!(((OpExp)exp).right instanceof OpExp) && rightType != NameTy.INT)))
            {
                indent(level); 
                System.out.println(ANSI_RED + "ERROR [ Invalid Operation ] Performing arithmetic operations with non-integer type(s) on line " + (exp.pos + 1) + ANSI_RESET);
            }
            else if (leftType != rightType && !(((OpExp)exp).left instanceof OpExp) && !(((OpExp)exp).right instanceof OpExp))
            {
                indent(level); 
                System.out.println(ANSI_RED + "ERROR [ Invalid Operation ] Invalid due to mismatched types on line " + (exp.pos + 1) + ANSI_RESET); 
            }
             
        }

        if (exp instanceof CallExp)
        { 
            if (((CallExp)exp).func.equals("input"))
            {
                return NameTy.INT; 
            }
            else if (((CallExp)exp).func.equals("output"))
            {
                return NameTy.VOID; 
            }


            return getFunctionTypeByName(((CallExp)exp).func); 
        }

        if (exp instanceof IntExp)
        {
            return NameTy.INT; 
        }

        if (exp instanceof BoolExp)
        {
            return NameTy.BOOL; 
        }

        if (exp instanceof NilExp)
        {
            return NameTy.VOID; 
        }

        if (exp instanceof VarExp)
        {
            Var v = ((VarExp)exp).variable; 
            if (v instanceof SimpleVar)
                return getTypeByName(((SimpleVar)v).name); 
            else if (v instanceof IndexVar)
                return getTypeByName(((IndexVar)v).name); 
        }
        
        return -1; 
    }

    private int getCurrentFunctionType()
    {
        // Iterate only through current scope, check if there is a function
        // (You can't have a function within a function so this works) 

        ArrayList<TableElement> list = table.get(0); 
        for (int i = 0; i < list.size(); i++)
            if (list.get(i).variableTypes != null && list.get(i).variableTypes.size() == 1 && list.get(i).variableTypes.get(0) == -1)
                return list.get(i).type; 
        return -1; 
    }

    private boolean functionExists(String name)
    {
        if (name.equals("input") || name.equals("output"))
            return true; 
        ArrayList<TableElement> list = table.get(0); 
        for (int i = 0; i < list.size(); i++)
            if (list.get(i).variableTypes != null && list.get(i).name.equals(name))
                return true; 
        return false; 
    }

    public static void indent( int level ) {
        for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
    }

    public void visit(NameTy typ, int level, boolean flag)
    {
        // Nothing to do
    }

    public void visit(SimpleVar var, int level, boolean flag)
    {
        // Nothing to do
    }

    public void visit(IndexVar var, int level, boolean flag)
    {

        int type = determineExpType(var.index, level); 

        if (type != NameTy.INT)
        {
            indent(level); 
            System.out.println(ANSI_RED + "ERROR [ Invalid Array Access ] on line " + (var.index.pos + 1) + " type with brackets must be an INT" + ANSI_RESET);  
        }

        var.index.accept(this, level, flag); 
        //if (determineType(var.index) != NameTy.INT)
        //    System.out.println("Error on line: " + var.pos + " Invalid array index type, must be INT."); 
    }

    public void visit (NilExp exp, int level, boolean flag)
    {
        // Nothing to do 
    }

    public void visit (IntExp exp, int level, boolean flag)
    {
        if (inParams)
            paramTypeList.add(NameTy.INT); 
    }

    public void visit (BoolExp exp, int level, boolean flag)
    {

        if (inParams)
            paramTypeList.add(NameTy.BOOL); 

    }

    public void visit (VarExp exp, int level, boolean flag)
    {
        exp.variable.accept(this, level, flag); 
    }

    public void visit (CallExp exp, int level, boolean flag)
    {
        ArrayList<Integer> typeList = null; 

        boolean functionExists = false; 

        // check if function exists. 
        if (functionExists(exp.func))
        {
            typeList = getFunctionParamsByName(exp.func); 
            /*
                FIXME
                For recursive functions, the function is not done being analyzed
                so if we want to compare the types, we won't be able to. 
                This is a flaw that I'm willing to live with for now. 
            */  
            functionExists = true; 
        }
        else 
        {
            indent(level); 
            System.out.println(ANSI_RED + "ERROR [ Undeclared Function ] call to function " + exp.func + " on line " + (exp.pos + 1) + " is invalid" + ANSI_RESET); 
        }


        int index = 0; 

        // compare expected types to 
        // make sure there is no problem

        ExpList list = exp.args; 
        while (list != null)
        {
            if (functionExists && typeList != null)
            {
                int typeListType = typeList.get(index++); 
                int expType = determineExpType(list.head, level); 
                System.out.println("PRINTING TYPES: " + typeListType +  " " + expType); 

                indent(level); 
                System.out.println(ANSI_RED + "ERROR [ Wrong type in function call ] Argument #" + index + " of wrong type on line " + (list.head.pos + 1) + ANSI_RESET); 
            }

            list.head.accept(this, level, flag); 
            list = list.tail; 
        }
    }

    public void visit(OpExp exp, int level, boolean flag) 
    {
        if (exp.left != null)
            exp.left.accept(this, level, flag);
        exp.right.accept(this, level, flag);
    }

    public void visit (AssignExp exp, int level, boolean flag)
    {
        
        int l = determineExpType(exp.lhs, level); 
        int r = determineExpType(exp.rhs, level); 

        if (l == -1)
        {
            indent(level); 
            System.out.println(ANSI_RED + "ERROR [ Invalid Assignment ] Left hand side variable of assignment on line " + (exp.pos + 1) + " is undeclared" + ANSI_RESET); 
        }

        if (r == -1 && !(exp.rhs instanceof OpExp))
        {
            indent(level); 
            System.out.println(ANSI_RED + "ERROR [ Invalid Assignment ] Right hand side variable of assignment on line " + (exp.pos + 1) + " is undeclared" + ANSI_RESET); 
        }

        if (l != -1 && !(exp.rhs instanceof OpExp))
        {
            if (l != r)
            {
                indent(level); 
                System.out.println(ANSI_RED + "ERROR [ Invalid Assignment ] Assignment on line " + (exp.pos + 1) + " invalid due to mismatched types" + ANSI_RESET); 
            }
        }

        exp.lhs.accept(this, level, flag); 
        exp.rhs.accept(this, level, flag); 
    }

    public void visit (IfExp exp, int level, boolean flag)
    {
        indent(++level); 
        level++; 
        System.out.println("Entering a new block:"); 

        scopes.push(++scopeCounter); 
        insertScopeToTable(scopeCounter); 

        if (exp.test != null)
        {  
            if (exp.test instanceof OpExp)
            {
                int left = determineExpType(((OpExp)exp.test).left, level); 
                int right = determineExpType(((OpExp)exp.test).right, level); 

                if (left == NameTy.VOID || right == NameTy.VOID)
                {
                    indent(level); 
                    System.out.println(ANSI_RED + "ERROR [ Invalid Condition ] on line " + (exp.pos + 1) + " invalid type used in comparison" + ANSI_RESET); 
                }
            }
            else
            {
                int type = determineExpType(exp.test, level); 

                if (type == NameTy.VOID || type == -1)
                {
                    indent(level); 
                    System.out.println(ANSI_RED + "ERROR [ Invalid Condition ] on line " + (exp.pos + 1) + " not all types used are BOOL or INT" + ANSI_RESET); 
                }
            }
            
            exp.test.accept(this, level, flag); 
        }
        if (exp.thenpart != null)
            exp.thenpart.accept(this, level, flag); 
        if (exp.elsepart != null)
            exp.elsepart.accept(this, level, flag); 


        printTable(scopes.peek()); 
        scopes.pop(); 

        level--; 
        indent(level--); 
        System.out.println("Leaving the block"); 
    }

    public void visit (WhileExp exp, int level, boolean flag)
    { 
        indent(++level); 
        level++; 

        scopes.push(++scopeCounter); 
        insertScopeToTable(scopeCounter); 

        System.out.println("Entering a new block:"); 
        if (exp.test != null)
        {
            if (exp.test instanceof OpExp)
            { 
                int left = determineExpType(((OpExp)exp.test).left, level); 
                int right = determineExpType(((OpExp)exp.test).right, level); 

                if (left == NameTy.VOID || right == NameTy.VOID)
                {
                    indent(level); 
                    System.out.println(ANSI_RED + "ERROR [ Invalid Condition ] on line " + (exp.pos + 1) + " invalid type used in comparison" + ANSI_RESET); 
                }
            }
            else
            {
                int type = determineExpType(exp.test, level); 

                if (type == NameTy.VOID || type == -1)
                {
                    indent(level); 
                    System.out.println(ANSI_RED + "ERROR [ Invalid Condition ] on line " + (exp.pos + 1) + " not all types used are BOOL or INT" + ANSI_RESET); 
                }
            }

            exp.test.accept(this, level, flag);   
        }
        if (exp.body != null)
            exp.body.accept(this, level, flag); 


        printTable(scopes.peek()); 
        scopes.pop(); 

        level--;  
        indent(level--); 
        System.out.println("Leaving the block"); 
    }

    public void visit (ReturnExp exp, int level, boolean flag)
    {
        if (exp.exp != null && determineExpType(exp.exp, level) != NameTy.VOID)
        { 
            if (determineExpType(exp.exp, level) == -1)
            {
                indent(level); 
                System.out.println(ANSI_RED + "ERROR [ Variable in return expression undeclared ] on line " + exp.pos + ANSI_RESET); 
            }
            else
            {
                /*
                System.out.println("------------------------------"); 
                System.out.println(getCurrentFunctionType()); 
                System.out.println(determineExpType(exp.exp)); 
                System.out.println("------------------------------"); 
                */

                int type = getCurrentFunctionType(); 
                if (type != determineExpType(exp.exp, level))
                {
                    indent(level); 
                    System.out.println(ANSI_RED + "ERROR [ Function return ] Invalid return due to type mismatch on line " + (exp.pos + 1) + ANSI_RESET); 
                }
            }
        }

        exp.exp.accept(this, level, flag); 
    }

    public void visit (CompoundExp exp, int level, boolean flag)
    {
        VarDecList vlist = exp.decs; 
        while(vlist != null)
        {
            vlist.head.accept(this, level, flag); 
            vlist = vlist.tail; 
        }

        ExpList elist = exp.exps; 
        while (elist != null)
        {
            elist.head.accept(this, level, flag); 
            elist = elist.tail; 
        }
    }




    public void visit (FunctionDec dec, int level, boolean flag)
    {
        indent(++level); 
        level++; 
        System.out.println("Entering the scope for function " + dec.func + ":"); 

        scopes.push(++scopeCounter); 
        insertScopeToTable(scopeCounter); 



        boolean hasBody = true; 
        ArrayList<Integer> tempList = new ArrayList<Integer>(); 
        tempList.add(-1); 

        paramTypeList = new ArrayList<Integer>(); 

        if (dec.body != null)
            hasBody = true; 


        TableElement element = new TableElement(dec.pos, dec.func, dec.result.typ, tempList, hasBody, level);         
        insertElementToTable(0, element, 1); 



        VarDecList list = dec.params; 

        inParams = true; 
        while (list != null && list.head != null)
        {
            list.head.accept(this, level, flag); 
            list = list.tail; 
        }
        inParams = false; 


        if (dec.body != null)
            dec.body.accept(this, level, flag);  


        insertParamsToCurrentFunction(dec.func, paramTypeList); 

        printTable(scopes.peek());   
        scopes.pop(); 

        level--;
        indent(level--); 
        System.out.println("Leaving the function scope"); 
    }

    public void visit(SimpleDec dec, int level, boolean flag)
    {
        if (inParams)
            paramTypeList.add(dec.typ.typ); 

        if (dec.typ.typ == NameTy.VOID)
        {
            indent(level);
            System.out.println(ANSI_RED + "ERROR [ Variable Type ] for " + dec.name + " on line " + (dec.pos + 1) + " expected (INT, BOOL) got VOID. Interpreting as INT." + ANSI_RESET); 
            dec.typ.typ = NameTy.INT; 
        }

        TableElement element = new TableElement(dec.pos, dec.name, dec.typ.typ, null, false, level); 
        insertElementToTable(scopeCounter, element, level); 
    }

    public void visit(ArrayDec dec, int level, boolean flag)
    {        
        if (inParams)
            paramTypeList.add(dec.typ.typ); 

        if (dec.typ.typ == NameTy.VOID)
        {
            indent(level); 
            System.out.println(ANSI_RED + "ERROR [ Array Type ] on line " + (dec.pos + 1) + " expected (INT, BOOL) got VOID. Interpreting as INT." + ANSI_RESET); 
            dec.typ.typ = NameTy.INT; 
        }

        TableElement element = new TableElement(dec.pos, dec.name, dec.typ.typ, null, false, level); 
        insertElementToTable(scopeCounter, element, level); 
    }

    public void visit(DecList list, int level, boolean flag)
    {
        while(list != null && list.head != null)
        {
            list.head.accept(this, level, flag);
            list = list.tail; 
        }
    }    

    public void visit(VarDecList list, int level, boolean flag)
    {
        while (list != null)
        {
            list.head.accept(this, level, flag); 
            list = list.tail; 
        }
    }

    public void visit(ExpList list, int level, boolean flag)
    {
        while (list != null)
        {
            list.head.accept(this, level, flag); 
            list = list.tail; 
        }
    }
}
