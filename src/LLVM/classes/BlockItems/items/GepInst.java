package LLVM.classes.BlockItems.items;

import LLVM.classes.BlockItems.Instruction;

public class GepInst extends Instruction {

    private int length;
    private String name;
    private int position;
    private boolean isInt;

    public GepInst(int id,int length,String name,int position,boolean isInt) {
        super(id);
        this.length = length;
        this.name = name;
        this.position = position;
        this.isInt = isInt;
    }

    public GepInst(int id,String name,int position,boolean isInt) {
        super(id);
        this.length = -1;
        this.name = name;
        this.position = position;
        this.isInt = isInt;
    }

    @Override
    public String toString() {
        if (length == -1) {
            if (isInt)
            {
                return "%" + id + " = getelementptr i32, i32* "+ name +",i32 " + position;
            }
            else
            {
                return "%" + id + " = getelementptr i8, i8* "+ name +",i8 " + position;
            }
        }
        if (isInt)
        {
            return "%" + id + " = getelementptr [" + length + " x i32], [" + length + " x i32]* " + name +
                    ", i32 0, i32 " + position;
        }
        else
        {
            return "%" + id + " = getelementptr [" + length + " x i8], [" + length + " x i8]* " + name +
                    ", i8 0, i8 " + position;
        }
    }
}
