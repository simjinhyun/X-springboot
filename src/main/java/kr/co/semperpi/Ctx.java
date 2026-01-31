package kr.co.semperpi;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.lang.reflect.Constructor;

public class Ctx {
    // 정형화된 JSON 응답
    public static class Response {
        public String code = "OK";
        public Object data;
        public String elapsed;
        public String reqid = Util.generateReqId();
    }

    // 트랜젝션 문맥에 포함 당한건지 판단 플래그
    private long startTime = System.currentTimeMillis();
    boolean withinTx = TransactionSynchronizationManager.isActualTransactionActive();
    public String path;
    public String parser;
    public String body;
    public boolean replied = false;
    public HttpServletRequest req;
    public HttpServletResponse res;
    public Response response = new Response();

    public Ctx(HttpServletRequest req, HttpServletResponse res) {
        this.req = req;
        this.res = res;
        this.path = req.getRequestURI();
        this.parser = req.getHeader("Parser");

        org.slf4j.MDC.put("reqid", this.response.reqid);
        if (this.withinTx) {
            X.logger.debug("트랜젝션시작");
        }
    }

    public <T> T parse(Class<T> clazz) {
        X.logger.debug("파서클래스: {} 요청헤더파서: {} ", clazz.getSimpleName(), this.parser);
        if (!clazz.getSimpleName().equals(this.parser)) {
            throw new X.Error("ParserNotFound", new Object[] { this.parser });
        }

        try {

            this.body = Util.readBody(this.req);
            Constructor<T> constructor = clazz.getConstructor(String.class);
            return constructor.newInstance(this.body);
        } catch (Throwable t) {
            throw new X.Error(t.getClass().getSimpleName(), new Object[] { this.body });
        }
    }

    public void done() {
        if (this.withinTx) {
            if ("OK".equals(response.code)) {
                X.logger.debug("트랜젝션커밋");
            } else {
                X.logger.debug("트랜젝션롤백");
            }
        }
        org.slf4j.MDC.clear();
        long elapsed = System.currentTimeMillis() - startTime;
        response.elapsed = Util.formatElapsed(elapsed);
    }
}