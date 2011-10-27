package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class GetDeviceListVDSCommandTest {

    @Test
    public void parseLunFromXmlRpcReturnsIscsiByDefault() throws Exception {
        testParseLunFromXmlRpcForDevtypeField(StorageType.ISCSI, "");
    }

    @Test
    public void parseLunFromXmlRpcReturnsUnknownForFcp() throws Exception {
        testParseLunFromXmlRpcForDevtypeField(StorageType.UNKNOWN, GetDeviceListVDSCommand.DEVTYPE_VALUE_FCP);
    }

    @Test
    public void parseLunFromXmlRpcReturnsUnknownForNoField() throws Exception {
        XmlRpcStruct xlun = mock(XmlRpcStruct.class);
        when(xlun.contains(GetDeviceListVDSCommand.DEVTYPE_FIELD)).thenReturn(false);

        LUNs lun = GetDeviceListVDSCommand.ParseLunFromXmlRpc(xlun);

        assertEquals(StorageType.UNKNOWN, lun.getLunType());
    }

    /**
     * Test that ParseLunFromXmlRpc parses the {@link GetDeviceListVDSCommand#DEVTYPE_FIELD} correctly.
     *
     * @param expectedStorageType
     *            The storage type expected to return.
     * @param mockDevtype
     *            The value that the XML RPC will hold.
     */
    private void testParseLunFromXmlRpcForDevtypeField(StorageType expectedStorageType, String mockDevtype) {
        XmlRpcStruct xlun = mock(XmlRpcStruct.class);
        when(xlun.contains(GetDeviceListVDSCommand.DEVTYPE_FIELD)).thenReturn(true);
        when(xlun.getItem(GetDeviceListVDSCommand.DEVTYPE_FIELD)).thenReturn(mockDevtype);

        LUNs lun = GetDeviceListVDSCommand.ParseLunFromXmlRpc(xlun);

        assertEquals(expectedStorageType, lun.getLunType());
    }
}
