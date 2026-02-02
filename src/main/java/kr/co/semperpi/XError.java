package kr.co.semperpi;

public class XError extends RuntimeException {
    public final String code;
    public final Object[] data;
    public final String fileLine; // (파일:라인) 형태

    public XError(String code, Object[] data) {
        super(""); // message는 무시
        this.code = code;
        this.data = data;
        this.fileLine = getFileLine();
    }

    private static String getFileLine() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        if (stack.length > 3) {
            StackTraceElement s = stack[3];
            return "(" + s.getFileName() + ":" + s.getLineNumber() + ")";
        }
        return "(unknown)";
    }

    public String toString() {
        return "code=" + code +
                ", data=" + java.util.Arrays.toString(data) +
                " " + fileLine;
    }
}
