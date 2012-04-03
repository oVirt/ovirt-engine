package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class GetDeviceListVDSCommand<P extends GetDeviceListVDSCommandParameters> extends VdsBrokerCommand<P> {

    protected static final String DEVTYPE_VALUE_FCP = "fcp";
    protected static final String DEVTYPE_FIELD = "devtype";

    private LUNListReturnForXmlRpc _result;

    public GetDeviceListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        _result = getBroker()
                .getDeviceList(
                        (int) getParameters().getStorageType().getValue());
        ProceedProxyReturnValue();
        setReturnValue(ParseLUNList(_result.lunList));
    }

    public static java.util.ArrayList<LUNs> ParseLUNList(XmlRpcStruct[] lunList) {
        java.util.ArrayList<LUNs> result = new java.util.ArrayList<LUNs>(lunList.length);
        for (XmlRpcStruct xlun : (XmlRpcStruct[]) lunList) {
            result.add(ParseLunFromXmlRpc(xlun));
        }
        return result;
    }

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
        if (xlun.contains("pathstatus")) {
            Object[] temp = (Object[]) xlun.getItem("pathstatus");
            XmlRpcStruct[] pathStatus = null;
            if (temp != null) {
                pathStatus = new XmlRpcStruct[temp.length];
                for (int i = 0; i < temp.length; i++) {
                    pathStatus[i] = new XmlRpcStruct((Map<String, Object>) temp[i]);
                }
                for (XmlRpcStruct xcon : pathStatus) {
                    if (xcon.contains("lun")) {
                        lun.setLunMapping(Integer.parseInt(xcon.getItem("lun").toString()));
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
        lun.setLunConnections(new java.util.ArrayList<storage_server_connections>());
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
        if (xlun.contains("pathstatus")) {
            lun.setPathsDictionary(new java.util.HashMap<String, Boolean>());
            Object[] temp = (Object[]) xlun.getItem("pathstatus");
            XmlRpcStruct[] pathStatus = null;
            if (temp != null) {
                pathStatus = new XmlRpcStruct[temp.length];
                for (int i = 0; i < temp.length; i++) {
                    pathStatus[i] = new XmlRpcStruct((Map<String, Object>) temp[i]);
                }
                for (XmlRpcStruct xpath : pathStatus) {
                    if (xpath.contains("physdev") && xpath.contains("state")) {
                        // set name and state - if active true, otherwise false
                        lun.getPathsDictionary().put(xpath.getItem("physdev").toString(),
                                StringHelper.EqOp(xpath.getItem("state").toString(), "active") ? true : false);
                    }
                }
            }
        }
        if (xlun.contains(DEVTYPE_FIELD)) {
            String devtype = xlun.getItem(DEVTYPE_FIELD).toString();
            if (!DEVTYPE_VALUE_FCP.equalsIgnoreCase(devtype)) {
                lun.setLunType(StorageType.ISCSI);
            }
        }
        return lun;
    }

    public static storage_server_connections ParseConnection(XmlRpcStruct xcon) {
        storage_server_connections con = new storage_server_connections();
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
