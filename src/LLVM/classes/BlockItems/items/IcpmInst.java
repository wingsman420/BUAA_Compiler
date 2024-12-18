package LLVM.classes.BlockItems.items;

import LLVM.classes.BlockItems.Instruction;

import java.lang.annotation.Inherited;

public class IcpmInst extends Instruction {

    private String type;
    private int left;
    private int right;

    public IcpmInst(int id,int left,int right,String type) {
        super(id);
        this.left = left;
        this.right = right;
        this.type = type;
    }

    @Override
    public String toString() {
        return "%" + id + " = icmp "+ type +  " i32 %" + left + ", %" + right;
    }

    //类型：eq ne sqt sqe slt sle
}
