package LLVM.classes.BlockItems.items;

import LLVM.classes.BlockItems.Instruction;

public class MulInst extends Instruction {
    private int value1;
    private int value2;

    public MulInst(int id,int value1,int value2) {
        super(id);
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public String toString() {
        return "%" + id + " = mul nsw i32 " + "%" + value1 + " , %" + value2;
    }
}
