package base.symbol.symbol;

import LLVM.RealValue.AChar;
import LLVM.RealValue.AInt;
import LLVM.RealValue.CharArray;
import LLVM.RealValue.IntArray;
import base.Token;
import base.symbol.Symbol;
import base.symbol.SymbolKey;

import java.util.ArrayList;

public class ConstIntArraySymbol extends Symbol {
    private int length;

    public ConstIntArraySymbol(int id, int lineNumber, String name, int length) {
        super(id, lineNumber, name);
        super.isConst = true;
        super.isArray = true;
        super.isFunc = false;
        super.isInt = true;
        super.isVoid = false;
        super.type = SymbolKey.ConstIntArray;
        this.length = length;
    }

    public ConstIntArraySymbol(int id, int lineNumber, String name) {
        super(id, lineNumber, name);
        super.isConst = true;
        super.isArray = true;
        super.isFunc = false;
        super.isInt = true;
        super.isVoid = false;
        super.type = SymbolKey.ConstIntArray;
        this.length = 0;
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
