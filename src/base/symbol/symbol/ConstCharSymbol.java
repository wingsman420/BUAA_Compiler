package base.symbol.symbol;

import LLVM.RealValue.AChar;
import base.Token;
import base.symbol.Symbol;
import base.symbol.SymbolKey;

import java.util.ArrayList;

public class ConstCharSymbol extends Symbol {
    public ConstCharSymbol(int id, int lineNumber, String name) {
        super(id, lineNumber, name);
        super.isConst = true;
        super.isArray = false;
        super.isFunc = false;
        super.isInt = false;
        super.isVoid = false;
        super.type = SymbolKey.ConstChar;
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

    @Override
    public int getLength() {
        return 0;
    }
}
