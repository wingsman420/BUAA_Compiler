package base.symbol.symbol;

import base.Token;
import base.symbol.Symbol;
import base.symbol.SymbolKey;

import java.util.ArrayList;

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

    public ArrayList<Symbol> getArgs() {
        return null;
    }
}
