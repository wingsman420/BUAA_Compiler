package LLVM.classes.BlockItems;

import LLVM.TempCounter;
import LLVM.classes.BlockItems.items.BrInst;
import LLVM.classes.BlockItems.items.RetInst;
import LLVM.classes.Value;
import frontend.FileProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BasicBlock extends Value {
    private String name;
    private List<Instruction> instructions;

    public BasicBlock(int id,String name) {
        super(id);
        this.name = name;
        this.instructions = new ArrayList<>();
    }

    public BasicBlock(int id) {
        super(id);
        this.name = "name";
        this.instructions = new ArrayList<>();
    }

    public int getOrder()
    {
        return instructions.get(0).getId();
    }

    public void addInstruction(Instruction instruction) {
        this.instructions.add(instruction);
    }

    public void addInstruction(ArrayList<Instruction> instructions) {
        this.instructions.addAll(instructions);
    }

    public String getName() {
        return name;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    @Override
    public String toString() {
        return "";
    }

    public void print(FileProcessor fileProcessor) throws IOException {
        boolean first = false;
        for (Instruction instruction : instructions) {


            fileProcessor.writeByLine("\t" +  instruction.toString());

            if (instruction instanceof BrInst)
            {
                first = true;
            }
            if ((instruction instanceof BrInst || instruction instanceof RetInst) && !instruction.equals(instructions.get(instructions.size()-1)))
            {
                fileProcessor.writeByLine("blockLcy" + TempCounter.counter++ + ":");
            }
        }
    }
}
