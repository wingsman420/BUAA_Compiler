package LLVM.classes.module;

public class ConstString extends GlobalValue{
    private String name;
    private String value;
    private int length;


    public ConstString( boolean isFunction, String name,String value, int length) {
        super(-1,isFunction, name);
        this.name = name;
        this.value = value;
        this.length = length;
    }

    public String getName() {
        return "@" + name;
    }

    public String escapeToLLVMString() {
        StringBuilder sb = new StringBuilder();
        sb.append("c\"");

        for (char c : value.toCharArray()) {
            if (c == '\n') {
                sb.append("\\0A");
            } else {
                if (c >= 32 && c <= 126) {
                    sb.append(c);
                } else {
                    // Non-printable characters are represented as \XX
                    sb.append(String.format("\\%02X", (int) c));
                }
            }
        }
        sb.append("\\00\"");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "@" + name + " = private unnamed_addr constant [" + length + " x i8] " + escapeToLLVMString() + ", align 1";
    }
}
