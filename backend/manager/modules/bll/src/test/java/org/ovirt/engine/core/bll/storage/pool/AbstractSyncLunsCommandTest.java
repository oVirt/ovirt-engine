package org.ovirt.engine.core.bll.storage.pool;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.SyncLunsParameters;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;

@RunWith(MockitoJUnitRunner.class)
public class AbstractSyncLunsCommandTest {

    private SyncLunsParameters parameters = new SyncLunsParameters();

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractSyncLunsCommand<SyncLunsParameters> command;

    @Before
    public void setUp() {
        doReturn(parameters).when(command).getParameters();
    }

    @Test
    public void testGetDeviceListWithNoDeviceList() {
        List<LUNs> deviceList = new LinkedList<>(Collections.singletonList(new LUNs()));
        doReturn(deviceList).when(command).runGetDeviceList(any());
        assertEquals(deviceList, command.getDeviceList());
    }

    @Test
    public void testGetDeviceListWithDeviceListAndNoLunsIds() {
        List<LUNs> deviceList = new LinkedList<>(Collections.singletonList(new LUNs()));
        parameters.setDeviceList(deviceList);
        assertEquals(deviceList, command.getDeviceList());
    }

    @Test
    public void testGetDeviceListWithDeviceListAndLunsIds() {
        LUNs lun1 = new LUNs();
        lun1.setId("lun1");
        LUNs lun2 = new LUNs();
        lun2.setId("lun2");

        parameters.setDeviceList(Arrays.asList(lun1, lun2));
        List<String> lunsIds = Collections.singletonList("lun2");

        assertEquals(command.getDeviceList(lunsIds), Collections.singletonList(lun2));
    }
}
