package base.symbol.symbol;

import base.symbol.Symbol;
import base.symbol.SymbolKey;
import base.symbol.SymbolTable;

import java.util.ArrayList;

public class IntFuncSymbol extends Symbol {
    ArrayList<Symbol> args;
    public IntFuncSymbol(int id, int lineNumber, String name, ArrayList<Symbol> args) {
        super(id, lineNumber, name);
        super.isConst = false;
        super.isArray = false;
        super.isFunc = true;
        super.isInt = true;
        super.isVoid = false;
        super.type = SymbolKey.IntFunc;
        this.args = args;
    }

    public ArrayList<Symbol> getArgs() {
        return args;
    }
}
