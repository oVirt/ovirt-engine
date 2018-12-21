package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.LunStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;

@SuppressWarnings({ "rawtypes", "unchecked" })
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
    protected static final String DEVICE_PATH_CAPACITY_FIELD = "capacity";

    @Inject
    private StoragePoolDao storagePoolDao;

    private LUNListReturn _result;

    public GetDeviceListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        int storageType = getParameters().getStorageType().getValue();
        Set<String> lunsIdList = getParameters().getLunIds();
        String[] lunsIdArray = lunsIdList != null ? lunsIdList.toArray(new String[lunsIdList.size()]) : null;
        _result = getBroker().getDeviceList(storageType, lunsIdArray, getParameters().isCheckStatus());

        proceedProxyReturnValue();
        Version compatibilityVersion = storagePoolDao.getForVds(getParameters().getVdsId()).getCompatibilityVersion();
        setReturnValue(parseLUNList(_result.lunList, compatibilityVersion));
    }

    public static ArrayList<LUNs> parseLUNList(Map<String, Object>[] lunList, Version compatibilityVersion) {
        ArrayList<LUNs> result = new ArrayList<>(lunList.length);
        for (Map<String, Object> xlun : lunList) {
            result.add(parseLun(xlun, compatibilityVersion));
        }
        return result;
    }

    protected static LUNs parseLun(Map<String, Object> xlun, Version compatibilityVersion) {
        LUNs lun = new LUNs();
        if (xlun.containsKey("GUID")) {
            lun.setLUNId(xlun.get("GUID").toString());
        }
        if (xlun.containsKey("pvUUID")) {
            lun.setPhysicalVolumeId(xlun.get("pvUUID").toString());
        }
        if (xlun.containsKey("vgUUID")) {
            lun.setVolumeGroupId(xlun.get("vgUUID").toString());
        } else {
            lun.setVolumeGroupId("");
        }
        if (xlun.containsKey("vgName")) {
            lun.setStorageDomainId(Guid.createGuidFromString(xlun.get("vgName").toString()));
        }
        if (xlun.containsKey("serial")) {
            lun.setSerial(xlun.get("serial").toString());
        }
        if (xlun.containsKey(PATHSTATUS)) {
            Object[] temp = (Object[]) xlun.get(PATHSTATUS);
            Map<String, Object>[] pathStatus = null;
            if (temp != null) {
                lun.setPathsDictionary(new HashMap<>());
                lun.setPathsCapacity(new HashMap<>());
                pathStatus = new Map[temp.length];
                for (int i = 0; i < temp.length; i++) {
                    pathStatus[i] = (Map<String, Object>) temp[i];
                }

                for (Map xcon : pathStatus) {
                    if (xcon.containsKey(LUN_FIELD)) {
                        lun.setLunMapping(Integer.parseInt(xcon.get(LUN_FIELD).toString()));
                    }

                    if (xcon.containsKey(PHYSICAL_DEVICE_FIELD) && xcon.containsKey(DEVICE_STATE_FIELD)) {
                        // set name and state - if active true, otherwise false
                        lun.getPathsDictionary()
                                .put(xcon.get(PHYSICAL_DEVICE_FIELD).toString(),
                                        DEVICE_ACTIVE_VALUE.equals(xcon.get(DEVICE_STATE_FIELD).toString()));
                    }
                    if (xcon.containsKey(PHYSICAL_DEVICE_FIELD) && xcon.containsKey(DEVICE_PATH_CAPACITY_FIELD)) {
                        // set name and capacity
                        Long size = IrsBrokerCommand.assignLongValue(xcon, DEVICE_PATH_CAPACITY_FIELD);
                        if (size != null) {
                            lun.getPathsCapacity()
                                    .put(xcon.get(PHYSICAL_DEVICE_FIELD).toString(),
                                            SizeConverter.convert(size,
                                                    SizeConverter.SizeUnit.BYTES, SizeConverter.SizeUnit.GiB)
                                                    .intValue());
                        }
                    }
                }
            }
        }
        if (xlun.containsKey("vendorID")) {
            lun.setVendorId(xlun.get("vendorID").toString());
        }
        if (xlun.containsKey("productID")) {
            lun.setProductId(xlun.get("productID").toString());
        }
        lun.setLunConnections(new ArrayList<>());
        if (xlun.containsKey("pathlist")) {
            Object[] temp = (Object[]) xlun.get("pathlist");
            Map[] pathList = null;
            if (temp != null) {
                pathList = new Map[temp.length];
                for (int i = 0; i < temp.length; i++) {
                    pathList[i] = (Map<String, Object>) temp[i];
                }
                for (Map xcon : pathList) {
                    lun.getLunConnections().add(parseConnection(xcon));
                }
            }
        }
        Long size = IrsBrokerCommand.assignLongValue(xlun, "devcapacity");
        if (size == null) {
            size = IrsBrokerCommand.assignLongValue(xlun, "capacity");
        }
        if (size != null) {
            lun.setDeviceSize((int) (size / SizeConverter.BYTES_IN_GB));
        }
        if (xlun.containsKey("pvsize")) {
            String pvSizeStr = (String) xlun.get("pvsize");
            if (!StringUtils.isEmpty(pvSizeStr)) {
                Long pvSize = IrsBrokerCommand.assignLongValue(xlun, "pvsize");
                if (pvSize != null) {
                    lun.setPvSize(SizeConverter.convert(pvSize,
                            SizeConverter.SizeUnit.BYTES, SizeConverter.SizeUnit.GiB).intValue());
                }
            }
        }

        if (xlun.containsKey("pe_count")) {
            lun.setPeCount(IrsBrokerCommand.assignLongValue(xlun, "pe_count"));
        }

        if (xlun.containsKey("pe_alloc_count")) {
            lun.setPeAllocatedCount(IrsBrokerCommand.assignLongValue(xlun, "pe_alloc_count"));
        }

        if (xlun.containsKey("discard_max_bytes")) {
            lun.setDiscardMaxSize(((Number) xlun.get("discard_max_bytes")).longValue());
        }

        if (xlun.containsKey("vendorID")) {
            lun.setVendorName(xlun.get("vendorID").toString());
        }

        if (xlun.containsKey(DEVTYPE_FIELD)) {
            String devtype = xlun.get(DEVTYPE_FIELD).toString();
            if (DEVTYPE_VALUE_FCP.equalsIgnoreCase(devtype)) {
                lun.setLunType(StorageType.FCP);
            } else {
                lun.setLunType(StorageType.ISCSI);
            }
        }
        if (xlun.containsKey(STATUS)) {
            String status = xlun.get(STATUS).toString();
            lun.setStatus(EnumUtils.valueOf(LunStatus.class, status, true));
        }
        return lun;
    }

    public static StorageServerConnections parseConnection(Map<String, Object> xcon) {
        StorageServerConnections con = new StorageServerConnections();
        if (xcon.containsKey("connection")) {
            con.setConnection(xcon.get("connection").toString());
        }
        if (xcon.containsKey("portal")) {
            con.setPortal(xcon.get("portal").toString());
        }
        if (xcon.containsKey("port")) {
            con.setPort(xcon.get("port").toString());
        }
        if (xcon.containsKey("iqn")) {
            con.setIqn(xcon.get("iqn").toString());
        }
        if (xcon.containsKey("user")) {
            con.setUserName(xcon.get("user").toString());
        }
        if (xcon.containsKey("password")) {
            con.setPassword(xcon.get("password").toString());
        }
        return con;
    }

    @Override
    protected Status getReturnStatus() {
        return _result.getStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }
}
