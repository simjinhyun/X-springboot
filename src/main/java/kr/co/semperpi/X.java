package kr.co.semperpi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.*;

import javax.sql.DataSource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
@SpringBootApplication
@RestController
@RequestMapping("/api")
public class X implements ApplicationListener<ApplicationReadyEvent> {

    public static class About {
        public final String BUILD_DATE = "###BUILD_DATE###";
        public final String VERSION = "###VERSION###";
        public final String REVISION = "###REVISION###";
    }

    ////////////////////////////////////////////////////////////////////////////
    /// X.Error
    ////////////////////////////////////////////////////////////////////////////
    public static class Error extends RuntimeException {
        public final String code;
        public final Object[] data;
        public final String fileLine; // (파일:라인) 형태

        public Error(String code, Object[] data) {
            super(""); // message는 무시
            this.code = code;
            this.data = data;
            this.fileLine = getFileLine();
        }

        private static String getFileLine() {
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            if (stack.length > 3) {
                StackTraceElement caller = stack[3];
                return "(" + caller.getFileName() + ":" + caller.getLineNumber() + ")";
            }
            return "(unknown)";
        }

        public String toString() {
            return "code=" + code +
                    ", data=" + java.util.Arrays.toString(data) +
                    " " + fileLine;
        }
    }

    private static Map<String, Method> api;
    public static final ObjectMapper MAPPER = new ObjectMapper();
    public static final Logger logger = LoggerFactory.getLogger(X.class);

    public static DataSource dataSource;
    public static JdbcTemplate jdbcTemplate;

    public X(@NonNull DataSource ds, Environment env, JdbcTemplate jt) {
        try {
            dataSource = ds;
            jdbcTemplate = jt;
            api = Util.scanHandler();
            Util.initAES(env);
            logger.info("X initialized");
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    // 스프링 부트 앱 초기화
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {

    }

    public static void main(String[] args) {
        SpringApplication.run(X.class, args);
    }

    @RequestMapping("/**")
    public Ctx.Response handle(HttpServletRequest req, HttpServletResponse res) {
        return dispatch(req, res);
    }

    // 트랜제션이 필요한 요청 facade
    @Transactional
    @RequestMapping("/tx/**")
    public Ctx.Response handleTX(HttpServletRequest req, HttpServletResponse res) {
        return dispatch(req, res);
    }

    // 요청 응답을 컨텍스트에 담아 각 핸들러로 분배
    Ctx.Response dispatch(HttpServletRequest req, HttpServletResponse res) {
        Ctx c = new Ctx(req, res);
        try {
            Method m = api.get(c.path);
            if (m != null) {
                m.invoke(null, c);
            } else {
                c.response.code = "ApiNotFound";
                c.response.data = c.path;
            }
        } catch (Throwable t) {
            if (t instanceof InvocationTargetException) {
                Throwable cause = t.getCause();
                if (cause instanceof X.Error) {
                    X.Error xe = (X.Error) cause;
                    c.response.code = xe.code;
                    c.response.data = xe.data;
                    logger.warn("감지한에러: " + xe);
                } else {
                    c.response.code = cause.getClass().getSimpleName();
                    c.response.data = cause.getMessage();
                    cause.printStackTrace();
                }
            } else {
                c.response.code = t.getClass().getSimpleName();
                c.response.data = t.getMessage();
                t.printStackTrace();
            }
            if (c.withinTx) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
        } finally {
            c.done();
        }

        if (c.replied) {
            return null;
        }
        return c.response;
    }
}