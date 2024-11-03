package parser;

import base.Token;
import base.symbol.Symbol;
import base.symbol.SymbolTable;
import base.symbol.symbol.*;
import frontend.FileProcessor;

import java.io.IOException;
import java.util.ArrayList;

public class SymbolAnalyser {

    private ArrayList<Token> grammar;
    private Token thisToken;
    private int nowPosition;
    private SymbolTable root;
    private SymbolTable now;
    private boolean alreadyIn;

    public SymbolAnalyser(ArrayList<Token> grammar)
    {

    }

}
