package org.ovirt.engine.ui.uicommonweb.models.vms;

@SuppressWarnings("unused")
public class SpiceMenuCommandItem extends SpiceMenuItem {
    private String privateText;

    public String getText() {
        return privateText;
    }

    public void setText(String value) {
        privateText = value;
    }

    private String privateCommandName;

    public String getCommandName() {
        return privateCommandName;
    }

    public void setCommandName(String value) {
        privateCommandName = value;
    }

    public SpiceMenuCommandItem(int id, String text, String commandName) {
        setId(id);
        setText(text);
        setCommandName(commandName);
    }
}
