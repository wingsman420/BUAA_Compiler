package LLVM;

import LLVM.RealValue.*;
import LLVM.classes.BlockItems.BasicBlock;
import LLVM.classes.BlockItems.Instruction;
import LLVM.classes.BlockItems.items.*;
import LLVM.classes.module.*;
import LLVM.classes.module.Module;
import Tree.BranchNode;
import Tree.LeafNode;
import Tree.Node;
import base.symbol.Symbol;
import base.symbol.SymbolTable;
import base.symbol.symbol.*;
import frontend.FileProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LLVMGenerator {

    private Module module;

    private BranchNode ASTRoot;
    private BranchNode ASTNow;
    private SymbolTable STRoot;
    private SymbolTable STNow;
    private int idCounter;
    private int tableIdCounter = 0;
    private Stack<Integer> idStack;
    private String nowReturnType;
    private int ConstStr;
    private Function nowFunction;
    private BasicBlock nowBasicBlock;
    private ArrayList<BasicBlock> basicBlocks;
    private ArrayList<Instruction> instructions;
    private int basicBlockIdCounter;


    public LLVMGenerator(BranchNode ASTRoot) {
        this.module = new Module();
        this.ASTRoot = ASTRoot;
        this.STRoot = new SymbolTable(null,tableIdCounter++);
        this.STNow = STRoot;
        this.ASTNow = ASTRoot;
        this.idCounter = 0;
        this.idStack = new Stack<>();
        ConstStr = 0;
        this.nowFunction = null;
        this.nowBasicBlock = null;
        this.basicBlocks = new ArrayList<>();
        this.instructions = new ArrayList<>();
        this.basicBlockIdCounter = 0;
    }



    public void analyseCompUnit()
    {
        //从根节点进来
        int i = 0;
        BranchNode temp = ASTNow;
        for (Node node : temp.getChildren())
        {
            if (node.getType().equals("<Decl>"))
            {
                ASTNow = (BranchNode) ASTNow.getChildren().get(i);
                analyseGlobalDecl();
                ASTNow = ASTNow.getParent();
            }
            else if (node.getType().equals("<FuncDef>"))
            {
                //System.out.println(ASTNow.getType());
                ASTNow = (BranchNode) ASTNow.getChildren().get(i);
                analyseFuncDef();
                ASTNow = ASTNow.getParent();
            }
            else
            {
                ASTNow = (BranchNode) ASTNow.getChildren().get(i);
                analyseMainFuncDef();
                ASTNow = ASTNow.getParent();
            }
            i++;
        }
    }

    private void analyseGlobalDecl() {
        ASTNow = (BranchNode) ASTNow.getChildren().get(0);
        //只有一个声明，所以直接进入
        if (ASTNow.getType().equals("<ConstDecl>"))
        {
            //常量
            //ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
            analyseGlobalConstDecl();
            ASTNow = ASTNow.getParent();
        }
        else
        {
            //变量
            analyseGlobalVarDecl();
            ASTNow = ASTNow.getParent();
        }
    }

    private void analyseGlobalVarDecl() {
        String type = ASTNow.getChildren().get(0).getType();
        //Btype
        for (Node node : ASTNow.getChildren()) {
            if (node.getType().equals("<VarDef>"))
            {
                ASTNow = (BranchNode) node;
                analyseGlobalVarDef(type);
                ASTNow = ASTNow.getParent();
            }
        }
    }

    private void analyseGlobalVarDef(String type) {
        String name = ((LeafNode)ASTNow.getChildren().get(0)).getToken().getValue();
        if (ASTNow.getChildren().size() > 3)
        {
            //Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
            ExpCalculator ec = new ExpCalculator(STNow);
            int length = ec.calculateConstExp((BranchNode) ASTNow.getChildren().get(2));
            if (type.equals("CHARTK"))
            {
                Symbol tmp =  new CharArraySymbol(1,1,name,length);
                CharArray rv = new CharArray(length);
                if (ASTNow.getChildren().size() > 4)
                {
                    if ((((BranchNode)ASTNow.getChildren().get(5)).getChildren().get(0)).getType().equals("LBRACE"))
                    {
                        StringBuilder sb = new StringBuilder();
                        for (Node node : ((BranchNode)ASTNow.getChildren().get(5)).getChildren())
                        {
                            if (node.getType().equals("<Exp>"))
                            {
                                sb.append((char) ec.calculateConstExp((BranchNode) node));
                            }
                        }
                        rv.setValue(sb.toString());
                    }
                    else
                    {
                        String temp = ((LeafNode)((BranchNode)ASTNow.getChildren().get(5)).getChildren().get(0)).getToken().getValue();
                        rv.setValue(temp.substring(1, temp.length() - 1));
                    }
                }
                tmp.setRv(rv);
                STNow.symbols.add(tmp);
                GlobalVariable gv = new GlobalVariable(idCounter++,name,type,false);
                gv.setValue(rv);
                module.addGlobalVariable(gv);
            }
            else
            {
                Symbol tmp =  new IntArraySymbol(1,1,name);
                IntArray rv = new IntArray(length);
                if (ASTNow.getChildren().size() > 4)
                {
                    ArrayList<Integer> values = new ArrayList<>();
                    for (Node node : ((BranchNode)ASTNow.getChildren().get(5)).getChildren())
                    {
                        if (node.getType().equals("<Exp>"))
                        {
                            values.add(ec.calculateConstExp((BranchNode) node));
                        }
                    }
                    rv.addToList(values);
                }
                tmp.setRv(rv);
                STNow.symbols.add(tmp);
                STNow.symbols.add(tmp);
                GlobalVariable gv = new GlobalVariable(idCounter++,name,type,false);
                gv.setValue(rv);
                module.addGlobalVariable(gv);
            }
        }
        else
        {
            //VarDef → Ident  | Ident  '=' InitVal
            ExpCalculator ec = new ExpCalculator(STNow);
            if (type.equals("CHARTK"))
            {
                Symbol tmp =  new CharSymbol(1,1,name);
                AChar rv = new AChar();
                if (ASTNow.getChildren().size() > 1)
                {
                    rv.setInitValue(ec.calculateConstExp((BranchNode) ((BranchNode) ASTNow.getChildren().get(2)).getChildren().get(0)));
                }
                tmp.setRv(rv);
                STNow.symbols.add(tmp);
                GlobalVariable gv = new GlobalVariable(idCounter++,name,type,false);
                gv.setValue(rv);
                module.addGlobalVariable(gv);
            }
            else
            {
                Symbol tmp =  new IntSymbol(1,1,name);
                AInt rv = new AInt();
                if (ASTNow.getChildren().size() > 1)
                {
                    rv.setInitValue(ec.calculateConstExp((BranchNode) ((BranchNode)ASTNow.getChildren().get(2)).getChildren().get(0)));
                }
                tmp.setRv(rv);
                STNow.symbols.add(tmp);
                GlobalVariable gv = new GlobalVariable(idCounter++,name,type,false);
                gv.setValue(rv);
                module.addGlobalVariable(gv);
            }
        }
    }

    private void analyseGlobalConstDecl() {
        String type = ASTNow.getChildren().get(1).getType();
        //Btype
        for (Node node : ASTNow.getChildren()) {
            if (node.getType().equals("<ConstDef>"))
            {
                ASTNow = (BranchNode) node;
                analyseGlobalConstDef(type);
                ASTNow = ASTNow.getParent();
            }
        }

    }

    private void analyseGlobalConstDef(String type) {
        String name = ((LeafNode)ASTNow.getChildren().get(0)).getToken().getValue();
        if (ASTNow.getChildren().size() > 3)
        {
            //ConstDef → Ident '[' ConstExp ']'  '=' ConstInitVal
            ExpCalculator ec = new ExpCalculator(STNow);
            int length = ec.calculateConstExp((BranchNode) ASTNow.getChildren().get(2));
            if (type.equals("CHARTK"))
            {
                Symbol tmp =  new ConstCharArraySymbol(1,1,name);
                CharArray rv = new CharArray(length);
                if ((((BranchNode)ASTNow.getChildren().get(5)).getChildren().get(0)).getType().equals("LBRACE"))
                {
                    StringBuilder sb = new StringBuilder();
                    for (Node node : ((BranchNode)ASTNow.getChildren().get(5)).getChildren())
                    {
                        if (node.getType().equals("<ConstExp>"))
                        {
                            sb.append((char) ec.calculateConstExp((BranchNode) node));
                        }
                    }
                    rv.setValue(sb.toString());
                }
                else
                {
                    String temp = ((LeafNode)((BranchNode)ASTNow.getChildren().get(5)).getChildren().get(0)).getToken().getValue();
                    rv.setValue(temp.substring(1, temp.length() - 1));
                }
                tmp.setRv(rv);
                STNow.symbols.add(tmp);
                GlobalVariable gv = new GlobalVariable(idCounter++,name,type,true);
                gv.setValue(rv);
                module.addGlobalVariable(gv);
            }
            else
            {
                Symbol tmp =  new ConstIntArraySymbol(1,1,name);
                IntArray rv = new IntArray(length);
                ArrayList<Integer> values = new ArrayList<>();
                for (Node node : ((BranchNode)ASTNow.getChildren().get(5)).getChildren())
                {
                    if (node.getType().equals("<ConstExp>"))
                    {
                        values.add(ec.calculateConstExp((BranchNode) node));
                    }
                }
                rv.addToList(values);
                tmp.setRv(rv);
                STNow.symbols.add(tmp);
                GlobalVariable gv = new GlobalVariable(idCounter++,name,type,true);
                gv.setValue(rv);
                module.addGlobalVariable(gv);
            }
        }
        else
        {
            //ConstDef → Ident '=' ConstInitVal
            ExpCalculator ec = new ExpCalculator(STNow);
            if (type.equals("CHARTK"))
            {
                Symbol tmp =  new ConstCharSymbol(1,1,name);
                AChar rv = new AChar();
                rv.setInitValue(ec.calculateConstExp((BranchNode) ((BranchNode) ASTNow.getChildren().get(2)).getChildren().get(0)));
                tmp.setRv(rv);
                STNow.symbols.add(tmp);
                GlobalVariable gv = new GlobalVariable(idCounter++,name,type,true);
                gv.setValue(rv);
                module.addGlobalVariable(gv);
            }
            else
            {
                Symbol tmp =  new ConstIntSymbol(1,1,name);
                AInt rv = new AInt();
                rv.setInitValue(ec.calculateConstExp((BranchNode) ((BranchNode)ASTNow.getChildren().get(2)).getChildren().get(0)));
                tmp.setRv(rv);
                STNow.symbols.add(tmp);
                GlobalVariable gv = new GlobalVariable(idCounter++,name,type,true);
                gv.setValue(rv);
                module.addGlobalVariable(gv);
            }
        }
    }

    private void analyseMainFuncDef()
    {
        //MainFuncDef → 'int' 'main' '(' ')' Block
        String type = "INTTK";
        String name = "main";
        Function function = new Function(idCounter++,name,type);
        nowFunction = function;
        //进入
        idStack.add(idCounter);
        idCounter = 0;
        SymbolTable st = new SymbolTable(STNow,1);
        STNow.children.add(st);
        STNow = st;
        nowReturnType = "INTTK";
        for (Node node : ASTNow.getChildren()) {
            if (node.getType().equals("<Block>"))
            {
                ASTNow = (BranchNode) node;
                BasicBlock mainBlock = new BasicBlock(idCounter++,name);
                nowBasicBlock = mainBlock;
                analyseFunctionBlock(name,new ArrayList<>());
                function.addBasicBlock(nowBasicBlock);
                ASTNow = ASTNow.getParent();
                break;
            }
        }
        //退出
        STNow = STNow.fatherTable;
        idCounter = idStack.pop();
        module.addFunction(function);
    }


    private void analyseFuncDef()
    {
        //FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        String type = ASTNow.getChildren().get(0).getType();
        //int/char/void
        String name = ((LeafNode)ASTNow.getChildren().get(1)).getToken().getValue();
        Function function = new Function(idCounter++,name,type);
        nowFunction = function;
        //进入function
        idStack.add(idCounter);
        idCounter = 0;
        SymbolTable st = new SymbolTable(STNow,1);
        STNow.children.add(st);
        STNow = st;
        ArrayList<Argument> args = new ArrayList();
        for (Node node : ASTNow.getChildren()) {
            if (node.getType().equals("RPARENT"))
            {
                break;
            }
            if (node.getType().equals("<FuncFParams>"))
            {
                ASTNow = (BranchNode) node;
                args.addAll(analyseFuncFParams());
                ASTNow = ASTNow.getParent();
                break;
            }
        }
        function.addArgument(args);
        module.addFunction(function);
        //先加入防止递归
        //分析基本块
        nowReturnType = type;
        for (Node node : ASTNow.getChildren()) {
            if (node.getType().equals("<Block>"))
            {
                ASTNow = (BranchNode) node;
                BasicBlock funcMainBlock = new BasicBlock(idCounter++,name);
                nowBasicBlock = funcMainBlock;
                analyseFunctionBlock(name,args);
                function.addBasicBlock(nowBasicBlock);
                ASTNow = ASTNow.getParent();
                break;
            }
        }
        //退出
        STNow = STNow.fatherTable;
        idCounter = idStack.pop();
        if (function.getReturnType().equals("VOIDTK"))
        {
            RetInst ri = new RetInst(0);
            function.getBasicBlocks().get(0).addInstruction(ri);
        }
        module.deleteFunction(function.name);
        module.addFunction(function);
        //不存在局部函数，所以我认为不需要给这些函数加入符号表
    }

    private ArrayList<Argument> analyseFuncFParams()
    {
        ArrayList<Argument> args = new ArrayList<>();
        BranchNode temp = ASTNow;
        for (Node node : temp.getChildren()) {
            if (node.getType().equals("<FuncFParam>"))
            {
                ASTNow = (BranchNode) node;
                args.add(analyseFuncFParam());
                ASTNow = ASTNow.getParent();
            }
        }
        return args;
    }

    private Argument analyseFuncFParam()
    {
        String type = ASTNow.getChildren().get(0).getType();
        String name = ((LeafNode)ASTNow.getChildren().get(1)).getToken().getValue();
        boolean isArray = false;
        if (ASTNow.getChildren().size() > 2)
        {
            isArray = true;
        }
        Symbol ii;
        if (type.equals("CHARTK"))
        {
            if (isArray)
            {
                ii = new CharArraySymbol(idCounter,1,name);

            }
            else
            {
                ii = new CharSymbol(idCounter,1,name);
            }
        }
        else
        {
            if (isArray)
            {
                ii = new IntArraySymbol(idCounter,1,name);
            }
            else
            {
                ii = new IntSymbol(idCounter,1,name);
            }
        }
        STNow.symbols.add(ii);
        return new Argument(idCounter++,name,type,isArray);
    }


    private void analyseFunctionBlock(String name,ArrayList<Argument> args)
    {
        //Block → '{' { BlockItem } '}'
        //先给形式参数分配空间
        for (Argument arg : args)
        {
            if (arg.getType().equals("INTTK"))
            {
                if (arg.isArray())
                {
                    AllocateInst ai = new AllocateInst(idCounter++,"i32*");
                    nowBasicBlock.addInstruction(ai);
                    int org = STNow.legalVar(arg.getName()).getId();
                    STNow.legalVar(arg.getName()).setid(idCounter - 1);
                    StoreInst si = new StoreInst(-1,org,idCounter - 1,true,false,true);
                    nowBasicBlock.addInstruction(si);
                }
                else
                {
                    AllocateInst ai = new AllocateInst(idCounter++,"i32");
                    nowBasicBlock.addInstruction(ai);
                    int org = STNow.legalVar(arg.getName()).getId();
                    STNow.legalVar(arg.getName()).setid(idCounter - 1);
                    StoreInst si = new StoreInst(-1,org,idCounter - 1,true,false,false);
                    nowBasicBlock.addInstruction(si);
                }
            }
            else
            {
                if (arg.isArray())
                {
                    AllocateInst ai = new AllocateInst(idCounter++,"i8*");
                    nowBasicBlock.addInstruction(ai);
                    int org = STNow.legalVar(arg.getName()).getId();
                    STNow.legalVar(arg.getName()).setid(idCounter - 1);
                    StoreInst si = new StoreInst(-1,org,idCounter - 1,false,false,true);
                    nowBasicBlock.addInstruction(si);
                }
                else
                {
                    AllocateInst ai = new AllocateInst(idCounter++,"i8");
                    nowBasicBlock.addInstruction(ai);
                    int org = STNow.legalVar(arg.getName()).getId();
                    STNow.legalVar(arg.getName()).setid(idCounter - 1);
                    StoreInst si = new StoreInst(-1,org,idCounter - 1,false,false,false);
                    nowBasicBlock.addInstruction(si);
                }
            }
        }
        BranchNode temp = ASTNow;
        for (Node node : temp.getChildren()) {
            if (node.getType().equals("<BlockItem>"))
            {
                ASTNow = (BranchNode) node;
                analyseBlockItem(0,0);
                //这时候可能basicBlock已经变了
                ASTNow = ASTNow.getParent();
            }
        }
        nowBasicBlock.addInstruction(instructions);
        instructions.clear();
    }

    private void analyseBlockItem(int STrueEnd,int SFalseEnd)
    {
        //BlockItem → Decl | Stmt
        if (ASTNow.getChildren().get(0).getType().equals("<Stmt>"))
        {
            ASTNow = (BranchNode) ASTNow.getChildren().get(0);
            analyseStmt(STrueEnd,SFalseEnd);
            ASTNow = ASTNow.getParent();
        }
        else
        {
            ASTNow = (BranchNode) ASTNow.getChildren().get(0);
            analyseDecl();
            ASTNow = ASTNow.getParent();
        }
    }

    private void analyseDecl()
    {
        String type;
        ASTNow = (BranchNode) ASTNow.getChildren().get(0);
        if (ASTNow.getType().equals("<ConstDecl>"))
        {
            type =  ASTNow.getChildren().get(1).getType();
            //ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
            for (Node node : ASTNow.getChildren()) {
                if (node.getType().equals("<ConstDef>"))
                {
                    ASTNow = (BranchNode) node;
                    analyseConstDef(type);
                    ASTNow = ASTNow.getParent();
                }
            }

        }
        else
        {
            type =  ASTNow.getChildren().get(0).getType();
            //VarDecl → BType VarDef { ',' VarDef } ';'
            for (Node node : ASTNow.getChildren()) {
                if (node.getType().equals("<VarDef>"))
                {
                    ASTNow = (BranchNode) node;
                    analyseVarDef(type);
                    ASTNow = ASTNow.getParent();
                }
            }
        }
        ASTNow = ASTNow.getParent();
    }

    private void analyseVarDef(String type)
    {
        //Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
        String name = ((LeafNode)ASTNow.getChildren().get(0)).getToken().getValue();
        if (ASTNow.getChildren().size() > 1 && ASTNow.getChildren().get(1).getType().equals("LBRACK"))
        {
            //说明是数组
            ExpCalculator ec = new ExpCalculator(STNow);
            int length = ec.calculateConstExp((BranchNode) ASTNow.getChildren().get(2));
            if (type.equals("INTTK"))
            {
                IntArraySymbol symbol = new IntArraySymbol(idCounter++,1,name);
                //没有初始值
                IntArray rv = new IntArray(length);
                symbol.setRv(rv);
                STNow.symbols.add(symbol);
                //int 数组
                AllocateInst inst = new AllocateInst(idCounter - 1,"[" + length + " x i32]");
                instructions.add(inst);
                String fakeName = "%" + (idCounter - 1);
                IntArraySymbol symbol1 = new IntArraySymbol(idCounter++,1,name+"wingman");
                STNow.symbols.add(symbol1);
                instructions.add(new GepInst(idCounter - 1,fakeName,true,length,true));
            }
            else
            {
                //字符串数组
                CharArraySymbol symbol = new CharArraySymbol(idCounter++,1,name);
                //没有初始值
                CharArray rv = new CharArray(length);
                symbol.setRv(rv);
                STNow.symbols.add(symbol);
                AllocateInst inst = new AllocateInst(idCounter - 1 ,"[" + length + " x i8]");
                instructions.add(inst);
                String fakeName = "%" + (idCounter - 1);
                CharArraySymbol symbol1 = new CharArraySymbol(idCounter++,1,name+"wingman");
                STNow.symbols.add(symbol1);
                instructions.add(new GepInst(idCounter - 1,fakeName,false,length,true));
            }
            //算初始值
            if (ASTNow.getChildren().size() > 4)
            {
                ASTNow = (BranchNode) ASTNow.getChildren().get(5);
                analyseInitVal(type,idCounter - 1,length);
                ASTNow = ASTNow.getParent();
            }
            //存初始值在上面做了
        }
        else
        {
            //说明是单个整数、字符
            if (type.equals("INTTK"))
            {
                IntSymbol symbol = new IntSymbol(idCounter++,1,name);
                AInt rv = new AInt();
                symbol.setRv(rv);
                STNow.symbols.add(symbol);
                AllocateInst inst = new AllocateInst(idCounter - 1,"i32");
                instructions.add(inst);
            }
            else
            {
                CharSymbol symbol = new CharSymbol(idCounter++,1,name);
                AChar rv = new AChar();
                symbol.setRv(rv);
                STNow.symbols.add(symbol);
                AllocateInst inst = new AllocateInst(idCounter - 1,"i8");
                instructions.add(inst);
            }
            if (ASTNow.getChildren().size() > 2)
            {
                ASTNow = (BranchNode) ASTNow.getChildren().get(2);
                analyseInitVal(type,idCounter - 1,0);
                ASTNow = ASTNow.getParent();
            }
        }
    }

    private void analyseInitVal(String type, int name,int length){
        if (type.equals("INTTK"))
        {
            if (length != 0)
            {
                int tmp;
                int i = 0;
                for (Node node : ASTNow.getChildren()) {
                    if (node.getType().equals("<Exp>") || node.getType().equals("<ConstExp>"))
                    {
                        ASTNow = (BranchNode) node;
                        analyseExp();
                        ASTNow = ASTNow.getParent();
                        tmp = instructions.get(instructions.size() - 1).getId();
                        GepInst inst = new GepInst(0,idCounter++,"%" + name,i,true,true);
                        instructions.add(inst);
                        StoreInst si = new StoreInst(-1,tmp,idCounter - 1,true,false);
                        instructions.add(si);
                        i++;
                    }
                }
            }
            else
            {
                //不是数组
                int tmp;
                ASTNow = (BranchNode) ASTNow.getChildren().get(0);
                analyseExp();
                ASTNow = ASTNow.getParent();
                tmp = instructions.get(instructions.size() - 1).getId();
                StoreInst si = new StoreInst(-1,tmp,name,true,false);
                instructions.add(si);
            }
        }
        else
        {
            //char
            if (length != 0)
            {
                //string
                if (ASTNow.getChildren().size() > 1)
                {
                    //"{a,b,c,d}"
                    int tmp;
                    int i = 0;
                    for (Node node : ASTNow.getChildren()) {
                        if (node.getType().equals("<Exp>")|| node.getType().equals("<ConstExp>"))
                        {
                            ASTNow = (BranchNode) node;
                            analyseExp();
                            ASTNow = ASTNow.getParent();
                            instructions.add(new TruncInst(idCounter++,true,idCounter - 2));
                            tmp = instructions.get(instructions.size() - 1).getId();
                            //tmp是存储的值
                            GepInst inst = new GepInst(0,idCounter++,"%" + name,i,false,true);
                            instructions.add(inst);
                            StoreInst si = new StoreInst(-1,tmp,idCounter - 1,false,false);
                            instructions.add(si);
                            i++;
                        }
                    }
                }
                else
                {
                    //"asdcda"
                    String initValue = ((LeafNode)ASTNow.getChildren().get(0)).getToken().getValue();
                    int  i = 0; // 实际位置
                    int j = 1; //字符串位置
                    for (i = 0;i < length;i++)
                    {
                        if (j < initValue.length() - 1)
                        {
                            char value = initValue.charAt(j);
                            if (value == '\\')
                            {
                                switch (initValue.charAt(j + 1)) {
                                    case '0':
                                        value = '\0';break;
                                    case 'n':
                                        value = '\n'; break;
                                    case 't':
                                        value = '\t';break;
                                    case 'b':
                                        value = '\b';break;
                                    case 'r':
                                        value = '\r';break;
                                    case 'f':
                                        value = '\f';break;
                                    case '\\':
                                        value = '\\';break;
                                    case '\'':
                                        value = '\'';break;
                                    case '\"':
                                        value = '\"';break;
                                }
                                j++;
                            }
                            GepInst inst = new GepInst(0,idCounter++,"%" + name,i,false,true);
                            instructions.add(inst);
                            StoreInst si = new StoreInst(-1,value,idCounter - 1,false,true);
                            instructions.add(si);
                            j++;
                        }
                        else
                        {
                            GepInst inst = new GepInst(0,idCounter++,"%" + name,i,false,true);
                            instructions.add(inst);
                            StoreInst si = new StoreInst(-1,0,idCounter - 1 ,false,true);
                            instructions.add(si);
                        }
                    }
                }
            }
            else
            {
                int tmp;
                ASTNow = (BranchNode) ASTNow.getChildren().get(0);
                analyseExp();
                ASTNow = ASTNow.getParent();
                tmp = instructions.get(instructions.size() - 1).getId();
                TruncInst ti = new TruncInst(idCounter++,true,idCounter - 2);
                instructions.add(ti);
                StoreInst si = new StoreInst(-1,instructions.get(instructions.size() - 1).getId(),name,false,false);
                instructions.add(si);
            }
        }
    }

    private void analyseConstDef(String type)
    {
        //Ident [ '[' ConstExp ']' ] '=' ConstInitVal
        String name = ((LeafNode)ASTNow.getChildren().get(0)).getToken().getValue();
        if (ASTNow.getChildren().get(1).getType().equals("LBRACK"))
        {
            //说明是数组
            ExpCalculator ec = new ExpCalculator(STNow);
            int length = ec.calculateConstExp((BranchNode) ASTNow.getChildren().get(2));
            if (type.equals("INTTK"))
            {
                ConstIntArraySymbol symbol = new ConstIntArraySymbol(idCounter++,1,name);
                //没有初始值
                IntArray rv = new IntArray(length);
                ArrayList<Integer> values = new ArrayList<>();
                for (Node node : ((BranchNode)ASTNow.getChildren().get(5)).getChildren())
                {
                    if (node.getType().equals("<ConstExp>"))
                    {
                        values.add(ec.calculateConstExp((BranchNode) node));
                    }
                }
                rv.addToList(values);
                symbol.setRv(rv);
                STNow.symbols.add(symbol);
                //int 数组
                AllocateInst inst = new AllocateInst(idCounter - 1,"[" + length + " x i32]");
                instructions.add(inst);
                String fakeName = "%" + (idCounter - 1);
                ConstIntArraySymbol symbol1 = new ConstIntArraySymbol(idCounter++,1,name+"wingman");
                STNow.symbols.add(symbol1);
                instructions.add(new GepInst(idCounter - 1,fakeName,true,length,true));
            }
            else
            {
                //字符串数组
                ConstCharArraySymbol symbol = new ConstCharArraySymbol(idCounter++,1,name);
                //没有初始值
                CharArray rv = new CharArray(length);
                if ((((BranchNode)ASTNow.getChildren().get(5)).getChildren().get(0)).getType().equals("LBRACE"))
                {
                    StringBuilder sb = new StringBuilder();
                    for (Node node : ((BranchNode)ASTNow.getChildren().get(5)).getChildren())
                    {
                        if (node.getType().equals("<ConstExp>"))
                        {
                            sb.append((char) ec.calculateConstExp((BranchNode) node));
                        }
                    }
                    rv.setValue(sb.toString());
                }
                else
                {
                    String temp = ((LeafNode)((BranchNode)ASTNow.getChildren().get(5)).getChildren().get(0)).getToken().getValue();
                    rv.setValue(temp.substring(1, temp.length() - 1));
                }
                symbol.setRv(rv);
                STNow.symbols.add(symbol);
                AllocateInst inst = new AllocateInst(idCounter - 1,"[" + length + " x i8]");
                instructions.add(inst);
                String fakeName = "%" + (idCounter - 1);
                ConstCharArraySymbol symbol1 = new ConstCharArraySymbol(idCounter++,1,name+"wingman");
                STNow.symbols.add(symbol1);
                instructions.add(new GepInst(idCounter - 1,fakeName,false,length,true));
            }
            //算初始值
            if (ASTNow.getChildren().size() > 4)
            {
                ASTNow = (BranchNode) ASTNow.getChildren().get(5);
                analyseInitVal(type,idCounter - 1,length);
                ASTNow = ASTNow.getParent();
            }
            //存初始值在上面做了
        }
        else
        {
            //说明是单个整数、字符
            ExpCalculator ec = new ExpCalculator(STNow);
            if (type.equals("INTTK"))
            {
                ConstIntSymbol symbol = new ConstIntSymbol(idCounter++,1,name);
                AInt rv = new AInt();
                rv.setInitValue(ec.calculateConstExp((BranchNode) ((BranchNode) ASTNow.getChildren().get(2)).getChildren().get(0)));
                symbol.setRv(rv);
                STNow.symbols.add(symbol);
                AllocateInst inst = new AllocateInst(idCounter - 1,"i32");
                instructions.add(inst);
            }
            else
            {
                ConstCharSymbol symbol = new ConstCharSymbol(idCounter++,1,name);
                AChar rv = new AChar();
                rv.setInitValue(ec.calculateConstExp((BranchNode) ((BranchNode) ASTNow.getChildren().get(2)).getChildren().get(0)));
                symbol.setRv(rv);
                STNow.symbols.add(symbol);
                AllocateInst inst = new AllocateInst(idCounter - 1,"i8");
                instructions.add(inst);
            }
            if (ASTNow.getChildren().size() > 2)
            {
                int a = 0;
                ASTNow = (BranchNode) ASTNow.getChildren().get(2);
                analyseInitVal(type,idCounter - 1,0);
                ASTNow = ASTNow.getParent();
            }
        }
    }


    private void analyseStmt(int STrueEnd,int SFalseEnd)
    {
        /*Stmt → LVal '=' Exp ';' #
        | [Exp] ';' #
        | Block
        | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
        | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
        | 'break' ';' | 'continue' ';'
        | 'return' [Exp] ';' #
        | LVal '=' 'getint''('')'';' #
        | LVal '=' 'getchar''('')'';' #
        | 'printf''('StringConst {','Exp}')'';' # */
        if (ASTNow.getChildren().get(0).getType().equals("<Exp>"))
        {
            ASTNow = (BranchNode) ASTNow.getChildren().get(0);
            analyseExp();
            ASTNow = ASTNow.getParent();
        }
        else if (ASTNow.getChildren().get(0).getType().equals("RETURNTK"))
        {
            if (ASTNow.getChildren().get(1).getType().equals("SEMICN"))
            {
                RetInst inst = new RetInst(idCounter);
                instructions.add(inst);
            }
            else
            {
                ASTNow = (BranchNode) ASTNow.getChildren().get(1);
                analyseExp();
                ASTNow = ASTNow.getParent();
                //需要考虑用不用类型转换
                //只有需要char才要转
                if (!nowReturnType.equals("INTTK"))
                {
                    TruncInst ti = new TruncInst(idCounter++,true,idCounter - 2);
                    instructions.add(ti);
                }
                RetInst inst = new RetInst(idCounter,nowReturnType.equals("INTTK"),false,idCounter - 1);
                instructions.add(inst);
            }
        }
        else if(ASTNow.getChildren().get(0).getType().equals("<LVal>"))
        {
            //可能是赋值或者读写
            if (ASTNow.getChildren().get(2).getType().equals("<Exp>"))
            {
                //赋值
                int i = 0;
                ASTNow = (BranchNode) ASTNow.getChildren().get(2);
                analyseExp();
                ASTNow = ASTNow.getParent();
                int temp = idCounter - 1;
                //上面是存储的值
                String address;
                if (((BranchNode)ASTNow.getChildren().get(0)).getChildren().size() == 1)
                {
                    //非数组
                    address = findSymbol(((LeafNode)((BranchNode)ASTNow.getChildren().get(0)).getChildren().get(0)).getToken().getValue(),STNow);
                    boolean isInt = isIntSymbol(((LeafNode)((BranchNode)ASTNow.getChildren().get(0)).getChildren().get(0)).getToken().getValue(),STNow);
                    if (address.charAt(0) == '%')
                    {
                        if (!isInt)
                        {
                            TruncInst ti = new TruncInst(idCounter++,true,temp);
                            instructions.add(ti);
                        }
                        StoreInst inst = new StoreInst(-1,instructions.get(instructions.size() - 1).getId(),Integer.parseInt(address.substring(1)),isInt,false);
                        instructions.add(inst);
                    }
                    else
                    {
                        if (!isInt)
                        {
                            TruncInst ti = new TruncInst(idCounter++,true,temp);
                            instructions.add(ti);
                        }
                        StoreInst inst = new StoreInst(-1,instructions.get(instructions.size() - 1).getId(),address,isInt,false);
                        instructions.add(inst);
                    }
                }
                else
                {
                    //需要先找到数组的第几位
                    ASTNow = (BranchNode) ((BranchNode) ASTNow.getChildren().get(0)).getChildren().get(2);
                    analyseExp();
                    ASTNow = ASTNow.getParent();
                    ASTNow = ASTNow.getParent();
                    int pos = idCounter - 1;
                    //这是第几位，下一步是找名字
                    String realName = ((LeafNode)((BranchNode)ASTNow.getChildren().get(0)).getChildren().get(0)).getToken().getValue();
                    //这是原始名称
                    //接下来要判断是不是全局、函数、局部数组
                    String add;
                    boolean isInt = isIntSymbol(realName,STNow);
                    if (findSymbol(realName,STNow).charAt(0) == '@')
                    {
                        int length = module.getGlobalVariablesLength(realName);
                        GepInst inst1 = new GepInst(idCounter++,length,"@" + realName,pos,isInt);
                        instructions.add(inst1);
                        add = "%" + (idCounter - 1);
                    }
                    else if(symbolFromArg(realName,STNow))
                    {
                        String addTemp = findSymbol(realName,STNow);
                        GepStarInst insts = new GepStarInst(idCounter++,isInt,addTemp);
                        instructions.add(insts);
                        GepInst inst = new GepInst(idCounter++,"%" + (idCounter - 2),pos,isInt);
                        instructions.add(inst);
                        add = "%" + (idCounter - 1);
                    }
                    else
                    {
                        String name = findSymbol(realName + "wingman",STNow);
                        GepInst inst = new GepInst(idCounter++,name,pos,isInt);
                        instructions.add(inst);
                        add = "%" + (idCounter - 1);
                    }
                    //现在找到地址了
                    if (!isInt)
                    {
                        TruncInst ti = new TruncInst(idCounter++,true,temp);
                        instructions.add(ti);
                        temp = idCounter - 1;
                    }
                    StoreInst si = new StoreInst(-1,temp,add,isInt,false);
                    instructions.add(si);
                }
            }
            else
            {
                int temp = 0;
                if (ASTNow.getChildren().get(2).getType().equals("GETINTTK"))
                {
                    CallFuncInst inst = new CallFuncInst(idCounter++,"@getint()","INTTK");
                    instructions.add(inst);
                    temp = idCounter - 1;
                }
                else if (ASTNow.getChildren().get(2).getType().equals("GETCHARTK"))
                {
                    CallFuncInst inst = new CallFuncInst(idCounter++,"@getchar()","INTTK");
                    instructions.add(inst);
                    temp = idCounter - 1;
                }
                String address;
                if (((BranchNode)ASTNow.getChildren().get(0)).getChildren().size() == 1)
                {
                    //非数组
                    address = findSymbol(((LeafNode)((BranchNode)ASTNow.getChildren().get(0)).getChildren().get(0)).getToken().getValue(),STNow);
                    boolean isInt = isIntSymbol(((LeafNode)((BranchNode)ASTNow.getChildren().get(0)).getChildren().get(0)).getToken().getValue(),STNow);
                    if (address.charAt(0) == '%')
                    {
                        if (!isInt)
                        {
                            TruncInst ti = new TruncInst(idCounter++,true,temp);
                            instructions.add(ti);
                        }
                        StoreInst inst = new StoreInst(-1,instructions.get(instructions.size() - 1).getId(),Integer.parseInt(address.substring(1)),isInt,false);
                        instructions.add(inst);
                    }
                    else
                    {
                        if (!isInt)
                        {
                            TruncInst ti = new TruncInst(idCounter++,true,temp);
                            instructions.add(ti);
                            temp = idCounter - 1;
                        }
                        StoreInst inst = new StoreInst(-1,temp,address,isInt,false);
                        instructions.add(inst);
                    }
                }
                else
                {
                    //需要先找到数组的第几位
                    ASTNow = (BranchNode) ((BranchNode) ASTNow.getChildren().get(0)).getChildren().get(2);
                    analyseExp();
                    ASTNow = ASTNow.getParent();
                    ASTNow = ASTNow.getParent();
                    int pos = idCounter - 1;
                    //这是第几位，下一步是找名字
                    String realName = ((LeafNode)((BranchNode)ASTNow.getChildren().get(0)).getChildren().get(0)).getToken().getValue();
                    //这是原始名称
                    //接下来要判断是不是全局、函数、局部数组
                    String add;
                    boolean isInt = isIntSymbol(realName,STNow);
                    if (findSymbol(realName,STNow).charAt(0) == '@')
                    {
                        int length = module.getGlobalVariablesLength(realName);
                        GepInst inst1 = new GepInst(idCounter++,length,"@" + realName,pos,isInt);
                        instructions.add(inst1);
                        add = "%" + (idCounter - 1);
                    }
                    else if(symbolFromArg(realName,STNow))
                    {
                        String addTemp = findSymbol(realName,STNow);
                        GepStarInst insts = new GepStarInst(idCounter++,isInt,addTemp);
                        instructions.add(insts);
                        GepInst inst = new GepInst(idCounter++,"%" + (idCounter - 2),pos,isInt);
                        instructions.add(inst);
                        add = "%" + (idCounter - 1);
                    }
                    else
                    {
                        String name = findSymbol(realName + "wingman",STNow);
                        GepInst inst = new GepInst(idCounter++,name,pos,isInt);
                        instructions.add(inst);
                        add = "%" + (idCounter - 1);
                    }
                    //现在找到地址了
                    if (!isInt)
                    {
                        TruncInst ti = new TruncInst(idCounter++,true,temp);
                        instructions.add(ti);
                        temp = idCounter - 1;
                    }
                    StoreInst si = new StoreInst(-1,temp,add,isInt,false);
                    instructions.add(si);
                }
            }
        }
        else if (ASTNow.getChildren().get(0).getType().equals("PRINTFTK"))
        {
            //首先需要分解字符串
            ArrayList<String> parts = new ArrayList<>(splitString(((LeafNode)ASTNow.getChildren().get(2)).getToken().getValue()));
            int i = 0;
            for (String part : parts)
            {
                //'printf''('StringConst {','Exp}')'';'
                //对应关系2i+4
                if (part.equals("%d"))
                {
                    ASTNow = (BranchNode) ASTNow.getChildren().get(2*i + 4);
                    analyseExp();
                    ASTNow = ASTNow.getParent();
                    int temp = idCounter - 1;
                    //上面是值
                    CallFuncInst inst = new CallFuncInst("@putint(i32 %"+ temp + ")","VOIDTK");
                    instructions.add(inst);
                    i++;
                }
                else if(part.equals("%c"))
                {
                    ASTNow = (BranchNode) ASTNow.getChildren().get(2*i + 4);
                    analyseExp();
                    ASTNow = ASTNow.getParent();
                    int temp = idCounter - 1;
                    //上面是值
                    CallFuncInst inst = new CallFuncInst("@putch(i32 %"+ temp + ")","VOIDTK");
                    instructions.add(inst);
                    i++;
                }
                else
                {
                    //常量表达式
                    int length = part.length() + 1;
                    ConstString cs = new ConstString(false,".str." + ConstStr,part,length);
                    module.addConstString(cs);
                    CallFuncInst inst = new CallFuncInst("@putstr(i8* getelementptr inbounds (["+ length + " x i8], ["
                            + length + " x i8]* " + cs.getName() + ", i64 0, i64 0))","VOIDTK");
                    instructions.add(inst);
                    ConstStr++;
                }
            }
        }
        else if(ASTNow.getChildren().get(0).getType().equals("<Block>"))
        {

            SymbolTable st = new SymbolTable(STNow,1);
            STNow.children.add(st);
            STNow = st;
            for (Node node : ((BranchNode)ASTNow.getChildren().get(0)).getChildren()) {
                if (node.getType().equals("<BlockItem>"))
                {
                    ASTNow = (BranchNode) node;
                    analyseBlockItem(STrueEnd,SFalseEnd);
                    ASTNow = ASTNow.getParent();
                    ASTNow = ASTNow.getParent();
                }
            }
            STNow = STNow.fatherTable;
        }
        else if (ASTNow.getChildren().get(0).getType().equals("IFTK"))
        {
            BasicBlock Condition = new BasicBlock(basicBlockIdCounter++);
            //nowFunction.addBasicBlock(Condition);
            BasicBlock trueEnd = new BasicBlock(basicBlockIdCounter++);

            //其实是if之外
            BasicBlock elseEnd = null;

            if (ASTNow.getChildren().size() > 5)
            {
                elseEnd = new BasicBlock(basicBlockIdCounter++);
            }

            BasicBlock falseEnd = new BasicBlock(basicBlockIdCounter++);
            if (elseEnd == null)
            {
                elseEnd = falseEnd;
            }


            //有可能需要出口编号
            //先把之前的指令全存了
            //跳转到cond块的指令
            instructions.add(new BrInst("block"+Condition.getId()));
            nowBasicBlock.addInstruction(instructions);
            //清空
            instructions.clear();
            //new
            nowFunction.addBasicBlock(nowBasicBlock);


            //先解析cond块
            nowBasicBlock = Condition;
            //剩下的指令应该装进cond
            ASTNow = (BranchNode) ASTNow.getChildren().get(2);
            //这里传两个，因为如果一样，就说明没else
            analyseCond(trueEnd.getId(),elseEnd.getId());
            ASTNow = ASTNow.getParent();
            nowBasicBlock.addInstruction(instructions);
            instructions.clear();
            //new
            nowFunction.addBasicBlock(nowBasicBlock);
            //清空指令槽

            nowBasicBlock = trueEnd;
            //分析真语句块
            ASTNow = (BranchNode) ASTNow.getChildren().get(4);
            analyseStmt(STrueEnd,SFalseEnd);
            ASTNow = ASTNow.getParent();
            //跳转到外围
            instructions.add(new BrInst("block" + falseEnd.getId()));
            nowBasicBlock.addInstruction(instructions);
            instructions.clear();
            nowFunction.addBasicBlock(nowBasicBlock);

            //清空指令槽

            if (ASTNow.getChildren().size() > 5)
            {
                //分析else语句块
                nowBasicBlock = elseEnd;
                ASTNow = (BranchNode) ASTNow.getChildren().get(6);
                analyseStmt(STrueEnd,SFalseEnd);
                ASTNow = ASTNow.getParent();
                instructions.add(new BrInst("block" + falseEnd.getId()));
                nowBasicBlock.addInstruction(instructions);
                instructions.clear();
                nowFunction.addBasicBlock(nowBasicBlock);
            }

            //退出语句
            nowBasicBlock = falseEnd;
            //nowFunction.addBasicBlock(falseEnd);
            //这里不需要清空，后续语句就存在里面了
        }
        else if(ASTNow.getChildren().get(0).getType().equals("FORTK"))
        {

            BasicBlock forCond;
            BasicBlock forEnd;
            BasicBlock forBody = new BasicBlock(basicBlockIdCounter++);
            BasicBlock outOfFor = new BasicBlock(basicBlockIdCounter++);
            int i = 0;
            for (i = 0;i < 3; i++)
            {
                if (ASTNow.getChildren().get(i).getType().equals("<ForStmt>"))
                {
                    ASTNow = (BranchNode) ASTNow.getChildren().get(i);
                    analyseForStmt();
                    ASTNow = ASTNow.getParent();
                }
            }
            for (i = 0;; i++)
            {
                if (ASTNow.getChildren().get(i).getType().equals("SEMICN"))
                {
                    break;
                }
            }
            //现在i指向第一个分号
            forCond = new BasicBlock(basicBlockIdCounter++);
            //nowFunction.addBasicBlock(forCond);
            instructions.add(new BrInst("block"+forCond.getId()));
            nowBasicBlock.addInstruction(instructions);
            instructions.clear();
            //new
            nowFunction.addBasicBlock(nowBasicBlock);

            nowBasicBlock = forCond;
            if (ASTNow.getChildren().get(i + 1).getType().equals("SEMICN"))
            {
                //说明没有cond
                instructions.add(new BrInst("block"+forBody.getId()));
                //没有cond相当于永远为真
                i = i + 1;
            }
            else
            {
                ASTNow = (BranchNode) ASTNow.getChildren().get(i + 1);
                analyseCond(forBody.getId(),outOfFor.getId());
                ASTNow = ASTNow.getParent();
                i = i + 2;
            }

            //现在i在第二个分号
            forEnd = new BasicBlock(basicBlockIdCounter++);

            nowBasicBlock.addInstruction(instructions);
            instructions.clear();
            //new
            nowFunction.addBasicBlock(nowBasicBlock);
            nowBasicBlock = forEnd;

            if (ASTNow.getChildren().get(i + 1).getType().equals("<ForStmt>"))
            {
                //有结束语句
                ASTNow = (BranchNode) ASTNow.getChildren().get(i + 1);
                analyseForStmt();
                instructions.add(new BrInst("block"+forCond.getId()));
                ASTNow = ASTNow.getParent();
                i = i + 2;
            }
            else
            {
                instructions.add(new BrInst("block"+forCond.getId()));
                i = i + 1;
            }
            //
            nowBasicBlock.addInstruction(instructions);
            instructions.clear();
            nowFunction.addBasicBlock(nowBasicBlock);


            nowBasicBlock = forBody;
            //nowFunction.addBasicBlock(forBody);
            ASTNow = (BranchNode) ASTNow.getChildren().get(i + 1);
            analyseStmt(forEnd.getId(),outOfFor.getId());
            ASTNow = ASTNow.getParent();
            instructions.add(new BrInst("block"+forEnd.getId()));


            nowBasicBlock.addInstruction(instructions);
            instructions.clear();
            nowFunction.addBasicBlock(nowBasicBlock);
            nowBasicBlock = outOfFor;
            //nowFunction.addBasicBlock(outOfFor);
            //剩下的都在outOfFor了
        }
        else if (ASTNow.getChildren().get(0).getType().equals("BREAKTK"))
        {
            instructions.add(new BrInst("block" + SFalseEnd));
        }
        else if(ASTNow.getChildren().get(0).getType().equals("CONTINUETK"))
        {
            instructions.add(new BrInst("block" + STrueEnd));
        }
        //其他部分没有写
    }

    private boolean symbolFromArg(String name,SymbolTable low) {
        for (Symbol symbol: low.symbols)
        {
            if (symbol.getName().equals(name))
            {
                if (low.fatherTable == null ||low.fatherTable.fatherTable == null)
                {
                    for (Argument argument:nowFunction.getArguments())
                    {
                        if (argument.getName().equals(name))
                        {
                            return true;
                        }
                    }
                    return false;
                }
                else
                {
                    return false;
                }
            }
        }
        if (low.fatherTable != null)
        {
            return symbolFromArg(name,low.fatherTable);
        }
        else
        {
            return false;
        }
    }


    private boolean isIntSymbol(String name,SymbolTable low) {
        for (Symbol symbol: low.symbols)
        {
            if (symbol.getName().equals(name))
            {
                return symbol.ISInt();
            }
        }
        if (low.fatherTable != null)
        {
            return isIntSymbol(name,low.fatherTable);
        }
        else
        {
            return true;
        }
    }

    private boolean isHideArray(String name,SymbolTable low) {
        for (Symbol symbol: low.symbols)
        {
            if (symbol.getName().equals(name))
            {
                return symbol.isArray;
            }
        }
        if (low.fatherTable != null)
        {
            return isHideArray(name,low.fatherTable);
        }
        else
        {
            return true;
        }}

    private String findSymbol(String name,SymbolTable low) {
        for (Symbol symbol: low.symbols)
        {
            if (symbol.getName().equals(name))
            {
                if (low.fatherTable != null)
                {
                    return "%" + symbol.getId();
                }
                else
                {
                    return "@" + symbol.getName();
                }
            }
        }
        if (low.fatherTable != null)
        {
            return findSymbol(name,low.fatherTable);
        }
        else
        {
            return null;
        }
    }

    private void analyseExp()
    {
        ASTNow = (BranchNode) ASTNow.getChildren().get(0);
        analyseAddExp();
        ASTNow = ASTNow.getParent();
    }

    private void analyseAddExp()
    {
        //AddExp → MulExp | AddExp ('+' | '−') MulExp
        if (ASTNow.getChildren().size() > 1)
        {
            ASTNow = (BranchNode) ASTNow.getChildren().get(2);
            analyseMulExp();
            ASTNow = ASTNow.getParent();
            int temp1 = idCounter - 1;
            //后面的值
            ASTNow = (BranchNode) ASTNow.getChildren().get(0);
            analyseAddExp();
            ASTNow = ASTNow.getParent();
            int temp2 = idCounter - 1;
            //前面的值
            if (ASTNow.getChildren().get(1).getType().equals("PLUS"))
            {
                AddInst ai = new AddInst(idCounter++,temp1,temp2);
                instructions.add(ai);
            }
            else
            {
                SubInst si = new SubInst(idCounter++,temp1,temp2);
                instructions.add(si);
            }
        }
        else
        {
            ASTNow = (BranchNode) ASTNow.getChildren().get(0);
            analyseMulExp();
            ASTNow = ASTNow.getParent();
        }
    }

    private void analyseMulExp()
    {
        //MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        if (ASTNow.getChildren().size() > 1)
        {
            ASTNow = (BranchNode) ASTNow.getChildren().get(2);
            analyseUnaryExp();
            ASTNow = ASTNow.getParent();
            int temp1 = idCounter - 1;
            //后面的值
            ASTNow = (BranchNode) ASTNow.getChildren().get(0);
            analyseMulExp();
            ASTNow = ASTNow.getParent();
            int temp2 = idCounter - 1;
            //前面的值
            if (ASTNow.getChildren().get(1).getType().equals("MULT"))
            {
                MulInst ai = new MulInst(idCounter++,temp1,temp2);
                instructions.add(ai);
            }
            else if (ASTNow.getChildren().get(1).getType().equals("MOD"))
            {
                SremInst si = new SremInst(idCounter++,temp1,temp2);
                instructions.add(si);
            }
            else
            {
                DivInst di = new DivInst(idCounter++,temp1,temp2);
                instructions.add(di);
            }
        }
        else
        {
            ASTNow = (BranchNode) ASTNow.getChildren().get(0);
            analyseUnaryExp();
            ASTNow = ASTNow.getParent();
        }
    }

    private void analyseUnaryExp()
    {
        //UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        if (ASTNow.getChildren().get(0).getType().equals("<PrimaryExp>"))
        {
            ASTNow = (BranchNode) ASTNow.getChildren().get(0);
            analysePrimaryExp();
            ASTNow = ASTNow.getParent();
        }
        else if (ASTNow.getChildren().get(0).getType().equals("<UnaryOp>"))
        {
            //先不考虑bool
            ASTNow = (BranchNode) ASTNow.getChildren().get(1);
            analyseUnaryExp();
            ASTNow = ASTNow.getParent();
            if (((BranchNode)ASTNow.getChildren().get(0)).getChildren().get(0).getType().equals("PLUS"))
            {

            }
            else if (((BranchNode)ASTNow.getChildren().get(0)).getChildren().get(0).getType().equals("MINU"))
            {
                ReverseInst ri = new ReverseInst(idCounter++,idCounter - 2);
                instructions.add(ri);
            }
            else
            {
                //非逻辑
                instructions.add(new NotInst(idCounter++,idCounter - 2));
                instructions.add(new TruncInst(idCounter++,idCounter - 2));
            }
        }
        else
        {
            //函数调用
            //考虑传参类型
            String name = ((LeafNode)ASTNow.getChildren().get(0)).getToken().getValue();
            if (ASTNow.getChildren().size() < 4)
            {
                //没参数
                if (module.getFunctionType(name).equals("VOIDTK"))
                {
                    CallFuncInst cf = new CallFuncInst(-1,"@" + name + "()",module.getFunctionType(name));
                    instructions.add(cf);
                }
                else
                {
                    CallFuncInst cf = new CallFuncInst(idCounter++,"@" + name + "()",module.getFunctionType(name));
                    instructions.add(cf);
                }

            }
            else
            {
                //想办法把实参表搞出来
                StringBuilder sb = new StringBuilder();
                sb.append("@");
                sb.append(name);
                sb.append("(");
                ASTNow = (BranchNode) ASTNow.getChildren().get(2);
                analyseFuncRParams(sb,module.getFunction(name));
                ASTNow = ASTNow.getParent();
                sb.append(")");
                if (module.getFunctionType(name).equals("VOIDTK"))
                {
                    CallFuncInst cf = new CallFuncInst(-1,sb.toString(),module.getFunctionType(name));
                    instructions.add(cf);
                }
                else
                {
                    CallFuncInst cf = new CallFuncInst(idCounter++,sb.toString(),module.getFunctionType(name));
                    instructions.add(cf);
                }
            }
            //传出来是char要转化
            if (module.getFunctionType(name).equals("CHARTK"))
            {
                TruncInst ti = new TruncInst(idCounter++,false,idCounter - 2);
                instructions.add(ti);
            }
        }
    }


    private void analyseFuncRParams(StringBuilder sb, Function ff)
    {
        ArrayList<Argument> args = (ArrayList<Argument>) ff.getArguments();
        //FuncRParams → Exp { ',' Exp }
        BranchNode tmp = ASTNow;
        int i = 0;
        for (Node n : tmp.getChildren())
        {
            if (n.getType().equals("<Exp>"))
            {
                ASTNow = (BranchNode) n;
                analyseExp();
                ASTNow = ASTNow.getParent();
                int temp1 = idCounter - 1;
                if (i != 0)
                {
                    sb.append(",");
                }
                if (args.get(i).getType().equals("CHARTK"))
                {
                    if(args.get(i).isArray())
                    {
                        sb.append("i8* %");
                        sb.append(temp1);
                    }
                    else
                    {
                        TruncInst ti = new TruncInst(idCounter++,true,idCounter - 2);
                        instructions.add(ti);
                        sb.append("i8 %");
                        sb.append(idCounter - 1);
                    }
                }
                else
                {
                    if(args.get(i).isArray())
                    {
                        sb.append("i32* %");
                        sb.append(temp1);
                    }
                    else
                    {

                        sb.append("i32 %");
                        sb.append(temp1);
                    }
                }
                i++;
            }
        }
    }


    private void analysePrimaryExp()
    {
        //PrimaryExp → '(' Exp ')' | LVal | Number | Character
        if (ASTNow.getChildren().get(0).getType().equals("LPARENT"))
        {
            ASTNow = (BranchNode) ASTNow.getChildren().get(1);
            analyseExp();
            ASTNow = ASTNow.getParent();
        }
        else if (ASTNow.getChildren().get(0).getType().equals("<LVal>"))
        {
            ASTNow = (BranchNode) ASTNow.getChildren().get(0);
            analyseLVal();
            ASTNow = ASTNow.getParent();
        }
        else if (ASTNow.getChildren().get(0).getType().equals("<Number>"))
        {
            ImmINInst ii = new ImmINInst(idCounter++,Integer.parseInt(((LeafNode)((BranchNode)ASTNow.getChildren().get(0)).getChildren().get(0)).getToken().getValue()));
            instructions.add(ii);
        }
        else
        {
            //char
            String temp = ((LeafNode)((BranchNode)ASTNow.getChildren().get(0)).getChildren().get(0)).getToken().getValue();
            char t  = temp.charAt(1);
            if (temp.length() == 4)
            {
                switch (temp.charAt(2)) {
                    case '0':
                        t = '\0';break;
                    case 'n':
                        t = '\n'; break;
                    case 't':
                        t = '\t';break;
                    case 'b':
                        t = '\b';break;
                    case 'r':
                        t = '\r';break;
                    case 'f':
                        t = '\f';break;
                    case '\\':
                        t = '\\';break;
                    case '\'':
                        t = '\'';break;
                    case '\"':
                        t = '\"';break;
                }
            }
            ImmINInst ii = new ImmINInst(idCounter++,t);
            instructions.add(ii);
        }
    }


    private void analyseLVal()
    {
        if (ASTNow.getChildren().size() > 1)
        {
            //是数组,计算地址
            //先找位置
            // LVal → Ident ['[' Exp ']']
            ASTNow = (BranchNode) ASTNow.getChildren().get(2);
            analyseExp();
            ASTNow = ASTNow.getParent();
            int position = idCounter - 1;
            //计算地址
            String realName = ((LeafNode)ASTNow.getChildren().get(0)).getToken().getValue();
            boolean isInt = isIntSymbol(realName,STNow);
            //接下来要判断是不是全局、函数、局部数组
            int add;
            if (findSymbol(realName,STNow).charAt(0) == '@')
            {
                int length = module.getGlobalVariablesLength(realName);
                GepInst inst1 = new GepInst(idCounter++,length,"@" + realName,position,isInt);
                instructions.add(inst1);
                add = (idCounter - 1);
            }
            else if(symbolFromArg(realName,STNow))
            {
                String addTemp = findSymbol(realName,STNow);
                GepStarInst insts = new GepStarInst(idCounter++,isInt,addTemp);
                instructions.add(insts);
                GepInst inst = new GepInst(idCounter++,"%" + (idCounter - 2),position,isInt);
                instructions.add(inst);
                add = (idCounter - 1);
            }
            else
            {
                String name = findSymbol(realName + "wingman",STNow);
                GepInst inst = new GepInst(idCounter++,name,position,isInt);
                instructions.add(inst);
                add = (idCounter - 1);
            }

            if (isInt)
            {
                LoadInst li = new LoadInst(idCounter++,add,"INTTK");
                instructions.add(li);
            }
            else
            {
                LoadInst li = new LoadInst(idCounter++,add,"CHARTK");
                instructions.add(li);
                TruncInst ti = new TruncInst(idCounter++,false,idCounter -2);
                instructions.add(ti);
            }
        }
        else
        {

            String realName = ((LeafNode)ASTNow.getChildren().get(0)).getToken().getValue();
            if (isHideArray(realName,STNow))
            {
                //处理传数组名的情况
                if (findSymbol(realName,STNow).charAt(0) == '@')
                {
                    int length = module.getGlobalVariablesLength(realName);
                    GepInst inst = new  GepInst(idCounter++,"@" + realName,isIntSymbol(realName,STNow),length,true);
                    instructions.add(inst);
                }
                else if(symbolFromArg(realName,STNow))
                {
                    String addTemp = findSymbol(realName,STNow);
                    instructions.add(new  GepStarInst(idCounter++,isIntSymbol(realName,STNow),addTemp));
                }
                else
                {
                    String name = findSymbol(realName + "wingman",STNow);
                    instructions.add(new GepInst(0,idCounter++,name,0,isIntSymbol(realName,STNow),true));
                }
            }
            else
            {
                boolean isInt = isIntSymbol(realName,STNow);
                String name = findSymbol(realName,STNow);
                boolean isFuncParams = isFuncParams(realName,STNow);
                if (isFuncParams)
                {
                    ImmINInst ii = new ImmINInst(idCounter++,Integer.parseInt(name.substring(1)),0);
                    instructions.add(ii);
                }
                else if (name.charAt(0) == '@')
                {
                    if (isInt)
                    {
                        LoadFromGlobalInst li = new LoadFromGlobalInst(idCounter++,name,"INTTK");
                        instructions.add(li);
                    }
                    else
                    {
                        LoadFromGlobalInst li = new LoadFromGlobalInst(idCounter++,name,"CHARTK");
                        instructions.add(li);
                        TruncInst ti = new TruncInst(idCounter++,false,idCounter -2);
                        instructions.add(ti);

                    }
                }
                else
                {
                    if (isInt)
                    {
                        LoadInst li = new LoadInst(idCounter++,Integer.parseInt(name.substring(1)),"INTTK");
                        instructions.add(li);
                    }
                    else
                    {
                        LoadInst li = new LoadInst(idCounter++,Integer.parseInt(name.substring(1)),"CHARTK");
                        instructions.add(li);
                        TruncInst ti = new TruncInst(idCounter++,false,idCounter -2);
                        instructions.add(ti);
                    }
                }
            }

        }
    }

    private boolean isFuncParams(String name,SymbolTable low) {
        for (Symbol symbol: low.symbols)
        {
            if (symbol.getName().equals(name))
            {
                return symbol.isFuncParam();
            }
        }
        if (low.fatherTable != null)
        {
            return isFuncParams(name,low.fatherTable);
        }
        else
        {
            return false;
        }

    }

    private void analyseForStmt()
    {
        //赋值
        int i = 0;
        ASTNow = (BranchNode) ASTNow.getChildren().get(2);
        analyseExp();
        ASTNow = ASTNow.getParent();
        int temp = idCounter - 1;
        //上面是存储的值
        String address;
        if (((BranchNode)ASTNow.getChildren().get(0)).getChildren().size() == 1)
        {
            //非数组
            address = findSymbol(((LeafNode)((BranchNode)ASTNow.getChildren().get(0)).getChildren().get(0)).getToken().getValue(),STNow);
            boolean isInt = isIntSymbol(((LeafNode)((BranchNode)ASTNow.getChildren().get(0)).getChildren().get(0)).getToken().getValue(),STNow);
            if (address.charAt(0) == '%')
            {
                if (!isInt)
                {
                    TruncInst ti = new TruncInst(idCounter++,true,temp);
                    instructions.add(ti);
                }
                StoreInst inst = new StoreInst(-1,instructions.get(instructions.size() - 1).getId(),Integer.parseInt(address.substring(1)),isInt,false);
                instructions.add(inst);
            }
            else
            {
                if (!isInt)
                {
                    TruncInst ti = new TruncInst(idCounter++,true,temp);
                    instructions.add(ti);
                }
                StoreInst inst = new StoreInst(-1,instructions.get(instructions.size() - 1).getId(),address,isInt,false);
                instructions.add(inst);
            }
        }
        else
        {
            //需要先找到数组的第几位
            ASTNow = (BranchNode) ((BranchNode) ASTNow.getChildren().get(0)).getChildren().get(2);
            analyseExp();
            ASTNow = ASTNow.getParent();
            ASTNow = ASTNow.getParent();
            int pos = idCounter - 1;
            //这是第几位，下一步是找名字
            String realName = ((LeafNode)((BranchNode)ASTNow.getChildren().get(0)).getChildren().get(0)).getToken().getValue();
            //这是原始名称
            //接下来要判断是不是全局、函数、局部数组
            String add;
            boolean isInt = isIntSymbol(realName,STNow);
            if (findSymbol(realName,STNow).charAt(0) == '@')
            {
                int length = module.getGlobalVariablesLength(realName);
                GepInst inst1 = new GepInst(idCounter++,length,"@" + realName,pos,isInt);
                instructions.add(inst1);
                add = "%" + (idCounter - 1);
            }
            else if(symbolFromArg(realName,STNow))
            {
                String addTemp = findSymbol(realName,STNow);
                GepStarInst insts = new GepStarInst(idCounter++,isInt,addTemp);
                instructions.add(insts);
                GepInst inst = new GepInst(idCounter++,"%" + (idCounter - 2),pos,isInt);
                instructions.add(inst);
                add = "%" + (idCounter - 1);
            }
            else
            {
                String name = findSymbol(realName + "wingman",STNow);
                GepInst inst = new GepInst(idCounter++,name,pos,isInt);
                instructions.add(inst);
                add = "%" + (idCounter - 1);
            }
            //现在找到地址了
            if (!isInt)
            {
                TruncInst ti = new TruncInst(idCounter++,true,temp);
                instructions.add(ti);
                temp = idCounter - 1;
            }
            StoreInst si = new StoreInst(-1,temp,add,isInt,false);
            instructions.add(si);
        }
    }
    private void analyseCond(int trueEnd,int elseEnd)
    {
        ASTNow = (BranchNode) ASTNow.getChildren().get(0);
        analyseLOrExp(trueEnd,elseEnd);
        ASTNow = ASTNow.getParent();
    }

    private void analyseLOrExp(int trueEnd,int elseEnd)
    {
        if (ASTNow.getChildren().size() > 1)
        {
            BasicBlock second = new BasicBlock(basicBlockIdCounter++);

            //上面是第二部分条件
            ASTNow = (BranchNode) ASTNow.getChildren().get(0);
            analyseLOrExp(trueEnd,second.getId());
            ASTNow = ASTNow.getParent();
            nowBasicBlock.addInstruction(instructions);
            instructions.clear();
            //new
            nowFunction.addBasicBlock(nowBasicBlock);

            nowBasicBlock = second;
            //分析第二部分
            ASTNow = (BranchNode) ASTNow.getChildren().get(2);
            analyseLAndExp(trueEnd,elseEnd);
            ASTNow = ASTNow.getParent();
            //nowFunction.addBasicBlock(second);

            //不需要封口，封口在外侧
        }
        else
        {
            ASTNow = (BranchNode) ASTNow.getChildren().get(0);
            analyseLAndExp(trueEnd,elseEnd);
            ASTNow = ASTNow.getParent();
        }
    }

    private void analyseLAndExp(int trueEnd,int elseEnd)
    {
        if (ASTNow.getChildren().size() > 1)
        {
            BasicBlock second = new BasicBlock(basicBlockIdCounter++);

            //上面是第二部分条件
            ASTNow = (BranchNode) ASTNow.getChildren().get(0);
            analyseLAndExp(second.getId(),elseEnd);
            ASTNow = ASTNow.getParent();
            nowBasicBlock.addInstruction(instructions);
            instructions.clear();
            //new
            nowFunction.addBasicBlock(nowBasicBlock);

            //nowFunction.addBasicBlock(second);
            nowBasicBlock = second;
            //分析第二部分
            ASTNow = (BranchNode) ASTNow.getChildren().get(2);
            analyseEqExp();
            instructions.add(new IsInst(idCounter++,idCounter -2));
            instructions.add(new BrInst("block" + trueEnd,idCounter - 1,"block" + elseEnd));
            ASTNow = ASTNow.getParent();
            //不需要封口，封口在外侧
        }
        else
        {
            ASTNow = (BranchNode) ASTNow.getChildren().get(0);
            analyseEqExp();
            instructions.add(new IsInst(idCounter++,idCounter -2));
            instructions.add(new BrInst("block" + trueEnd,idCounter - 1,"block" + elseEnd));
            ASTNow = ASTNow.getParent();
        }
    }

    private void analyseEqExp()
    {
        //这里出现真实计算了
        if (ASTNow.getChildren().size() > 1)
        {
            ASTNow = (BranchNode) ASTNow.getChildren().get(0);
            analyseEqExp();
            ASTNow = ASTNow.getParent();
            int left = instructions.get(instructions.size() - 1).getId();
            ASTNow = (BranchNode) ASTNow.getChildren().get(2);
            analyseRelExp();
            ASTNow = ASTNow.getParent();
            int right = instructions.get(instructions.size() - 1).getId();
            if (((LeafNode) ASTNow.getChildren().get(1)).getToken().getType().equals("EQL"))
            {
                instructions.add(new IcpmInst(idCounter++,left,right,"eq"));
                instructions.add(new TruncInst(idCounter++,idCounter - 2));
            }
            else
            {
                instructions.add(new IcpmInst(idCounter++,left,right,"ne"));
                instructions.add(new TruncInst(idCounter++,idCounter - 2));
            }
        }
        else
        {
            ASTNow = (BranchNode) ASTNow.getChildren().get(0);
            analyseRelExp();
            ASTNow = ASTNow.getParent();
        }
    }

    private void analyseRelExp()
    {
        if (ASTNow.getChildren().size() > 1)
        {
            ASTNow = (BranchNode) ASTNow.getChildren().get(0);
            analyseRelExp();
            ASTNow = ASTNow.getParent();
            int left = instructions.get(instructions.size() - 1).getId();
            ASTNow = (BranchNode) ASTNow.getChildren().get(2);
            analyseAddExp();
            ASTNow = ASTNow.getParent();
            int right = instructions.get(instructions.size() - 1).getId();
            if (((LeafNode) ASTNow.getChildren().get(1)).getToken().getType().equals("LSS"))
            {
                instructions.add(new IcpmInst(idCounter++,left,right,"slt"));
                instructions.add(new TruncInst(idCounter++,idCounter - 2));
            }
            else if (((LeafNode) ASTNow.getChildren().get(1)).getToken().getType().equals("LEQ"))
            {
                instructions.add(new IcpmInst(idCounter++,left,right,"sle"));
                instructions.add(new TruncInst(idCounter++,idCounter - 2));
            }
            else if (((LeafNode) ASTNow.getChildren().get(1)).getToken().getType().equals("GRE"))
            {
                instructions.add(new IcpmInst(idCounter++,left,right,"sgt"));
                instructions.add(new TruncInst(idCounter++,idCounter - 2));
            }
            else
            {
                instructions.add(new IcpmInst(idCounter++,left,right,"sge"));
                instructions.add(new TruncInst(idCounter++,idCounter - 2));
            }
        }
        else
        {
            ASTNow = (BranchNode) ASTNow.getChildren().get(0);
            analyseAddExp();
            ASTNow = ASTNow.getParent();
        }
    }

    public ArrayList<String> splitString(String input1) {
        // 创建一个ArrayList来保存结果
        String input = input1.substring(1, input1.length() - 1);
        ArrayList<String> parts = new ArrayList<>();

        // 使用正则表达式来匹配%d和%c，以及其他部分
        Pattern pattern = Pattern.compile("(%d|%c|[^%]+|%)");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String tmp= matcher.group();
            StringBuilder sb = new StringBuilder();
            for (int i = 0 ; i < tmp.length();i++)
            {
                if (tmp.charAt(i) == '\\' && tmp.charAt(i+1) == 'n')
                {
                    sb.append('\n');
                    i++;
                }
                else
                {
                    sb.append(tmp.charAt(i));
                }
            }
            parts.add(sb.toString());
        }

        return parts;
    }

    public void print(FileProcessor tokenFileProcessor) throws IOException {
        module.print(tokenFileProcessor);
    }
}
