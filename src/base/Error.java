package base;

public class Error {
    private int lineNumber;
    private String errorCode;

    public Error(int lineNumber, String errorCode) {
        this.lineNumber = lineNumber;
        this.errorCode = errorCode;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "Error at line " + lineNumber + ": " + errorCode;
    }
}
