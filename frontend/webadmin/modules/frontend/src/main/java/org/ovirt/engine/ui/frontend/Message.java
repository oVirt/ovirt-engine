package org.ovirt.engine.ui.frontend;

public class Message {
    private String description;
    private String text;

    public Message(String text) {
        this.text = text;
    }

    public Message(String description, String text) {
        this.description = description;
        this.text = text;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
