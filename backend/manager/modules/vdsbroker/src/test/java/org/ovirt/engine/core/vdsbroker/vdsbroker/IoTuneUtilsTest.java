package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.ovirt.engine.core.vdsbroker.vdsbroker.IoTuneUtils.MB_TO_BYTES;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;

public class IoTuneUtilsTest {

    private StorageQos qos;

    @BeforeEach
    public void setUp() {
        qos = new StorageQos();
    }

    void assertIoTune(long totalBytesSec, long readBytesSec, long writeBytesSec,
                      long totalIopsSec, long readIopsSec, long writeIopsSec) {
        Map<String, Long> ioTune = IoTuneUtils.ioTuneMapFrom(qos);

        assertEquals(ioTune.get(VdsProperties.TotalBytesSec).longValue(), totalBytesSec);
        assertEquals(ioTune.get(VdsProperties.ReadBytesSec).longValue(), readBytesSec);
        assertEquals(ioTune.get(VdsProperties.WriteBytesSec).longValue(), writeBytesSec);

        assertEquals(ioTune.get(VdsProperties.TotalIopsSec).longValue(), totalIopsSec);
        assertEquals(ioTune.get(VdsProperties.ReadIopsSec).longValue(), readIopsSec);
        assertEquals(ioTune.get(VdsProperties.WriteIopsSec).longValue(), writeIopsSec);
    }

    @Test
    public void testUnlimited() {
        assertIoTune(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void testThroughput() {
        qos.setMaxThroughput(100);
        assertIoTune(100L * MB_TO_BYTES, 0, 0, 0, 0, 0);

        qos.setMaxThroughput(null);
        qos.setMaxReadThroughput(60);
        qos.setMaxWriteThroughput(30);
        assertIoTune(0, 60L * MB_TO_BYTES, 30L * MB_TO_BYTES, 0, 0, 0);
    }

    @Test
    public void testZeroThroughputIsUnlimited() {
        qos.setMaxThroughput(0);
        assertIoTune(0, 0, 0, 0, 0, 0);

        qos.setMaxThroughput(null);
        qos.setMaxReadThroughput(0);
        qos.setMaxWriteThroughput(1);
        assertIoTune(0, 0, 1L * MB_TO_BYTES, 0, 0, 0);
    }

    @Test
    public void testIopsSec() {
        qos.setMaxIops(10000);
        assertIoTune(0, 0, 0, 10000, 0, 0);

        qos.setMaxIops(null);
        qos.setMaxReadIops(10000);
        qos.setMaxWriteIops(5000);
        assertIoTune(0, 0, 0, 0, 10000, 5000);
    }

    @Test
    public void testZeroIopsSecIsUnlimited() {
        qos.setMaxIops(0);
        assertIoTune(0, 0, 0, 0, 0, 0);

        qos.setMaxIops(null);
        qos.setMaxReadIops(0);
        qos.setMaxWriteIops(1);
        assertIoTune(0, 0, 0, 0, 0, 1);
    }
}
