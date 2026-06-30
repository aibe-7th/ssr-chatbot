const chatForm = document.getElementById('chatForm');
const messages = document.querySelector('.messages');

function scrollMessagesToBottom() {
    messages.scrollTop = messages.scrollHeight;
}

chatForm.addEventListener('submit', () => {
    requestAnimationFrame(() => {
        const controls = chatForm.querySelectorAll('select, input, button');
        controls.forEach((control) => {
            control.disabled = true;
        });
    });
});

window.addEventListener('DOMContentLoaded', () => {
    if (!window.marked || !window.DOMPurify) {
        return;
    }

    marked.setOptions({
        breaks: true
    });

    document.querySelectorAll('.markdown').forEach((element) => {
        const markdown = element.textContent;
        element.innerHTML = DOMPurify.sanitize(marked.parse(markdown));
    });

    scrollMessagesToBottom();
});

window.addEventListener('load', scrollMessagesToBottom);
