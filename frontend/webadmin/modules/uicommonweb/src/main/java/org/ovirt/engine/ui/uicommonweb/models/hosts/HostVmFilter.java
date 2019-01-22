package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.uicommonweb.ViewFilter;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public enum HostVmFilter implements ViewFilter<HostVmFilter> {
    RUNNING_ON_CURRENT_HOST(ConstantsManager.getInstance().getConstants().runningOnCurrentHost()) {
        @Override
        public void executeQuery(Guid hostId, AsyncQuery<List<VM>> aQuery) {
            // During the migration, the VM should be visible on source host (Migrating From), and also
            // on destination host (Migrating To)
            AsyncDataProvider.getInstance().getVmsRunningOnOrMigratingToVds(aQuery, hostId);
        }
    },

    PINNED_TO_CURRENT_HOST(ConstantsManager.getInstance().getConstants().pinnedToCurrentHost()) {
        @Override
        public void executeQuery(Guid hostId, AsyncQuery<List<VM>> aQuery) {
            AsyncDataProvider.getInstance().getVmsPinnedToHost(aQuery, hostId);
        }
    },

    BOTH(ConstantsManager.getInstance().getConstants().both()) {
        @Override
        public void executeQuery(Guid hostId, AsyncQuery<List<VM>> aQuery) {
            Set<VM> resultSet = new HashSet<>();
            int[] callCount = {2};

            AsyncCallback<List<VM>> callback = vmList -> {
                resultSet.addAll(vmList);
                callCount[0] -= 1;
                if (callCount[0] == 0) {
                    aQuery.getAsyncCallback().onSuccess(new ArrayList<>(resultSet));
                }
            };

            RUNNING_ON_CURRENT_HOST.executeQuery(hostId, new AsyncQuery<>(callback));
            PINNED_TO_CURRENT_HOST.executeQuery(hostId, new AsyncQuery<>(callback));
        }
    };

    private String text;

    HostVmFilter(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public HostVmFilter getValue() {
        return this;
    }

    public abstract void executeQuery(Guid hostId, AsyncQuery<List<VM>> aQuery);
}
