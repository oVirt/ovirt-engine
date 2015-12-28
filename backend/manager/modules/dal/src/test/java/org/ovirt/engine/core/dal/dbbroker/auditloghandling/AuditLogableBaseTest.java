package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;

public class AuditLogableBaseTest {

    protected static final Guid GUID = new Guid("11111111-1111-1111-1111-111111111111");
    protected static final Guid GUID2 = new Guid("11111111-1111-1111-1111-111111111112");
    protected static final Guid GUID3 = new Guid("11111111-1111-1111-1111-111111111113");
    protected static final String NAME = "testName";
    protected static final String DOMAIN = "testDomain";

    @Test
    public void nGuidCtor() {
        final AuditLogableBase b = new AuditLogableBase(GUID);
        final Guid v = b.getVdsId();
        assertEquals(GUID, v);
    }

    @Test
    public void nGuidCtorNull() {
        final Guid n = null;
        final AuditLogableBase b = new AuditLogableBase(n);
        final Guid g = b.getVdsId();
        assertEquals(Guid.Empty, g);
    }

    @Test
    public void nGuidGuidCtor() {
        final AuditLogableBase b = new AuditLogableBase(GUID, GUID2);
        final Guid g = b.getVdsId();
        assertEquals(GUID, g);
        final Guid gu = b.getVmId();
        assertEquals(GUID2, gu);
    }

    @Test
    public void nGuidGuidCtorNullNGuid() {
        final AuditLogableBase b = new AuditLogableBase(null, GUID2);
        final Guid g = b.getVdsId();
        assertEquals(Guid.Empty, g);
        final Guid gu = b.getVmId();
        assertEquals(GUID2, gu);
    }

    @Test
    public void nGuidGuidCtorNullGuid() {
        final AuditLogableBase b = new AuditLogableBase(GUID, null);
        final Guid g = b.getVdsId();
        assertEquals(GUID, g);
        final Guid gu = b.getVmId();
        assertEquals(Guid.Empty, gu);
    }

    @Test
    public void nGuidGuidCtorNull() {
        final AuditLogableBase b = new AuditLogableBase(null, null);
        final Guid g = b.getVdsId();
        assertEquals(Guid.Empty, g);
        final Guid gu = b.getVmId();
        assertEquals(Guid.Empty, gu);
    }

    @Test
    public void getUserIdDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final Guid g = b.getUserId();
        assertEquals(Guid.Empty, g);
    }

    @Test
    public void getUserIdIdSet() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setUserId(GUID);
        final Guid g = b.getUserId();
        assertEquals(GUID, g);
    }

    @Test
    public void getUserIdVdcUserDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final DbUser u = new DbUser();
        b.setCurrentUser(u);
        final Guid g = b.getUserId();
        assertEquals(null, g);
    }

    @Test
    public void getUserIdVdcUserId() {
        final AuditLogableBase b = new AuditLogableBase();
        final DbUser u = new DbUser();
        u.setId(GUID);
        b.setCurrentUser(u);
        final Guid g = b.getUserId();
        assertEquals(GUID, g);
    }

    @Test
    public void getUserNameDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final String n = b.getUserName();
        assertNull(n);
    }

    @Test
    public void getUserNameNull() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setUserName(null);
        final String n = b.getUserName();
        assertNull(n);
    }

    @Test
    public void getUserName() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setUserName(NAME);
        final String n = b.getUserName();
        assertEquals(NAME, n);
    }

    @Test
    public void getUserNameFromUser() {
        final AuditLogableBase b = new AuditLogableBase();
        final DbUser u = new DbUser();
        u.setLoginName(NAME);
        u.setDomain(DOMAIN);
        b.setCurrentUser(u);
        final String un = b.getUserName();
        assertEquals(String.format("%s@%s", NAME, DOMAIN), un);
    }

    @Test
    public void currentUserDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final DbUser u = b.getCurrentUser();
        assertNull(u);
    }

    @Test
    public void currentUserNull() {
        final AuditLogableBase b = new AuditLogableBase();
        final DbUser u = null;
        b.setCurrentUser(u);
        final DbUser cu = b.getCurrentUser();
        assertEquals(u, cu);
    }

    @Test
    public void currentUser() {
        final AuditLogableBase b = new AuditLogableBase();
        final DbUser u = new DbUser();
        b.setCurrentUser(u);
        final DbUser cu = b.getCurrentUser();
        assertEquals(u, cu);
    }

    @Test
    public void vmTemplateIdDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final Guid g = b.getVmTemplateId();
        assertEquals(Guid.Empty, g);
    }

    @Test
    public void vmTemplateId() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVmTemplateId(GUID);
        final Guid g = b.getVmTemplateId();
        assertEquals(GUID, g);
    }

    @Test
    public void vmTemplateIdRefDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final Guid g = b.getVmTemplateIdRef();
        assertNull(g);
    }

    @Test
    public void vmTemplateIdRef() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVmTemplateId(GUID);
        final Guid g = b.getVmTemplateIdRef();
        assertEquals(GUID, g);
    }

    @Test
    public void vmTemplateIdRefWithVm() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final VM v = new VM();
        b.setVm(v);
        final Guid g = b.getVmTemplateIdRef();
        assertEquals(GUID, g);
    }

    @Test
    public void vmTemplateNameDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final String n = b.getVmTemplateName();
        assertNull(n);
    }

    @Test
    public void vmTemplateName() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVmTemplateName(NAME);
        final String nm = b.getVmTemplateName();
        assertEquals(NAME, nm);
    }

    @Test
    public void vmTemplateNameWithVm() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final VM v = new VM();
        b.setVm(v);
        final String n = b.getVmTemplateName();
        assertEquals(NAME, n);
    }

    @Test
    public void vmIdDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final Guid i = b.getVmId();
        assertEquals(Guid.Empty, i);
    }

    @Test
    public void vmIdNull() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVmId(null);
        final Guid i = b.getVmId();
        assertEquals(Guid.Empty, i);
    }

    @Test
    public void vmId() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVmId(GUID);
        final Guid i = b.getVmId();
        assertEquals(GUID, i);
    }

    @Test
    public void snapshotNameDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final String s = b.getSnapshotName();
        assertNull(s);
    }

    @Test
    public void snapshotNameNull() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setSnapshotName(null);
        final String s = b.getSnapshotName();
        assertNull(s);
    }

    @Test
    public void snapshotNameEmpty() {
        final AuditLogableBase b = new AuditLogableBase();
        final String e = "";
        b.setSnapshotName(e);
        final String s = b.getSnapshotName();
        assertEquals(e, s);
    }

    @Test
    public void snapshotName() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setSnapshotName(NAME);
        final String s = b.getSnapshotName();
        assertEquals(NAME, s);
    }

    @Test
    public void vmIdRefDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final Guid g = b.getVmIdRef();
        assertEquals(Guid.Empty, g);
    }

    @Test
    public void vmIdRefNullVmId() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVmId(null);
        final Guid g = b.getVmIdRef();
        assertNull(g);
    }

    @Test
    public void vmIdRefNullVm() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVmId(null);
        final VM v = new VM();
        v.setId(GUID);
        b.setVm(v);
        final Guid g = b.getVmIdRef();
        assertEquals(GUID, g);
    }

    @Test
    public void vmNameDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final String n = b.getVmName();
        assertNull(n);
    }

    @Test
    public void vmNameNull() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVmName(null);
        final String n = b.getVmName();
        assertNull(n);
    }

    @Test
    public void vmNameNullVm() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVmName(null);
        final VM v = new VM();
        v.setName(NAME);
        b.setVm(v);
        final String n = b.getVmName();
        assertEquals(NAME, n);
    }

    @Test
    public void vmName() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVmName(NAME);
        final String n = b.getVmName();
        assertEquals(NAME, n);
    }

    @Test
    public void vdsIdRefDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final Guid g = b.getVdsIdRef();
        assertNull(g);
    }

    @Test
    public void vdsIdRefNull() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVdsIdRef(null);
        final Guid g = b.getVdsIdRef();
        assertNull(g);
    }

    @Test
    public void vdsIdRef() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVdsIdRef(GUID);
        final Guid g = b.getVdsIdRef();
        assertEquals(GUID, g);
    }

    @Test
    public void vdsIdRefVds() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVdsIdRef(null);
        final VDS v = new VDS();
        v.setId(GUID);
        b.setVds(v);
        final Guid g = b.getVdsIdRef();
        assertEquals(GUID, g);
    }

    @Test
    public void vdsIdDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final Guid g = b.getVdsId();
        assertEquals(Guid.Empty, g);
    }

    @Test
    public void vdsIdNull() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVdsId(null);
        final Guid g = b.getVdsId();
        assertEquals(Guid.Empty, g);
    }

    @Test
    public void vdsId() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVdsId(GUID);
        final Guid g = b.getVdsId();
        assertEquals(GUID, g);
    }

    @Test
    public void vdsNameDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final String s = b.getVdsName();
        assertNull(s);
    }

    @Test
    public void vdsNameNull() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVdsName(null);
        final String s = b.getVdsName();
        assertNull(s);
    }

    @Test
    public void vdsName() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVdsName(NAME);
        final String s = b.getVdsName();
        assertEquals(NAME, s);
    }

    @Test
    public void vdsNameVds() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVdsName(null);
        final VDS v = new VDS();
        v.setVdsName(NAME);
        b.setVds(v);
        final String s = b.getVdsName();
        assertEquals(NAME, s);
    }

    @Test
    public void storageDomainDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final StorageDomain s = b.getStorageDomain();
        assertNull(s);
    }

    @Test
    public void storageDomainNull() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setStorageDomain(null);
        final StorageDomain s = b.getStorageDomain();
        assertNull(s);
    }

    @Test
    public void storageDomain() {
        final AuditLogableBase b = new AuditLogableBase();
        final StorageDomain s = new StorageDomain();
        b.setStorageDomain(s);
        final StorageDomain st = b.getStorageDomain();
        assertEquals(s, st);
    }

    @Test
    public void storageDomainWithId() {
        final TestAuditLogableBase b = new TestAuditLogableBase();
        b.setStorageDomainId(GUID);
        b.setStoragePoolId(GUID);
        final StorageDomain s = b.getStorageDomain();
        assertEquals(b.STORAGE_DOMAIN, s);
    }

    @Test
    public void storageDomainWithIdNullPool() {
        final TestAuditLogableBase b = new TestAuditLogableBase();
        b.setStorageDomainId(GUID);
        b.setStoragePoolId(GUID2);
        final StorageDomain s = b.getStorageDomain();
        assertNull(s);
    }

    @Test
    public void storageDomainWithNullId() {
        final TestAuditLogableBase b = new TestAuditLogableBase();
        b.setStorageDomainId(GUID2);
        final StorageDomain s = b.getStorageDomain();
        assertEquals(b.STORAGE_DOMAIN, s);
    }

    @Test
    public void storageDomainIdDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final Guid g = b.getStorageDomainId();
        assertNull(g);
    }

    @Test
    public void storageDomainIdNull() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setStorageDomainId(null);
        final Guid g = b.getStorageDomainId();
        assertNull(g);
    }

    @Test
    public void storageDomainId() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setStorageDomainId(GUID);
        final Guid g = b.getStorageDomainId();
        assertEquals(GUID, g);
    }

    @Test
    public void storageDomainIdWithStorageDomain() {
        final AuditLogableBase b = new AuditLogableBase();
        final StorageDomain s = new StorageDomain();
        s.setId(GUID);
        b.setStorageDomain(s);
        final Guid g = b.getStorageDomainId();
        assertEquals(GUID, g);
    }

    @Test
    public void storageDomainNameDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final String s = b.getStorageDomainName();
        assertEquals("", s);
    }

    @Test
    public void storageDomainName() {
        final AuditLogableBase b = new AuditLogableBase();
        final StorageDomain s = new StorageDomain();
        s.setStorageName(NAME);
        b.setStorageDomain(s);
        final String n = b.getStorageDomainName();
        assertEquals(NAME, n);
    }

    @Test
    public void storagePoolDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final StoragePool p = b.getStoragePool();
        assertNull(p);
    }

    @Test
    public void storagePoolWithId() {
        final AuditLogableBase b = new TestAuditLogableBase();
        b.setStoragePoolId(GUID);
        final StoragePool p = b.getStoragePool();
        assertNotNull(p);
    }

    @Test
    public void storagePool() {
        final AuditLogableBase b = new AuditLogableBase();
        final StoragePool p = new StoragePool();
        b.setStoragePool(p);
        final StoragePool sp = b.getStoragePool();
        assertEquals(p, sp);
    }

    @Test
    public void storagePoolIdDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final Guid n = b.getStoragePoolId();
        assertNull(n);
    }

    @Test
    public void storagePoolIdNull() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setStoragePoolId(null);
        final Guid n = b.getStoragePoolId();
        assertNull(n);
    }

    @Test
    public void storagePoolId() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setStoragePoolId(GUID);
        final Guid n = b.getStoragePoolId();
        assertEquals(GUID, n);
    }

    @Test
    public void storagePoolIdWithStoragePool() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setStoragePoolId(null);
        final StoragePool p = new StoragePool();
        p.setId(GUID);
        b.setStoragePool(p);
        final Guid n = b.getStoragePoolId();
        assertEquals(GUID, n);
    }

    @Test
    public void storagePoolIdWithStorageDomain() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setStoragePoolId(null);
        b.setStoragePool(null);
        final StorageDomain s = new StorageDomain();
        s.setStoragePoolId(GUID);
        b.setStorageDomain(s);
        final Guid n = b.getStoragePoolId();
        assertEquals(GUID, n);
    }

    @Test
    public void storagePoolNameDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final String s = b.getStoragePoolName();
        assertEquals("", s);
    }

    @Test
    public void storagePoolName() {
        final AuditLogableBase b = new AuditLogableBase();
        final StoragePool p = new StoragePool();
        p.setName(NAME);
        b.setStoragePool(p);
        final String s = b.getStoragePoolName();
        assertEquals(NAME, s);
    }

    @Test
    public void auditLogTypeValue() {
        final AuditLogableBase b = new AuditLogableBase();
        final AuditLogType t = b.getAuditLogTypeValue();
        assertEquals(AuditLogType.UNASSIGNED, t);
    }

    @Test
    public void getVdsDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final VDS v = b.getVds();
        assertNull(v);
    }

    @Test
    public void getVdsNullAll() {
        final AuditLogableBase b = new AuditLogableBase();
        final VDS vds = null;
        final VM vm = null;
        final Guid vdsId = null;
        b.setVds(vds);
        b.setVdsId(vdsId);
        b.setVm(vm);
        final VDS v = b.getVds();
        assertNull(v);
    }

    @Test
    public void getVdsNullVdsId() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final VDS vds = null;
        final VM vm = new VM();
        vm.setRunOnVds(GUID3);
        final Guid vdsId = null;
        b.setVds(vds);
        b.setVdsId(vdsId);
        b.setVm(vm);
        final VDS v = b.getVds();
        assertNull(v);
    }

    @Test
    public void getVdsNullRun() {
        final AuditLogableBase b = new AuditLogableBase();
        final VDS vds = null;
        final VM vm = new VM();
        vm.setRunOnVds(null);
        final Guid vdsId = null;
        b.setVds(vds);
        b.setVdsId(vdsId);
        b.setVm(vm);
        final VDS v = b.getVds();
        assertNull(v);
    }

    @Test
    public void getVdsWithVds() {
        final AuditLogableBase b = new AuditLogableBase();
        final VDS vds = new VDS();
        final VM vm = null;
        final Guid vdsId = null;
        b.setVds(vds);
        b.setVdsId(vdsId);
        b.setVm(vm);
        final VDS v = b.getVds();
        assertEquals(vds, v);
    }

    @Test
    public void getVdsWithVdsId() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final VM vm = new VM();
        vm.setRunOnVds(GUID2);
        final Guid vdsId = GUID;
        b.setVdsId(vdsId);
        b.setVm(vm);
        final VDS v = b.getVds();
        assertEquals(GUID, v.getId());
    }

    @Test
    public void getVdsWithVm() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final VDS vds = null;
        final VM vm = new VM();
        vm.setRunOnVds(GUID2);
        final Guid vdsId = null;
        b.setVds(vds);
        b.setVdsId(vdsId);
        b.setVm(vm);
        final VDS v = b.getVds();
        assertEquals(GUID2, v.getId());
    }

    @Test
    public void getVdsSwallowsException() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final VDS vds = null;
        final VM vm = new VM();
        vm.setRunOnVds(GUID2);
        final Guid vdsId = GUID3;
        b.setVds(vds);
        b.setVdsId(vdsId);
        b.setVm(vm);
        final VDS v = b.getVds();
        assertNull(v);
    }

    @Test
    public void getVmDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final VM v = b.getVm();
        assertNull(v);
    }

    @Test
    public void getVm() {
        final AuditLogableBase b = new AuditLogableBase();
        final VM v = new VM();
        b.setVm(v);
        final VM vm = b.getVm();
        assertEquals(v, vm);
    }

    @Test
    public void getVmNullId() {
        final AuditLogableBase b = new AuditLogableBase();
        final VM v = null;
        b.setVm(v);
        b.setVmId(null);
        final VM vm = b.getVm();
        assertNull(vm);
    }

    @Test
    public void getVmEmptyId() {
        final AuditLogableBase b = new AuditLogableBase();
        final VM v = null;
        b.setVm(v);
        b.setVmId(Guid.Empty);
        final VM vm = b.getVm();
        assertNull(vm);
    }

    @Test
    public void getVmFromId() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final VM v = null;
        b.setVm(v);
        b.setVmId(GUID);
        final VM vm = b.getVm();
        assertNotNull(vm);
    }

    @Test
    public void getVmSwallowsExceptions() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final VM v = null;
        b.setVm(v);
        b.setVmId(GUID3);
        final VM vm = b.getVm();
        assertNull(vm);
    }

    @Test
    public void getVmTemplateDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final VmTemplate t = b.getVmTemplate();
        assertNull(t);
    }

    @Test
    public void getVmTemplateNull() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVmTemplate(null);
        final VmTemplate t = b.getVmTemplate();
        assertNull(t);
    }

    @Test
    public void getVmTemplateWithId() {
        final AuditLogableBase b = new TestAuditLogableBase();
        b.setVmTemplate(null);
        b.setVmTemplateId(GUID);
        final VmTemplate t = b.getVmTemplate();
        assertNotNull(t);
    }

    @Test
    public void getVmTemplateWithVm() {
        final AuditLogableBase b = new TestAuditLogableBase();
        b.setVmTemplate(null);
        b.setVmTemplateId(null);
        final VM vm = new VM();
        vm.setVmtGuid(GUID);
        b.setVm(vm);
        final VmTemplate t = b.getVmTemplate();
        assertNotNull(t);
    }

    @Test
    public void getClusterIdDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final Guid g = b.getClusterId();
        assertEquals(Guid.Empty, g);
    }

    @Test
    public void getClusterId() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setClusterId(GUID);
        final Guid g = b.getClusterId();
        assertEquals(GUID, g);
    }

    @Test
    public void getClusterIdCluster() {
        final AuditLogableBase b = new AuditLogableBase();
        final Cluster gr = new Cluster();
        gr.setId(GUID);
        b.setCluster(gr);
        final Guid g = b.getClusterId();
        assertEquals(GUID, g);
    }

    @Test
    public void getClusterDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final Cluster g = b.getCluster();
        assertNull(g);
    }

    @Test
    public void getClusterNotNull() {
        final AuditLogableBase b = new AuditLogableBase();
        final Cluster g = new Cluster();
        b.setCluster(g);
        final Cluster gr = b.getCluster();
        assertEquals(g, gr);
    }

    @Test
    public void getClusterWithId() {
        final AuditLogableBase b = new TestAuditLogableBase();
        b.setClusterId(GUID);
        final Cluster g = b.getCluster();
        assertEquals(GUID, g.getId());
    }

    @Test
    public void getClusterWithVds() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final VDS v = new VDS();
        v.setClusterId(GUID);
        b.setVds(v);
        final Cluster g = b.getCluster();
        assertEquals(GUID, g.getId());
    }

    @Test
    public void getClusterWithVm() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final VM v = new VM();
        v.setClusterId(GUID);
        b.setVm(v);
        final Cluster g = b.getCluster();
        assertEquals(GUID, g.getId());
    }

    @Test
    public void getClusterNameDefault() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final String n = b.getClusterName();
        assertEquals("", n);
    }

    @Test
    public void getClusterNameNullVds() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final Cluster g = null;
        b.setCluster(g);
        final String n = b.getClusterName();
        assertEquals("", n);
    }

    @Test
    public void getClusterName() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final Cluster g = new Cluster();
        g.setName(NAME);
        b.setCluster(g);
        final String n = b.getClusterName();
        assertEquals(NAME, n);
    }

    @Test(expected = NullPointerException.class)
    public void addCustomValueDoesNotHandleNullKeys() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final String key = null;
        final String value = NAME;
        b.addCustomValue(key, value);
        final String v = b.getCustomValue(key);
        assertEquals(value, v);
    }

    @Test
    public void addCustomValueWillNotReturnANull() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final String key = NAME;
        final String value = null;
        b.addCustomValue(key, value);
        final String v = b.getCustomValue(key);
        assertEquals("", v);
    }

    @Test
    public void customValue() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final String key = "foo";
        final String value = NAME;
        b.addCustomValue(key, value);
        final String v = b.getCustomValue(key);
        assertEquals(value, v);
    }

    @Test
    public void getCustomValuesLeaksInternalStructure() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final String key = "foo";
        final String value = NAME;
        b.addCustomValue(key, value);
        final String v = b.getCustomValue(key);
        assertEquals(value, v);
        final Map<String, String> m = b.getCustomValues();
        m.clear();
        final String s = b.getCustomValue(key);
        assertEquals("", s);
    }

    @Test
    public void appendCustomValue() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final String key = "foo";
        final String value = NAME;
        final String sep = "_";
        b.appendCustomValue(key, value, sep);
        final String s = b.getCustomValue(key);
        assertEquals(value, s);
    }

    @Test
    public void appendCustomValueAppend() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final String key = "foo";
        final String value = NAME;
        final String newVal = "bar";
        final String sep = "_";
        b.addCustomValue(key, value);
        b.appendCustomValue(key, newVal, sep);
        final String s = b.getCustomValue(key);
        assertEquals(value + sep + newVal, s);
    }

    @Test(expected = NullPointerException.class)
    public void appendCustomValueDoesntHandleNullKeys() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final String key = null;
        final String value = NAME;
        final String sep = "_";
        b.appendCustomValue(key, value, sep);
        final String s = b.getCustomValue(key);
        assertEquals(value, s);
    }

    @Test
    public void appendCustomValueAppendsWithNull() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final String key = "foo";
        final String value = null;
        final String newVal = "bar";
        final String sep = "_";
        b.addCustomValue(key, value);
        b.appendCustomValue(key, newVal, sep);
        final String s = b.getCustomValue(key);
        assertEquals(value + sep + newVal, s);
    }

    @Test
    public void appendCustomValueUsesNullSeparator() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final String key = "foo";
        final String value = NAME;
        final String newVal = "bar";
        final String sep = null;
        b.addCustomValue(key, value);
        b.appendCustomValue(key, newVal, sep);
        final String s = b.getCustomValue(key);
        assertEquals(value + sep + newVal, s);
    }

    @Test
    public void getCustomValueFromEmptyMap() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final String s = b.getCustomValue(NAME);
        assertEquals("", s);
    }

    @Test
    public void key() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final String s = b.getKey();
        assertEquals(AuditLogType.UNASSIGNED.toString(), s);
    }

    protected static class TestAuditLogableBase extends AuditLogableBase {
        public final StorageDomain STORAGE_DOMAIN = new StorageDomain();

        @Override
        public VmTemplateDao getVmTemplateDao() {
            final VmTemplateDao vt = mock(VmTemplateDao.class);
            final VmTemplate t = new VmTemplate();
            t.setId(GUID);
            t.setName(NAME);
            when(vt.get(Guid.Empty)).thenReturn(t);
            when(vt.get(GUID)).thenReturn(new VmTemplate());
            return vt;
        }

        @Override
        public VmDao getVmDao() {
            final VmDao v = mock(VmDao.class);
            when(v.get(GUID)).thenReturn(new VM());
            when(v.get(GUID3)).thenThrow(new RuntimeException());
            return v;
        }

        @Override
        public StorageDomainDao getStorageDomainDao() {
            final StorageDomainDao d = mock(StorageDomainDao.class);
            when(d.getForStoragePool(GUID, GUID)).thenReturn(STORAGE_DOMAIN);
            when(d.getAllForStorageDomain(GUID2)).thenReturn(getStorageDomainList());
            return d;
        }

        @Override
        public StoragePoolDao getStoragePoolDao() {
            final StoragePoolDao s = mock(StoragePoolDao.class);
            final StoragePool p = new StoragePool();
            p.setId(GUID);
            when(s.get(GUID)).thenReturn(p);
            when(s.get(GUID2)).thenReturn(null);
            return s;
        }

        @Override
        public VdsDao getVdsDao() {
            final VdsDao v = mock(VdsDao.class);
            final VDS vds1 = new VDS();
            vds1.setId(GUID);
            final VDS vds2 = new VDS();
            vds2.setId(GUID2);
            when(v.get(GUID)).thenReturn(vds1);
            when(v.get(GUID2)).thenReturn(vds2);
            when(v.get(GUID3)).thenThrow(new RuntimeException());
            return v;
        }

        @Override
        public ClusterDao getClusterDao() {
            final ClusterDao v = mock(ClusterDao.class);
            final Cluster g = new Cluster();
            g.setClusterId(GUID);
            when(v.get(GUID)).thenReturn(g);
            return v;
        }

        @Override
        public VmNetworkInterfaceDao getVmNetworkInterfaceDao() {
            return mock(VmNetworkInterfaceDao.class);
        }

        private List<StorageDomain> getStorageDomainList() {
            final List<StorageDomain> l = new ArrayList<>();
            final StorageDomain s = new StorageDomain();
            s.setStatus(StorageDomainStatus.Inactive);
            l.add(s);
            final StorageDomain s2 = new StorageDomain();
            s2.setStatus(null);
            l.add(s2);
            STORAGE_DOMAIN.setStatus(StorageDomainStatus.Active);
            l.add(STORAGE_DOMAIN);
            return l;
        }
    }
}
