package LLVM;

import Tree.BranchNode;
import Tree.LeafNode;
import Tree.Node;
import base.symbol.Symbol;
import base.symbol.SymbolTable;

public class ExpCalculator {

    SymbolTable nowTable;

    public ExpCalculator(SymbolTable symbolTable) {
        nowTable = symbolTable;
    }


    public int calculateConstExp(BranchNode ConstExp) {
        return calculateAddExp((BranchNode) ConstExp.getChildren().get(0));
    }

    private  int calculateAddExp(BranchNode AddExp) {
        int sum = 0;
        if (AddExp.getChildren().get(0).getType().equals("<AddExp>")) {
            sum += calculateAddExp((BranchNode) AddExp.getChildren().get(0));
            if (AddExp.getChildren().get(1).getType().equals("PLUS")) {
                sum += calculateMulExp((BranchNode) AddExp.getChildren().get(2));
            }
            else
            {
                sum -= calculateMulExp((BranchNode) AddExp.getChildren().get(2));
            }
            return sum;
        }
        else
        {
            return calculateMulExp((BranchNode) AddExp.getChildren().get(0));
        }
    }

    private int calculateMulExp(BranchNode MulExp) {
        int sum = 0;
        if (MulExp.getChildren().get(0).getType().equals("<MulExp>")) {
            sum = calculateMulExp((BranchNode) MulExp.getChildren().get(0));
            if (MulExp.getChildren().get(1).getType().equals("MULT")) {
                sum *= calculateUnaryExp((BranchNode) MulExp.getChildren().get(2));
            }
            else if (MulExp.getChildren().get(1).getType().equals("DIV"))
            {
                sum /= calculateUnaryExp((BranchNode) MulExp.getChildren().get(2));
            }
            else
            {
                sum %= calculateUnaryExp((BranchNode) MulExp.getChildren().get(2));
            }
            return sum;
        }
        else
        {
            return calculateUnaryExp((BranchNode) MulExp.getChildren().get(0));
        }
    }

    private int calculateUnaryExp(BranchNode UnaryExp) {
        if (UnaryExp.getChildren().get(0).getType().equals("<PrimaryExp>")) {
            return calculatePrimaryExp((BranchNode) UnaryExp.getChildren().get(0));
        }
        else if (UnaryExp.getChildren().get(0).getType().equals("<UnaryOp>"))
        {
            if (((BranchNode)UnaryExp.getChildren().get(0)).getChildren().get(0).getType().equals("MINU"))
            {
                return -1 * calculateUnaryExp((BranchNode)UnaryExp.getChildren().get(1));
            }
            else
            {
                return calculateUnaryExp((BranchNode)UnaryExp.getChildren().get(1));
            }
        }
        else
        {
            return 0;
        }
    }

    private int calculatePrimaryExp(BranchNode PrimaryExp) {
        if (PrimaryExp.getChildren().get(0).getType().equals("LPARENT")) {
            int a = 0;
            return calculateAddExp((BranchNode) ((BranchNode)PrimaryExp.getChildren().get(1)).getChildren().get(0));
        }
        else if (PrimaryExp.getChildren().get(0).getType().equals("<Number>")) {
            return Integer.parseInt(((LeafNode)((BranchNode)PrimaryExp.getChildren().get(0)).getChildren().get(0)).getToken().getValue());
        }
        else if (PrimaryExp.getChildren().get(0).getType().equals("<Character>")) {
            return (int) ((LeafNode)((BranchNode)PrimaryExp.getChildren().get(0)).getChildren().get(0)).getToken().getValue().charAt(1);
        }
        else
        {
            return calculateLVal((BranchNode)PrimaryExp.getChildren().get(0));
        }
    }

    private int calculateLVal(BranchNode LVal) {
        Symbol tmp = nowTable.legalVar(((LeafNode)LVal.getChildren().get(0)).getToken().getValue());
        if (tmp.ISArray())
        {
            int length = calculateConstExp((BranchNode) LVal.getChildren().get(2));
            return tmp.getinitValueByPosition(length);
        }
        else
        {
            return tmp.getInitValue();
        }
    }
}
