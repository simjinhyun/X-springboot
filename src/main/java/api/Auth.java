package api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.co.semperpi.*;

public class Auth {

    public static void whoami(Ctx c) {
        String XToken = c.req.getHeader("XToken");
        if (XToken == null) {
            c.response.code = "NeedLogin";
        } else {
            X.logger.debug("XToken: " + XToken);
            String json = Util.decrypt(XToken);
            try {
                c.response.data = X.MAPPER.readValue(json, Object.class);
            } catch (Exception e) {
                e.printStackTrace();
                throw new X.Error("InvalidJSON", null);
            }
        }
    }

    public static void login(Ctx c) {
        SPList spl = c.parse(SPList.class);
        List<List<Map<String, Object>>> list = spl.call();

        // 해당사용자 없음
        if (list.size() <= 0) {
            throw new X.Error("InvalidLogin", null);
        }

        // DB 암호 획득
        List<Map<String, Object>> r1 = list.get(0);
        Map<String, Object> user = r1.get(0);
        String dbPass = (String) user.get("c_pass");
        X.logger.debug("DB암호: " + dbPass);

        // 평문암호 획득
        SPList.SP sp = spl.get(0);
        X.logger.debug("요청SP목록의 첫번째SP: {}", sp);
        String reqPass = (String) sp.params.get("p_pass");
        X.logger.debug("평문암호: {}", reqPass);

        // DB에 저장된 암호의 salt를 이용하여 요청 평문 암호를 해시후 비교
        try {
            if (!Util.match(reqPass, dbPass)) {
                throw new X.Error("LoginFailed", null);
            }
        } catch (Exception e) {
            throw new X.Error(e.getClass().getSimpleName(), new Object[] { e.getMessage() });
        }

        // 토큰 생성 및 응답
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("userid", user.get("c_id"));
        tokenData.put("issuedAt", System.currentTimeMillis());
        tokenData.put("role", user.get("c_role"));
        X.logger.debug("생성토큰: " + tokenData);

        try {
            String tokenJson = X.MAPPER.writeValueAsString(tokenData);
            c.response.data = Util.encrypt(tokenJson);
        } catch (JsonProcessingException e) {
            throw new X.Error("JsonProcessingException", null);
        }
    }
}
