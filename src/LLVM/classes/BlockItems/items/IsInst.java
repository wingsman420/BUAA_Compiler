package LLVM.classes.BlockItems.items;

import LLVM.classes.BlockItems.Instruction;

public class IsInst extends Instruction {
    private int value;

    public IsInst(int id,int value) {
        super(id);
        this.value = value;
    }

    @Override
    public String toString() {
        return "%" + id + " = icmp ne i32 %" + value + ", 0";
    }
}
