package base.symbol.symbol;

import LLVM.RealValue.CharArray;
import base.symbol.Symbol;
import base.symbol.SymbolKey;

import java.util.ArrayList;

public class CharArraySymbol extends Symbol {
    private int length;

    public CharArraySymbol(int id, int lineNumber, String name,int length) {
        super(id, lineNumber, name);
        super.isConst = false;
        super.isArray = true;
        super.isFunc = false;
        super.isInt = false;
        super.isVoid = false;
        super.type = SymbolKey.CharArray;
        this.length = length;
    }

    public CharArraySymbol(int id, int lineNumber, String name) {
        super(id, lineNumber, name);
        super.isConst = false;
        super.isArray = true;
        super.isFunc = false;
        super.isInt = false;
        super.isVoid = false;
        super.type = SymbolKey.CharArray;
        this.length = -1;
    }

    @Override
    public int getLength() {
        return length;
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
