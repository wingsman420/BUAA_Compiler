package LLVM.classes.module;

import LLVM.classes.BlockItems.BasicBlock;
import LLVM.classes.Value;
import frontend.FileProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Function extends GlobalValue{

    private String returnType;
    private List<Argument> arguments;
    private List<BasicBlock> basicBlocks;

    public Function(int id,String name, String returnType) {
        super(id,true,name);
        this.returnType = returnType;
        this.arguments = new ArrayList<>();
        this.basicBlocks = new ArrayList<>();
    }

    public void addArgument(Argument argument) {
        this.arguments.add(argument);
    }

    public void addArgument(ArrayList<Argument> arguments) {
        this.arguments.addAll(arguments);
    }

    public void addBasicBlock(BasicBlock basicBlock) {
        this.basicBlocks.add(basicBlock);
    }

    public String getReturnType() {
        return returnType;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    public List<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    @Override
    public String toString() {
        return "";
    }

    public void print(FileProcessor fileProcessor) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(" ");
        int i = 0;
        for (Argument argument : arguments) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(argument.toString());
            i++;
        }
        if (returnType.equals("VOIDTK"))
        {
            fileProcessor.writeByLine("define dso_local void @" + name+"("+ sb +")" + "{");
        }
        else if (returnType.equals("INTTK"))
        {
            fileProcessor.writeByLine("define dso_local i32 @" + name +"("+ sb +")" + "{");
        }
        else
        {
            fileProcessor.writeByLine("define dso_local i8 @" + name +"("+ sb +")" + "{");
        }
        for (BasicBlock basicBlock : basicBlocks)
        {
            basicBlock.print(fileProcessor);
        }
        fileProcessor.writeByLine("}");
        fileProcessor.writeByLine("\n");
    }
}
