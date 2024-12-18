package LLVM.classes.BlockItems.items;

import LLVM.classes.BlockItems.BinaryOperator;
import LLVM.classes.BlockItems.Instruction;
import LLVM.classes.Value;

public class StoreInst extends Instruction {
    private int address;
    private int value;
    private boolean isInt;
    private boolean imm;
    private boolean global;
    private String globalAddress;
    private boolean ptr;

    public StoreInst(int id, int value, int address, boolean isInt, boolean imm)
    {
        super(id);
        this.address = address;
        this.value = value;
        this.isInt = isInt;
        this.ptr = false;
        this.imm = imm;
    }

    public StoreInst(int id, int value, int address, boolean isInt, boolean imm,boolean ptr)
    {
        super(id);
        this.address = address;
        this.value = value;
        this.isInt = isInt;
        this.ptr = ptr;
        this.imm = false;
    }

    public StoreInst(int id, int value, String address, boolean isInt, boolean imm)
    {
        super(id);
        this.globalAddress = address;
        this.value = value;
        this.isInt = isInt;
        this.global = true;
        this.ptr = false;
        this.imm = false;
    }

    public StoreInst(int id, int value, String address, boolean isInt, boolean imm,boolean ptr)
    {
        super(id);
        this.globalAddress = address;
        this.value = value;
        this.isInt = isInt;
        this.global = true;
        this.ptr = ptr;
        this.imm = false;
    }


    @Override
    public String toString() {
        if (ptr)
        {
            if(global)
            {
                if (imm)
                {
                    if (isInt)
                    {
                        return "store i32* " + value + ", i32** " + globalAddress;
                    }
                    else
                    {
                        return "store i8* " + value + ", i8** " + globalAddress;
                    }
                }
                if (isInt)
                {
                    return "store i32* %" + value + ", i32** " + globalAddress;
                }
                else
                {
                    return "store i8* %" + value + ", i8** " + globalAddress;
                }
            }
            if (imm)
            {
                if (isInt)
                {
                    return "store i32* " + value + ", i32** %" + address;
                }
                else
                {
                    return "store i8* " + value + ", i8** %" + address;
                }
            }
            if (isInt)
            {
                return "store i32* %" + value + ", i32** %" + address;
            }
            else
            {
                return "store i8* %" + value + ", i8** %" + address;
            }
        }
        else
        {
            if(global)
            {
                if (imm)
                {
                    if (isInt)
                    {
                        return "store i32 " + value + ", i32* " + globalAddress;
                    }
                    else
                    {
                        return "store i8 " + value + ", i8* " + globalAddress;
                    }
                }
                if (isInt)
                {
                    return "store i32 %" + value + ", i32* " + globalAddress;
                }
                else
                {
                    return "store i8 %" + value + ", i8* " + globalAddress;
                }
            }
            if (imm)
            {
                if (isInt)
                {
                    return "store i32 " + value + ", i32* %" + address;
                }
                else
                {
                    return "store i8 " + value + ", i8* %" + address;
                }
            }
            if (isInt)
            {
                return "store i32 %" + value + ", i32* %" + address;
            }
            else
            {
                return "store i8 %" + value + ", i8* %" + address;
            }
        }

    }
}
