package LLVM.classes.BlockItems.items;

import LLVM.classes.BlockItems.Instruction;

public class BrInst extends Instruction {
    private String trueDestination;

    private int cond;

    private String falseDestination;

    private boolean hasCond;



    public BrInst(String trueDestination, int cond, String falseDestination) {
        super(-1);
        this.trueDestination = trueDestination;
        this.cond = cond;
        this.falseDestination = falseDestination;
        hasCond = true;
    }

    public BrInst(String trueDestination) {
        super(-1);
        this.trueDestination = trueDestination;
        hasCond = false;
    }

    @Override
    public String toString() {
        if (hasCond)
        {
            return "br i1 %" + cond + " , label %" + trueDestination + " , label %" + falseDestination;
        }
        else
        {
            return "br label %" + trueDestination;
        }

    }
}
