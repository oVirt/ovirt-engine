package org.ovirt.engine.core.common.queries;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.AdUser;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.RepoFileMetaData;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.TabType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.common.businessentities.bookmarks;
import org.ovirt.engine.core.common.businessentities.event_map;
import org.ovirt.engine.core.common.businessentities.event_notification_methods;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.core.common.businessentities.roles_actions;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.businessentities.tags_vm_map;
import org.ovirt.engine.core.common.businessentities.vm_pool_map;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.Version;

/**
 * Query return value class, If inheriting from this class add logic to QueriesCommandBase class.
 */
@XmlSeeAlso({
        // java.util.HashMap<String, int>.class,
        tags.class,
        tags_vm_map.class,
        // java.util.ArrayList<tags_vm_map>.class,
        // java.util.ArrayList<tags>.class,
        bookmarks.class,
        // java.util.ArrayList<bookmarks>.class,
        vm_pools.class,
        VM.class,
        VmStatic.class,
        VmDynamic.class,
        VmStatistics.class,
        // java.util.ArrayList<String>.class,
        vm_pool_map.class,
        // java.util.ArrayList<vm_pool_map>.class,
        VDSGroup.class,
        // java.util.ArrayList<VDSGroup>.class,
        VDS.class,
        VdsDynamic.class,
        VdsStatic.class,
        VdsStatistics.class,
        // java.util.ArrayList<VDS>.class,
        VdsStatic.class,
        // java.util.ArrayList<VdsStatic>.class,
        VM.class,
        VmStatic.class,
        // java.util.ArrayList<VM>.class,
        VmTemplate.class,
        // java.util.ArrayList<VmTemplate>.class,
        DbUser.class,
        // java.util.ArrayList<DbUser>.class,
        ad_groups.class,
        // java.util.ArrayList<ad_groups>.class,
        vm_pools.class,
        // java.util.ArrayList<vm_pools>.class,
        // java.util.ArrayList<GetAllVmSnapshotsByDriveQueryReturnValue>.class,
        TabType.class,
        AuditLog.class,
        RepoFileMetaData.class,
        // java.util.ArrayList<AuditLog>.class,
        AdUser.class,
        // java.util.ArrayList<AdUser>.class,
        // IVdcQueryable.class,
        // java.util.ArrayList<IVdcQueryable>.class,
        SearchReturnValue.class,
        LicenseReturnValue.class,
        GetAllVmSnapshotsByDriveQueryReturnValue.class,
        // java.util.HashMap<String, java.util.ArrayList<DiskImage>>.class,
        // java.util.ArrayList<DiskImage>.class,
        DiskImage.class,
        // java.util.HashMap<String, String>.class,
        // java.util.Map.Entry<String, String>.class,
        ImportCandidateInfoBase.class,
        VmCandidateInfo.class,
        TemplateCandidateInfo.class,
        ImportCandidateSourceEnum.class,
        // java.util.HashMap<ImportCandidateSourceEnum,
        // java.util.HashMap<String, ImportCandidateInfoBase>>.class,
        // java.util.HashMap<String, ImportCandidateInfoBase>.class,
        ServerCpu.class,
        // java.util.ArrayList<ServerCpu>.class,
        roles.class,
        // java.util.ArrayList<roles>.class,
        roles_actions.class,
        // java.util.ArrayList<roles_actions>.class,
        VdsNetworkInterface.class,
        VmNetworkInterface.class,
        // java.util.ArrayList<Interface>.class,
        network.class,
        // java.util.ArrayList<network>.class,
        permissions.class,
        // java.util.ArrayList<permissions>.class,
        storage_pool.class,
        // java.util.ArrayList<storage_pool>.class,
        storage_domains.class,
        // java.util.ArrayList<storage_domains>.class,
        storage_domain_static.class,
        // java.util.ArrayList<storage_domain_static>.class,
        storage_server_connections.class,
        // java.util.ArrayList<storage_server_connections>.class,
        event_notification_methods.class,
        // java.util.ArrayList<event_notification_methods>.class,
        LUNs.class,
        // java.util.ArrayList<LUNs>.class,
        DiskImageBase.class,
        // java.util.ArrayList<DiskImageBase>.class,
        event_map.class,
        // java.util.ArrayList<event_map>.class,
        AsyncTaskStatus.class,
        // java.util.ArrayList<AsyncTaskStatus>.class,
        event_subscriber.class,
        // java.util.ArrayList<event_subscriber>.class,
        // VDSReturnValue.class,
        // java.util.HashMap<VmTemplate, java.util.ArrayList<DiskImage>>.class,
        ListIVdcQueryableUpdatedData.class, KeyValuePairCompat.class, DiskImageList.class,
        FenceStatusReturnValue.class, Version.class
// java.util.ArrayList<Version>.class
})
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdcQueryReturnValue")
public class VdcQueryReturnValue implements Serializable {
    private static final long serialVersionUID = -7737597005584540780L;

    private boolean _succeeded;
    private String _exceptionString;
    private Object returnValue;

    @XmlElement(name = "ReturnValueWrapper")
    public ValueObject getSerializaedReturnValue() {
        return ValueObject.createValueObject(returnValue);
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object value) {
        returnValue = value;
    }

    @XmlElement(name = "ExceptionString")
    public String getExceptionString() {
        return _exceptionString;
    }

    public void setExceptionString(String value) {
        _exceptionString = value;
    }

    @XmlElement(name = "Succeeded")
    public boolean getSucceeded() {
        return _succeeded;
    }

    public void setSucceeded(boolean value) {
        _succeeded = value;
    }

    public VdcQueryReturnValue() {
    }
}
