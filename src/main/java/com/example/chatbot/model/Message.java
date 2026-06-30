package com.example.chatbot.model;

// 채팅 화면에 표시할 한 개의 메시지를 표현한다.
// role은 보낸 사람 구분(user/model), text는 실제 메시지 내용이다.
public record Message(String role, String text) implements java.io.Serializable {
    // JSP EL은 getRole() 형태의 JavaBean getter를 기준으로 msg.role을 찾는다.
    public String getRole() {
        return role;
    }

    // JSP에서 msg.text로 메시지 내용을 읽을 수 있도록 getter를 제공한다.
    public String getText() {
        return text;
    }
}
