package cz.utb.thesisapp;

import java.util.Date;

public class Event {
    public Date time;
    public float data;
    public boolean delay;
    public boolean download;
    public boolean upload;
    public boolean error;

    public Event(Date time, float data, String eventType) {
        this.time = time;
        this.data = data;
        switch (eventType) {
            case "delay":
                delay = true;
                break;
            case "UPLOAD":
                upload = true;
                break;
            case "DOWNLOAD":
                download = true;
                break;
            default:
                error = true;
                break;

        }
    }
}
