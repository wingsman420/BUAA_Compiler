package base.symbol;

public abstract class Symbol {
    private int id;
    private int lineNumber;
    private String name;
    protected SymbolKey type;
    protected boolean isFunc;
    protected boolean isArray;
    protected boolean isConst;
    protected boolean isInt;
    protected boolean isVoid;

    public Symbol(int id, int lineNumber,String name) {
        this.id = id;
        this.lineNumber = lineNumber;
        this.name = name;
    }

    public boolean ISFunc()
    {
        return isFunc;
    }

    public boolean ISArray()
    {
        return isArray;
    }

    public boolean ISConst()
    {
        return isConst;
    }

    public boolean ISInt()
    {
        return isInt;
    }

    public boolean ISVoid()
    {
        return isVoid;
    }

    @Override
    public String toString()
    {
        return  name + " " + type.getName();
    }
}
