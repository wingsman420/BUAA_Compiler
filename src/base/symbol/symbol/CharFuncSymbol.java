package base.symbol.symbol;

import base.Token;
import base.symbol.Symbol;
import base.symbol.SymbolKey;

import java.util.ArrayList;

public class CharFuncSymbol extends Symbol {
    ArrayList<Symbol> args;
    public CharFuncSymbol(int id, int lineNumber, String name, ArrayList<Symbol> args) {
        super(id, lineNumber, name);
        super.isConst = false;
        super.isArray = false;
        super.isFunc = true;
        super.isInt = false;
        super.isVoid = false;
        super.type = SymbolKey.CharFunc;
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
