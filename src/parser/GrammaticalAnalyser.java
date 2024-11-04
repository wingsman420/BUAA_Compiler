package parser;

//import Tree.MultiTreeNode;
import Tree.BranchNode;
import Tree.LeafNode;
import Tree.Node;
import base.Token;
import frontend.FileProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

public class GrammaticalAnalyser {
    //语法分析器
    private ArrayList<Token> tokens;
    private Token thisToken;
    private int thisNum;
    private ArrayList<String> grammar;
    private TreeMap<Integer,String> errors;
    private ArrayList<Token> grammarTokens;
    private BranchNode root;
    private BranchNode currentNode;
    //   private MultiTreeNode currentNode;

    public GrammaticalAnalyser(ArrayList<Token> tokens , TreeMap<Integer,String> otherErrors) {
        this.tokens = tokens;
        this.thisNum = 1;
        thisToken = tokens.get(0);
        grammar = new ArrayList<>();
        grammar.add(thisToken.toString());
        grammarTokens = new ArrayList<>();
        grammarTokens.add(thisToken);
        //开始状态：thisnum = 1 token = int
        this.errors = new TreeMap<>();
        errors.putAll(otherErrors);
        //这里已经设置好了第一个字符，数字指针指向1
        analyseCompUnit();
    }

    public BranchNode getRoot() {
        return root;
    }

    public ArrayList<String> getGrammar() {
        return grammar;
    }

    public ArrayList<Token> getGrammarTokens() {
        return grammarTokens;
    }

    public TreeMap<Integer,String> getErrors() {
        return errors;
    }

    /*private void creatAndGoToNode(String type,MultiTreeNode node) {
        MultiTreeNode newNode = new MultiTreeNode(type,node);
        currentNode.addChild(newNode);
        currentNode = node;
    }*/

    private Token nextToken()
    {
        this.thisToken = tokens.get(this.thisNum);
        grammar.add(thisToken.toString());
        grammarTokens.add(thisToken);
        //防止遗漏
        thisNum++;
        return thisToken;
    }

    private Token peekToken(int next)
    {
        //因为thisNum永远会快一个，所以next为0为下一个
        return tokens.get(this.thisNum + next);
    }

    private void creatAndGoToNode(String name)
    {
        BranchNode temp = new BranchNode(name,currentNode);
        currentNode.addChild(temp);
        currentNode = temp;
    }

    private void creatLeafNode()
    {
        currentNode.addChild(new LeafNode(thisToken));
    }

    private void creatLeafNode(Token token)
    {
        currentNode.addChild(new LeafNode(token));
    }

    private void goBack()
    {
        currentNode = currentNode.getParent();
    }

    private void analyseCompUnit()
    {
        //CompUnit → {Decl} {FuncDef} MainFuncDef
        root = new BranchNode("<CompUnit>",null);
        currentNode = root;
        while(thisToken.getType().equals("INTTK") ||
                thisToken.getType().equals("VOIDTK") || thisToken.getType().equals("CHARTK")
                || thisToken.getType().equals("CONSTTK"))
        {
            if (thisToken.getType().equals("INTTK") && peekToken(0).getType().equals("MAINTK"))
            {
                //进主函数了,MainFuncDef
                //creatAndGoToNode("MainFuncDef",currentNode);

                creatAndGoToNode("<MainFuncDef>");
                analyseMainFuncDef();
                goBack();
                break;
            }
            else if (peekToken(1).getType().equals("LPARENT"))
            {
                //进函数了{FuncDef}
                //creatAndGoToNode("FuncDef",currentNode);
                creatAndGoToNode("<FuncDef>");
                analyseFuncDef();
                goBack();
            }
            else
            {
                //{Decl}
                //creatAndGoToNode("Decl",currentNode);
                creatAndGoToNode("<Decl>");
                analyseDecl();
                goBack();
            }
            nextToken();//;下一位

        }
        /*currentNode = currentNode.getFather();
        if (currentNode != null)
        {
            System.out.println("tree Error");
        }*/
        grammar.add("<CompUnit>");
        grammarTokens.add(new Token("GRAMMAR","<CompUnit>",thisToken.getLineNumber()));
    }

    private void analyseMainFuncDef()
    {
        creatLeafNode();//int
        nextToken(); //main
        creatLeafNode();//main
        nextToken(); //(
        creatLeafNode();//(
        if (!peekToken(0).getType().equals("RPARENT"))
        {
            //错误j
            creatLeafNode(new Token("RPARENT",")",thisToken.getLineNumber()));
            errors.put(thisToken.getLineNumber(),"j");
        }
        else
        {
            nextToken(); //)
            creatLeafNode();//)
        }
        nextToken();//进入block
        //creatAndGoToNode("Block",currentNode);
        creatAndGoToNode("<Block>");
        analyseBlock();
        goBack();
        //退出时为;
        grammar.add("<MainFuncDef>");
        grammarTokens.add(new Token("GRAMMAR","<MainFuncDef>",thisToken.getLineNumber()));
    }

    private void analyseDecl()
    {
        //Decl → ConstDecl | VarDecl
        //进入为类型
        if (thisToken.getType().equals("CONSTTK"))
        {
            //creatAndGoToNode("ConstDecl",currentNode);
            creatAndGoToNode("<ConstDecl>");
            analyseConstDecl();
            goBack();
        }
        else
        {
            //creatAndGoToNode("VarDecl",currentNode);
            creatAndGoToNode("<VarDecl>");
            analyseVarDecl();
            goBack();
        }
        //退出时为;
    }

    private void analyseFuncDef()
    {
        //进入为类型
        //FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // j
        creatLeafNode();
        grammar.add("<FuncType>");
        grammarTokens.add(new Token("GRAMMAR","<FuncType>",thisToken.getLineNumber()));
        nextToken();//ident
        creatLeafNode();
        nextToken();//(
        creatLeafNode();
        if (peekToken(0).getType().equals("RPARENT"))
        {
            nextToken();//)
            creatLeafNode();
        }
        else
        {
            if (peekToken(0).getType().equals("LBRACE"))
            {
                errors.put(thisToken.getLineNumber(),"j");
                creatLeafNode(new Token("RPARENT",")",thisToken.getLineNumber()));
            }
            else
            {
                nextToken();//进下一个了
                //creatAndGoToNode("FuncFParams",currentNode);
                creatAndGoToNode("<FuncFParams>");
                analyseFuncFParams();
                goBack();
                if (peekToken(0).getType().equals("LBRACE"))
                {
                    errors.put(thisToken.getLineNumber(),"j");
                    creatLeafNode(new Token("RPARENT",")",thisToken.getLineNumber()));
                }
                else
                {
                    nextToken();//)
                    creatLeafNode();
                }
            }

        }
        nextToken();//{
        creatLeafNode();
        //creatAndGoToNode("Block",currentNode);
        creatAndGoToNode("<Block>");
        analyseBlock();
        goBack();
        //退出时为}
        //currentNode = currentNode.getFather();
        grammar.add("<FuncDef>");
        grammarTokens.add(new Token("GRAMMAR","<FuncDef>",thisNum));
    }

    private void analyseBlock()
    {
        //进入时为{
        creatLeafNode();//{
        while (!peekToken(0).getType().equals("RBRACE"))
        {
            nextToken();//第一位
            creatAndGoToNode("<BlockItem>");
            analyseBlockItem();
            goBack();
        }
        nextToken();//}
        creatLeafNode();//}
        grammar.add("<Block>");
        grammarTokens.add(new Token("GRAMMAR","<Block>",thisNum));
    }

    private void analyseBlockItem() {
        //语句块项 BlockItem → Decl | Stmt
        //进入为第一个
        if (thisToken.getType().equals("CONSTTK")
                || thisToken.getType().equals("VOIDTK") || thisToken.getType().equals("CHARTK")
                ||thisToken.getType().equals("INTTK"))
        {
            creatAndGoToNode("<Decl>");
            analyseDecl();
            goBack();
        }
        else
        {
            creatAndGoToNode("<Stmt>");
            analyseStmt();
            goBack();
        }
        //grammar.add("<BlockItem>");
    }

    private void analyseVarDecl() {
        //进入时为INTTK;
        //BType VarDef { ',' VarDef } ';' // i
        creatLeafNode();
        nextToken();// ident
        creatAndGoToNode("<VarDef>");
        analyseVarDef();
        goBack();
        while (peekToken(0).getType().equals("COMMA"))
        {
            nextToken();//,
            creatLeafNode();
            nextToken();//ident
            creatAndGoToNode("<VarDef>");
            analyseVarDef();
            goBack();
        }
        if (!peekToken(0).getType().equals("SEMICN"))
        {
            errors.put(thisToken.getLineNumber(),"i");
            creatLeafNode(new Token("SEMICN",";",thisToken.getLineNumber()));
        }
        else
        {
            nextToken();//;
            creatLeafNode();
        }
        //退出时为;
        //currentNode = currentNode.getFather();
        grammar.add("<VarDecl>");
        grammarTokens.add(new Token("GRAMMAR","<VarDecl>",thisNum));
    }

    private void analyseVarDef() {
        //进入时为ident
        //变量定义 VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal // k
        creatLeafNode();
        if (peekToken(0).getType().equals("LBRACK"))
        {
            nextToken();//[
            creatLeafNode();
            nextToken();//进入
            creatAndGoToNode("<ConstExp>");
            analyseConstExp();
            goBack();
            if (peekToken(0).getType().equals("RBRACK"))
            {
                nextToken();//]
                creatLeafNode();
            }
            else
            {
                errors.put(thisToken.getLineNumber(),"k");
                creatLeafNode(new Token("RBRACK","]",thisToken.getLineNumber()));
            }
        }
        if (peekToken(0).getType().equals("ASSIGN"))
        {
            nextToken();//=
            creatLeafNode();
            nextToken();//进下一个
            creatAndGoToNode("<InitVal>");
            analyseInitVal();
            goBack();
        }
        //退出时没有向前
        grammar.add("<VarDef>");
        grammarTokens.add(new Token("GRAMMAR","<VarDef>",thisNum));
    }

    private void analyseInitVal() {
        //进入时为第一个
        //变量初值 InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst
        if (thisToken.getType().equals("LBRACE"))
        {
            if (peekToken(0).getType().equals("RBRACE"))
            {
                creatLeafNode();
                nextToken();//}
                creatLeafNode();
                grammar.add("<InitVal>");
                grammarTokens.add(new Token("GRAMMAR","<InitVal>",thisNum));
                //结束时为最后一个
            }
            else
            {
                creatLeafNode();
                int i = 0;
                do {
                    if (i++ != 0 && peekToken(0).getType().equals("COMMA"))
                    {
                        nextToken();//,
                        creatLeafNode();
                    }
                    nextToken();//进入
                    creatAndGoToNode("<Exp>");
                    analyseExp();
                    goBack();
                } while (peekToken(0).getType().equals("COMMA"));
                nextToken();//}
                creatLeafNode();
                grammar.add("<InitVal>");
                grammarTokens.add(new Token("GRAMMAR","<InitVal>",thisNum));
                //结束时为最后一个
            }
        }
        else if (thisToken.getType().equals("STRCON"))
        {
            creatLeafNode();
            grammar.add("<InitVal>");
            grammarTokens.add(new Token("GRAMMAR","<InitVal>",thisNum));
        }
        else
        {
            creatAndGoToNode("<Exp>");
            analyseExp();
            goBack();
            grammar.add("<InitVal>");
            grammarTokens.add(new Token("GRAMMAR","<InitVal>",thisNum));
        }
        //currentNode = currentNode.getFather();
    }

    private void analyseConstExp() {

        creatAndGoToNode("<AddExp>");
        //进入时为第一位
        analyseAddExp();
        goBack();
        //退出时为最后一位
        grammar.add("<ConstExp>");
        grammarTokens.add(new Token("GRAMMAR","<ConstExp>",thisNum));
    }

    private void analyseAddExp() {
        //进入为第一位
        int i = 0;
        do {
            if (i++ != 0 && (peekToken(0).getType().equals("PLUS") || peekToken(0).getType().equals("MINU")))
            {
                grammar.add("<AddExp>");
                grammarTokens.add(new Token("GRAMMAR","<AddExp>",thisNum));
                nextToken();//+-
                creatLeafNode();
                nextToken();//第一位
            }
            creatAndGoToNode("<MulExp>");
            analyseMulExp();
            goBack();
        }while(peekToken(0).getType().equals("PLUS") || peekToken(0).getType().equals("MINU"));
        //退出为最后一位
        grammar.add("<AddExp>");
        grammarTokens.add(new Token("GRAMMAR","<AddExp>",thisNum));
    }

    private void analyseMulExp() {
        int i = 0;
        //进入为第一位
        do {
            if (i++ != 0 && (peekToken(0).getType().equals("MULT") || peekToken(0).getType().equals("DIV")
                    || peekToken(0).getType().equals("MOD")))
            {
                grammar.add("<MulExp>");
                grammarTokens.add(new Token("GRAMMAR","<MulExp>",thisNum));
                nextToken();//*/%
                creatLeafNode();
                nextToken();//进入第一位
            }
            creatAndGoToNode("<UnaryExp>");
            analyseUnaryExp();
            goBack();
        }
        while (peekToken(0).getType().equals("MULT") || peekToken(0).getType().equals("DIV")
                || peekToken(0).getType().equals("MOD"));
        grammar.add("<MulExp>");
        grammarTokens.add(new Token("GRAMMAR","<MulExp>",thisNum));
    }

    private void analyseUnaryOp()
    {
        creatLeafNode();
        grammar.add("<UnaryOp>");
        grammarTokens.add(new Token("GRAMMAR","<UnaryOp>",thisNum));
    }

    private void analyseUnaryExp() {
        //进入为第一位
        if (thisToken.getType().equals("PLUS") || thisToken.getType().equals("MINU") || thisToken.getType().equals("NOT"))
        {
            creatAndGoToNode("<UnaryOp>");
            analyseUnaryOp();
            goBack();
            nextToken();
            creatAndGoToNode("<UnaryExp>");
            analyseUnaryExp();
            goBack();
            grammar.add("<UnaryExp>");
            grammarTokens.add(new Token("GRAMMAR","<UnaryExp>",thisNum));
        }
        //这里需要分辨是函数还是左值
        else if (thisToken.getType().equals("IDENFR") && peekToken(0).getType().equals("LPARENT"))
            // Ident '(' [FuncRParams] ')'
        {
            creatLeafNode();
            nextToken();//(
            creatLeafNode();
            if (peekToken(0).getType().equals("RPARENT"))
            {
                nextToken();//)
                creatLeafNode();
                grammar.add("<UnaryExp>");
                grammarTokens.add(new Token("GRAMMAR","<UnaryExp>",thisNum));
                return;
            }
            else if (peekToken(0).getType().equals("SEMICN") || isNotFuncRParams())
            {
                errors.put(thisToken.getLineNumber(),"j");
                creatLeafNode(new Token("RPARENT",")",thisToken.getLineNumber()));
                grammar.add("<UnaryExp>");
                grammarTokens.add(new Token("GRAMMAR","<UnaryExp>",thisNum));
                return;
            }
            else
            {
                //看看有没有实参表
                nextToken();//第一位
                creatAndGoToNode("<FuncRParams>");
                analyseFuncRParams();
                goBack();
                if (peekToken(0).getType().equals("RPARENT"))
                {
                    nextToken();//)
                    creatLeafNode();
                    grammar.add("<UnaryExp>");
                    grammarTokens.add(new Token("GRAMMAR","<UnaryExp>",thisNum));
                    return;
                }
                else
                {
                    errors.put(thisToken.getLineNumber(),"j");
                    creatLeafNode(new Token("RPARENT",")",thisToken.getLineNumber()));
                    grammar.add("<UnaryExp>");
                    grammarTokens.add(new Token("GRAMMAR","<UnaryExp>",thisNum));
                    return;
                }
            }
        }
        else
        {
            creatAndGoToNode("<PrimaryExp>");
            analysePrimaryExp();
            goBack();
            grammar.add("<UnaryExp>");
            grammarTokens.add(new Token("GRAMMAR","<UnaryExp>",thisNum));
        }
    }

    private boolean isNotFuncRParams() {
        String now = peekToken(0).getType();
        if (now.equals("SEMICN") || now.equals("MULT")
                || now.equals("DIV") || now.equals("MOD")
                || now.equals("EQL") || now.equals("NEQ") || now.equals("LSS")||
                now.equals("LEQ") || now.equals("GRE") || now.equals("GEQ")
                || now.equals("AND") || now.equals("OR") || now.equals("LE"))
            return true;
        else
        {
            return false;
        }
    }

    private void analyseFuncRParams() {
        //进入为第一位
        int i = 0;
        do {
            if (i++ != 0 &&(peekToken(0).getType().equals("COMMA")))
            {
                nextToken();//,
                creatLeafNode();
                nextToken();//第一位
            }
            creatAndGoToNode("<Exp>");
            analyseExp();
            goBack();
        }while(peekToken(0).getType().equals("COMMA"));
        grammar.add("<FuncRParams>");
        grammarTokens.add(new Token("GRAMMAR","<FuncRParams>",thisNum));
    }

    private void analysePrimaryExp() {
        //进入为第一位
        if (thisToken.getType().equals("LPARENT"))
        {
            creatLeafNode();
            nextToken();//(
            creatAndGoToNode("<Exp>");
            analyseExp();
            goBack();
            if (!peekToken(0).getType().equals("RPARENT"))
            {
                errors.put(thisToken.getLineNumber(),"j");
                creatLeafNode(new Token("RPARENT",")",thisToken.getLineNumber()));
            }
            else
            {
                nextToken();//)
                creatLeafNode();
            }
        }
        else if (thisToken.getType().equals("INTCON"))
        {
            //nextToken();//intConst
            creatAndGoToNode("<Number>");
            analyseNumber();
            goBack();
        }
        else if (thisToken.getType().equals("CHRCON"))
        {
            //nextToken();//CHARCONST
            creatAndGoToNode("<Character>");
            analyseCharacter();
            goBack();
        }
        else
        {
            //进入第一个
            creatAndGoToNode("<LVal>");
            analyseLVal();
            goBack();
        }
        grammar.add("<PrimaryExp>");
        grammarTokens.add(new Token("GRAMMAR","<PrimaryExp>",thisNum));
    }

    private void analyseCharacter()
    {
        creatLeafNode();
        grammar.add("<Character>");
        grammarTokens.add(new Token("GRAMMAR","<Character>",thisNum));
    }

    private void analyseNumber()
    {
        creatLeafNode();
        grammar.add("<Number>");
        grammarTokens.add(new Token("GRAMMAR","<Number>",thisNum));
    }

    private void analyseLVal() {
        //进入为第一个
        creatLeafNode();//ident
        if (peekToken(0).getType().equals("LBRACK"))
        {
            nextToken();//[
            creatLeafNode();
            if (peekToken(0).getType().equals("RBRACK"))
            {
                nextToken();//]
                creatLeafNode();
            }
            else if (peekToken(0).getType().equals("LPARENT") || peekToken(0).getType().equals("IDENFR") ||
                        peekToken(0).getType().equals("INTCON") || peekToken(0).getType().equals("CHACON") ||
                        peekToken(0).getType().equals("PLUS") || peekToken(0).getType().equals("MINU"))
            {
                nextToken();//进第一个
                creatAndGoToNode("<Exp>");
                analyseExp();
                goBack();
                if (peekToken(0).getType().equals("RBRACK"))
                {
                    nextToken();//]
                    creatLeafNode();
                }
                else
                {
                    errors.put(thisToken.getLineNumber(),"k");
                    creatLeafNode(new Token("RBRACK","]",thisToken.getLineNumber()));
                }
            }
            else
            {
                errors.put(thisToken.getLineNumber(),"k");
                creatLeafNode(new Token("RBRACK","]",thisToken.getLineNumber()));
            }

        }
        //退出为最后一个
        grammar.add("<LVal>");
        grammarTokens.add(new Token("GRAMMAR","<LVal>",thisNum));
    }


    private void analyseConstDecl()
    {
        //进入时为const
        //ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';' // i
        creatLeafNode();
        nextToken();//bType
        creatLeafNode();
        nextToken();//进入第一位
        int i = 0;
        do {
            if (i++ != 0 && (peekToken(0).getType().equals("COMMA")))
            {
                nextToken();//,
                creatLeafNode();
                nextToken();//进第一位
            }
            creatAndGoToNode("<ConstDef>");
            analyseConstDef();
            goBack();
        } while (peekToken(0).getType().equals("COMMA"));
        if (peekToken(0).getType().equals("SEMICN"))
        {
            nextToken();//;
            creatLeafNode();
        }
        else
        {
            errors.put(thisToken.getLineNumber(),"i");
            creatLeafNode(new Token("SEMICN",";",thisToken.getLineNumber()));
        }
        grammar.add("<ConstDecl>");
        grammarTokens.add(new Token("GRAMMAR","<ConstDecl>",thisNum));
    }

    private void analyseConstDef() { //Ident [ '[' ConstExp ']' ] '=' ConstInitVal // k
        //进入第一位
        creatLeafNode();
        if (peekToken(0).getType().equals("LBRACK"))
        {
            nextToken();//[
            creatLeafNode();
            if (peekToken(0).getType().equals("RBRACK"))
            {
                nextToken();//]
                creatLeafNode();
            }
            else if (peekToken(0).getType().equals("ASSIGN"))
            {
                errors.put(thisToken.getLineNumber(),"k");
                creatLeafNode(new Token("RBRACK","]",thisToken.getLineNumber()));
            }
            else
            {
                nextToken();//进入
                creatAndGoToNode("<ConstExp>");
                analyseConstExp();
                goBack();
                if (peekToken(0).getType().equals("RBRACK"))
                {
                    nextToken();//]
                    creatLeafNode();
                }
                else
                {
                    errors.put(thisToken.getLineNumber(),"k");
                    creatLeafNode(new Token("RBRACK","]",thisToken.getLineNumber()));
                }
            }
        }
        nextToken();//=
        creatLeafNode();
        nextToken();//进入
        creatAndGoToNode("<ConstInitVal>");
        analyseConstInitVal();
        goBack();
        grammar.add("<ConstDef>");
        grammarTokens.add(new Token("GRAMMAR","<ConstDef>",thisNum));
    }

    private void analyseConstInitVal() {
        //常量初值 ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
        //进入为第一个

        if (thisToken.getType().equals("STRCON"))
        {
            creatLeafNode();
            grammar.add("<ConstInitVal>");//str
            grammarTokens.add(new Token("GRAMMAR","<ConstInitVal>",thisNum));
            return;
        }
        else if (thisToken.getType().equals("LBRACE"))
        {
            creatLeafNode();
            //现在已经是{
            if (peekToken(0).getType().equals("RBRACE"))
            {
                nextToken();//}
                creatLeafNode();
            }
            else
            {
                nextToken();//第一个
                int i = 0;
                do {
                    if (i++ != 0  && (peekToken(0).getType().equals("COMMA")))
                    {
                        nextToken();//,
                        creatLeafNode();
                        nextToken();//第一个
                    }
                    creatAndGoToNode("<ConstExp>");
                    analyseConstExp();
                    goBack();
                }while (peekToken(0).getType().equals("COMMA"));
                nextToken();//}
                creatLeafNode();
            }
        }
        else
        {
            creatAndGoToNode("<ConstExp>");
            analyseConstExp();
            goBack();
        }
        grammar.add("<ConstInitVal>");
        grammarTokens.add(new Token("GRAMMAR","<ConstInitVal>",thisNum));
    }

    private void analyseFuncFParams()
    { //FuncFParams → FuncFParam { ',' FuncFParam }
        //进入时为（ 的后一位
        int i = 0;
        do {
            if (i++ != 0 && (peekToken(0).getType().equals("COMMA")))
            {
                nextToken();//,
                creatLeafNode();
                nextToken();//第一个
            }
            creatAndGoToNode("<FuncFParam>");
            analyseFuncFParam();
            goBack();
        }while(peekToken(0).getType().equals("COMMA"));
        grammar.add("<FuncFParams>");
        grammarTokens.add(new Token("GRAMMAR","<FuncFParams>",thisNum));
    }

    private void analyseFuncFParam() {
        //FuncFParam → BType Ident ['[' ']'] // k
        //进入为第一位
        creatLeafNode();
        nextToken();//Ident
        creatLeafNode();
        if (peekToken(0).getType().equals("LBRACK"))
        {
            nextToken();//[
            creatLeafNode();
            if (peekToken(0).getType().equals("RBRACK"))
            {
                nextToken();//]
                creatLeafNode();
            }
            else
            {
                errors.put(thisToken.getLineNumber(),"k");
                creatLeafNode(new Token("RBRACK","]",thisToken.getLineNumber()));
            }
        }
        grammar.add("<FuncFParam>");
        grammarTokens.add(new Token("GRAMMAR","<FuncFParam>",thisNum));
    }

    private boolean isLVal()
    {
        int now = thisToken.getLineNumber();
        for (int i = 0; ;i++)
        {
            if (peekToken(i).getLineNumber() != now)
            {
                return false;
            }
            if (peekToken(i).getType().equals("ASSIGN"))
            {
                return true;
            }
            if (peekToken(i).getType().equals("SEMICN"))
            {
                return false;
            }
        }
    }

    private void analyseStmt()
    {
        /*
            Stmt → *LVal '=' Exp ';' // i
            | ????存疑[Exp] ';' // i
            | *Block
            | *'if' '(' Cond ')' Stmt [ 'else' Stmt ] // j
            | *'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
            | 'break' ';' | 'continue' ';' // i
            | *'return' [Exp] ';' // i
            | *LVal '=' 'getint''('')'';' // i j
            | *LVal '=' 'getchar''('')'';' // i j
            | *'printf''('StringConst {','Exp}')'';' // i j */
        //进入时为第一位
        if (thisToken.getType().equals("IDENFR") && (peekToken(0).getType().equals("ASSIGN") || isLVal())) //LVal
        {
            creatAndGoToNode("<LVal>");
            analyseLVal();
            goBack();
            if (peekToken(0).getType().equals("SEMICN")) //[Exp] ';' // i
            {
                nextToken();//;
                creatLeafNode();
                grammar.add("<Stmt>");
                grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
                return;
            }
            else if (peekToken(0).getType().equals("ASSIGN"))
            {
                nextToken();//=
                creatLeafNode();
                if (peekToken(0).getType().equals("GETINTTK") || peekToken(0).getType().equals("GETCHARTK"))//get*2
                {
                    nextToken();//get
                    creatLeafNode();
                    nextToken();//(
                    creatLeafNode();
                    if (peekToken(0).getType().equals("SEMICN"))
                    {
                        errors.put(thisToken.getLineNumber(),"j");
                        creatLeafNode(new Token("RPARENT",")",thisToken.getLineNumber()));
                        nextToken();//;
                        creatLeafNode();
                        grammar.add("<Stmt>");
                        grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
                        return;
                    }
                    else
                    {
                        nextToken();//)
                        creatLeafNode();
                        if (!peekToken(0).getType().equals("SEMICN"))
                        {
                            errors.put(thisToken.getLineNumber(),"i");
                            creatLeafNode(new Token("SEMICN",";",thisToken.getLineNumber()));
                        }
                        else
                        {
                            nextToken();//;
                            creatLeafNode();
                        }
                        grammar.add("<Stmt>");
                        grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
                        return;
                    }
                }
                else //exp
                {
                    nextToken();//进
                    creatAndGoToNode("<Exp>");
                    analyseExp();
                    goBack();
                    if (peekToken(0).getType().equals("SEMICN"))
                    {
                        nextToken();//;
                        creatLeafNode();
                    }
                    else
                    {
                        errors.put(thisToken.getLineNumber(),"i");
                        creatLeafNode(new Token("SEMICN",";",thisToken.getLineNumber()));
                    }
                    grammar.add("<Stmt>");
                    grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
                    return;
                }
            }
        }
        else if (thisToken.getType().equals("LBRACE")) // block
        {
            creatAndGoToNode("<Block>");
            analyseBlock();
            goBack();
            grammar.add("<Stmt>");
            grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
        }
        else if (thisToken.getType().equals("IFTK")) //if
        {
            creatLeafNode();
            nextToken();//(
            creatLeafNode();
            nextToken();//进入
            creatAndGoToNode("<Cond>");
            analyseCond();
            goBack();
            if (!peekToken(0).getType().equals("RPARENT"))
            {
                errors.put(thisToken.getLineNumber(),"j");
                creatLeafNode(new Token("RPARENT",")",thisToken.getLineNumber()));
            }
            else
            {
                nextToken();//)
                creatLeafNode();
            }
            nextToken();//进
            creatAndGoToNode("<Stmt>");
            analyseStmt();
            goBack();
            if (peekToken(0).getType().equals("ELSETK"))
            {
                nextToken();//else
                creatLeafNode();
                nextToken();//进
                creatAndGoToNode("<Stmt>");
                analyseStmt();
                goBack();
            }
            grammar.add("<Stmt>");
            grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
            return;
        }
        else if (thisToken.getType().equals("PRINTFTK"))
        {
            creatLeafNode();
            //'printf''('StringConst {','Exp}')'';' // i j
            nextToken();//(
            creatLeafNode();
            nextToken();//str
            creatLeafNode();
            while (peekToken(0).getType().equals("COMMA"))
            {
                nextToken();//,
                creatLeafNode();
                nextToken();//进入
                creatAndGoToNode("<Exp>");
                analyseExp();
                goBack();
            }
            if (peekToken(0).getType().equals("SEMICN"))
            {
                errors.put(thisToken.getLineNumber(),"j");
                creatLeafNode(new Token("RPARENT",")",thisToken.getLineNumber()));
            }
            else
            {
                nextToken();//)
                creatLeafNode();
                if (!peekToken(0).getType().equals("SEMICN"))
                {
                    errors.put(thisToken.getLineNumber(),"i");
                    creatLeafNode(new Token("SEMICN",";",thisToken.getLineNumber()));
                }
                else
                {
                    nextToken();//;
                    creatLeafNode();
                }
            }
            grammar.add("<Stmt>");
            grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
            return;
        }
        else if(thisToken.getType().equals("RETURNTK"))
        {
            creatLeafNode();
            if (peekToken(0).getType().equals("SEMICN"))
            {
                nextToken();//;
                creatLeafNode();
                grammar.add("<Stmt>");
                grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
                return;
            }
            else
            {
                if (isExp())
                {
                    nextToken();
                    creatAndGoToNode("<Exp>");
                    analyseExp();
                    goBack();
                    if (peekToken(0).getType().equals("SEMICN"))
                    {
                        nextToken();//;
                        creatLeafNode();
                    }
                    else
                    {
                        errors.put(thisToken.getLineNumber(),"i");
                        creatLeafNode(new Token("SEMICN",";",thisToken.getLineNumber()));
                    }
                }
                else
                {
                    errors.put(thisToken.getLineNumber(),"i");
                    creatLeafNode(new Token("SEMICN",";",thisToken.getLineNumber()));
                }
            }
            grammar.add("<Stmt>");
            grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
            return;
        }
        else if(thisToken.getType().equals("FORTK")) //'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
        {
            creatLeafNode();
            nextToken();//(
            creatLeafNode();
            if (!peekToken(0).getType().equals("SEMICN"))
            {
                nextToken();//进入
                creatAndGoToNode("<ForStmt>");
                analyseForStmt();
                goBack();
                nextToken();//;
                creatLeafNode();
            }
            else
            {
                nextToken();//;
                creatLeafNode();
            }
            if (!peekToken(0).getType().equals("SEMICN"))
            {
                nextToken();//进入
                creatAndGoToNode("<Cond>");
                analyseCond();
                goBack();
                nextToken();//;
                creatLeafNode();
            }
            else
            {
                nextToken();//;
                creatLeafNode();
            }
            if (!peekToken(0).getType().equals("RPARENT"))
            {
                nextToken();//进入
                creatAndGoToNode("<ForStmt>");
                analyseForStmt();
                goBack();
                nextToken();//)
                creatLeafNode();
            }
            else
            {
                nextToken();//)
                creatLeafNode();
            }
            nextToken();//进入
            creatAndGoToNode("<Stmt>");
            analyseStmt();
            goBack();
            grammar.add("<Stmt>");
            grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
            return;
        }
        else if(thisToken.getType().equals("BREAKTK") || thisToken.getType().equals("CONTINUETK"))
        {
            creatLeafNode();
            if (peekToken(0).getType().equals("SEMICN"))
            {
                nextToken();//;
                creatLeafNode();
            }
            else
            {
                errors.put(thisToken.getLineNumber(),"i");
                creatLeafNode(new Token("SEMICN",";",thisToken.getLineNumber()));
            }
            grammar.add("<Stmt>");
            grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
            return;
        }
        else if (thisToken.getType().equals("SEMICN"))
        {
            creatLeafNode();
            grammar.add("<Stmt>");
            grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
            return;
        }
        else
        {
            creatAndGoToNode("<Exp>");
            analyseExp();
            goBack();
            if (peekToken(0).getType().equals("SEMICN"))
            {
                nextToken();//;
                creatLeafNode();
            }
            else
            {
                errors.put(thisToken.getLineNumber(),"i");
                creatLeafNode(new Token("SEMICN",";",thisToken.getLineNumber()));
            }
            grammar.add("<Stmt>");
            grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
        }

    }

    public boolean isExp()
    {
        String target = peekToken(0).getType();
        if (target.equals("PLUS") || target.equals("MINU") || target.equals("NOT") ||
                target.equals("IDENFR") || target.equals("LPARENT") || target.equals("INTCON") || target.equals("CHRCON"))
        {
            int now = thisToken.getLineNumber();
            for (int i = 1;;i++)
            {
                if (peekToken(i).getLineNumber() != now)
                {
                    return true;
                }
                /*if (peekToken(i).getType().equals("SEMICN"))
                {
                    return true;
                }*/
                if (peekToken(i).getType().equals("ASSIGN") || peekToken(i).getType().equals("RBRACE"))
                {
                    return false;
                }
            }
        }
        return false;
    }

    private void analyseForStmt() {
        //进入为第一项
        creatAndGoToNode("<LVal>");
        analyseLVal();
        goBack();
        nextToken();//=
        creatLeafNode();
        nextToken();//进
        creatAndGoToNode("<Exp>");
        analyseExp();
        goBack();
        grammar.add("<ForStmt>");
        grammarTokens.add(new Token("GRAMMAR","<ForStmt>",thisNum));
    }

    private void analyseCond() {
        creatAndGoToNode("<LOrExp>");
        analyseLOrExp();
        goBack();
        grammar.add("<Cond>");
        grammarTokens.add(new Token("GRAMMAR","<Cond>",thisNum));
    }

    private void analyseLOrExp() {
        int i = 0;
        do {
            if (i++ != 0 && peekToken(0).getType().equals("OR"))
            {
                grammar.add("<LOrExp>");
                grammarTokens.add(new Token("GRAMMAR","<LOrExp>",thisNum));
                nextToken();//||
                creatLeafNode();
                nextToken();//进
            }
            creatAndGoToNode("<LAndExp>");
            analyseLAndExp();
            goBack();
        }while (peekToken(0).getType().equals("OR"));
        grammar.add("<LOrExp>");
        grammarTokens.add(new Token("GRAMMAR","<LOrExp>",thisNum));
    }

    private void analyseLAndExp() {
        int i = 0;
        do {
            if (i++ != 0 && peekToken(0).getType().equals("AND"))
            {
                grammar.add("<LAndExp>");
                grammarTokens.add(new Token("GRAMMAR","<LAndExp>",thisNum));
                nextToken();//&&
                creatLeafNode();
                nextToken();//进
            }
            creatAndGoToNode("<EqExp>");
            analyseEqExp();
            goBack();
        }while (peekToken(0).getType().equals("AND"));
        grammar.add("<LAndExp>");
        grammarTokens.add(new Token("GRAMMAR","<LAndExp>",thisNum));
    }

    private void analyseEqExp() {
        int i = 0;
        do {
            if (i++ != 0 && (peekToken(0).getType().equals("EQL") || peekToken(0).getType().equals("NEQ")))
            {
                grammar.add("<EqExp>");
                grammarTokens.add(new Token("GRAMMAR","<EqExp>",thisNum));
                nextToken();//== !=
                creatLeafNode();
                nextToken();//进
            }
            creatAndGoToNode("<RelExp>");
            analyseRelExp();
            goBack();
        }while (peekToken(0).getType().equals("EQL") || peekToken(0).getType().equals("NEQ"));
        grammar.add("<EqExp>");
        grammarTokens.add(new Token("GRAMMAR","<EqExp>",thisNum));
    }

    private void analyseRelExp() {
        int i = 0;
        do {
            if (i++ != 0 && (peekToken(0).getType().equals("LSS") || peekToken(0).getType().equals("LEQ")||
            peekToken(0).getType().equals("GRE") || peekToken(0).getType().equals("GEQ")))
            {
                grammar.add("<RelExp>");
                grammarTokens.add(new Token("GRAMMAR","<RelExp>",thisNum));
                nextToken();//== !=
                creatLeafNode();
                nextToken();//进
            }
            creatAndGoToNode("<AddExp>");
            analyseAddExp();
            goBack();
        }while (peekToken(0).getType().equals("LSS") || peekToken(0).getType().equals("LEQ")||
                peekToken(0).getType().equals("GRE") || peekToken(0).getType().equals("GEQ"));
        grammar.add("<RelExp>");
        grammarTokens.add(new Token("GRAMMAR","<RelExp>",thisNum));
    }

    private void analyseExp() {
        //进入时为第一个
        creatAndGoToNode("<AddExp>");
        analyseAddExp();
        goBack();
        grammar.add("<Exp>");
        grammarTokens.add(new Token("GRAMMAR","<Exp>",thisNum));
    }

    public void writeTokens(FileProcessor fileProcessor) throws IOException {
        for (String temp : grammar) {
            fileProcessor.writeByLine(temp);
        }
    }

    // 输出错误信息
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
