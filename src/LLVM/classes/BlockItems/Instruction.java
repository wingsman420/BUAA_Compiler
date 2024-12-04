package LLVM.classes.BlockItems;

import LLVM.classes.User;
import LLVM.classes.Value;

public abstract class Instruction extends User {

    public Instruction(int id) {
        super(id);
    }


    @Override
    public abstract String toString();

}
