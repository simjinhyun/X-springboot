package api;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.boot.SpringApplication;

import kr.co.semperpi.*;

public class App {
    public static void about(XContext c) {
        c.response.data = "App 클래스에 ### 홀더 놓고 치환해라";
    }

    public static void custom(XContext c) {
        try {
            c.res.setContentType("text/plain; charset=UTF-8");
            PrintWriter out = c.res.getWriter();
            out.write("Hello, world!");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 스프링부트에 X를 넣고 실행
    public static void main(String[] args) {
        SpringApplication.run(X.class, args);
    }
}
