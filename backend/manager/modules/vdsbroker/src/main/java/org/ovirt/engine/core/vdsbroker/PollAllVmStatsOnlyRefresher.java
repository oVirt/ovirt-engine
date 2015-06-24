package org.ovirt.engine.core.vdsbroker;

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
    protected boolean getRefreshStatistics() {
        return true;
    }
}
