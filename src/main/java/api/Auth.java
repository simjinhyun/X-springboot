package api;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.co.semperpi.*;
import kr.co.semperpi.parser.SPList;

public class Auth {

    // 세션정보확인
    public static void whoami(XContext c) {
        String XToken = c.req.getHeader("XToken");
        if (XToken == null) {
            c.response.code = "NeedLogin";
        } else {
            String json = XUtil.decrypt(XToken);
            try {
                c.response.data = X.MAPPER.readValue(json, Object.class);
            } catch (Exception e) {
                e.printStackTrace();
                throw new XError("ParseTokenFailed", null);
            }
        }
    }

    // 로그인
    public static void login(XContext c) {
        // 평문 아이디 & 암호 획득
        SPList spl = c.parse(SPList.class);
        SPList.SP sp = spl.get(0);
        String p_id = (String) sp.params.get("p_id");
        String p_pass = (String) sp.params.get("p_pass");
        X.logger.debug("평문아이디: {} 평문암호: {}", p_id, p_pass);

        // DB에 저장된 암호의 salt를 이용하여 요청 평문 암호를 해시후 비교
        Map<String, Object> user = readUserFromDB(spl);
        if (!XUtil.match(p_pass, (String) user.get("c_pass"))) {
            throw new XError("LoginFailed", new Object[] { p_id });
        }

        try {
            user.put("issuedAt", System.currentTimeMillis());
            user.remove("c_pass");
            String tokenJson = X.MAPPER.writeValueAsString(user);
            c.response.data = XUtil.encrypt(tokenJson);
        } catch (JsonProcessingException e) {
            throw new XError("CreateTokenFailed", null);
        }
    }

    // DB에서 사용자 읽기
    private static Map<String, Object> readUserFromDB(SPList spl) {
        List<XResult> list = spl.exec();
        // SP 결과 집합 없음
        if (list.size() <= 0) {
            throw new XError("LoginFailed", null);
        }
        // 해당사용자 ID 없음
        XResult r1 = list.get(0);
        if (r1.size() <= 0) {
            throw new XError("LoginFailed", null);
        }
        Map<String, Object> user = r1.get(0);
        X.logger.debug("사용자레코드: " + user);
        return user;
    }
}
