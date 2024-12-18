package LLVM.classes.BlockItems.items;

import LLVM.classes.BlockItems.Instruction;

public class TruncInst extends Instruction {

    boolean intToChar;
    int value;
    boolean intTo1;

    public TruncInst(int id, boolean intToChar, int value) {
        super(id);
        this.intToChar = intToChar;
        this.value = value;
        this.intTo1 = false;
    }

    public TruncInst(int id,int value, boolean intTo1) {
        super(id);
        this.intToChar = false;
        this.value = value;
        this.intTo1 = intTo1;
    }

    public TruncInst(int id,int value) {
        super(id);
        this.intToChar = true;
        this.value = value;
        this.intTo1 = true;
    }

    public boolean isIntToChar() {
        return intToChar;
    }

    @Override
    public String toString() {
        if (intTo1 && intToChar) {
            return "%" + id + " = zext i1 %" + value + " to i32 ";
        }
        if (intToChar) {
            return "%" + id + " = trunc i32 %" + value + " to i8 ";
        }
        else if(intTo1)
        {
            return "%" + id + " = trunc i32 %" + value + " to i1 ";
        }
        else
        {
            return "%" + id + " = zext i8 %" + value + " to i32 ";
        }

    }
}
