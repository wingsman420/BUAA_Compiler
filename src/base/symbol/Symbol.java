package base.symbol;

import LLVM.RealValue.RealValue;
import LLVM.classes.module.GlobalVariable;

import java.util.ArrayList;

public abstract class Symbol {
    private int id;
    private int lineNumber;
    private String name;
    protected SymbolKey type;
    protected boolean isFunc;
    public boolean isArray;
    protected boolean isConst;
    protected boolean isInt;
    protected boolean isVoid;
    protected RealValue rv;
    protected boolean isFuncParam;

    public Symbol(int id, int lineNumber,String name) {
        this.id = id;
        this.lineNumber = lineNumber;
        this.name = name;
        this.isFuncParam = false;
    }

    public int getId() {
        return id;
    }
    public void setFuncParam(boolean funcParam) {
        isFuncParam = funcParam;
    }

    public boolean isFuncParam() {
        return false;
        //逻辑修改暂时不用相关函数
    }

    public abstract int getLength();

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

    public String getName() {
        return name;
    }

    public String getType()
    {
        return type.toString();
    }

    public abstract ArrayList<Symbol> getArgs();

    @Override
    public String toString()
    {
        return  name + " " + type.getName();
    }

    public abstract int getinitValueByPosition(int length);

    public abstract int getInitValue();

    public void setRv(RealValue rv)
    {
        this.rv = rv;
    }

    public void setid(int i) {
        id = i;
    }
}
