package base.Rs;

import base.symbol.Symbol;

import java.util.ArrayList;

public class RealFuncSymbol{
    public String type;
    public String name;
    public int lineNumber;
    public ArrayList<RealVarSymbol> args;

    public RealFuncSymbol(String type, String name, int lineNumber,ArrayList<RealVarSymbol> args) {
        this.type = type;
        this.name = name;
        this.lineNumber = lineNumber;
        this.args = args;
    }
}
