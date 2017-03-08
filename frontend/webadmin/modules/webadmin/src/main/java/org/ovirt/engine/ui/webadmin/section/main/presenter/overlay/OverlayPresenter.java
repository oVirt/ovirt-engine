package org.ovirt.engine.ui.webadmin.section.main.presenter.overlay;

public interface OverlayPresenter {
    public enum OverlayType {
        TASKS,
        BOOKMARK,
        TAGS;
    }

    OverlayType getOverlayType();
}
