package LLVM.classes.BlockItems.items;

import LLVM.classes.BlockItems.Instruction;

public class LoadFromGlobalInst extends Instruction {
    private String name;
    private String type;

    public LoadFromGlobalInst(int id, String name, String type) {
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
            return "%" + id + " = load i32, i32* " + name;
        }
        else
        {
            return "%" + id + " = load i8, i8* " + name;
        }

    }
}
