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
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SymbolAnalyser {


    private SymbolTable root;
    private SymbolTable now;
    private int nowLevel;
    private boolean alreadyIn;
    private TreeMap<Integer,String> errors;

    public SymbolAnalyser(BranchNode rootFromParser,TreeMap<Integer,String> errors)
    {
        nowLevel = 1;
        root = new SymbolTable(null,nowLevel);
        now = root;
        alreadyIn = false;
        this.errors = new TreeMap<>();
        analyse(rootFromParser);
        this.errors.putAll(errors);
    }

    public SymbolTable getSTRoot()
    {
        return root;
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
            int i = 0;
            //进入新的区域
            SymbolTable temp = new SymbolTable(now,nowLevel++);
            now.children.add(temp);
            now = temp;
            in = true;
            alreadyIn = true;
            boolean returnFlag = false;
            boolean needReturn = false;
            for (Node child : node.getChildren())
            {
                if (i < 2)
                {
                    i++;
                    continue;
                }
                if (child.getType().equals("<Block>"))
                {
                    ArrayList<Symbol> symbols = new ArrayList<>(now.symbols);
                    if (now.fatherTable.alreadyExist(temp2.getValue()))
                    {
                        errors.put(temp1.getLineNumber(),"b");
                    }
                    if (temp1.getType().equals("VOIDTK"))
                    {
                        now.fatherTable.symbols.add(new VoidFuncSymbol(1,temp1.getLineNumber(),temp2.getValue(),symbols));
                    }
                    else if (temp1.getType().equals("INTTK"))
                    {
                        now.fatherTable.symbols.add(new IntFuncSymbol(1,temp1.getLineNumber(),temp2.getValue(),symbols));
                        needReturn = true;
                    }
                    else
                    {
                        now.fatherTable.symbols.add(new CharFuncSymbol(1,temp1.getLineNumber(),temp2.getValue(),symbols));
                        needReturn = true;
                    }
                    if (!needReturn)
                    {
                        checkFError(child);
                    }
                    if (needReturn)
                    {
                        if (!hasReturn(child))
                        {//block
                            BranchNode block = (BranchNode)child;
                            int num = ((LeafNode)block.getChildren().get(block.getChildren().size() - 1)).getToken().getLineNumber();
                            errors.put(num,"g");
                        }
                    }
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
                if (child.getType().equals("<Block>"))
                {
                    if (!hasReturn(child))
                    {
                        BranchNode block = (BranchNode)child;
                        int num = ((LeafNode)block.getChildren().get(block.getChildren().size() - 1)).getToken().getLineNumber();
                        errors.put(num,"g");
                    }
                }
                if (child instanceof BranchNode)
                {
                    analyse((BranchNode) child);
                }
            }
        }
        else if (node.getType().equals("<ConstDecl>") || node.getType().equals("<VarDecl>"))//非函数声明
        {
            boolean isInt = false;
            boolean isConst = false;
            boolean isArray = false;
            int i = 0;
            boolean assignFlag = false;
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
                    analyse((BranchNode) child);
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
                    String name = ((LeafNode)child).getToken().getValue();
                    if(now.alreadyExist(name))
                    {
                        errors.put(((LeafNode)child).getToken().getLineNumber(),"b");
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
        else if (node.getType().equals("<Stmt>"))
        {
            if(node.getChildren().get(0).getType().equals("PRINTFTK"))
            {
                int line = ((LeafNode)node.getChildren().get(0)).getToken().getLineNumber();
                int Fnum = 0;
                int Rnum = 0;
                for (Node child : node.getChildren())
                {
                    if (child.getType().equals("STRCON"))
                    {
                        LeafNode str = (LeafNode)child;
                        Pattern patternD = Pattern.compile("%d");
                        Pattern patternC = Pattern.compile("%c");

                        // 创建Matcher对象
                        Matcher matcherD = patternD.matcher(str.getValue());
                        Matcher matcherC = patternC.matcher(str.getValue());
                        while (matcherD.find()) {
                            Fnum++;
                        }
                        while (matcherC.find()) {
                            Fnum++;
                        }

                    }
                    if (child.getType().equals("COMMA"))
                    {
                        Rnum++;
                    }
                    if (child.getType().equals("<Exp>") && child instanceof BranchNode)
                    {
                        analyse((BranchNode)child);
                    }
                }
                if (Fnum != Rnum)
                {
                    errors.put(line,"l");
                }
            }
            else if (!hasASymbol(node,"ASSIGN"))
            {
                for (Node child : node.getChildren())
                {
                    if (child instanceof LeafNode)
                    {
                        Token temp = ((LeafNode)child).getToken();
                        if (temp.getType().equals("IDENFR"))
                        {
                            if (now.legalVar(temp.getValue()) == null)
                            {
                                errors.put(getFirstLineNumber(child),"c");
                            }
                        }
                        if (temp.getType().equals("CONTINUETK") || temp.getType().equals("BREAKTK"))
                        {
                            checkMError(node.getParent(),temp.getLineNumber());
                        }
                        continue;
                    }
                    analyse((BranchNode)child);
                }
            }
            else
            {
                for (Node child : node.getChildren())
                {
                    if (child.getType().equals("<LVal>"))
                    {
                        String name = ((LeafNode)((BranchNode)child).getChildren().get(0)).getToken().getValue();
                        Symbol tem = now.legalVar(name);
                        if (tem == null)
                        {
                            errors.put(getFirstLineNumber(child),"c");
                        }
                        else
                        {
                            if (tem.ISConst())
                            {
                                errors.put(getFirstLineNumber(child),"h");
                            }
                        }
                    }
                    if (child instanceof LeafNode)
                    {
                        continue;
                    }
                    analyse((BranchNode)child);
                }
            }
        }
        else
        {
            if (node.getType().equals("<UnaryExp>"))
            {
                if (node.getChildren().get(0).getType().equals("IDENFR"))
                {
                    if (node.getChildren().size() > 1)
                    {
                        if (node.getChildren().get(1).getType().equals("LPARENT"))
                        {
                            Symbol Func = now.legalVar(((LeafNode)node.getChildren().get(0)).getToken().getValue());
                            if (Func == null)
                            {
                                errors.put(getFirstLineNumber(node),"h");
                            }
                            else
                            {
                                for (Node child : node.getChildren())
                                {
                                    if (child.getType().equals("<FuncRParams>"))
                                    {
                                        BranchNode FuncRParams = (BranchNode)child;
                                        int num = 0;
                                        for (Node child2 : FuncRParams.getChildren())
                                        {
                                            if (child2.getType().equals("<Exp>"))
                                            {
                                                num++;
                                            }
                                        }
                                        if (num != Func.getArgs().size())
                                        {
                                            errors.put(getFirstLineNumber(child),"d");
                                        }
                                        else
                                        {
                                            BranchNode bn;
                                            int i = 0;
                                            String name;
                                            Token inToken;
                                            Symbol low;
                                            for (Node child2 : FuncRParams.getChildren())
                                            {
                                                if (child2.getType().equals("<Exp>"))
                                                {
                                                    bn = getLowest(child2);
                                                    inToken = ((LeafNode)bn.getChildren().get(0)).getToken();
                                                    name = ((LeafNode)bn.getChildren().get(0)).getToken().getValue();
                                                    boolean isNotArray = false;
                                                    {
                                                        if (bn.getChildren().size() > 1)
                                                        {
                                                            if (bn.getChildren().get(1) instanceof LeafNode)
                                                            {
                                                                if (((LeafNode)bn.getChildren().get(1)).getToken().getType().equals("LBRACK"))
                                                                    isNotArray = true;
                                                            }
                                                        }
                                                    }
                                                    if (inToken.getType().equals("INTCON") || inToken.getType().equals("CHRCON"))
                                                    {
                                                        low = new ConstIntSymbol(1,inToken.getLineNumber(),"name");
                                                    }
                                                    else
                                                    {
                                                        low = now.legalVar(name);
                                                    }
                                                    if (low == null)
                                                    {
                                                        errors.put(getFirstLineNumber(bn),"h");
                                                    }
                                                    else
                                                    {
                                                        if (isNotArray)
                                                        {
                                                            if (Func.getArgs().get(i).ISInt() != low.ISInt() ||
                                                                    Func.getArgs().get(i).ISArray())
                                                            {
                                                                errors.put(getFirstLineNumber(bn),"e");
                                                            }
                                                        }
                                                        else
                                                        {
                                                            if (Func.getArgs().get(i).ISInt() != low.ISInt() ||
                                                                    Func.getArgs().get(i).ISArray() != low.ISArray())
                                                            {
                                                                errors.put(getFirstLineNumber(bn),"e");
                                                            }
                                                        }
                                                    }
                                                    i++;
                                                }


                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            for (Node child : node.getChildren())
            {
                if (child instanceof LeafNode)
                {
                    Token temp = ((LeafNode)child).getToken();
                    if (temp.getType().equals("IDENFR"))
                    {
                        if (now.legalVar(temp.getValue()) == null)
                        {
                            errors.put(getFirstLineNumber(child),"c");
                        }
                    }
                    if (temp.getType().equals("CONTINUETK") || temp.getType().equals("BREAKTK"))
                    {
                        checkMError(node.getParent(),temp.getLineNumber());
                    }
                    continue;
                }
                if (child.getType().equals("<LVal>") && ((BranchNode)child).getParent().getType().equals("<ForStmt>"))
                {
                    String name = ((LeafNode)((BranchNode)child).getChildren().get(0)).getToken().getValue();
                    Symbol tem = now.legalVar(name);
                    if (tem == null)
                    {
                        errors.put(getFirstLineNumber(child),"c");
                    }
                    else
                    {
                        if (tem.ISConst())
                        {
                            errors.put(getFirstLineNumber(child),"h");
                        }
                    }
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

    private void checkMError(BranchNode node,int line) {
        for (Node child : node.getChildren())
        {
            if (child.getType().equals("FORTK"))
            {
                return;
            }
        }
        if (node.getParent() != null)
        {
            checkMError(node.getParent(),line);
        }
        else
        {
            errors.put(line,"m");
        }
    }

    private void checkFError(Node child) {
        for (Node temp : ((BranchNode)child).getChildren())
        {
            if (temp instanceof LeafNode)
            {
                if (((LeafNode) temp).getToken().getType().equals("RETURNTK"))
                {
                    BranchNode father = ((BranchNode)child);
                    int id = father.getChildren().indexOf(temp);
                    if(father.getChildren().size() <= id + 1)
                    {
                        continue;

                    }
                    else
                    {
                        Node nextNode = father.getChildren().get(id + 1);
                        if (nextNode instanceof LeafNode && ((LeafNode) nextNode).getToken().getType().equals("SEMICN"))
                        {
                            continue;
                        }
                    }
                    errors.put(((LeafNode) temp).getToken().getLineNumber(),"f");
                }
                continue;
            }
            checkFError(temp);
        }
    }

    private int getFirstLineNumber(Node node)
    {
        if (node instanceof BranchNode)
        {
            return getFirstLineNumber(((BranchNode)node).getChildren().get(0));
        }
        else
        {
            return ((LeafNode)node).getToken().getLineNumber();
        }
    }

    private BranchNode getLowest(Node node)
    {
        if (((BranchNode)node).getChildren().get(0) instanceof LeafNode)
        {
            return ((BranchNode)node);
        }
        else
        {
            return getLowest(((BranchNode)node).getChildren().get(0));
        }
    }


    private boolean hasASymbol(Node child,String name)
    {
        if (child instanceof LeafNode)
        {
            return child.getType().equals(name);
        }
        //只允许block进来
        BranchNode temp = (BranchNode) child;
        boolean flag = false;
        for (Node Anode : temp.getChildren())
        {
            if (Anode.getType().equals(name))
                return true;
            if (Anode instanceof BranchNode)
            {
                flag = hasASymbol(Anode,name);
            }
            if (flag)
            {
                return true;
            }
        }
        return false;
    }

    private boolean hasReturn(Node child) {
        //进来是block
        if (((BranchNode)child).getChildren().size() <= 2)
        {
            return false;
        }
        BranchNode blockItem = (BranchNode) ((BranchNode)child).getChildren().get(((BranchNode)child).getChildren().size() - 2);
        if (blockItem.getChildren() == null)
        {
            return false;
        }
        BranchNode stmt = (BranchNode) blockItem.getChildren().get(0);
        if (!stmt.getType().equals("<Stmt>"))
        {
            return false;
        }
        if (stmt.getChildren() == null)
        {
            return false;
        }
        Node returnTk = stmt.getChildren().get(0);
        if (returnTk instanceof LeafNode)
        {
            if (returnTk.getType().equals("RETURNTK"))
            {
                return true;
            }
        }
        return false;
    }

    private void analyseDef(BranchNode node,boolean isConst,boolean isInt) {
        boolean isArray = node.getChildren().size() > 1 && node.getChildren().get(1).getType().equals("LBRACK");
        String name = ((LeafNode)node.getChildren().get(0)).getToken().getValue();
        if (now.alreadyExist(name))
        {
            errors.put(((LeafNode)node.getChildren().get(0)).getToken().getLineNumber(),"b");
        }
        else
        {
            creatNewNode(node.getChildren().get(0),isInt,isArray,isConst);
        }
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

    public void writeErrors(FileProcessor fileProcessor) throws IOException {
        StringBuilder error = new StringBuilder();
        for (int temp : errors.keySet()) {
            error.append(temp);
            error.append(" ");
            error.append(errors.get(temp));
            fileProcessor.writeByLine(error.toString());
            error.replace(0, error.length(), "");
        }
    }
}
