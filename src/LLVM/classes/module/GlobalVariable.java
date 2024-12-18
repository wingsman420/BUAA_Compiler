package LLVM.classes.module;

import LLVM.RealValue.*;
import LLVM.classes.Value;

public class GlobalVariable extends GlobalValue{
    private String type;
    private RealValue value;
    private boolean isConst;

    public GlobalVariable(int id,String name, String type,boolean isConst) {
        super(id,false,name);
        this.type = type;
        this.isConst = false;
        //修改了输出
    }

    public void setValue(RealValue value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public RealValue getInitialValue() {
        return value;
    }

    public boolean isConst() {
        return isConst;
    }

    public int getLength()
    {
        return value.getLength();
    }

    @Override
    public String toString() {
        if (!isConst)
        {
            if (value instanceof AChar) {
                return "@" + getName() + " " + "= global i8 " + ((AChar) value).getInitValue();
            }
            else if (value instanceof AInt) {
                return "@" + getName() + " " + "= global i32 " + ((AInt) value).getInitValue();
            }
            else if(value instanceof CharArray)
            {
                return "@" + getName() + " " +  "= dso_local global [" + ((CharArray) value).getLength()
                        + " x i8] " +  value.toString();
            }
            else
            {
                return "@" + getName() + " " +  "= dso_local global [" + ((IntArray) value).getLength()
                        + " x i32] " +  value.toString();
            }
        }
        else
        {
            if (value instanceof AChar) {
                return "@" + getName() + " " + "= constant i8 " + ((AChar) value).getInitValue();
            }
            else if (value instanceof AInt) {
                return "@" + getName() + " " + "= constant i32 " + ((AInt) value).getInitValue();
            }
            else if(value instanceof CharArray)
            {
                return "@" + getName() + " " +  "= constant [" + ((CharArray) value).getLength()
                        + " x i8] " +  value.toString();
            }
            else
            {
                return "@" + getName() + " " +  "= constant [" + ((IntArray) value).getLength()
                        + " x i32] " +  value.toString();
            }
        }
    }
}
