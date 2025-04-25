package absyn; 

public class OpExp extends Exp
{
    public final static int PLUS = 0; 
    public final static int MINUS = 1; 
    public final static int UMINUS = 2; 
    public final static int MUL = 3; 
    public final static int DIV = 4; 
    public final static int EQ = 5; 
    public final static int NE = 6; 
    public final static int LT = 7; 
    public final static int LE = 8; 
    public final static int GT = 9; 
    public final static int GE = 10; 
    public final static int NOT = 11; 
    public final static int AND = 12; 
    public final static int OR = 13; 
    public final static int TESTEQ = 14; 
    public final static int ERROR = 15; 

    public Exp left; 
    public int op; 
    public Exp right; 

    public OpExp(int pos, Exp left, int op, Exp right)
    {
        this.pos = pos; 
        this.left = left; 
        this.op = op; 
        this.right = right; 
    }

    public void accept(AbsynVisitor visitor, int level, boolean flag)
    {
        visitor.visit(this, level, flag); 
    }

}