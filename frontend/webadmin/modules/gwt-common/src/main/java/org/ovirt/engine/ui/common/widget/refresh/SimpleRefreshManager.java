package org.ovirt.engine.ui.common.widget.refresh;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.GridController;

import com.google.gwt.event.shared.EventBus;

public class SimpleRefreshManager extends AbstractRefreshManager<RefreshPanel> {

    public SimpleRefreshManager(ModelProvider<? extends GridController> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage);
    }

    @Override
    protected RefreshPanel createRefreshPanel() {
        return new RefreshPanel(this);
    }

}
