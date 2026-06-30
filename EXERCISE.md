## 단계 1. 서블릿+JSP 채팅 뼈대
만지는 파일: `src/main/java/com/example/chatbot/controller/ChatServlet.java`, `src/main/webapp/WEB-INF/views/chat.jsp`
- `HttpServlet`의 `doGet`/`doPost`로 채팅 흐름을 만든다.
- 세션에 `List<Message>` 이력을 저장한다.
- `POST -> redirect -> GET` PRG 패턴을 적용한다.
- `chat.jsp`에서 JSTL `forEach`로 메시지를 출력하고, 봇은 고정 응답 `"알겠습니다"`를 사용한다.

## 단계 2. 레이아웃
만지는 파일: `src/main/webapp/WEB-INF/views/chat.jsp`, `src/main/webapp/style.css`
- 화면 높이를 `100vh` 기준으로 잡고 `flex` 레이아웃을 구성한다.
- 전체 폭은 `max-width: 640px`로 제한하고 가운데 정렬한다.
- `body`, `.chat`, `.messages`, `.form` 구조를 기준으로 배치한다.

## 단계 3. 메시지 좌우 정렬
만지는 파일: `src/main/webapp/style.css`, `src/main/webapp/WEB-INF/views/chat.jsp`
- 사용자 메시지는 `.message.me`로 처리한다.
- `.message.me`는 `align-self: flex-end`로 우측 정렬한다.
- 봇 메시지는 `.message.bot`로 처리한다.
- `.message.bot`는 `align-self: flex-start`로 좌측 정렬한다.

## 단계 4. CSS 외부 분리
만지는 파일: `src/main/webapp/WEB-INF/views/chat.jsp`, `src/main/webapp/style.css`
- 인라인 `style`을 제거한다.
- 스타일을 `webapp/style.css`로 분리한다.
- `chat.jsp`에서 `rel="stylesheet"`로 연결한다.

## 단계 5. MVC 패키지 구조 + Message record
만지는 파일: `src/main/java/com/example/chatbot/controller/ChatServlet.java`, `src/main/java/com/example/chatbot/model/Message.java`
- 패키지를 `com.example.chatbot.controller`와 `com.example.chatbot.model`로 나눈다.
- `ChatServlet`은 `com.example.chatbot.controller.ChatServlet`로 둔다.
- `Message`는 `record(role, text) implements Serializable`로 만든다.
- EL에서 쓰기 쉽도록 getter를 추가한다.

## 단계 6. 루트 서블릿
만지는 파일: `src/main/java/com/example/chatbot/controller/RootServlet.java`
- `@WebServlet("/")`로 루트 요청을 받는다.
- 정적 파일은 `getNamedDispatcher("default")`로 위임한다.
- 그 외 요청은 `/chat`으로 redirect한다.

## 단계 7. JS 외부 분리
만지는 파일: `src/main/webapp/WEB-INF/views/chat.jsp`, `src/main/webapp/script.js`
- 인라인 `script`를 제거한다.
- 스크립트를 `webapp/script.js`로 분리한다.
- `chat.jsp`에서 `script src`로 연결한다.
- `marked.js`와 `DOMPurify`를 사용해 markdown 렌더링을 처리한다.

## 단계 8. 예정: Gemini 연동
만지는 파일: `src/main/java/com/example/chatbot/service/AIService.java`, `src/main/java/com/example/chatbot/controller/ChatServlet.java`, `src/main/webapp/WEB-INF/views/chat.jsp`
- `AIService`를 통해 응답 생성 로직을 분리한다.
- `GEMINI_API_KEY` 환경변수를 사용한다.
- 모델은 `gemma-4-26b-a4b-it` 같은 선택값을 사용한다.
- reset 기능을 추가해 대화를 초기화한다.
