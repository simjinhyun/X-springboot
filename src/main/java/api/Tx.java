package api;

import kr.co.semperpi.*;

public class Tx {
    public static void call(Ctx c) {
        SPList spl = c.parse(SPList.class);
        c.response.data = spl.call();
    }
}
