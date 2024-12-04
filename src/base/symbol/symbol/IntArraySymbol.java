package base.symbol.symbol;

import LLVM.RealValue.IntArray;
import base.symbol.Symbol;
import base.symbol.SymbolKey;

import java.util.ArrayList;

public class IntArraySymbol extends Symbol {
    public IntArraySymbol(int id, int lineNumber, String name) {
        super(id, lineNumber, name);
        super.isConst = false;
        super.isArray = true;
        super.isFunc = false;
        super.isInt = true;
        super.isVoid = false;
        super.type = SymbolKey.IntArray;
    }

    public ArrayList<Symbol> getArgs() {
        return null;
    }

    @Override
    public int getinitValueByPosition(int length) {
        return ((IntArray)rv).getList().get(length);
    }

    @Override
    public int getInitValue() {
        return 0;
    }
}
