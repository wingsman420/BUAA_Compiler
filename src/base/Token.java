package base;

public class Token {
    private String type;  // 类别码
    private String value; // 单词的字符/字符串形式
    private int lineNumber; // 所在行号

    public Token(String type, String value, int lineNumber) {
        this.type = type;
        this.value = value;
        this.lineNumber = lineNumber;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {
        return type + " " + value;
    }
}
