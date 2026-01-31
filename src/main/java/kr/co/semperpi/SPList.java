package kr.co.semperpi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import java.util.Iterator;

import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

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

    private final List<SP> procedures = new ArrayList<>();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
    };

    public SPList() {
    }

    public SPList(String json) throws Exception {
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

    @SuppressWarnings("unchecked")
    public List<List<Map<String, Object>>> call() {
        List<List<Map<String, Object>>> results = new ArrayList<>();

        for (SP sp : procedures) {
            X.logger.info("SP: " + sp);
            SimpleJdbcCall sjc = new SimpleJdbcCall(Objects.requireNonNull(X.jdbcTemplate))
                    .withProcedureName(Objects.requireNonNull(sp.procName));

            Map<String, Object> out = (sp.params == null) ? sjc.execute() : sjc.execute(sp.params);
            Object resultSet = out.get("#result-set-1");

            if (resultSet instanceof List) {
                results.add((List<Map<String, Object>>) resultSet);
            } else {
                results.add(List.of(out));
            }
        }
        X.logger.debug("결과: " + results);
        return results;
    }

    public String toString() {
        return procedures.toString();
    }
}
