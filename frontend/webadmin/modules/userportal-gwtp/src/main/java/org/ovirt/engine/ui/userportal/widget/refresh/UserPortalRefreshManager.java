package org.ovirt.engine.ui.userportal.widget.refresh;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.common.widget.refresh.SimpleRefreshManager;
import org.ovirt.engine.ui.uicommonweb.models.GridController;
import org.ovirt.engine.ui.userportal.gin.ClientGinjectorProvider;
import com.google.gwt.event.shared.EventBus;

public class UserPortalRefreshManager extends SimpleRefreshManager {

    private static final Integer SLOWER_REFRESH_RATE = Integer.valueOf(30000);

    public UserPortalRefreshManager(ModelProvider<? extends GridController> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage);
    }

    @Override
    protected int getDefaultRefreshRate() {
        // IE optimization: Use slower default refresh rate
        return ClientGinjectorProvider.getClientAgentType().isIE8OrBelow()
                ? SLOWER_REFRESH_RATE : super.getDefaultRefreshRate();
    }

}
