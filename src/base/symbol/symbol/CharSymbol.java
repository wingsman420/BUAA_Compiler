package base.symbol.symbol;

import LLVM.RealValue.AChar;
import base.symbol.Symbol;
import base.symbol.SymbolKey;

import java.util.ArrayList;

public class CharSymbol extends Symbol {
    public CharSymbol(int id, int lineNumber, String name) {
        super(id, lineNumber, name);
        super.isConst = false;
        super.isArray = false;
        super.isFunc = false;
        super.isInt = false;
        super.isVoid = false;
        super.type = SymbolKey.Char;
    }

    public ArrayList<Symbol> getArgs() {
        return null;
    }

    @Override
    public int getinitValueByPosition(int length) {
        return 0;
    }

    @Override
    public int getInitValue() {
        return ((AChar)rv).getInitValue();
    }
}
