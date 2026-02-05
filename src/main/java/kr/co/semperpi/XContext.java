package kr.co.semperpi;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

public class XContext extends java.util.HashMap<String, Object> {

    ////////////////////////////////////////////////////////////////////////////
    /// 프라이빗 멤버와 메서드
    ////////////////////////////////////////////////////////////////////////////
    private long startTime = System.currentTimeMillis();

    private void readBody() throws IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStream inputStream = req.getInputStream();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        body = sb.toString();
        X.logger.debug("요청바디:" + body);
    }

    ////////////////////////////////////////////////////////////////////////////
    /// 퍼블릭 멤버와 메서드
    ////////////////////////////////////////////////////////////////////////////
    public boolean withinTx;
    public String path;
    public String parser;
    public String body;
    public boolean replied = false;
    public HttpServletRequest req;
    public HttpServletResponse res;
    public XResponse response = new XResponse();

    public <T> T parse(Class<T> clazz) {
        X.logger.debug("파서클래스: {} 요청헤더파서: {} ", clazz.getSimpleName(), parser);
        if (!clazz.getSimpleName().equals(parser)) {
            throw new XError("ParserNotFound", new Object[] { parser });
        }

        try {
            readBody();
            Constructor<T> constructor = clazz.getConstructor(this.getClass(), String.class);
            return constructor.newInstance(this, body);
        } catch (Throwable t) {
            throw new XError(t.getClass().getSimpleName(), new Object[] { body });
        }
    }

    public void done() {
        if (withinTx) {
            if ("OK".equals(response.code)) {
                X.logger.debug("트랜젝션커밋");
            } else {
                X.logger.debug("트랜젝션롤백");
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        response.elapsed = XUtil.formatElapsed(elapsed);
        X.logger.info("응답: {} {} {}", path, response.code, response.elapsed);
        org.slf4j.MDC.clear();
    }

    // 요청을 각 핸들러로 분배
    public XResponse dispatch() {

        try {
            Method m = X.api.get(path);
            if (m != null) {
                X.before.invoke(null, this);
                m.invoke(null, this);
                X.after.invoke(null, this);
            } else {
                response.code = "ApiNotFound";
                response.data = path;
            }
        } catch (Throwable t) {
            // cause에 실제 발생한 에러 담기
            Throwable cause = (t instanceof InvocationTargetException) ? t.getCause() : t;
            if (cause instanceof XError xe) {
                response.code = xe.code;
                response.data = xe.data;
            } else {
                response.code = cause.getClass().getSimpleName();
                response.data = cause.getMessage();
                // 감지한 에러가 아니라면 스텍트레이스
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                cause.printStackTrace(pw);
                X.logger.error("런타임에러\n{}", sw.toString());
            }
        } finally {

            done();
        }
        return (res.isCommitted()) ? null : response;
    }

    ////////////////////////////////////////////////////////////////////////////
    /// 유일한 생성자
    ////////////////////////////////////////////////////////////////////////////
    public XContext(HttpServletRequest req, HttpServletResponse res) {
        this.req = req;
        this.res = res;
        this.path = req.getRequestURI();
        this.parser = req.getHeader("Parser");
        this.withinTx = "true".equalsIgnoreCase(req.getHeader("Tx"));

        org.slf4j.MDC.put("reqid", response.reqid);
        X.logger.debug("요청: {} @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@", path);
        if (withinTx) {
            X.logger.debug("트랜젝션시작");
        }
    }

}