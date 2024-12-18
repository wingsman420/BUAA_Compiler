package base.symbol.symbol;

import LLVM.RealValue.IntArray;
import base.symbol.Symbol;
import base.symbol.SymbolKey;

import java.util.ArrayList;

public class IntArraySymbol extends Symbol {
    private int length;

    public IntArraySymbol(int id, int lineNumber, String name) {
        super(id, lineNumber, name);
        super.isConst = false;
        super.isArray = true;
        super.isFunc = false;
        super.isInt = true;
        super.isVoid = false;
        super.type = SymbolKey.IntArray;
        this.length = 0;
    }

    public IntArraySymbol(int id, int lineNumber, String name,int length) {
        super(id, lineNumber, name);
        super.isConst = false;
        super.isArray = true;
        super.isFunc = false;
        super.isInt = true;
        super.isVoid = false;
        super.type = SymbolKey.IntArray;
        this.length = length;
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

    @Override
    public int getLength() {
        return length;
    }
}
