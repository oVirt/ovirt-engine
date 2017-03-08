package org.ovirt.engine.ui.webadmin.widget.tab;

import com.google.gwt.dom.client.Style.HasCssName;

public class MenuLayoutMenuDetails {
    private String primaryTitle;
    private String secondaryTitle;
    private int primaryPriority;
    private int secondaryPriority;
    private HasCssName icon;

    public String getPrimaryTitle() {
        return primaryTitle;
    }

    public void setPrimaryTitle(String primaryTitle) {
        this.primaryTitle = primaryTitle;
    }

    public String getSecondaryTitle() {
        return secondaryTitle;
    }

    public void setSecondaryTitle(String secondaryTitle) {
        this.secondaryTitle = secondaryTitle;
    }

    public int getPrimaryPriority() {
        return primaryPriority;
    }

    public void setPrimaryPriority(int primaryPriority) {
        this.primaryPriority = primaryPriority;
    }

    public int getSecondaryPriority() {
        return secondaryPriority;
    }

    public void setSecondaryPriority(int secondaryPriority) {
        this.secondaryPriority = secondaryPriority;
    }

    public HasCssName getIcon() {
        return icon;
    }

    public void setIcon(HasCssName icon) {
        this.icon = icon;
    }

}
