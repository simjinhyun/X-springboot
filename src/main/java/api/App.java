package api;

import kr.co.semperpi.*;

public class App {
    public static void about(Ctx c) {
        c.response.data = new X.About();
    }
}
