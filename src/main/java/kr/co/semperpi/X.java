package kr.co.semperpi;

import java.lang.reflect.Method;

import java.util.*;

import javax.sql.DataSource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
@RestController
public class X {
    ////////////////////////////////////////////////////////////////////////////
    /// 퍼블릭 멤버와 메서드
    ////////////////////////////////////////////////////////////////////////////
    public static final ObjectMapper MAPPER = new ObjectMapper();
    public static final Logger logger = LoggerFactory.getLogger(X.class);
    public static Map<String, Method> api;
    public static DataSource dataSource;
    public static JdbcTemplate jdbcTemplate;
    public static Method before;
    public static Method after;
    public static Method spBefore;
    public static Method spAfter;

    public static void dummy(XContext c) {
    }

    ////////////////////////////////////////////////////////////////////////////
    /// 프라이빗 멤버
    ////////////////////////////////////////////////////////////////////////////

    private static Method dummy;

    static {
        dummy = XUtil.findFilter("kr.co.semperpi.X", "dummy");
        before = XUtil.findFilter("filter.Main", "before");
        after = XUtil.findFilter("filter.Main", "after");
        spBefore = XUtil.findFilter("filter.SP", "before");
        spAfter = XUtil.findFilter("filter.SP", "after");

        if (before == null)
            before = dummy;
        if (after == null)
            after = dummy;
        if (spBefore == null)
            spBefore = dummy;
        if (spAfter == null)
            spAfter = dummy;
    }

    ////////////////////////////////////////////////////////////////////////////
    /// 유일한 생성자
    ////////////////////////////////////////////////////////////////////////////
    public X(@NonNull DataSource ds, Environment env, JdbcTemplate jt) {

        try {
            dataSource = ds;
            jdbcTemplate = jt;
            api = XUtil.scanHandler("api");
            XUtil.initAES(env);
            listMethodMap("API", api);
            logger.info("X initialized");
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    // 로딩된 핸들러 맵 출력
    private void listMethodMap(String name, Map<String, Method> map) {
        logger.info("{} 목록", name);
        Map<String, Method> sortedMap = new TreeMap<>(map);
        for (Map.Entry<String, Method> entry : sortedMap.entrySet()) {
            String path = entry.getKey();
            Method m = entry.getValue();
            String methodInfo = String.format("%s.%s(%s)",
                    m.getDeclaringClass().getSimpleName(),
                    m.getName(),
                    m.getParameterTypes()[0].getSimpleName());

            logger.info("{}: {}", path, methodInfo);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    /// 모든 API 요청의 진입점
    ////////////////////////////////////////////////////////////////////////////
    @Autowired
    private TransactionTemplate transactionTemplate;

    @RequestMapping("/api/**")
    public XResponse handle(
            HttpServletRequest req, HttpServletResponse res) {
        XContext c = new XContext(req, res);

        if (!c.withinTx) {
            return c.dispatch();
        }

        return transactionTemplate.execute(status -> {
            XResponse r = c.dispatch();
            if (!"OK".equals(r.code)) {
                status.setRollbackOnly();
            }
            return r;
        });
    }
}
