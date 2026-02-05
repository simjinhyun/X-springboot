package kr.co.semperpi;

// 정형화된 JSON 응답
public class XResponse {
    public String code = "OK";
    public Object data;
    public String elapsed;
    public String reqid = XUtil.generateReqId();
}