package parser;

import Tree.BranchNode;
import Tree.LeafNode;
import Tree.Node;
import base.Token;
import base.symbol.Symbol;
import base.symbol.SymbolTable;
import base.symbol.symbol.*;
import frontend.FileProcessor;

import java.io.IOException;
import java.util.ArrayList;

public class SymbolAnalyser {

    private BranchNode rootFromParser;
    private BranchNode currentParserNode;
    private SymbolTable root;
    private SymbolTable now;
    private int nowLevel;
    private boolean alreadyIn;

    public SymbolAnalyser(BranchNode rootFromParser)
    {
        this.rootFromParser = rootFromParser;
        nowLevel = 1;
        root = new SymbolTable(null,nowLevel);
        currentParserNode = rootFromParser;
        now = root;
        alreadyIn = false;
        analyse(rootFromParser);
    }

    public boolean isNewStack(Node node)
    {
        if (node.getType().equals("<FuncDef>") || node.getType().equals("<MainFuncDef>"))
        {
            alreadyIn = true;
            return true;
        }
        if (node.getType().equals("<Block>"))
        {
            if (alreadyIn)
            {
                alreadyIn = false;
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean isNewIdents(Node node)
    {
        return node.getType().equals("<ConstDecl>") || node.getType().equals("<VarDecl>");
    }

    public void analyse(BranchNode node)
    {
        boolean in = false;
        if (node.getType().equals("<Block>"))
        {
            if (alreadyIn)
            {
                alreadyIn = false;
            }
            else
            {
                SymbolTable temp = new SymbolTable(now,nowLevel++);
                now.children.add(temp);
                now = temp;
                in = true;
            }
            for (Node child : node.getChildren())
            {
                if (child instanceof BranchNode)
                {
                    analyse((BranchNode) child);
                }
            }
        }
        else if (node.getType().equals("<FuncDef>"))//函数声明
        {
            Token temp1 = ((LeafNode)node.getChildren().get(0)).getToken();
            Token temp2 = ((LeafNode)node.getChildren().get(1)).getToken();
            if (temp1.getType().equals("VOIDTK"))
            {
                now.symbols.add(new VoidFuncSymbol(1,temp1.getLineNumber(),temp2.getValue()));
            }
            else if (temp1.getType().equals("INTTK"))
            {
                now.symbols.add(new IntFuncSymbol(1,temp1.getLineNumber(),temp2.getValue()));
            }
            else
            {
                now.symbols.add(new CharFuncSymbol(1,temp1.getLineNumber(),temp2.getValue()));
            }
            int i = 0;
            //进入新的区域
            SymbolTable temp = new SymbolTable(now,nowLevel++);
            now.children.add(temp);
            now = temp;
            in = true;
            alreadyIn = true;
            for (Node child : node.getChildren())
            {
                if (i < 2)
                {
                    i++;
                    continue;
                }
                if (child instanceof BranchNode)
                {
                    analyse((BranchNode) child);
                }
            }
        }
        else if(node.getType().equals("<MainFuncDef>"))
        {
            SymbolTable temp = new SymbolTable(now,nowLevel++);
            now.children.add(temp);
            now = temp;
            in = true;
            alreadyIn = true;
            for (Node child : node.getChildren())
            {
                if (child instanceof BranchNode)
                {
                    analyse((BranchNode) child);
                }
            }
        }
        else if (isNewIdents(node))//非函数声明
        {
            boolean isInt = false;
            boolean isConst = false;
            boolean isArray = false;
            int i = 0;
            for (Node child : node.getChildren())
            {
                if (child.getType().equals("CONSTTK"))
                {
                    isConst = true;
                }
                if (child.getType().equals("INTTK"))
                {
                    isInt = true;
                }
                if (child.getType().equals("<ConstDef>") || child.getType().equals("<VarDef>"))
                {
                    analyseDef((BranchNode)child,isConst,isInt);
                }
                i++;
            }
            return;
        }
        else if (node.getType().equals("<FuncFParam>"))//函数声明:形式参数
        {
            boolean isInt = false;
            boolean isArray = false;
            int i = 0;
            for (Node child : node.getChildren())
            {
                if (child.getType().equals("INTTK"))
                {
                    isInt = true;
                }
                if (child.getType().equals("IDENFR"))
                {
                    if (node.getChildren().size() <=i+1)
                    {
                        isArray = false;

                    }
                    else if (node.getChildren().get(i+1).getType().equals("LBRACK"))
                    {
                        isArray = true;
                    }
                    creatNewNode(child,isInt,isArray,false);
                    isInt = false;
                    isArray = false;
                }
                if (child.getType().equals("<Block>"))
                {
                    analyse((BranchNode)child);
                }
                i++;
            }

        }
        else
        {
            for (Node child : node.getChildren())
            {
                if (child instanceof LeafNode)
                {
                    continue;
                }
                analyse((BranchNode)child);
            }
        }
        if (in)
        {
            now = now.fatherTable;
            nowLevel--;
        }
    }

    private void analyseDef(BranchNode node,boolean isConst,boolean isInt) {
        boolean isArray = node.getChildren().size() > 1 && node.getChildren().get(1).getType().equals("LBRACK");
        creatNewNode(node.getChildren().get(0),isInt,isArray,isConst);
    }

    private void creatNewNode(Node child, boolean isInt, boolean isArray, boolean isConst) {
        Token temp = ((LeafNode) child).getToken();
        if (isConst)
        {
            if (isArray)
            {
                if (isInt)
                {
                    now.symbols.add(new ConstIntArraySymbol(1,temp.getLineNumber(),temp.getValue()));
                }
                else
                {
                    now.symbols.add(new ConstCharArraySymbol(1,temp.getLineNumber(),temp.getValue()));
                }
            }
            else
            {
                if (isInt)
                {
                    now.symbols.add(new ConstIntSymbol(1,temp.getLineNumber(),temp.getValue()));
                }
                else
                {
                    now.symbols.add(new ConstCharSymbol(1,temp.getLineNumber(),temp.getValue()));
                }
            }
        }
        else
        {
            if (isArray)
            {
                if (isInt)
                {
                    now.symbols.add(new IntArraySymbol(1,temp.getLineNumber(),temp.getValue()));
                }
                else
                {
                    now.symbols.add(new CharArraySymbol(1,temp.getLineNumber(),temp.getValue()));
                }
            }
            else
            {
                if (isInt)
                {
                    now.symbols.add(new IntSymbol(1,temp.getLineNumber(),temp.getValue()));
                }
                else
                {
                    now.symbols.add(new CharSymbol(1,temp.getLineNumber(),temp.getValue()));
                }
            }
        }
    }

    public void writeTokens(FileProcessor tokenFileProcessor) throws IOException {
        ArrayList<SymbolTable> stack = new ArrayList<>();
        stack.add(root);
        stack.addAll(root.returnAsStack());
        int i = 1;
        for (SymbolTable symbolTable : stack)
        {
            for (Symbol symbol : symbolTable.symbols)
            {
                tokenFileProcessor.writeByLine(i + " " + symbol.toString());

            }
            i++;
        }
        //root.output(tokenFileProcessor,1);
    }
}
