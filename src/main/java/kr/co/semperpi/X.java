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
    public static class About {
        public final String BUILD_DATE = "###BUILD_DATE###";
        public final String VERSION = "###VERSION###";
        public final String REVISION = "###REVISION###";
    }

    public static final ObjectMapper MAPPER = new ObjectMapper();
    public static final Logger logger = LoggerFactory.getLogger(X.class);
    public static Map<String, Method> api;
    public static DataSource dataSource;
    public static JdbcTemplate jdbcTemplate;

    ////////////////////////////////////////////////////////////////////////////
    /// 유일한 생성자
    ////////////////////////////////////////////////////////////////////////////
    public X(@NonNull DataSource ds, Environment env, JdbcTemplate jt) {
        try {
            dataSource = ds;
            jdbcTemplate = jt;
            api = XUtil.scanHandler();
            XUtil.initAES(env);
            logger.info("X initialized");
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Autowired
    private TransactionTemplate transactionTemplate;

    @RequestMapping("/api/**")
    public XContext.Response handle(
            HttpServletRequest req, HttpServletResponse res) {
        XContext c = new XContext(req, res);
        if (c.withinTx) {
            return transactionTemplate.execute(status -> {
                XContext.Response r = c.dispatch();
                if (!"OK".equals(r.code)) {
                    status.setRollbackOnly();
                }
                return r;
            });
        }
        return c.dispatch();
    }
}
