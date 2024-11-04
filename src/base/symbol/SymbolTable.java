package base.symbol;

import frontend.FileProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SymbolTable {
    private int level;
    public SymbolTable fatherTable;
    public ArrayList<Symbol> symbols;
    public ArrayList<SymbolTable> children;
    public int all;

    public SymbolTable(SymbolTable fatherTable, int level) {
        this.fatherTable = fatherTable;
        this.level = level;
        this.symbols = new ArrayList<>();;
        this.children = new ArrayList<>();
        this.all = 0;
    }

    public int getLevel() {
        return level;
    }

    public void output(FileProcessor fileProcessor,int id) throws IOException {
        for (Symbol symbol : symbols) {
            fileProcessor.writeByLine(id+ " " + symbol.toString());
        };
        int temp = 0;
        for (SymbolTable child : children) {
            child.output(fileProcessor, id + ++temp);
        }
    }

    public ArrayList<SymbolTable> returnAsStack() {
        ArrayList<SymbolTable> stack = new ArrayList<>();
        for (SymbolTable child : children) {
            stack.add(child);
            stack.addAll(child.returnAsStack());
        }
        return stack;
    }


}
