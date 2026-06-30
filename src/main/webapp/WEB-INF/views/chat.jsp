<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- 채팅 화면을 렌더링하는 JSP이다. 메시지 출력에는 JSTL을 사용한다. --%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chat</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/style.css">
    <script src="https://cdn.jsdelivr.net/npm/dompurify@3.2.6/dist/purify.min.js" defer></script>
    <script src="https://cdn.jsdelivr.net/npm/marked@15.0.12/marked.min.js" defer></script>
</head>
<body>
<div class="chat">
    <%-- 서블릿이 request에 담아 준 messages 목록을 순회하며 대화 내역을 출력한다. --%>
    <section class="messages" aria-label="Chat history">
        <c:forEach var="msg" items="${messages}">
            <%-- 사용자 메시지는 me, 모델 메시지는 bot 클래스를 붙여 CSS에서 다르게 보이게 한다. --%>
            <div class="message ${msg.role eq 'user' ? 'me' : 'bot'}">
                <p class="role"><c:out value="${msg.role}"/></p>
                <p class="text markdown"><c:out value="${msg.text}"/></p>
            </div>
        </c:forEach>
    </section>

    <%-- 선택한 모델과 입력 메시지는 POST /chat으로 전송되어 세션의 대화 내역에 추가된다. --%>
    <form method="post" action="chat" id="chatForm">
        <select name="model" aria-label="Model">
            <c:forEach var="model" items="${availableModels}">
                <c:choose>
                    <c:when test="${model eq selectedModel}">
                        <option value="${model}" selected><c:out value="${model}"/></option>
                    </c:when>
                    <c:otherwise>
                        <option value="${model}"><c:out value="${model}"/></option>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </select>
        <input type="text" name="message" autocomplete="off" />
        <button type="submit">Send</button>
    </form>
</div>
<script src="${pageContext.request.contextPath}/script.js"></script>
</body>
</html>
