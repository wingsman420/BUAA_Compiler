package base.symbol.symbol;

import LLVM.RealValue.CharArray;
import base.symbol.Symbol;
import base.symbol.SymbolKey;

import java.util.ArrayList;

public class ConstCharArraySymbol extends Symbol {
    private int length;

    public ConstCharArraySymbol(int id, int lineNumber, String name) {
        super(id, lineNumber, name);
        super.isConst = true;
        super.isArray = true;
        super.isFunc = false;
        super.isInt = false;
        super.isVoid = false;
        super.type = SymbolKey.ConstCharArray;
        length = 0;
    }

    public ConstCharArraySymbol(int id, int lineNumber, String name,int length) {
        super(id, lineNumber, name);
        super.isConst = true;
        super.isArray = true;
        super.isFunc = false;
        super.isInt = false;
        super.isVoid = false;
        super.type = SymbolKey.ConstCharArray;
        this.length = length;
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

    @Override
    public int getLength() {
        return length;
    }
}
