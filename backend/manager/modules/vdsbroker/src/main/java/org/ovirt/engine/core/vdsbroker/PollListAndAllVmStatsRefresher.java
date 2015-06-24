package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;

public class PollListAndAllVmStatsRefresher extends PollVMStatsRefresher {

    private int refreshIteration;

    public PollListAndAllVmStatsRefresher(VdsManager vdsManager) {
        super(vdsManager, VMS_REFRESH_RATE);
    }

    @Override
    protected VmsListFetcher getVmsFetcher() {
        return getRefreshStatistics() ?
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
        refreshIteration =  (++refreshIteration) % NUMBER_VMS_REFRESHES_BEFORE_SAVE;
    }

    @Override
    public boolean getRefreshStatistics() {
        return refreshIteration == 0;
    }
}
