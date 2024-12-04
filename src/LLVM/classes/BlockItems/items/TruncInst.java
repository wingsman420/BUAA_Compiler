package LLVM.classes.BlockItems.items;

import LLVM.classes.BlockItems.Instruction;

public class TruncInst extends Instruction {

    boolean intToChar;
    int value;

    public TruncInst(int id, boolean intToChar, int value) {
        super(id);
        this.intToChar = intToChar;
        this.value = value;
    }

    public boolean isIntToChar() {
        return intToChar;
    }

    @Override
    public String toString() {
        if (intToChar) {
            return "%" + id + " = trunc i32 %" + value + " to i8 ";
        }
        else
        {
            return "%" + id + " = zext i8 %" + value + " to i32 ";
        }

    }
}
