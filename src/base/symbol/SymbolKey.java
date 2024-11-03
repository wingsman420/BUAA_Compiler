package base.symbol;

public enum SymbolKey {
    ConstChar("ConstChar"),
    ConstInt("ConstInt"),
    ConstCharArray("ConstCharArray"),
    ConstIntArray("ConstIntArray"),
    Char("Char"),
    Int("Int"),
    CharArray("CharArray"),
    IntArray("IntArray"),
    VoidFunc("VoidFunc"),
    CharFunc("CharFunc"),
    IntFunc("IntFunc");

    private final String name;

    SymbolKey(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // 静态方法，用于检查给定的字符串是否是枚举中的一个值
    public static boolean containsName(String name) {
        for (SymbolKey symbolKey : SymbolKey.values()) {
            if (symbolKey.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}


