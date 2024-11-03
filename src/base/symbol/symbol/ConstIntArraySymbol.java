package base.symbol.symbol;

import base.Token;
import base.symbol.Symbol;
import base.symbol.SymbolKey;

public class ConstIntArraySymbol extends Symbol {
    public ConstIntArraySymbol(int id, int lineNumber, String name) {
        super(id, lineNumber, name);
        super.isConst = true;
        super.isArray = true;
        super.isFunc = false;
        super.isInt = true;
        super.isVoid = false;
        super.type = SymbolKey.ConstIntArray;
    }
}
