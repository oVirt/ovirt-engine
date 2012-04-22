package org.ovirt.engine.ui.webadmin.widget.footer;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.ui.Label;

public class StatusLabel extends Label {

    protected static final int DURATION = 200;

    private String pendingText = ""; //$NON-NLS-1$

    private Animation fadeInAnimation = new Animation() {

        @Override
        protected void onUpdate(double progress) {
            getElement().getStyle().setOpacity(progress);
        }

    };

    private Animation fadeOutAnimation = new Animation() {

        @Override
        protected void onComplete() {
            super.onComplete();
            setText(pendingText);
            pendingText = ""; //$NON-NLS-1$
            fadeInAnimation.run(DURATION);
        }

        @Override
        protected void onCancel() {
            // Override default implementation since it calls onComplete()
        };

        @Override
        protected void onUpdate(double progress) {
            getElement().getStyle().setOpacity(1 - progress);
        }

    };

    public StatusLabel() {
        super();
    }

    public StatusLabel(String text) {
        super(text);
    }

    public void setFadeText(String text) {
        pendingText = text;
        fadeInAnimation.cancel();
        fadeOutAnimation.run(DURATION);
    }
}
