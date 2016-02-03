package org.ovirt.engine.core.vdsbroker.monitoring;

import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.vdsbroker.VdsManager;

public class PollListAndAllVmStatsRefresher extends PollVmStatsRefresher {

    private int refreshIteration;

    public PollListAndAllVmStatsRefresher(VdsManager vdsManager) {
        super(vdsManager, VMS_REFRESH_RATE);
    }

    @Override
    protected VmsListFetcher getVmsFetcher() {
        return isTimeToRefreshStatistics() ?
                new VmsStatisticsFetcher(vdsManager) :
                new VmsListFetcher(vdsManager);
    }

    @Override
    @OnTimerMethodAnnotation("poll")
    public void poll() {
        super.poll();
        updateIteration();
    }

    private void updateIteration() {
        refreshIteration =  ++refreshIteration % NUMBER_VMS_REFRESHES_BEFORE_SAVE;
    }

    @Override
    protected boolean isTimeToRefreshStatistics() {
        return refreshIteration == 0;
    }
}
