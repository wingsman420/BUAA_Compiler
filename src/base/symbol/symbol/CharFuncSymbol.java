package base.symbol.symbol;

import base.Token;
import base.symbol.Symbol;
import base.symbol.SymbolKey;

public class CharFuncSymbol extends Symbol {
    public CharFuncSymbol(int id, int lineNumber, String name) {
        super(id, lineNumber, name);
        super.isConst = false;
        super.isArray = false;
        super.isFunc = true;
        super.isInt = false;
        super.isVoid = false;
        super.type = SymbolKey.CharFunc;
    }
}
