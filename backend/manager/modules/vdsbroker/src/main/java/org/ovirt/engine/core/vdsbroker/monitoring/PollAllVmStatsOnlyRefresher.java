package org.ovirt.engine.core.vdsbroker.monitoring;

import org.ovirt.engine.core.vdsbroker.VdsManager;

public class PollAllVmStatsOnlyRefresher extends PollVmStatsRefresher {

    public PollAllVmStatsOnlyRefresher(VdsManager vdsManager) {
        super(vdsManager, getRefreshRate());
    }

    private static int getRefreshRate() {
        return VMS_REFRESH_RATE * NUMBER_VMS_REFRESHES_BEFORE_SAVE;
    }

    @Override
    protected VmsListFetcher getVmsFetcher() {
        return new VmsStatisticsFetcher(vdsManager);
    }

    @Override
    protected boolean isTimeToRefreshStatistics() {
        return true;
    }
}
