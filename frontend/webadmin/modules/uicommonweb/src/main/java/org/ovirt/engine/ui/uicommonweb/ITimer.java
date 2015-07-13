package org.ovirt.engine.ui.uicommonweb;

@SuppressWarnings("unused")
public interface ITimer extends IProvideTickEvent {
    int getInterval();

    void setInterval(int value);

    void start();

    void stop();
}
