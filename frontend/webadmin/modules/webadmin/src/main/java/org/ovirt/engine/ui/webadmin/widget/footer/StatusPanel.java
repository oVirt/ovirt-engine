package org.ovirt.engine.ui.webadmin.widget.footer;

import com.google.gwt.user.client.ui.SimplePanel;

public final class StatusPanel extends SimplePanel {

    private final StatusLabel statusLabel;

    private String foregroundStyle;
    private String backgroundStyle;

    public StatusPanel() {
        this.statusLabel = new StatusLabel() {
            @Override
            protected void onFadeInComplete() {
                setStylePrimaryName(foregroundStyle);
                StatusPanel.this.setStylePrimaryName(backgroundStyle);
            }
        };
        add(statusLabel);
    }

    public void setTextAndStyle(String text, String backgroundStyle, String foregroundStyle) {
        this.backgroundStyle = backgroundStyle;
        this.foregroundStyle = foregroundStyle;
        statusLabel.setFadeText(text);
    }

}
