package LLVM.classes.BlockItems.items;

import LLVM.classes.BlockItems.Instruction;

public class NotInst extends Instruction {
    private int value;

    public NotInst(int id,int value) {
        super(id);
        this.value = value;
    }

    @Override
    public String toString() {
        return "%" + id + " = icmp eq i32 %" + value + ", 0";
    }
}
