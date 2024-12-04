package base.symbol.symbol;

import base.symbol.Symbol;
import base.symbol.SymbolKey;

import java.util.ArrayList;

public class VoidFuncSymbol extends Symbol {
    ArrayList<Symbol> args;
    public VoidFuncSymbol(int id, int lineNumber, String name, ArrayList<Symbol> args) {
        super(id, lineNumber, name);
        super.isConst = false;
        super.isArray = false;
        super.isFunc = true;
        super.isInt = false;
        super.isVoid = true;
        super.type = SymbolKey.VoidFunc;
        this.args = args;
    }

    public ArrayList<Symbol> getArgs() {
        return args;
    }

    @Override
    public int getinitValueByPosition(int length) {
        return 0;
    }

    @Override
    public int getInitValue() {
        return 0;
    }
}
