package MIPS;

import LLVM.RealValue.AChar;
import LLVM.RealValue.AInt;
import LLVM.RealValue.CharArray;
import LLVM.RealValue.IntArray;
import LLVM.classes.BlockItems.BasicBlock;
import LLVM.classes.BlockItems.Instruction;
import LLVM.classes.BlockItems.items.AddInst;
import LLVM.classes.BlockItems.items.AllocateInst;
import LLVM.classes.BlockItems.items.BrInst;
import LLVM.classes.BlockItems.items.RetInst;
import LLVM.classes.module.*;
import LLVM.classes.module.Module;
import frontend.FileProcessor;

import java.io.IOException;
import java.util.HashMap;

public class MIPSGenerator {
    private Module module;
    private FileProcessor fileProcessor;
    private HashMap<Integer, Integer> regTable;
    //用来存%t -- t（fp）
    private int counter;
    //栈顶寄存器，记录我的sp走到哪里了

    public MIPSGenerator(Module module, FileProcessor fileProcessor) {
        this.module = module;
        this.fileProcessor = fileProcessor;
        regTable = new HashMap<>();
        counter = 0;
    }

    private void analyseModule() throws IOException {
        fileProcessor.writeByLine(".data\n");
        for (GlobalVariable globalVariable : module.getGlobalVariables())
        {
            if (globalVariable.getInitialValue() instanceof AInt)
            {
                fileProcessor.writeByLine('\t' + globalVariable.getName() + ": .word " + globalVariable.getInitialValue().toMips() + '\n');
            }
            else if(globalVariable.getInitialValue() instanceof AChar)
            {
                fileProcessor.writeByLine('\t' + globalVariable.getName() + ": .byte " + globalVariable.getInitialValue().toMips() + '\n');
            }
            else if(globalVariable.getInitialValue() instanceof IntArray)
            {
                fileProcessor.writeByLine('\t' + globalVariable.getName() + ": .word " + globalVariable.getInitialValue().toMips() + '\n');
            }
            else if(globalVariable.getInitialValue() instanceof CharArray)
            {
                fileProcessor.writeByLine('\t' + globalVariable.getName() + ": .byte " + globalVariable.getInitialValue().toMips() + '\n');
            }
        }
        for (ConstString constString : module.constStrings)
        {
            fileProcessor.writeByLine('\t' + constString.getName() + ": .asciiz " + constString.getValue() + '\n');
        }
        //到这里定义完了data段
        fileProcessor.writeByLine("\n.text\n");
        fileProcessor.writeByLine("\njal main\n");
        fileProcessor.writeByLine("li $v0, 10\n");
        fileProcessor.writeByLine("syscall\n\n");
        //跳转到main，并且之后直接结束

        for (Function function : module.getFunctions())
        {
            fileProcessor.writeByLine(function.getName()+ ":" + '\n');
            fileProcessor.writeByLine('\t' +  "addi $sp, $sp, -8");
            fileProcessor.writeByLine('\t' +  "sw   $ra, 4($sp)");
            fileProcessor.writeByLine('\t' +  "sw   $fp, 0($sp)");
            fileProcessor.writeByLine('\t' +  "move $fp, $sp");
            counter = 1;
            //参数在8开始的位置倒叙排列，也就是说，8（sp）代表最后一个被压入的参数
            int i = 0;
            for (BasicBlock basicBlock: function.getBasicBlocks())
            {
                if (i != 0)
                {
                    fileProcessor.writeByLine("block" + basicBlock.getId() + ":");
                }
                i++;
                for (Instruction instruction : basicBlock.getInstructions())
                {
                    analyseInstruction(instruction);
                    if (instruction instanceof RetInst || instruction instanceof BrInst)
                    {
                        break;
                    }
                }
            }
        }
    }

    private void analyseInstruction(Instruction instruction) throws IOException {
        if (instruction instanceof AddInst)
        {
            analyseAddInst((AddInst)instruction);
        }
        else if (instruction instanceof AllocateInst)
        {
            analyseAllocateInst((AllocateInst)instruction);
        }
        else if (instruction instanceof BrInst)
        {

        }
    }

    private void analyseAllocateInst(AllocateInst instruction) {

    }

    private void load(String reg) throws IOException {
        fileProcessor.writeByLine("li " + reg + ", " + reg + "\n");
    }

    private void load(String reg, int number) throws IOException {
        //栈读入
        fileProcessor.writeByLine("lw " + reg + ", -" + regTable.get(number) + "(fp)\n");
    }

    private void Store(String reg,int value) throws IOException {
        fileProcessor.writeByLine('\t'+ "addi $sp, $sp, -4");
        regTable.put(value,counter++ * 4);
        fileProcessor.writeByLine('\t' +  "sw   " + reg +  ", 0($sp)");
    }

    private void analyseAddInst(AddInst instruction) throws IOException {
        load("$t0",instruction.value1);
        load("$t1",instruction.value2);
        fileProcessor.writeByLine('\t' + "addu $t2, $t0, $t1\n");
        Store("$t2",instruction.getId());
    }
}
