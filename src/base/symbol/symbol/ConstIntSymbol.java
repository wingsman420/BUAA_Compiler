package base.symbol.symbol;

import LLVM.RealValue.AInt;
import base.Token;
import base.symbol.Symbol;
import base.symbol.SymbolKey;

import java.util.ArrayList;

public class ConstIntSymbol extends Symbol {
    public ConstIntSymbol(int id, int lineNumber, String name) {
        super(id, lineNumber, name);
        super.isConst = true;
        super.isArray = false;
        super.isFunc = false;
        super.isInt = true;
        super.isVoid = false;
        super.type = SymbolKey.ConstInt;
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
