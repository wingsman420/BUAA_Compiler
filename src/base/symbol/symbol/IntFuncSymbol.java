package base.symbol.symbol;

import base.symbol.Symbol;
import base.symbol.SymbolKey;
import base.symbol.SymbolTable;

public class IntFuncSymbol extends Symbol {
    public IntFuncSymbol(int id, int lineNumber, String name) {
        super(id, lineNumber, name);
        super.isConst = false;
        super.isArray = false;
        super.isFunc = true;
        super.isInt = true;
        super.isVoid = false;
        super.type = SymbolKey.IntFunc;
    }
}
