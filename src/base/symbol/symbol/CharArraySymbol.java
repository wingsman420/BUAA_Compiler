package base.symbol.symbol;

import LLVM.RealValue.CharArray;
import base.symbol.Symbol;
import base.symbol.SymbolKey;

import java.util.ArrayList;

public class CharArraySymbol extends Symbol {

    public CharArraySymbol(int id, int lineNumber, String name) {
        super(id, lineNumber, name);
        super.isConst = false;
        super.isArray = true;
        super.isFunc = false;
        super.isInt = false;
        super.isVoid = false;
        super.type = SymbolKey.CharArray;
    }
    public ArrayList<Symbol> getArgs() {
        return null;
    }

    @Override
    public int getinitValueByPosition(int length) {
        return ((CharArray)rv).getValue().charAt(length);
    }

    @Override
    public int getInitValue() {
        return 0;
    }
}
