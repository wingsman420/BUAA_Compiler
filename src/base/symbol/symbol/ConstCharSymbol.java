package base.symbol.symbol;

import base.Token;
import base.symbol.Symbol;
import base.symbol.SymbolKey;

import java.util.ArrayList;

public class ConstCharSymbol extends Symbol {
    public ConstCharSymbol(int id, int lineNumber, String name) {
        super(id, lineNumber, name);
        super.isConst = true;
        super.isArray = false;
        super.isFunc = false;
        super.isInt = false;
        super.isVoid = false;
        super.type = SymbolKey.ConstChar;
    }

    public ArrayList<Symbol> getArgs() {
        return null;
    }
}
