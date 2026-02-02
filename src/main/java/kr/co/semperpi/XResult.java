package kr.co.semperpi;

import java.util.List;
import java.util.Map;

public class XResult {
    public List<Map<String, Object>> rows;

    public XResult(List<Map<String, Object>> rows) {
        this.rows = rows;
    }

    public Map<String, Object> get(int idx) {
        return rows.get(idx);
    }

    public int size() {
        return rows.size();
    }

    public String toString() {
        return rows.toString();
    }
}
