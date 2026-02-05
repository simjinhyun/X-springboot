package filter;

import kr.co.semperpi.*;

public class Main {
    public static void before(XContext c) {
        X.logger.debug("전처리기 실행");
    }

    public static void after(XContext c) {
        X.logger.debug("후처리기 실행");
    }
}
