package org.ovirt.engine.ui.webadmin.widget.footer;

import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.SimplePanel;

public final class StatusPanel extends SimplePanel {

    private final StatusLabel statusLabel;

    private String foregroundStyle;
    private String backgroundStyle;

    @UiConstructor
    public StatusPanel(String text, String backgroundStyle, String foregroundStyle) {
        this.foregroundStyle = foregroundStyle;
        this.backgroundStyle = backgroundStyle;
        setStylePrimaryName(backgroundStyle);
        this.statusLabel = new StatusLabel(text, foregroundStyle) {
            @Override
            protected void onFadeInComplete() {
                setStylePrimaryName(StatusPanel.this.foregroundStyle);
                StatusPanel.this.setStylePrimaryName(StatusPanel.this.backgroundStyle);
            }
        };
        add(statusLabel);
    }

    public void setTextAndStyle(String text, String backgroundStyle, String foregroundStyle) {
        this.backgroundStyle = backgroundStyle;
        this.foregroundStyle = foregroundStyle;
        setFadeText(text);
    }

    public void setFadeText(String text) {
        statusLabel.setFadeText(text);
    }
}
