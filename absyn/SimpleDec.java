package absyn; 

public class SimpleDec extends VarDec
{
    public NameTy typ; 
    public String name; 

    public SimpleDec(int pos, NameTy typ, String name)
    {
        this.pos = pos; 
        this.typ = typ; 
        this.name = name; 
    }

    public void accept(AbsynVisitor visitor, int level, boolean flag)
    {
        visitor.visit(this, level, flag); 
    }
}


/*
    public void visit(AssignExp exp, int offset, boolean isMemAddress)
	{
        emitComment("-> op (assign)"); 
        exp.lhs.accept(this, offset, true); 
        emitRM("ST", AC, offset, FP, "op: push left"); 
        exp.rhs.accept(this, offset, isMemAddress); 
        emitRM("LD", AC1, offset, FP, "op: load left"); 
        emitRM("ST", AC, 0, AC1, "assign: store value"); 
        emitComment("<-op"); 
	} 
*/

