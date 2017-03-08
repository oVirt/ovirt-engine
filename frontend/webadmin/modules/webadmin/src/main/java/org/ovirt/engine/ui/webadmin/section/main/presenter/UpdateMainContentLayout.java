package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.webadmin.section.main.presenter.overlay.OverlayPresenter.OverlayType;

import com.gwtplatform.dispatch.annotation.GenEvent;

/**
 * Event triggered when {@link MainContentPresenter} should update its layout.
 */
@GenEvent
public class UpdateMainContentLayout {
    public enum ContentDisplayType {
        MAIN,
        SUB,
        OVERLAY,
        RESTORE;
    }

    ContentDisplayType contentDisplayType;
    OverlayType overlayType;
}
