package com.example.chatbot.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URL;

// 애플리케이션의 루트 요청을 처리한다.
// 실제 정적 리소스가 있으면 기본 서블릿에 맡기고, 그 외 요청은 채팅 화면으로 보낸다.
@WebServlet("/")
public class RootServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 컨텍스트 경로를 제외한 실제 요청 경로만 추출한다.
        String uri = req.getRequestURI();
        String ctx = req.getContextPath();
        String path = uri.substring(ctx.length());
        if (path.isEmpty()) {
            path = "/";
        }

        // CSS 같은 정적 파일 요청은 톰캣의 기본 서블릿이 직접 응답하도록 전달한다.
        URL resource = getServletContext().getResource(path);
        if (resource != null && !path.endsWith("/")) {
            RequestDispatcher dispatcher = getServletContext().getNamedDispatcher("default");
            dispatcher.forward(req, resp);
        } else {
            // 루트나 알 수 없는 경로는 사용자가 바로 채팅 화면을 보도록 리다이렉트한다.
            resp.sendRedirect(req.getContextPath() + "/chat");
        }
    }
}
