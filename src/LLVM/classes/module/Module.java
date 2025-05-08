package LLVM.classes.module;

import frontend.FileProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Module {
    private List<GlobalVariable> globalVariables;
    public List<ConstString> constStrings;
    private List<Function> functions;

    public Module() {
        this.globalVariables = new ArrayList<>();
        this.functions = new ArrayList<>();
        this.constStrings = new ArrayList<>();
    }

    public void addConstString(ConstString constString) {
        this.constStrings.add(constString);
    }

    public void addGlobalVariable(GlobalVariable globalVariable) {
        this.globalVariables.add(globalVariable);
    }

    public void addFunction(Function function) {
        this.functions.add(function);
    }

    public List<GlobalVariable> getGlobalVariables() {
        return globalVariables;
    }

    public List<Function> getFunctions() {
        return functions;
    }

    public void deleteFunction(String functionName) {
        this.functions.removeIf(function -> function.getName().equals(functionName));
    }

    public String getFunctionType(String name) {
        for (Function function : functions) {
            if (function.getName().equals(name)) {
                return function.getReturnType();
            }
        }
        return null;
    }

    public Function getFunction(String name) {
        for (Function function : functions) {
            if (function.getName().equals(name)) {
                return function;
            }
        }
        return null;
    }

    public void print(FileProcessor fileProcessor) throws IOException {
        fileProcessor.writeByLine("declare i32 @getint()");
        fileProcessor.writeByLine("declare i32 @getchar()");
        fileProcessor.writeByLine("declare void @putint(i32)");
        fileProcessor.writeByLine("declare void @putch(i32)");
        fileProcessor.writeByLine("declare void @putstr(i8*)");
        //基本声明
        fileProcessor.writeByLine("\n");
        fileProcessor.writeByLine("\n");
        for (GlobalVariable globalVariable : globalVariables) {
            fileProcessor.writeByLine(globalVariable.toString());
        }
        fileProcessor.writeByLine("\n");
        fileProcessor.writeByLine("\n");
        for (ConstString constString : constStrings) {
            fileProcessor.writeByLine(constString.toString());
        }
        fileProcessor.writeByLine("\n");
        fileProcessor.writeByLine("\n");
        for (Function function : functions) {
            function.print(fileProcessor);
        }
    }

    public int getGlobalVariablesLength(String realName) {
        for (GlobalVariable globalVariable : globalVariables) {
            if (globalVariable.getName().equals(realName)) {
                return globalVariable.getLength();
            }
        }
        return -1;
    }
}
