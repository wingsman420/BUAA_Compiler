package base.symbol.symbol;

import base.symbol.Symbol;
import base.symbol.SymbolKey;

import java.util.ArrayList;

public class ConstCharArraySymbol extends Symbol {
    public ConstCharArraySymbol(int id, int lineNumber, String name) {
        super(id, lineNumber, name);
        super.isConst = true;
        super.isArray = true;
        super.isFunc = false;
        super.isInt = false;
        super.isVoid = false;
        super.type = SymbolKey.ConstCharArray;
    }

    public ArrayList<Symbol> getArgs() {
        return null;
    }
}
