package filter;

import kr.co.semperpi.*;

public class SP {
    public static void before(XContext c) {
        X.logger.debug("SP 전처리기 실행");
    }

    public static void after(XContext c) {
        X.logger.debug("SP 후처리기 실행");
    }
}
