package LLVM.classes.BlockItems.items;

import LLVM.classes.BlockItems.BinaryOperator;
import LLVM.classes.BlockItems.Instruction;
import LLVM.classes.Value;

public class AllocateInst extends Instruction {

    String space;

    public AllocateInst(int id,String space)
    {
        super(id);
        this.space = space;
    }

    @Override
    public String toString() {
        return "%" + id + " = alloca " + space;
    }
}
