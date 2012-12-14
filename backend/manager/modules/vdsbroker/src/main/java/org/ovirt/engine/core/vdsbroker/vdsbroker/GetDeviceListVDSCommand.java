package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.LunStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class GetDeviceListVDSCommand<P extends GetDeviceListVDSCommandParameters> extends VdsBrokerCommand<P> {

    protected static final String DEVTYPE_VALUE_FCP = "fcp";
    protected static final String DEVTYPE_FIELD = "devtype";
    protected static final String STATUS = "status";

    /* Paths */
    protected static final String PATHSTATUS = "pathstatus";
    protected static final String LUN_FIELD = "lun";
    protected static final String DEVICE_ACTIVE_VALUE = "active";
    protected static final String DEVICE_STATE_FIELD = "state";
    protected static final String PHYSICAL_DEVICE_FIELD = "physdev";

    private LUNListReturnForXmlRpc _result;

    public GetDeviceListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        int storageType = getParameters().getStorageType().getValue();
        _result = getBroker().getDeviceList(storageType);

        ProceedProxyReturnValue();
        setReturnValue(ParseLUNList(_result.lunList));
    }

    public static ArrayList<LUNs> ParseLUNList(XmlRpcStruct[] lunList) {
        ArrayList<LUNs> result = new ArrayList<LUNs>(lunList.length);
        for (XmlRpcStruct xlun : lunList) {
            result.add(ParseLunFromXmlRpc(xlun));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static LUNs ParseLunFromXmlRpc(XmlRpcStruct xlun) {
        LUNs lun = new LUNs();
        if (xlun.contains("GUID")) {
            lun.setLUN_id(xlun.getItem("GUID").toString());
        }
        if (xlun.contains("pvUUID")) {
            lun.setphysical_volume_id(xlun.getItem("pvUUID").toString());
        }
        if (xlun.contains("vgUUID")) {
            lun.setvolume_group_id(xlun.getItem("vgUUID").toString());
        } else {
            lun.setvolume_group_id("");
        }
        if (xlun.contains("serial")) {
            lun.setSerial(xlun.getItem("serial").toString());
        }
        if (xlun.contains(PATHSTATUS)) {
            Object[] temp = (Object[]) xlun.getItem(PATHSTATUS);
            XmlRpcStruct[] pathStatus = null;
            if (temp != null) {
                lun.setPathsDictionary(new HashMap<String, Boolean>());
                pathStatus = new XmlRpcStruct[temp.length];
                for (int i = 0; i < temp.length; i++) {
                    pathStatus[i] = new XmlRpcStruct((Map<String, Object>) temp[i]);
                }

                for (XmlRpcStruct xcon : pathStatus) {
                    if (xcon.contains(LUN_FIELD)) {
                        lun.setLunMapping(Integer.parseInt(xcon.getItem(LUN_FIELD).toString()));
                    }

                    if (xcon.contains(PHYSICAL_DEVICE_FIELD) && xcon.contains(DEVICE_STATE_FIELD)) {
                        // set name and state - if active true, otherwise false
                        lun.getPathsDictionary()
                                .put(xcon.getItem(PHYSICAL_DEVICE_FIELD).toString(),
                                        DEVICE_ACTIVE_VALUE.equals(xcon.getItem(DEVICE_STATE_FIELD).toString()));
                    }
                }
            }
        }
        if (xlun.contains("vendorID")) {
            lun.setVendorId(xlun.getItem("vendorID").toString());
        }
        if (xlun.contains("productID")) {
            lun.setProductId(xlun.getItem("productID").toString());
        }
        lun.setLunConnections(new ArrayList<StorageServerConnections>());
        if (xlun.contains("pathlist")) {
            Object[] temp = (Object[]) xlun.getItem("pathlist");
            XmlRpcStruct[] pathList = null;
            if (temp != null) {
                pathList = new XmlRpcStruct[temp.length];
                for (int i = 0; i < temp.length; i++) {
                    pathList[i] = new XmlRpcStruct((Map<String, Object>) temp[i]);
                }
                for (XmlRpcStruct xcon : pathList) {
                    lun.getLunConnections().add(ParseConnection(xcon));
                }
            }
        }
        Long size = IrsBrokerCommand.AssignLongValue(xlun, "devcapacity");
        if (size == null) {
            size = IrsBrokerCommand.AssignLongValue(xlun, "capacity");
        }
        if (size != null) {
            lun.setDeviceSize((int) (size / IrsBrokerCommand.BYTES_TO_GB));
        }
        if (xlun.contains("vendorID")) {
            lun.setVendorName(xlun.getItem("vendorID").toString());
        }

        if (xlun.contains(DEVTYPE_FIELD)) {
            String devtype = xlun.getItem(DEVTYPE_FIELD).toString();
            if (!DEVTYPE_VALUE_FCP.equalsIgnoreCase(devtype)) {
                lun.setLunType(StorageType.ISCSI);
            }
        }
        if (xlun.contains(STATUS)) {
            String status = xlun.getItem(STATUS).toString();
            lun.setStatus(EnumUtils.valueOf(LunStatus.class, status, true));
        }
        return lun;
    }

    public static StorageServerConnections ParseConnection(XmlRpcStruct xcon) {
        StorageServerConnections con = new StorageServerConnections();
        if (xcon.contains("connection")) {
            con.setconnection(xcon.getItem("connection").toString());
        }
        if (xcon.contains("portal")) {
            con.setportal(xcon.getItem("portal").toString());
        }
        if (xcon.contains("port")) {
            con.setport(xcon.getItem("port").toString());
        }
        if (xcon.contains("iqn")) {
            con.setiqn(xcon.getItem("iqn").toString());
        }
        if (xcon.contains("user")) {
            con.setuser_name(xcon.getItem("user").toString());
        }
        if (xcon.contains("password")) {
            con.setpassword(xcon.getItem("password").toString());
        }
        return con;
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }
}
