import absyn.*; 
import java.util.*; 

/*
    Class works for both variables and functions.
*/
public class TableElement
{
    public int pos; 
    public String name; 
    public int type; 
    public ArrayList<Integer> variableTypes; 
    public boolean hasBody; 
    public int level; 

    private String determineType(int typeVal)
    {
        switch(typeVal)
        {
            case NameTy.BOOL: return "bool"; 
            case NameTy.INT:  return "int"; 
            case NameTy.VOID: return "void"; 
        } 
        return "unknown"; 
    }

    public void print()
    {
    

        if (variableTypes == null)
        {
            SemanticAnalyzer.indent(level); 
            System.out.println(name + ": " + determineType(type)); 
        }
        else 
        {
            SemanticAnalyzer.indent(1); 
            System.out.printf(name + ": ("); 
            if (variableTypes.size() == 0)
                System.out.printf("void"); 
            else
            {
                for (int i = 0; i < variableTypes.size(); i++)
                {
                    System.out.printf(determineType(variableTypes.get(i))); 
                    
                    if (i + 1 != variableTypes.size())
                        System.out.printf(", "); 
                } 
            }
            System.out.println(") -> " + determineType(type)); 
        }
    }

    public TableElement(int pos, String name, int type, ArrayList<Integer> variableTypes, boolean hasBody, int level)
    {
        this.pos = pos; 
        this.name = name; 
        this.type = type; 
        this.variableTypes = variableTypes; 
        this.hasBody = hasBody; 
        this.level = level; 
    }
}

