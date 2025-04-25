package absyn; 

public class NilExp extends Exp
{
    public NilExp(int pos)
    {
        this.pos = pos;
    }

    public void accept(AbsynVisitor visitor, int level, boolean flag)
    {
        visitor.visit(this, level, flag); 
    }
}
