import absyn.*;

public class ShowTreeVisitor implements AbsynVisitor {

    final static int SPACES = 4;

    private void indent( int level ) {
        for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
    }

    public void visit(NameTy typ, int level, boolean flag)
    {
        indent(level); 
        System.out.println("NameTy: "); 
        if (typ.typ == NameTy.BOOL)
            System.out.println("BOOL"); 
        else if (typ.typ == NameTy.INT)
            System.out.println("INT"); 
        else if (typ.typ == NameTy.VOID)
            System.out.println("VOID"); 
    }

    public void visit(SimpleVar var, int level, boolean flag)
    {
        indent(level); 
        System.out.println("SimpleVar: " + var.name); 
    }

    public void visit(IndexVar var, int level, boolean flag)
    {
        indent(level++); 
        System.out.println("IndexVar: " + var.name); 
        var.index.accept(this, level, flag); 
    }

    public void visit (NilExp exp, int level, boolean flag)
    {
        indent(level); 
        System.out.println("NilExp: NULL"); 
    }

    public void visit (IntExp exp, int level, boolean flag)
    {
        indent(level); 
        System.out.println("IntExp: " + exp.value); 
    }

    public void visit (BoolExp exp, int level, boolean flag)
    {
        indent(level++); 
        System.out.println("BoolExp: " + exp.value); 
    }

    public void visit (VarExp exp, int level, boolean flag)
    {
        indent(level++); 
        System.out.println("VarExp: "); 
        exp.variable.accept(this, level, flag); 
    }

    public void visit (CallExp exp, int level, boolean flag)
    {
        indent(level++); 
        System.out.println("CallExp:"); 
        indent(level); 
        System.out.println("FunctionName: " + exp.func); 
        ExpList list = exp.args; 
        while (list != null)
        {
            list.head.accept(this, level, flag); 
            list = list.tail; 
        }
    }

    public void visit(OpExp exp, int level, boolean flag) 
    {
        indent(level++);
        System.out.print("OpExp:"); 

        switch(exp.op)
        {
            case OpExp.PLUS:   System.out.println( " + " );  break;
            case OpExp.MINUS:  System.out.println( " - " );  break;
            case OpExp.UMINUS: System.out.println( " - " );  break;
            case OpExp.MUL:    System.out.println( " * " );  break;
            case OpExp.DIV:    System.out.println( " / " );  break;
            case OpExp.EQ:     System.out.println( " = " );  break;
            case OpExp.NE:     System.out.println( " != " ); break; 
            case OpExp.LT:     System.out.println( " < " );  break;
            case OpExp.LE:     System.out.println( " <= ");  break; 
            case OpExp.GT:     System.out.println( " > " );  break;
            case OpExp.GE:     System.out.println( " >= ");  break; 
            case OpExp.NOT:    System.out.println( " ~ " );  break; 
            case OpExp.AND:    System.out.println( " && " ); break; 
            case OpExp.OR:     System.out.println( " || " ); break; 
            default: System.out.println( "Unrecognized operator at line " + exp.pos);
        }

        if (exp.left != null)
            exp.left.accept(this, level, flag);

        exp.right.accept(this, level, flag); 
    }

    public void visit (AssignExp exp, int level, boolean flag)
    {
        indent(level++); 
        System.out.println("AssignExp:"); 
        exp.lhs.accept(this, level, flag); 
        exp.rhs.accept(this, level, flag); 
    }

    public void visit (IfExp exp, int level, boolean flag)
    {
        indent(level++); 
        System.out.println("IfExp:"); 
        if (exp.test != null)
            exp.test.accept(this, level, flag); 
        if (exp.thenpart != null)
            exp.thenpart.accept(this, level, flag); 
        if (exp.elsepart != null)
            exp.elsepart.accept(this, level, flag); 
    }

    public void visit (WhileExp exp, int level, boolean flag)
    {
        indent(level++); 
        System.out.println("WhileExp:"); 
        if (exp.test != null)
            exp.test.accept(this, level, flag);   
        if (exp.body != null)
            exp.body.accept(this, level, flag); 
    }

    public void visit (ReturnExp exp, int level, boolean flag)
    {
        indent(level++); 
        System.out.println("ReturnExp:"); 
        if (exp.exp != null)
            exp.exp.accept(this, level, flag); 
    }

    public void visit (CompoundExp exp, int level, boolean flag)
    {
        indent(level++); 
        System.out.println("CompoundExp:"); 

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
        indent(level++); 
        System.out.println("FunctionDec: "); 
        indent(level); 

        if (dec.result.typ == NameTy.BOOL)
            System.out.println("(Function Type) NameTy: BOOL"); 
        else if (dec.result.typ == NameTy.INT)
            System.out.println("(Function Type) NameTy: INT"); 
        else if (dec.result.typ == NameTy.VOID)
            System.out.println("(Function Type) NameTy: VOID"); 

        VarDecList list = dec.params; 

        while (list != null && list.head != null)
        {
            list.head.accept(this, level, flag); 
            list = list.tail; 
        }
        
        if (dec.body != null)
            dec.body.accept(this, level, flag);  
    }

    public void visit(SimpleDec dec, int level, boolean flag)
    {
        indent(level++); 
        System.out.println("SimpleDec:"); 
        indent(level); 
        if (dec.typ.typ == NameTy.BOOL)
            System.out.println("(Variable Type) NameTy: BOOL"); 
        else if (dec.typ.typ == NameTy.INT)
            System.out.println("(Variable Type) NameTy: INT"); 
        else if (dec.typ.typ == NameTy.VOID)
            System.out.println("(Variable Type) NameTy: VOID"); 
        indent(level); 
        System.out.println("(Variable Name) SimpleDecName: " + dec.name); 
    }

    public void visit(ArrayDec dec, int level, boolean flag)
    {
        indent(level++);
        System.out.println("ArrayDec: "); 
        indent(level); 
        if (dec.typ.typ == NameTy.BOOL)
            System.out.println("(Array Type) NameTy: BOOL"); 
        else if (dec.typ.typ == NameTy.INT)
            System.out.println("(Array Type) NameTy: INT"); 
        else if (dec.typ.typ == NameTy.VOID)
            System.out.println("(Array Type) NameTy: VOID"); 
        indent(level); 
        System.out.println("(Array Name) Name: " + dec.name); 
        indent(level); 
        System.out.println("(Array Size) Size: " + ((dec.size == 0) ? "0" : "based on a variable")); 
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
