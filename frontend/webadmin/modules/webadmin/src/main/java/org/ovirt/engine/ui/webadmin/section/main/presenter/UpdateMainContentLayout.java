package org.ovirt.engine.ui.webadmin.section.main.presenter;

import com.gwtplatform.dispatch.annotation.GenEvent;

/**
 * Event triggered when {@link MainContentPresenter} should update its layout.
 */
@GenEvent
public class UpdateMainContentLayout {

    boolean subTabPanelVisible;

}
