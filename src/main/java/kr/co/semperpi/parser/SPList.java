package kr.co.semperpi.parser;

import kr.co.semperpi.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import java.util.Iterator;

import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import kr.co.semperpi.X;

public class SPList {
    public static class SP {
        public final String procName;
        public final Map<String, ?> params;

        public SP(String procName, Map<String, Object> params) {
            this.procName = procName;
            this.params = params;
        }

        public String toString() {
            return procName + ":" + params;
        }
    }

    private XContext ctx;
    private final List<SP> procedures = new ArrayList<>();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
    };

    public SPList(XContext c, String json) throws Exception {
        this.ctx = c; // 주입!
        JsonNode root = X.MAPPER.readTree(json);

        for (JsonNode objNode : root) {
            Iterator<String> fieldNames = objNode.fieldNames();
            while (fieldNames.hasNext()) {
                String procName = fieldNames.next();
                JsonNode paramsNode = objNode.get(procName);
                Map<String, Object> params = X.MAPPER.convertValue(paramsNode, MAP_TYPE);
                procedures.add(new SP(procName, params));
            }
        }
        X.logger.debug("파싱결과:" + toString());
    }

    public SPList.SP get(int idx) {
        return procedures.get(idx);
    }

    public SPList add(SP sp) {
        procedures.add(sp);
        return this;
    }

    public List<XResult> exec() {
        List<XResult> results = new ArrayList<>();
        for (SP sp : procedures) {
            results.add(execSP(sp));
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    private XResult execSP(SP sp) {
        try {
            SimpleJdbcCall sjc = new SimpleJdbcCall(Objects.requireNonNull(X.jdbcTemplate))
                    .withProcedureName(Objects.requireNonNull(sp.procName));

            ctx.put("SP", sp);
            X.spBefore.invoke(null, ctx);
            X.logger.debug("호출SP: {} {}", sp.procName, sp.params);

            Map<String, Object> out = (sp.params == null) ? sjc.execute() : sjc.execute(sp.params);
            X.logger.debug("SimpleJdbcCall 결과: {}", out);

            Object resultSet = null;
            for (Object value : out.values()) {
                if (value instanceof List) {
                    resultSet = value;
                    break;
                }
            }

            XResult xr = (resultSet != null)
                    ? new XResult((List<Map<String, Object>>) resultSet)
                    : new XResult(List.of(out));

            X.logger.debug("호출결과: {}", xr);
            ctx.put("RESULT", xr);
            X.spAfter.invoke(null, ctx);

            return xr;
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            throw (cause instanceof RuntimeException re) ? re : new RuntimeException(cause);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("FilterMethodAccessFailed", e);
        }
    }

}
