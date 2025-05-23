package absyn; 

public class ArrayDec extends VarDec
{
    public NameTy typ; 
    public String name; 
    public int size;  

    public ArrayDec (int pos, NameTy typ, String name, int size)
    {
        this.pos = pos; 
        this.typ = typ; 
        this.name = name; 
        this.size = size; 
    }

    public void accept (AbsynVisitor visitor, int level, boolean flag)
    {
        visitor.visit(this, level, flag); 
    }

}