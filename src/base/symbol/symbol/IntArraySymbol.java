package base.symbol.symbol;

import base.symbol.Symbol;
import base.symbol.SymbolKey;

public class IntArraySymbol extends Symbol {
    public IntArraySymbol(int id, int lineNumber, String name) {
        super(id, lineNumber, name);
        super.isConst = false;
        super.isArray = true;
        super.isFunc = false;
        super.isInt = true;
        super.isVoid = false;
        super.type = SymbolKey.IntArray;
    }
}
