package org.ovirt.engine.core.dao.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.NetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatistics;
import org.ovirt.engine.core.dao.BaseDaoTestCase;

public abstract class NetworkStatisticsDaoTest<T extends NetworkStatistics> extends BaseDaoTestCase  {

    protected abstract List<? extends NetworkInterface<T>> getAllInterfaces();

    protected abstract void updateStatistics(T stats);

    /**
     * Ensures that updating statistics for an interface works as expected.
     */
    protected void testUpdateStatistics(Double doubleValue, Long longValue) {
        List<? extends NetworkInterface<T>> before = getAllInterfaces();
        T stats = before.get(0).getStatistics();

        stats.setReceiveDropRate(999.0);
        stats.setReceiveRate(999.0);
        stats.setReceivedBytes(longValue);
        stats.setReceivedBytesOffset(longValue);
        stats.setTransmitDropRate(999.0);
        stats.setTransmitRate(999.0);
        stats.setTransmitDropRate(999.0);
        stats.setTransmittedBytes(longValue);
        stats.setTransmittedBytesOffset(longValue);
        stats.setSampleTime(doubleValue);

        updateStatistics(stats);

        List<? extends NetworkInterface<T>> after = getAllInterfaces();
        boolean found = false;

        for (NetworkInterface<T> ifaced : after) {
            if (ifaced.getStatistics().getId().equals(stats.getId())) {
                found = true;
                assertEquals(stats.getReceiveDropRate(), ifaced.getStatistics().getReceiveDropRate());
                assertEquals(stats.getReceiveRate(), ifaced.getStatistics().getReceiveRate());
                assertEquals(stats.getReceivedBytes(), ifaced.getStatistics().getReceivedBytes());
                assertEquals(stats.getReceivedBytesOffset(), ifaced.getStatistics().getReceivedBytesOffset());
                assertEquals(stats.getTransmitDropRate(), ifaced.getStatistics().getTransmitDropRate());
                assertEquals(stats.getTransmitRate(), ifaced.getStatistics().getTransmitRate());
                assertEquals(stats.getTransmitDropRate(), ifaced.getStatistics().getTransmitDropRate());
                assertEquals(stats.getTransmittedBytes(), ifaced.getStatistics().getTransmittedBytes());
                assertEquals(stats.getTransmittedBytesOffset(), ifaced.getStatistics().getTransmittedBytesOffset());
                assertEquals(stats.getSampleTime(), ifaced.getStatistics().getSampleTime());
            }
        }

        if (!found) {
            fail("Did not find statistics which is bad.");
        }
    }

}
