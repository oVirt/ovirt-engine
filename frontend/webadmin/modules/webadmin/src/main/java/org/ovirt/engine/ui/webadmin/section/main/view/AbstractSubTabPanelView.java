package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.common.view.AbstractTabPanelView;

public abstract class AbstractSubTabPanelView extends AbstractTabPanelView {

    protected AbstractSubTabPanelView() {
        generateIds();
    }

    protected abstract void generateIds();
}
