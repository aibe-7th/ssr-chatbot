# JSP 챗봇

Jakarta EE 기반의 웹 챗봇 애플리케이션입니다. Servlet, JSP/JSTL/EL로 화면을 구성하고, Google GenAI SDK로 응답을 생성합니다.

## 동작 흐름

```mermaid
sequenceDiagram
    actor Browser
    participant RootServlet
    participant ChatServlet
    participant ChatJsp as chat.jsp
    participant AIService

    Browser->>RootServlet: GET /
    RootServlet-->>Browser: redirect /chat
    Browser->>ChatServlet: GET /chat
    ChatServlet->>ChatJsp: render
    ChatJsp-->>Browser: HTML 응답

    Browser->>ChatServlet: POST /chat
    ChatServlet->>AIService: 메시지 전송
    AIService-->>ChatServlet: 응답 반환
    ChatServlet->>ChatServlet: session 메시지 목록 갱신
    ChatServlet-->>Browser: redirect /chat
    Browser->>ChatServlet: GET /chat
    ChatServlet->>ChatJsp: render
    ChatJsp-->>Browser: HTML 응답
```

## 구조

```mermaid
flowchart TD
    A[src/main/java/com/example/chatbot] --> B[controller]
    A --> C[model]
    A --> D[service]
    E[src/main/webapp] --> F[WEB-INF/views/chat.jsp]
    E --> G[style.css]
    E --> H[script.js]
```

## 실행 방법

1. 빌드

```bash
./mvnw clean package
```

2. 생성된 WAR를 Tomcat 10+에 배포합니다.

## 환경변수

- `GEMINI_API_KEY`: Google GenAI 호출에 필요한 API 키
