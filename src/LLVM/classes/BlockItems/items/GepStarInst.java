package LLVM.classes.BlockItems.items;

import LLVM.classes.BlockItems.Instruction;

public class GepStarInst extends Instruction {

    private String value;
    private boolean isInt;

    public GepStarInst(int id, boolean isInt, String value) {
        super(id);
        this.isInt = isInt;
        this.value = value;
    }

    @Override
    public String toString() {
        if (isInt)
        {
            return "%" + id + " = load i32*, i32** " + value;
        }
        else
        {
            return "%" + id + " = load i8*, i8** " + value;
        }
    }
}
