package com.example.jetbrainsvcstask;

public class WebhookEvent {
    private String eventType;
    private String eventID;
    private String eventTime;

    public WebhookEvent(String eventType, String eventID, String eventTime) {
        this.eventType = eventType;
        this.eventID = eventID;
        this.eventTime = eventTime;
    }


    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }
}
