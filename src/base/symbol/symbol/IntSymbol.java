package base.symbol.symbol;

import base.symbol.Symbol;
import base.symbol.SymbolKey;

public class IntSymbol extends Symbol {
    public IntSymbol(int id, int lineNumber, String name) {
        super(id, lineNumber, name);
        super.isConst = false;
        super.isArray = false;
        super.isFunc = false;
        super.isInt = true;
        super.isVoid = false;
        super.type = SymbolKey.Int;
    }
}
