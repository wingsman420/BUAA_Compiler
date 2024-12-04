package LLVM.classes.BlockItems.items;

import LLVM.classes.BlockItems.Instruction;

public class ReverseInst extends Instruction {
    private int value;

    public ReverseInst(int id,int value) {
        super(id);
        this.value = value;
    }

    @Override
    public String toString() {
        return "%" + id + " = mul nsw i32 " + "%" + value + " , -1" ;
    }
}
