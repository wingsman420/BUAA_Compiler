package LLVM.classes.BlockItems.items;

import LLVM.classes.BlockItems.Instruction;

public class ImmINInst extends Instruction {
    private boolean isIG;
    private boolean typed;
    //立即数读取
    private int value;
    public ImmINInst(int id, int value) {
        super(id);
        this.value = value;
        this.isIG = false;
        this.typed = false;
    }

    public ImmINInst(int id, int value, int zero) {
        super(id);
        this.value = value;
        this.isIG = true;
        this.typed = false;
    }

    public ImmINInst(int id, int value, String type) {
        super(id);
        this.value = value;
        this.isIG = true;
        this.typed = true;
    }

    @Override
    public String toString() {
        if (isIG) {
            return "%" + id + " = add nsw i32 0, %"  + value;
        }
        else
        {
            return "%" + id + " = add nsw i32 0,"  + value;
        }

    }
}
