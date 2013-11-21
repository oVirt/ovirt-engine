package org.ovirt.engine.ui.common.widget.tab;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.PushButton;

/**
 * The {@code RepeatingPushButton} allows the user to press and hold down the button while an action is fired on a
 * specified interval. This for instance allows for smooth scrolling actions on the {@code ScrollableTabBar}.
 */
public class RepeatingPushButton extends PushButton {
    /**
     * The repeating period, in milliseconds.
     */
    private int period;
    /**
     * The timer used to repeat.
     */
    private final Timer timer;

    /**
     * The constructor
     */
    public RepeatingPushButton() {
        super();
        timer = new Timer() {
            @Override
            public void run() {
                RepeatingPushButton.super.onClick();
            }
        };
    }

    /**
     * Set the repeating interval in milliseconds.
     * @param interval The interval in milliseconds.
     */
    public void setRepeatInterval(int interval) {
        this.period = interval;
    }

    /**
     * Called when the user finished clicking on this button.
     */
    @Override
    protected void onClick(){
        timer.cancel();
        super.onClick();
    }

    /**
     * Called when the user aborts a click in progress; for example,
     * by dragging the mouse outside of the button before releasing
     * the mouse button.
     */
    @Override
    protected void onClickCancel(){
        timer.cancel();
        super.onClickCancel();
    }

    /**
     * Called when the user begins to click on this button.
     */
    @Override
    protected void onClickStart(){
        timer.scheduleRepeating(period);
        super.onClickStart();
    }
}
