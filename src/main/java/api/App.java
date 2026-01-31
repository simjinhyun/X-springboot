package api;

import kr.co.semperpi.*;

public class App {
    public static void about(Ctx c) {
        try {
            c.res.setContentType("text/html; charset=UTF-8");
            java.io.PrintWriter out = c.res.getWriter();
            out.println("이게 되냐?");
            c.replied = true;
        } catch (Exception e) {
            throw new X.Error(e.getClass().getSimpleName(), new Object[] { e.getMessage() });
        }
    }
}
