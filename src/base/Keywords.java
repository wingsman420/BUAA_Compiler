package base;

import java.util.HashMap;
import java.util.Map;

public class Keywords {
    // 关键字和符号的类别码定义
    public static final Map<String, String> keywords = new HashMap<>();

    static {
        // 关键字
        keywords.put("const", "CONSTTK");
        keywords.put("int", "INTTK");
        keywords.put("main", "MAINTK");
        keywords.put("if", "IFTK");
        keywords.put("else", "ELSETK");
        keywords.put("void", "VOIDTK");
        keywords.put("char", "CHARTK");
        keywords.put("for", "FORTK");
        keywords.put("printf", "PRINTFTK");
        keywords.put("return", "RETURNTK");
        keywords.put("getint", "GETINTTK");
        keywords.put("getchar", "GETCHARTK");
        keywords.put("break", "BREAKTK");
        keywords.put("continue", "CONTINUETK");
        keywords.put("+", "PLUS");
        keywords.put("-", "MINU");
        keywords.put("*", "MULT");
        keywords.put("/", "DIV");
        keywords.put("%","MOD");
        keywords.put("=", "ASSIGN");
        keywords.put("==", "EQL");
        keywords.put("!=", "NEQ");
        keywords.put("<", "LSS");
        keywords.put("<=", "LEQ");
        keywords.put(">", "GRE");
        keywords.put(">=", "GEQ");
        keywords.put("&&", "AND");
        keywords.put("||", "OR");
        keywords.put("!", "NOT");
        keywords.put(";", "SEMICN");
        keywords.put(",", "COMMA");
        keywords.put("(", "LPARENT");
        keywords.put(")", "RPARENT");
        keywords.put("[", "LBRACK");
        keywords.put("]", "RBRACK");
        keywords.put("{", "LBRACE");
        keywords.put("}", "RBRACE");
        // 添加其他符号...
    }

    // 根据输入的单词返回类别码
    public static String getTokenType(String word) {
        return keywords.getOrDefault(word, "IDENFR");  // 默认返回 IDENFR（标识符）
    }
}

