package org.ovirt.engine.core.vdsbroker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.SpmStatusResult;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_domain_dynamic;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;
import org.ovirt.engine.core.common.vdscommands.ConnectStorageServerVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetStoragePoolInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetVmsInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SetStoragePoolDescriptionVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SpmStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.generic.DBConfigUtils;
import org.ovirt.engine.core.vdsbroker.proxy.ResourceManagerProxy;

public class ResourceManagerTest {

    // @Ignore
    @Test
    public void testResource() {
        try {
            System.out.println("before");
            IConfigUtilsInterface confInstance = new DBConfigUtils();
            Config.setConfigUtils(confInstance);
            ResourceManager.getInstance();
            // runCommands();
            // runSetStoragePoolDescriptionVDSCommand("java storage pool description");
            runGetVmsList();
            // runValidateStorageServerConnection();
            System.out.println("after");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runValidateStorageServerConnection() {
        List<storage_pool> nfsStoragePools = DbFacade.getInstance().getStoragePoolDAO().getAllOfType(StorageType.NFS);
        storage_pool pool = nfsStoragePools.get(0);

        List<storage_server_connections> connections = DbFacade.getInstance()
                .getStorageServerConnectionDAO().getAllForStoragePool(pool.getId());
        // TODO : Change vds id to first vds GUID
        ConnectStorageServerVDSCommandParameters params = new ConnectStorageServerVDSCommandParameters(Guid.Empty,
                pool.getId(), StorageType.NFS, connections);
        ResourceManagerProxy proxy = new ResourceManagerProxy();

        Object returnValue = proxy.runVdsCommand(VDSCommandType.ValidateStorageServerConnection, params)
                .getReturnValue();
        java.util.HashMap<String, Boolean> validateConnections = (java.util.HashMap<String, Boolean>) returnValue;
        for (Map.Entry<String, Boolean> tempEntry : validateConnections.entrySet()) {
            System.out.println("key = " + tempEntry.getKey());
            System.out.println("value = " + tempEntry.getValue());
        }

    }

    public void runSetStoragePoolDescriptionVDSCommand(String description) {
        List<storage_pool> nfsStoragePools = DbFacade.getInstance().getStoragePoolDAO().getAllOfType(StorageType.NFS);
        storage_pool pool = nfsStoragePools.get(0);

        ResourceManagerProxy proxy = new ResourceManagerProxy();
        SetStoragePoolDescriptionVDSCommandParameters params = new SetStoragePoolDescriptionVDSCommandParameters(
                pool.getId(), description);
        VDSReturnValue returnValue = proxy.runVdsCommand(VDSCommandType.SetStoragePoolDescription, params);
    }

    public void runGetVmsList() {
        List<storage_pool> nfsStoragePools = DbFacade.getInstance().getStoragePoolDAO().getAllOfType(StorageType.NFS);
        storage_pool pool = nfsStoragePools.get(0);
        List<storage_domains> storageDomain = DbFacade.getInstance()
                .getStorageDomainDAO().getAllForStoragePool(pool.getId());
        storage_domains domain = storageDomain.get(0);

        ResourceManagerProxy proxy = new ResourceManagerProxy();

        GetVmsInfoVDSCommandParameters TempVar2 = new GetVmsInfoVDSCommandParameters(pool.getId());
        TempVar2.setStorageDomainId(domain.getid());
        VDSReturnValue retVal = proxy.runVdsCommand(VDSCommandType.GetVmsList, TempVar2);
        String[] ids = (String[]) ((retVal.getReturnValue() instanceof String[]) ? retVal.getReturnValue() : null);
        for (String id : ids) {
            System.out.println("the vm id is-" + id);
        }
    }

    public void runCommands() {
        // VdsIdAndVdsVDSCommandParametersBase getStatsParams = new
        // VdsIdAndVdsVDSCommandParametersBase(1);
        // VDS vds = DbFacade.getInstance().GetVdsByVdsId(1);
        // getStatsParams.setVds(vds);
        // GetStatsVDSCommand command = new GetStatsVDSCommand(getStatsParams);
        // command.Execute();
        // VDSReturnValue returnValue = command.getVDSReturnValue();
        // boolean b = returnValue.getSucceeded();
        // System.out.println("command GetStatsVDSCommand "+b);

        ResourceManagerProxy proxy = new ResourceManagerProxy();

        List<storage_pool> nfsStoragePools = DbFacade.getInstance().getStoragePoolDAO().getAllOfType(StorageType.NFS);
        storage_pool pool = nfsStoragePools.get(0);
        // TODO : replace with real Guid
        SpmStatusVDSCommandParameters spmStatusParams = new SpmStatusVDSCommandParameters(Guid.Empty, pool.getId());
        VDSReturnValue returnValue = proxy.runVdsCommand(VDSCommandType.SpmStatus, spmStatusParams);
        SpmStatusResult statusResult = (SpmStatusResult) returnValue.getReturnValue();

        GetStoragePoolInfoVDSCommandParameters storagePoolInfoParam = new GetStoragePoolInfoVDSCommandParameters(
                pool.getId());
        returnValue = proxy.runVdsCommand(VDSCommandType.GetStoragePoolInfo, storagePoolInfoParam);
        KeyValuePairCompat<storage_pool, java.util.List<storage_domain_dynamic>> data =
                (KeyValuePairCompat<storage_pool, java.util.List<storage_domain_dynamic>>) returnValue
                        .getReturnValue();
        for (storage_domain_dynamic dynamicData : data.getValue()) {
            Guid g = dynamicData.getId();
            Integer i1 = dynamicData.getavailable_disk_size();
            Integer i2 = dynamicData.getused_disk_size();
        }

    }

    @Ignore
    @Test
    public void CreateCommand() {
        // SpmStatusVDSCommandParameters params = new
        // SpmStatusVDSCommandParameters();

        ResourceManager manager = ResourceManager.getInstance();
        manager.runVdsCommand(VDSCommandType.SpmStatus, null);
    }

    @Ignore
    @Test
    public void testSomthing() {
        Boolean b = true;
        Boolean f = false;
        Boolean f3 = false;

        List<Boolean> l = new ArrayList<Boolean>();
        l.add(b);
        l.add(f);
        l.add(f3);
        for (boolean temp : l) {
            System.out.println(temp);
        }
        Collections.sort(l);
        for (boolean temp : l) {
            System.out.println(temp);
        }
    }
}
