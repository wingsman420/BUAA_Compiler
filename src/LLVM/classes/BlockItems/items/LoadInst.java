package LLVM.classes.BlockItems.items;

import LLVM.classes.BlockItems.Instruction;

public class LoadInst extends Instruction {
    private int name;
    private String type;

    public LoadInst(int id, int name, String type) {
        super(id);
        this.name = name;
        this.type = type;
    }

    public boolean isInt()
    {
        return type.equals("INTTK");
    }

    @Override
    public String toString() {
        if (type.equals("INTTK")) {
            return "%" + id + " = load i32, i32* %" + name;
        }
        else
        {
            return "%" + id + " = load i8, i8* %" + name;
        }
    }
}
