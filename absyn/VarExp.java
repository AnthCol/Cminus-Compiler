package absyn; 

public class VarExp extends Exp
{
    public Var variable; 

    public VarExp(int pos, Var variable)
    {
        this.pos = pos; 
        this.variable = variable; 
    }

    public void accept(AbsynVisitor visitor, int level, boolean flag)
    {
        visitor.visit(this, level, flag); 
    }

}


/*
    void callExp(CallExp exp)
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

*/