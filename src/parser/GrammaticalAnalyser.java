package parser;

//import Tree.MultiTreeNode;
import Tree.BranchNode;
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

    private void analyseCompUnit()
    {
        //CompUnit → {Decl} {FuncDef} MainFuncDef
        while(thisToken.getType().equals("INTTK") ||
                thisToken.getType().equals("VOIDTK") || thisToken.getType().equals("CHARTK")
                || thisToken.getType().equals("CONSTTK"))
        {
            if (thisToken.getType().equals("INTTK") && peekToken(0).getType().equals("MAINTK"))
            {
                //进主函数了,MainFuncDef
                //creatAndGoToNode("MainFuncDef",currentNode);
                analyseMainFuncDef();
                break;
            }
            else if (peekToken(1).getType().equals("LPARENT"))
            {
                //进函数了{FuncDef}
                //creatAndGoToNode("FuncDef",currentNode);
                analyseFuncDef();
            }
            else
            {
                //{Decl}
                //creatAndGoToNode("Decl",currentNode);
                analyseDecl();
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
        nextToken(); //main
        nextToken(); //(
        if (!peekToken(0).getType().equals("RPARENT"))
        {
            //错误j
            errors.put(thisToken.getLineNumber(),"j");
        }
        else
        {
            nextToken(); //)
        }
        nextToken();//进入block
        //creatAndGoToNode("Block",currentNode);
        analyseBlock();
        //退出时为;
        //currentNode = currentNode.getFather();
        grammar.add("<MainFuncDef>");
        grammarTokens.add(new Token("GRAMMAR","<MainFuncDef>",thisToken.getLineNumber()));
    }

    private void analyseDecl()
    {
        //进入为类型
        if (thisToken.getType().equals("CONSTTK"))
        {
            //creatAndGoToNode("ConstDecl",currentNode);
            analyseConstDecl();
        }
        else
        {
            //creatAndGoToNode("VarDecl",currentNode);
            analyseVarDecl();
        }
        //退出时为;
        //currentNode = currentNode.getFather();
        //grammar.add("<Decl>");
    }

    private void analyseFuncDef()
    {
        //进入为类型
        //FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // j
        grammar.add("<FuncType>");
        grammarTokens.add(new Token("GRAMMAR","<FuncType>",thisToken.getLineNumber()));
        nextToken();//ident
        nextToken();//(
        if (peekToken(0).getType().equals("RPARENT"))
        {
            nextToken();//)
        }
        else
        {
            if (peekToken(0).getType().equals("LBRACE"))
            {
                errors.put(thisToken.getLineNumber(),"j");
            }
            else
            {
                nextToken();//进下一个了
                //creatAndGoToNode("FuncFParams",currentNode);
                analyseFuncFParams();
                if (peekToken(0).getType().equals("LBRACE"))
                {
                    errors.put(thisToken.getLineNumber(),"j");
                }
                else
                {
                    nextToken();//)
                }
            }

        }
        nextToken();//{
        //creatAndGoToNode("Block",currentNode);
        analyseBlock();
        //退出时为{
        //currentNode = currentNode.getFather();
        grammar.add("<FuncDef>");
        grammarTokens.add(new Token("GRAMMAR","<FuncDef>",thisNum));
    }

    private void analyseBlock()
    {
        //进入时为{
        while (!peekToken(0).getType().equals("RBRACE"))
        {
            nextToken();//第一位
            //creatAndGoToNode("BlockItem",currentNode);
            analyseBlockItem();
        }
        nextToken();//}
        //currentNode = currentNode.getFather();
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
            analyseDecl();
        }
        else
        {
            analyseStmt();
        }
        //grammar.add("<BlockItem>");
    }

    private void analyseVarDecl() {
        //进入时为INTTK;
        //BType VarDef { ',' VarDef } ';' // i
        nextToken();// ident
        //creatAndGoToNode("VarDef",currentNode);
        analyseVarDef();
        while (peekToken(0).getType().equals("COMMA"))
        {
            nextToken();//,
            nextToken();//ident
           // creatAndGoToNode("VarDef",currentNode);
            analyseVarDef();
        }
        if (!peekToken(0).getType().equals("SEMICN"))
        {
            errors.put(thisToken.getLineNumber(),"i");
        }
        else
        {
            nextToken();//;
        }
        //退出时为;
        //currentNode = currentNode.getFather();
        grammar.add("<VarDecl>");
        grammarTokens.add(new Token("GRAMMAR","<VarDecl>",thisNum));
    }

    private void analyseVarDef() {
        //进入时为ident
        //变量定义 VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal // k
        if (peekToken(0).getType().equals("LBRACK"))
        {
            nextToken();//[
            nextToken();//进入
            analyseConstExp();
            if (peekToken(0).getType().equals("RBRACK"))
            {
                nextToken();//]
            }
            else
            {
                errors.put(thisToken.getLineNumber(),"k");
            }
        }
        if (peekToken(0).getType().equals("ASSIGN"))
        {
            nextToken();//=
            nextToken();//进下一个
            //creatAndGoToNode("InitVal",currentNode);
            analyseInitVal();
        }
        //退出时没有向前
        //currentNode = currentNode.getFather();
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
                nextToken();//}
                //currentNode.setValue();
                grammar.add("<InitVal>");
                grammarTokens.add(new Token("GRAMMAR","<InitVal>",thisNum));
                //结束时为最后一个
            }
            else
            {
                int i = 0;
                do {
                    if (i++ != 0 && peekToken(0).getType().equals("COMMA"))
                    {
                        nextToken();//,
                    }
                    nextToken();//进入
                    analyseExp();
                } while (peekToken(0).getType().equals("COMMA"));
                nextToken();//}
                grammar.add("<InitVal>");
                grammarTokens.add(new Token("GRAMMAR","<InitVal>",thisNum));
                //结束时为最后一个
            }
        }
        else if (thisToken.getType().equals("STRCON"))
        {
            grammar.add("<InitVal>");
            grammarTokens.add(new Token("GRAMMAR","<InitVal>",thisNum));
        }
        else
        {
            analyseExp();
            grammar.add("<InitVal>");
            grammarTokens.add(new Token("GRAMMAR","<InitVal>",thisNum));
        }
        //currentNode = currentNode.getFather();
    }

    private void analyseConstExp() {
        //进入时为第一位
        analyseAddExp();
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
                nextToken();//第一位
            }
            analyseMulExp();
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
                nextToken();//进入第一位
            }
            analyseUnaryExp();
        }
        while (peekToken(0).getType().equals("MULT") || peekToken(0).getType().equals("DIV")
                || peekToken(0).getType().equals("MOD"));
        grammar.add("<MulExp>");
        grammarTokens.add(new Token("GRAMMAR","<MulExp>",thisNum));
    }

    private void analyseUnaryOp()
    {
        grammar.add("<UnaryOp>");
        grammarTokens.add(new Token("GRAMMAR","<UnaryOp>",thisNum));
    }

    private void analyseUnaryExp() {
        //进入为第一位
        if (thisToken.getType().equals("PLUS") || thisToken.getType().equals("MINU") || thisToken.getType().equals("NOT"))
        {
            analyseUnaryOp();
            nextToken();
            analyseUnaryExp();
            grammar.add("<UnaryExp>");
            grammarTokens.add(new Token("GRAMMAR","<UnaryExp>",thisNum));
        }
        //这里需要分辨是函数还是左值
        else if (thisToken.getType().equals("IDENFR") && peekToken(0).getType().equals("LPARENT"))
            // Ident '(' [FuncRParams] ')'
        {
            nextToken();//(
            if (peekToken(0).getType().equals("RPARENT"))
            {
                nextToken();//)
                grammar.add("<UnaryExp>");
                grammarTokens.add(new Token("GRAMMAR","<UnaryExp>",thisNum));
                return;
            }
            else if (peekToken(0).getType().equals("SEMICN") || isNotFuncRParams())
            {
                errors.put(thisToken.getLineNumber(),"j");
                grammar.add("<UnaryExp>");
                grammarTokens.add(new Token("GRAMMAR","<UnaryExp>",thisNum));
                return;
            }
            else
            {
                //看看有没有实参表
                nextToken();//第一位
                analyseFuncRParams();
                if (peekToken(0).getType().equals("RPARENT"))
                {
                    nextToken();//)
                    grammar.add("<UnaryExp>");
                    grammarTokens.add(new Token("GRAMMAR","<UnaryExp>",thisNum));
                    return;
                }
                else
                {
                    errors.put(thisToken.getLineNumber(),"j");
                    grammar.add("<UnaryExp>");
                    grammarTokens.add(new Token("GRAMMAR","<UnaryExp>",thisNum));
                    return;
                }
            }
        }
        else
        {
            analysePrimaryExp();
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
                nextToken();//第一位
            }
            analyseExp();
        }while(peekToken(0).getType().equals("COMMA"));
        grammar.add("<FuncRParams>");
        grammarTokens.add(new Token("GRAMMAR","<FuncRParams>",thisNum));
    }

    private void analysePrimaryExp() {
        //进入为第一位
        if (thisToken.getType().equals("LPARENT"))
        {
            nextToken();//(
            analyseExp();
            if (!peekToken(0).getType().equals("RPARENT"))
            {
                errors.put(thisToken.getLineNumber(),"j");
            }
            else
            {
                nextToken();//)
            }
        }
        else if (thisToken.getType().equals("INTCON"))
        {
            //nextToken();//intConst
            analyseNumber();
        }
        else if (thisToken.getType().equals("CHRCON"))
        {
            //nextToken();//CHARCONST
            analyseCharacter();
        }
        else
        {
            //进入第一个
            analyseLVal();
        }
        grammar.add("<PrimaryExp>");
        grammarTokens.add(new Token("GRAMMAR","<PrimaryExp>",thisNum));
    }

    private void analyseCharacter()
    {
        grammar.add("<Character>");
        grammarTokens.add(new Token("GRAMMAR","<Character>",thisNum));
    }

    private void analyseNumber()
    {
        grammar.add("<Number>");
        grammarTokens.add(new Token("GRAMMAR","<Number>",thisNum));
    }

    private void analyseLVal() {
        //进入为第一个
        if (peekToken(0).getType().equals("LBRACK"))
        {
            nextToken();//[
            if (peekToken(0).getType().equals("RBRACK"))
            {
                nextToken();//]
            }
            else if (peekToken(0).getType().equals("LPARENT") || peekToken(0).getType().equals("IDENFR") ||
                        peekToken(0).getType().equals("INTCON") || peekToken(0).getType().equals("CHACON") ||
                        peekToken(0).getType().equals("PLUS") || peekToken(0).getType().equals("MINU"))
            {
                nextToken();//进第一个
                analyseExp();
                if (peekToken(0).getType().equals("RBRACK"))
                {
                    nextToken();//]
                }
                else
                {
                    errors.put(thisToken.getLineNumber(),"k");
                }
            }
            else
            {
                errors.put(thisToken.getLineNumber(),"k");
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
        nextToken();//bType
        nextToken();//进入第一位
        int i = 0;
        do {
            if (i++ != 0 && (peekToken(0).getType().equals("COMMA")))
            {
                nextToken();//,
                nextToken();//进第一位
            }
            analyseConstDef();
        } while (peekToken(0).getType().equals("COMMA"));
        if (peekToken(0).getType().equals("SEMICN"))
        {
            nextToken();//;
        }
        else
        {
            errors.put(thisToken.getLineNumber(),"i");
        }
        grammar.add("<ConstDecl>");
        grammarTokens.add(new Token("GRAMMAR","<ConstDecl>",thisNum));
    }

    private void analyseConstDef() { //Ident [ '[' ConstExp ']' ] '=' ConstInitVal // k
        //进入第一位
        if (peekToken(0).getType().equals("LBRACK"))
        {
            nextToken();//[
            if (peekToken(0).getType().equals("RBRACK"))
            {
                nextToken();//]
            }
            else if (peekToken(0).getType().equals("ASSIGN"))
            {
                errors.put(thisToken.getLineNumber(),"k");
            }
            else
            {
                nextToken();//进入
                analyseConstExp();
                if (peekToken(0).getType().equals("RBRACK"))
                {
                    nextToken();//]
                }
                else
                {
                    errors.put(thisToken.getLineNumber(),"k");
                }
            }
        }
        nextToken();//=
        nextToken();//进入
        analyseConstInitVal();
        grammar.add("<ConstDef>");
        grammarTokens.add(new Token("GRAMMAR","<ConstDef>",thisNum));
    }

    private void analyseConstInitVal() {
        //常量初值 ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
        //进入为第一个
        if (thisToken.getType().equals("STRCON"))
        {
            grammar.add("<ConstInitVal>");//str
            grammarTokens.add(new Token("GRAMMAR","<ConstInitVal>",thisNum));
            return;
        }
        else if (thisToken.getType().equals("LBRACE"))
        {
            //现在已经是{
            if (peekToken(0).getType().equals("RBRACE"))
            {
                nextToken();//}
            }
            else
            {
                nextToken();//第一个
                int i = 0;
                do {
                    if (i++ != 0  && (peekToken(0).getType().equals("COMMA")))
                    {
                        nextToken();//,
                        nextToken();//第一个
                    }
                    analyseConstExp();
                }while (peekToken(0).getType().equals("COMMA"));
                nextToken();//}
            }
        }
        else
        {
            analyseConstExp();
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
                nextToken();//第一个
            }
            analyseFuncFParam();
        }while(peekToken(0).getType().equals("COMMA"));
        grammar.add("<FuncFParams>");
        grammarTokens.add(new Token("GRAMMAR","<FuncFParams>",thisNum));
    }

    private void analyseFuncFParam() {
        //FuncFParam → BType Ident ['[' ']'] // k
        //进入为第一位
        nextToken();//Ident
        if (peekToken(0).getType().equals("LBRACK"))
        {
            nextToken();//[
            if (peekToken(0).getType().equals("RBRACK"))
            {
                nextToken();//]
            }
            else
            {
                errors.put(thisToken.getLineNumber(),"k");
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
            analyseLVal();
            if (peekToken(0).getType().equals("SEMICN")) //[Exp] ';' // i
            {
                nextToken();//;
                grammar.add("<Stmt>");
                grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
                return;
            }
            else if (peekToken(0).getType().equals("ASSIGN"))
            {
                nextToken();//=
                if (peekToken(0).getType().equals("GETINTTK") || peekToken(0).getType().equals("GETCHARTK"))//get*2
                {
                    nextToken();//get
                    nextToken();//(
                    if (peekToken(0).getType().equals("SEMICN"))
                    {
                        errors.put(thisToken.getLineNumber(),"j");
                        nextToken();//;
                        grammar.add("<Stmt>");
                        grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
                        return;
                    }
                    else
                    {
                        nextToken();//)
                        if (!peekToken(0).getType().equals("SEMICN"))
                        {
                            errors.put(thisToken.getLineNumber(),"i");
                        }
                        else
                        {
                            nextToken();//;
                        }
                        grammar.add("<Stmt>");
                        grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
                        return;
                    }
                }
                else //exp
                {
                    nextToken();//进
                    analyseExp();
                    if (peekToken(0).getType().equals("SEMICN"))
                    {
                        nextToken();//;
                    }
                    else
                    {
                        errors.put(thisToken.getLineNumber(),"i");

                    }
                    grammar.add("<Stmt>");
                    grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
                    return;
                }
            }
        }
        else if (thisToken.getType().equals("LBRACE")) // block
        {
            analyseBlock();
            grammar.add("<Stmt>");
            grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
        }
        else if (thisToken.getType().equals("IFTK")) //if
        {
            nextToken();//(
            nextToken();//进入
            analyseCond();
            if (!peekToken(0).getType().equals("RPARENT"))
            {
                errors.put(thisToken.getLineNumber(),"j");
            }
            else
            {
                nextToken();//)
            }
            nextToken();//进
            analyseStmt();
            if (peekToken(0).getType().equals("ELSETK"))
            {
                nextToken();//else
                nextToken();//进
                analyseStmt();
            }
            grammar.add("<Stmt>");
            grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
            return;
        }
        else if (thisToken.getType().equals("PRINTFTK"))
        {
            //'printf''('StringConst {','Exp}')'';' // i j
            nextToken();//(
            nextToken();//str
            while (peekToken(0).getType().equals("COMMA"))
            {
                nextToken();//,
                nextToken();//进入
                analyseExp();
            }
            if (peekToken(0).getType().equals("SEMICN"))
            {
                errors.put(thisToken.getLineNumber(),"j");
            }
            else
            {
                nextToken();//)
                if (!peekToken(0).getType().equals("SEMICN"))
                {
                    errors.put(thisToken.getLineNumber(),"i");
                }
                else
                {
                    nextToken();//;
                }
            }
            grammar.add("<Stmt>");
            grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
            return;
        }
        else if(thisToken.getType().equals("RETURNTK"))
        {
            if (peekToken(0).getType().equals("SEMICN"))
            {
                nextToken();//;
                grammar.add("<Stmt>");
                grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
                return;
            }
            else
            {
                if (isExp())
                {
                    nextToken();
                    analyseExp();
                    if (peekToken(0).getType().equals("SEMICN"))
                    {
                        nextToken();//;
                    }
                    else
                    {
                        errors.put(thisToken.getLineNumber(),"i");
                    }
                }
                else
                {
                    errors.put(thisToken.getLineNumber(),"i");
                }
            }
            grammar.add("<Stmt>");
            grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
            return;
        }
        else if(thisToken.getType().equals("FORTK")) //'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
        {
            nextToken();//(
            if (!peekToken(0).getType().equals("SEMICN"))
            {
                nextToken();//进入
                analyseForStmt();
                nextToken();//;
            }
            else
            {
                nextToken();//;
            }
            if (!peekToken(0).getType().equals("SEMICN"))
            {
                nextToken();//进入
                analyseCond();
                nextToken();//;
            }
            else
            {
                nextToken();//;
            }
            if (!peekToken(0).getType().equals("RPARENT"))
            {
                nextToken();//进入
                analyseForStmt();
                nextToken();//)
            }
            else
            {
                nextToken();//)
            }
            nextToken();//进入
            analyseStmt();
            grammar.add("<Stmt>");
            grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
            return;
        }
        else if(thisToken.getType().equals("BREAKTK") || thisToken.getType().equals("CONTINUETK"))
        {
            if (peekToken(0).getType().equals("SEMICN"))
            {
                nextToken();//;
            }
            else
            {
                errors.put(thisToken.getLineNumber(),"i");
            }
            grammar.add("<Stmt>");
            grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
            return;
        }
        else if (thisToken.getType().equals("SEMICN"))
        {
            grammar.add("<Stmt>");
            grammarTokens.add(new Token("GRAMMAR","<Stmt>",thisNum));
            return;
        }
        else
        {
            analyseExp();
            if (peekToken(0).getType().equals("SEMICN"))
            {
                nextToken();//;
            }
            else
            {
                errors.put(thisToken.getLineNumber(),"i");
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
        analyseLVal();
        nextToken();//=
        nextToken();//进
        analyseExp();
        grammar.add("<ForStmt>");
        grammarTokens.add(new Token("GRAMMAR","<ForStmt>",thisNum));
    }

    private void analyseCond() {
        analyseLOrExp();
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
                nextToken();//进
            }
            analyseLAndExp();
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
                nextToken();//进
            }
            analyseEqExp();
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
                nextToken();//进
            }
            analyseRelExp();
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
                nextToken();//进
            }
            analyseAddExp();
        }while (peekToken(0).getType().equals("LSS") || peekToken(0).getType().equals("LEQ")||
                peekToken(0).getType().equals("GRE") || peekToken(0).getType().equals("GEQ"));
        grammar.add("<RelExp>");
        grammarTokens.add(new Token("GRAMMAR","<RelExp>",thisNum));
    }

    private void analyseExp() {
        //进入时为第一个
        analyseAddExp();
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
