package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.junit.Assert.assertEquals;

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

    /**
     * Test that ParseLunFromXmlRpc parses the {@link GetDeviceListVDSCommand#DEVTYPE_FIELD} correctly.
     *
     * @param expectedStorageType
     *            The storage type expected to return.
     * @param mockDevtype
     *            The value that the XML RPC will hold.
     */
    private static void testParseLunFromXmlRpcForDevtypeField(StorageType expectedStorageType, String mockDevtype) {
        XmlRpcStruct xlun = new XmlRpcStruct();
        xlun.add(GetDeviceListVDSCommand.DEVTYPE_FIELD, mockDevtype);

        LUNs lun = GetDeviceListVDSCommand.ParseLunFromXmlRpc(xlun);

        assertEquals(expectedStorageType, lun.getLunType());
    }

    @Test
    public void parseLunFromXmlRpcReturnsUnknownForNoField() throws Exception {
        XmlRpcStruct xlun = new XmlRpcStruct();
        LUNs lun = GetDeviceListVDSCommand.ParseLunFromXmlRpc(xlun);

        assertEquals(StorageType.UNKNOWN, lun.getLunType());
    }
}
