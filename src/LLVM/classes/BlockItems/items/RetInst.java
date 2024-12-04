package LLVM.classes.BlockItems.items;

import LLVM.classes.BlockItems.Instruction;

public class RetInst extends Instruction {
    boolean immediate;
    boolean hasValue;
    boolean isInt;
    int value;

    public RetInst(int id) {
        super(id);
        hasValue = false;
    }

    public RetInst(int id, boolean isInt, boolean immediate, int value) {
        super(id);
        hasValue = true;
        this.isInt = isInt;
        this.immediate = immediate;
        this.value = value;
    }

    @Override
    public String toString() {
        if(hasValue)
        {
            if (immediate)
            {
                if (isInt)
                {
                    return "ret i32 " + value;
                }
                else
                {
                    return "ret i8 " + value;
                }
            }
            else
            {
                if (isInt)
                {
                    return "ret i32 %" + value;
                }
                else
                {
                    return "ret i8 %" + value;
                }
            }
        }
        else
        {
            return "ret void";
        }
    }
}
