package LLVM.classes.BlockItems.items;

import LLVM.classes.BlockItems.Instruction;

public class GepInst extends Instruction {

    private int length;
    private String name;
    private int position;
    private boolean isInt;
    private boolean getHeadAddress;
    private boolean positionIsImm;

    public GepInst(int id,int length,String name,int position,boolean isInt) {
        super(id);
        this.length = length;
        this.name = name;
        this.position = position;
        this.isInt = isInt;
        getHeadAddress = false;
        positionIsImm = false;
    }

    public GepInst(int FUCKYOU,int id,String name,int position,boolean isInt,boolean positionIsImm) {
        super(id);
        this.length = -1;
        this.name = name;
        this.position = position;
        this.isInt = isInt;
        getHeadAddress = false;
        this.positionIsImm = true;
    }

    public GepInst(int id,String name,int position,boolean isInt) {
        super(id);
        this.length = -1;
        this.name = name;
        this.position = position;
        this.isInt = isInt;
        getHeadAddress = false;
        positionIsImm = false;
    }

    public GepInst(int id,String name,boolean isInt,int length,boolean getHeadAddress) {
        super(id);
        this.length = length;
        this.name = name;
        this.position = 0;
        this.isInt = isInt;
        this.getHeadAddress = true;
        positionIsImm = false;
    }



    @Override
    public String toString() {
        if (getHeadAddress){
            if (isInt)
            {
                return "%" + id + " = getelementptr [" + length + " x i32], [" + length + " x i32]* " + name +
                        ", i32 0, i32 0";
            }
            else
            {
                return "%" + id + " = getelementptr [" + length + " x i8], [" + length + " x i8]* " + name +
                        ", i32 0, i32 0";
            }
        }
        if (positionIsImm)
        {
            if (isInt)
            {
                return "%" + id + " = getelementptr i32, i32* "+ name +",i32 " + position;
            }
            else
            {
                return "%" + id + " = getelementptr i8, i8* "+ name +",i32 " + position;
            }
        }
        if (length == -1) {

            if (isInt)
            {
                return "%" + id + " = getelementptr i32, i32* "+ name +",i32 %" + position;
            }
            else
            {
                return "%" + id + " = getelementptr i8, i8* "+ name +",i32 %" + position;
            }
        }
        if (isInt)
        {
            return "%" + id + " = getelementptr [" + length + " x i32], [" + length + " x i32]* " + name +
                    ", i32 0, i32 %" + position;
        }
        else
        {
            return "%" + id + " = getelementptr [" + length + " x i8], [" + length + " x i8]* " + name +
                    ", i8 0, i32 %" + position;
        }
    }
}
