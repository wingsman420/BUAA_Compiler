package LLVM.classes.BlockItems.items;

import LLVM.classes.BlockItems.Instruction;

public class CallFuncInst extends Instruction {

    private String functionName;
    private String functionType;
    private boolean output;


    public CallFuncInst(int id,String functionName,String functionType) {
        super(id);
        this.functionName = functionName;
        this.functionType = functionType;
        output = false;
    }

    public CallFuncInst(String functionName,String functionType) {
        super(-1);
        this.functionName = functionName;
        this.functionType = functionType;
        output = true;
    }

    public String getFunctionName() {
        return functionName;
    }
    public String getFunctionType() {
        return functionType;
    }
    public boolean isOutput() {
        return output;
    }

    @Override
    public String toString() {
        if (output)
        {
            return "call void " + functionName;
        }
        else
        {
            if (functionType.equals("INTTK"))
            {
                return "%" + id + " = call i32 " + functionName;
            }
            else if (functionType.equals("CHARTK"))
            {
                return "%" + id + " = call i8 " + functionName;
            }
            else
            {
                return "call void " + functionName;

            }
        }
    }
}
