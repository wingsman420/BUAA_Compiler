package base.symbol;

import base.Rs.RealFuncSymbol;
import base.Rs.RealVarSymbol;
import frontend.FileProcessor;

import java.io.IOException;
import java.util.ArrayList;

public class SymbolTable {
    private int level;
    public SymbolTable fatherTable;
    public ArrayList<Symbol> symbols;
    public ArrayList<SymbolTable> children;
    public int all;
    public ArrayList<RealFuncSymbol> Fs;
    public ArrayList<RealVarSymbol> Vs;

    public SymbolTable(SymbolTable fatherTable, int level) {
        this.fatherTable = fatherTable;
        this.level = level;
        this.symbols = new ArrayList<>();;
        this.children = new ArrayList<>();
        this.all = 0;
        this.Fs = new ArrayList<>();
        this.Vs = new ArrayList<>();
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

    public boolean alreadyExist(String name)
    {
        for (Symbol symbol : symbols) {
            if (symbol.getName().equals(name))
            {
                return true;
            }
        }
        return false;
    }

    public Symbol legalVar(String name)
    {
        SymbolTable symbolTable = this;
        do {
            for (Symbol symbol : symbolTable.symbols) {
                if (symbol.getName().equals(name))
                {
                    return symbol;
                }
            }
            symbolTable = symbolTable.fatherTable;
        } while (symbolTable != null);
        return null;
    }


}
