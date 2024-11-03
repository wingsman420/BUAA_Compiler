package base.symbol.symbol;

import base.symbol.Symbol;
import base.symbol.SymbolKey;

public class VoidFuncSymbol extends Symbol {
    public VoidFuncSymbol(int id, int lineNumber, String name) {
        super(id, lineNumber, name);
        super.isConst = false;
        super.isArray = false;
        super.isFunc = true;
        super.isInt = false;
        super.isVoid = true;
        super.type = SymbolKey.VoidFunc;
    }
}
