# JSP 채팅봇 실습

## 1. 개요
이 실습은 `Jakarta Servlet`과 `JSP`로 채팅 UI를 만들고, `Google GenAI` SDK를 통해 실제 응답을 생성하는 웹 애플리케이션을 완성하는 과정입니다. 최종적으로는 `/chat`에서 대화 화면을 보여주고, 세션에 메시지 이력을 저장하며, `GEMINI_API_KEY` 환경변수를 사용해 `com.google.genai.Client`로 답변을 생성합니다.

---

## 2. 단계별 실습

### 1단계. Maven 기반 실행 환경 만들기
**목표**  
프로젝트가 `WAR`로 패키징되고, `Jakarta Servlet`, `JSP JSTL`, `Google GenAI`를 사용할 수 있는 기본 빌드 환경을 구성합니다.

**생성/수정 파일**  
- `pom.xml`
- `src/main/webapp/WEB-INF/web.xml`

**핵심 구현 포인트**  
- `groupId`는 `com.example`, `artifactId`는 `chatbot`, `packaging`은 `war`, Java 버전은 `17`으로 설정합니다.
- 의존성은 정확히 다음을 사용합니다.
  - `jakarta.servlet.jsp.jstl-api:3.0.2` (`compile`)
  - `jakarta.servlet.jsp.jstl` (`glassfish`): `3.0.1` (`runtime`)
  - `google-genai:1.60.0` (`compile`)
  - `jakarta.servlet-api:6.1.0` (`provided`)
- `web.xml`은 Jakarta EE `web-app` 버전 `6.0`의 최소 설정만 둡니다.
- 서블릿 등록은 `web.xml`이 아니라 `@WebServlet` 애노테이션으로 처리합니다.

**이 순서인 이유**  
이후 단계에서 작성할 서블릿, JSP, GenAI 코드가 모두 이 빌드 설정 위에서 동작하므로, 가장 먼저 실행 환경을 고정해야 합니다.

### 2단계. 메시지 도메인 모델 정의하기
**목표**  
채팅 이력을 저장하고 JSP에서 출력할 `Message` 타입을 만듭니다.

**생성/수정 파일**  
- `src/main/java/com/example/chatbot/model/Message.java`

**핵심 구현 포인트**  
- `public record Message(String role, String text) implements java.io.Serializable` 형태로 정의합니다.
- JSP EL에서 값을 읽을 수 있도록 `getRole()`과 `getText()`를 명시적으로 제공합니다.
- `role`은 메시지의 작성 주체를 나타내고, `text`는 실제 메시지 내용을 담습니다.

**이 순서인 이유**  
세션에 저장할 데이터 구조가 먼저 있어야 서블릿에서 대화 이력 관리와 화면 출력을 일관되게 구현할 수 있습니다.

### 3단계. Gemini 응답 생성 서비스 구현하기
**목표**  
Google GenAI SDK를 직접 호출하는 `AIService`를 만들어, 사용자 입력과 대화 이력을 기반으로 모델 응답을 생성합니다.

**생성/수정 파일**  
- `src/main/java/com/example/chatbot/service/AIService.java`

**핵심 구현 포인트**  
- `com.google.genai.Client`를 사용합니다.
- API 키는 `System.getenv()`로 읽는 `GEMINI_API_KEY` 환경변수를 사용합니다.
- 허용 모델은 다음 3개만 사용합니다.
  - `gemma-4-26b-a4b-it`
  - `gemma-4-31b-it`
  - `gemini-3.1-flash-lite`
- 기본 모델은 `ALLOWED_MODELS.get(0)`인 `gemma-4-26b-a4b-it`입니다.
- `MAX_OUTPUT_TOKENS`는 `512`입니다.
- `MAX_HISTORY_MESSAGES`는 `10`이며, 최근 메시지 10개만 `subList`로 잘라 사용합니다.
- `normalizeModel()`은 입력 모델이 허용 목록에 있으면 그대로 쓰고, 아니면 기본 모델로 되돌립니다.
- `answer()`는 `Client.builder().apiKey(apiKey).build()`로 클라이언트를 만들고 `client.models.generateContent()`를 호출합니다.
- `GenerateContentConfig`에는 `maxOutputTokens`, `ThinkingConfig(includeThoughts=false, level=MINIMAL)`, `systemInstruction`이 포함됩니다.
- 대화 이력은 `Message.role()`이 `"user"`이면 GenAI `Content`의 role을 `"user"`로, 그 외에는 `"model"`로 매핑합니다.

**이 순서인 이유**  
서블릿이 실제 답변을 만들기 전에, 모델 선택과 이력 처리 규칙을 서비스 계층에 고정해 두어야 화면 로직과 분리할 수 있습니다.

### 4단계. 루트 진입 경로 처리하기
**목표**  
애플리케이션의 `/` 요청을 적절히 처리해 정적 리소스는 그대로 제공하고, 기본 진입점은 채팅 화면으로 보냅니다.

**생성/수정 파일**  
- `src/main/java/com/example/chatbot/controller/RootServlet.java`

**핵심 구현 포인트**  
- `@WebServlet("/")`로 루트 요청을 받습니다.
- `service()`를 오버라이드합니다.
- `URI`에서 `contextPath`를 뺀 뒤 경로를 구합니다.
- `getServletContext().getResource(path)`로 실제 리소스가 존재하고, 경로가 `/`로 끝나지 않으면 `getNamedDispatcher("default").forward()`로 정적 자원 처리에 위임합니다.
- 그 외의 요청은 `resp.sendRedirect(contextPath + "/chat")`로 보냅니다.

**이 순서인 이유**  
JSP 채팅 화면을 만들기 전에, 사용자가 애플리케이션에 들어왔을 때 어디로 이동할지 먼저 결정해야 전체 흐름이 안정적입니다.

### 5단계. 채팅 서블릿으로 대화 흐름 연결하기
**목표**  
세션 기반 메시지 저장, 모델 선택, AI 응답 생성, PRG 패턴을 한 곳에서 처리합니다.

**생성/수정 파일**  
- `src/main/java/com/example/chatbot/controller/ChatServlet.java`

**핵심 구현 포인트**  
- `@WebServlet("/chat")`로 매핑합니다.
- 필드는 `MESSAGES_KEY = "messages"`, `SELECTED_MODEL_KEY = "selectedModel"`, `AIService aiService`입니다.
- `doGet()`은 세션의 `List<Message>`를 가져와서 `messages`, `availableModels`, `selectedModel`을 request attribute로 넣고 `/WEB-INF/views/chat.jsp`로 forward합니다.
- `availableModels`는 `AIService.ALLOWED_MODELS`입니다.
- `doPost()`는 `setCharacterEncoding("UTF-8")`를 호출한 뒤, 세션 메시지 목록을 가져오고 모델을 정규화한 다음 사용자 메시지를 추가합니다.
- 사용자 메시지를 저장한 뒤 AI 답변을 생성해 다시 목록에 추가하고, 마지막에 `response.sendRedirect("chat")`을 실행합니다.
- `getMessages()`는 세션에 기존 `List<Message>`가 있으면 재사용하고, 없으면 새 `ArrayList`를 만들어 세션에 저장합니다.
- `getSelectedModel()`은 세션 값이 있어도 `normalizeModel()`로 다시 검증합니다.
- `generateAnswer()`는 `aiService.answer()`를 호출하고, 예외가 나면 오류 문자열을 반환합니다.

**이 순서인 이유**  
데이터 구조와 AI 서비스가 준비된 뒤에야 실제 사용자 입력을 받아 세션에 쌓고, 응답을 생성하는 흐름을 안전하게 묶을 수 있습니다.

### 6단계. JSP 화면에서 대화 UI 구성하기
**목표**  
채팅 메시지, 모델 선택 폼, 입력창을 가진 화면을 `JSP`로 렌더링합니다.

**생성/수정 파일**  
- `src/main/webapp/WEB-INF/views/chat.jsp`

**핵심 구현 포인트**  
- JSTL 코어 태그 라이브러리는 `<%@ taglib prefix="c" uri="jakarta.tags.core" %>`를 사용합니다.
- 스타일시트는 `${pageContext.request.contextPath}/style.css`로 연결합니다.
- 메시지 표시에는 `c:forEach`를 사용하고, 각 메시지는 `msg.role`이 `"user"`이면 `message me`, 아니면 `message bot` 클래스를 적용합니다.
- 역할과 본문은 각각 `<c:out value="${msg.role}"/>`, `<c:out value="${msg.text}"/>`로 출력합니다.
- 폼은 `method="post"`, `action="chat"`, `id="chatForm"`입니다.
- 모델 선택 `<select>`는 `availableModels`를 `c:forEach`로 돌며, `c:choose`와 `c:when`으로 현재 선택값을 `selected` 처리합니다.
- 입력 필드는 `type="text"`와 `name="message"`를 사용합니다.
- 하단에는 `${pageContext.request.contextPath}/script.js`를 연결합니다.
- 화면에서 마크다운 렌더링을 위해 `DOMPurify@3.2.6`과 `marked@15.0.12` CDN 스크립트를 `defer`로 불러옵니다.

**이 순서인 이유**  
서버에서 넘겨준 메시지와 모델 목록이 있어야 화면 구조를 확정할 수 있고, 이후 스타일과 스크립트를 붙이기 쉬워집니다.

### 7단계. 채팅 화면 스타일링하기
**목표**  
채팅 영역이 화면 중앙에 정렬되고, 사용자와 봇 메시지가 좌우로 구분되도록 시각적 구조를 만듭니다.

**생성/수정 파일**  
- `src/main/webapp/style.css`

**핵심 구현 포인트**  
- `body`는 `flex` 컬럼 레이아웃, `min-height: 100vh`, `align-items: center`로 구성합니다.
- `.chat`은 `flex` 컬럼, `max-width: 640px`, `margin: auto`, `height: calc(95vh - 48px)`, `gap: 12px`를 사용합니다.
- `.messages`는 `flex: 1`, `overflow-y: auto`, `scrollbar-width: none`, `flex-direction: column`, `gap: 12px`로 설정합니다.
- `.message`는 `max-width: 80%`, `border-radius: 8px`를 사용합니다.
- `.message.me`는 `align-self: flex-end`, 배경색 `#e8f4fd`로 표시합니다.
- `.message.bot`는 `align-self: flex-start`, 배경색 `#f5f5f5`로 표시합니다.
- `.role`은 `font-weight: bold`입니다.
- `form`은 `display: flex`, `gap: 12px`입니다.
- `@media max-width: 640px`에서는 `form`을 세로 배치로 전환합니다.

**이 순서인 이유**  
화면의 구조가 완성된 뒤 스타일을 입혀야, 실제 출력 요소와 레이아웃 규칙이 정확히 대응됩니다.

### 8단계. 클라이언트 스크립트로 사용자 경험 다듬기
**목표**  
중복 제출을 막고, 마크다운 렌더링과 자동 스크롤을 처리합니다.

**생성/수정 파일**  
- `src/main/webapp/script.js`

**핵심 구현 포인트**  
- `chatForm`의 `submit` 이벤트에서 `requestAnimationFrame`을 사용해 모든 `select`, `input`, `button`을 비활성화합니다.
- 이 동작은 중복 제출을 줄이기 위한 것입니다.
- `DOMContentLoaded`에서 `window.marked`와 `window.DOMPurify` 존재 여부를 확인합니다.
- `marked.setOptions({breaks: true})`를 설정합니다.
- `.markdown` 요소를 찾아 `marked.parse(element.textContent)` 결과를 `DOMPurify.sanitize()`로 정리한 뒤 `innerHTML`에 넣습니다.
- 렌더링 후에는 메시지 영역을 하단으로 스크롤합니다.
- `window.load`에서도 `scrollMessagesToBottom()`를 호출합니다.

**이 순서인 이유**  
서버 렌더링과 기본 스타일이 준비된 뒤에야, 클라이언트에서 상호작용 보강과 렌더링 보정 작업을 안전하게 적용할 수 있습니다.

---

## 3. 최종 디렉터리 구성
```text
jsp-chatbot/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── example/
        │           └── chatbot/
        │               ├── controller/
        │               │   ├── ChatServlet.java
        │               │   └── RootServlet.java
        │               ├── model/
        │               │   └── Message.java
        │               └── service/
        │                   └── AIService.java
        └── webapp/
            ├── script.js
            ├── style.css
            └── WEB-INF/
                ├── web.xml
                └── views/
                    └── chat.jsp
```

## 검증 포인트
- 모델 이름이 `gemma-4-26b-a4b-it`, `gemma-4-31b-it`, `gemini-3.1-flash-lite`와 정확히 일치하는지 확인합니다.
- 환경변수 이름이 `GEMINI_API_KEY`인지 확인합니다.
- 클래스명이 `Message`, `AIService`, `ChatServlet`, `RootServlet`인지 확인합니다.
- `pom.xml`의 버전이 실제 구현과 같은지 다시 확인합니다.
