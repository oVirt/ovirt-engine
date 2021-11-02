package org.ovirt.engine.core.dao.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigInteger;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.NetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatistics;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.Dao;

public abstract class NetworkStatisticsDaoTest<D extends Dao, T extends NetworkStatistics> extends BaseDaoTestCase<D> {

    protected abstract List<? extends NetworkInterface<T>> getAllInterfaces();

    protected abstract void updateStatistics(T stats);

    /**
     * Ensures that updating statistics for an interface works as expected.
     */
    protected void testUpdateStatistics(Double doubleValue, BigInteger bigIntValue) {
        List<? extends NetworkInterface<T>> before = getAllInterfaces();
        T stats = before.get(0).getStatistics();
        var bigInt999 = new BigInteger("999");
        stats.setReceiveDrops(bigInt999);
        stats.setReceiveRate(999.0);
        stats.setReceivedBytes(bigIntValue);
        stats.setReceivedBytesOffset(bigIntValue);
        stats.setTransmitDrops(bigInt999);
        stats.setTransmitRate(999.0);
        stats.setTransmitDrops(bigInt999);
        stats.setTransmittedBytes(bigIntValue);
        stats.setTransmittedBytesOffset(bigIntValue);
        stats.setSampleTime(doubleValue);

        updateStatistics(stats);

        List<? extends NetworkInterface<T>> after = getAllInterfaces();
        boolean found = false;

        for (NetworkInterface<T> ifaced : after) {
            if (ifaced.getStatistics().getId().equals(stats.getId())) {
                found = true;
                assertEquals(stats.getReceiveDrops(), ifaced.getStatistics().getReceiveDrops());
                assertEquals(stats.getReceiveRate(), ifaced.getStatistics().getReceiveRate());
                assertEquals(stats.getReceivedBytes(), ifaced.getStatistics().getReceivedBytes());
                assertEquals(stats.getReceivedBytesOffset(), ifaced.getStatistics().getReceivedBytesOffset());
                assertEquals(stats.getTransmitDrops(), ifaced.getStatistics().getTransmitDrops());
                assertEquals(stats.getTransmitRate(), ifaced.getStatistics().getTransmitRate());
                assertEquals(stats.getTransmitDrops(), ifaced.getStatistics().getTransmitDrops());
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
