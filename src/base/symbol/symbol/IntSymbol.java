package base.symbol.symbol;

import LLVM.RealValue.AInt;
import base.symbol.Symbol;
import base.symbol.SymbolKey;

import java.util.ArrayList;

public class IntSymbol extends Symbol {
    public IntSymbol(int id, int lineNumber, String name) {
        super(id, lineNumber, name);
        super.isConst = false;
        super.isArray = false;
        super.isFunc = false;
        super.isInt = true;
        super.isVoid = false;
        super.type = SymbolKey.Int;
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
        return ((AInt)rv).getInitValue();
    }
}
