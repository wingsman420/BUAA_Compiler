package base.Rs;

import base.symbol.Symbol;

public class RealVarSymbol{
    public String type;
    public String name;
    public int lineNumber;

    public RealVarSymbol(String type, String name, int lineNumber) {
        this.type = type;
        this.name = name;
    }
}
