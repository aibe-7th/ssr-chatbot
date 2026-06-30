package com.example.chatbot.controller;

import com.example.chatbot.model.Message;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// /chat 요청을 담당하는 서블릿이다.
// 세션에 채팅 내역을 보관하고 JSP 화면에 전달한다.
@WebServlet("/chat")
public class ChatServlet extends HttpServlet {
    // 세션과 request attribute에서 공통으로 사용할 메시지 목록 이름이다.
    private static final String MESSAGES_KEY = "messages";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 현재 세션의 메시지 목록을 JSP에서 사용할 수 있도록 request에 담는다.
        HttpSession session = request.getSession();
        List<Message> messages = getMessages(session);
        request.setAttribute(MESSAGES_KEY, messages);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/chat.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 사용자가 보낸 메시지를 세션 내역에 추가하고 간단한 봇 응답을 붙인다.
        HttpSession session = request.getSession();
        List<Message> messages = getMessages(session);

        String text = request.getParameter("message");
        if (text != null) {
            text = text.trim();
        }
        if (text != null && !text.isBlank()) {
            messages.add(new Message("user", text));
            messages.add(new Message("model", "알겠습니다"));
        }

        // 새로고침 시 같은 POST가 반복되지 않도록 GET /chat으로 되돌린다.
        response.sendRedirect("chat");
    }

    @SuppressWarnings("unchecked")
    private List<Message> getMessages(HttpSession session) {
        // 이미 세션에 보관된 메시지 목록이 있으면 그대로 재사용한다.
        Object value = session.getAttribute(MESSAGES_KEY);
        if (value instanceof List<?>) {
            return (List<Message>) value;
        }

        // 첫 방문자는 빈 메시지 목록을 만들고 이후 요청에서 이어서 사용한다.
        List<Message> messages = new ArrayList<>();
        session.setAttribute(MESSAGES_KEY, messages);
        return messages;
    }
}
