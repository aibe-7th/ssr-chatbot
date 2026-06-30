<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chat</title>
    <style>
        body {
            margin: 0;
            padding: 24px;
        }

        .chat {
            display: flex;
            flex-direction: column;
            gap: 16px;
            max-width: 720px;
            margin: 0 auto;
        }

        .messages {
            display: flex;
            flex-direction: column;
            gap: 12px;
            padding: 16px;
            border: 1px solid #ccc;
        }

        .message {
            padding: 12px;
            border: 1px solid #ddd;
        }

        .role {
            margin: 0 0 6px 0;
            font-weight: bold;
        }

        .text {
            margin: 0;
        }

        form {
            display: flex;
            gap: 12px;
        }

        input[type="text"] {
            flex: 1;
            padding: 10px;
            border: 1px solid #bbb;
        }

        button {
            padding: 10px 16px;
            border: 1px solid #bbb;
        }
    </style>
</head>
<body>
<main class="chat">
    <section class="messages" aria-label="Chat history">
        <c:forEach var="msg" items="${messages}">
            <div class="message">
                <p class="role"><c:out value="${msg.role}"/></p>
                <p class="text"><c:out value="${msg.text}"/></p>
            </div>
        </c:forEach>
    </section>

    <form method="post" action="chat">
        <input type="text" name="message" autocomplete="off" />
        <button type="submit">Send</button>
    </form>
</main>
</body>
</html>
