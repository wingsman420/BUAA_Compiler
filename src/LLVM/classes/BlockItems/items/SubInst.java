package LLVM.classes.BlockItems.items;

import LLVM.classes.BlockItems.Instruction;

public class SubInst extends Instruction {
    private int value1;
    private int value2;

    public SubInst(int id,int value1,int value2) {
        super(id);
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public String toString() {
        return "%" + id + " = sub nsw i32 " + "%" + value2 + " , %" + value1;
    }
}
