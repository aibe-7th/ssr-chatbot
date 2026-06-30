package com.example.chatbot;

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

@WebServlet("/chat")
public class ChatServlet extends HttpServlet {
    private static final String MESSAGES_KEY = "messages";

    public static class Message {
        private final String role;
        private final String text;

        public Message(String role, String text) {
            this.role = role;
            this.text = text;
        }

        public String getRole() {
            return role;
        }

        public String getText() {
            return text;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        List<Message> messages = getMessages(session);
        request.setAttribute(MESSAGES_KEY, messages);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/chat.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        List<Message> messages = getMessages(session);

        String text = request.getParameter("message");
        if (text != null) {
            text = text.trim();
        }
        if (text != null && !text.isBlank()) {
            messages.add(new Message("user", text));
        }

        response.sendRedirect("chat");
    }

    @SuppressWarnings("unchecked")
    private List<Message> getMessages(HttpSession session) {
        Object value = session.getAttribute(MESSAGES_KEY);
        if (value instanceof List<?>) {
            return (List<Message>) value;
        }

        List<Message> messages = new ArrayList<>();
        session.setAttribute(MESSAGES_KEY, messages);
        return messages;
    }
}
