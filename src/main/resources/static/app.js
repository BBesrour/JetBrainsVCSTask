const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/gs-guide-websocket'
});

stompClient.onConnect = (frame) => {
    setConnected(true);
    console.log('Connected: ' + frame);
    stompClient.subscribe('/topic/webhookEvent', (event) => {
        showWebhookEvent(JSON.parse(event.body));
    });
};

stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

function setConnected(connected) {
    document.getElementById("connect").disabled = connected;
    document.getElementById("disconnect").disabled = !connected;
}

function connect() {
    stompClient.activate();
}

function disconnect() {
    stompClient.deactivate();
    setConnected(false);
    console.log("Disconnected");
}

function showWebhookEvent({eventID, eventTime, eventType}) {
    let list = document.getElementById("webhookEventsList");
    let li = document.createElement("li");
    li.classList.add("list-group-item");
    li.appendChild(document.createTextNode(`Event ID: ${eventID}, Event Time: ${eventTime}, Event Type: ${eventType}`));
    list.appendChild(li);
}

document.addEventListener("DOMContentLoaded", function() {
    document.getElementById("webhookForm").addEventListener('submit', (e) => e.preventDefault());
    document.getElementById("connect").addEventListener('click', connect);
    document.getElementById("disconnect").addEventListener('click', disconnect);
});