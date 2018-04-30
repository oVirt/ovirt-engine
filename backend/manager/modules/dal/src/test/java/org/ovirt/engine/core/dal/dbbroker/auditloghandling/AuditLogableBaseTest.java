package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuditLogableBaseTest {

    protected static final Guid GUID = new Guid("11111111-1111-1111-1111-111111111111");
    protected static final Guid GUID2 = new Guid("11111111-1111-1111-1111-111111111112");
    protected static final Guid GUID3 = new Guid("11111111-1111-1111-1111-111111111113");
    protected static final String NAME = "testName";
    protected static final String DOMAIN = "testDomain";
    private static final StorageDomain STORAGE_DOMAIN = new StorageDomain();

    @Mock
    StorageDomainDao storageDomainDao;

    @Mock
    StoragePoolDao storagePoolDao;

    @Mock
    VmDao vmDao;

    @Mock
    VmTemplateDao vmTemplateDao;

    @Mock
    ClusterDao clusterDao;

    @Mock
    VdsDao vdsDao;

    @InjectMocks
    private AuditLogableBase b = new AuditLogableBase();

    @BeforeEach
    public void setUp() {
        when(vmDao.get(GUID)).thenReturn(new VM());
        when(vmDao.get(GUID3)).thenThrow(new RuntimeException());

        final VDS vds1 = new VDS();
        vds1.setId(GUID);
        final VDS vds2 = new VDS();
        vds2.setId(GUID2);
        when(vdsDao.get(GUID)).thenReturn(vds1);
        when(vdsDao.get(GUID2)).thenReturn(vds2);
        when(vdsDao.get(GUID3)).thenThrow(new RuntimeException());

        final StoragePool p = new StoragePool();
        p.setId(GUID);
        when(storagePoolDao.get(GUID)).thenReturn(p);

        final StorageDomain domain = new StorageDomain();
        domain.setStatus(StorageDomainStatus.Active);

        final StorageDomain sd1 = new StorageDomain();
        sd1.setStatus(StorageDomainStatus.Inactive);
        final StorageDomain sd2 = new StorageDomain();
        sd2.setStatus(null);
        final List<StorageDomain> storageDomainList = Arrays.asList(sd1, sd2, domain);
        when(storageDomainDao.getForStoragePool(GUID, GUID)).thenReturn(domain);
        when(storageDomainDao.getAllForStorageDomain(GUID2)).thenReturn(storageDomainList);

        final VmTemplate t = new VmTemplate();
        t.setId(GUID);
        t.setName(NAME);
        when(vmTemplateDao.get(Guid.Empty)).thenReturn(t);
        when(vmTemplateDao.get(GUID)).thenReturn(new VmTemplate());

        final Cluster g = new Cluster();
        g.setClusterId(GUID);
        when(clusterDao.get(GUID)).thenReturn(g);
    }

    @Test
    public void nGuidCtor() {
        b = new AuditLogableBase(GUID);
        final Guid v = b.getVdsId();
        assertEquals(GUID, v);
    }

    @Test
    public void nGuidCtorNull() {
        final Guid n = null;
        b = new AuditLogableBase(n);
        final Guid g = b.getVdsId();
        assertEquals(Guid.Empty, g);
    }

    @Test
    public void nGuidGuidCtor() {
        b = new AuditLogableBase(GUID, GUID2);
        final Guid g = b.getVdsId();
        assertEquals(GUID, g);
        final Guid gu = b.getVmId();
        assertEquals(GUID2, gu);
    }

    @Test
    public void nGuidGuidCtorNullNGuid() {
        b = new AuditLogableBase(null, GUID2);
        final Guid g = b.getVdsId();
        assertEquals(Guid.Empty, g);
        final Guid gu = b.getVmId();
        assertEquals(GUID2, gu);
    }

    @Test
    public void nGuidGuidCtorNullGuid() {
        b = new AuditLogableBase(GUID, null);
        final Guid g = b.getVdsId();
        assertEquals(GUID, g);
        final Guid gu = b.getVmId();
        assertEquals(Guid.Empty, gu);
    }

    @Test
    public void nGuidGuidCtorNull() {
        b = new AuditLogableBase(null, null);
        final Guid g = b.getVdsId();
        assertEquals(Guid.Empty, g);
        final Guid gu = b.getVmId();
        assertEquals(Guid.Empty, gu);
    }

    @Test
    public void getUserIdDefault() {
        final Guid g = b.getUserId();
        assertEquals(Guid.Empty, g);
    }

    @Test
    public void getUserIdIdSet() {
        b.setUserId(GUID);
        final Guid g = b.getUserId();
        assertEquals(GUID, g);
    }

    @Test
    public void getUserIdVdcUserDefault() {
        final DbUser u = new DbUser();
        b.setCurrentUser(u);
        final Guid g = b.getUserId();
        assertNull(g);
    }

    @Test
    public void getUserIdVdcUserId() {
        final DbUser u = new DbUser();
        u.setId(GUID);
        b.setCurrentUser(u);
        final Guid g = b.getUserId();
        assertEquals(GUID, g);
    }

    @Test
    public void getUserNameDefault() {
        final String n = b.getUserName();
        assertNull(n);
    }

    @Test
    public void getUserNameNull() {
        b.setUserName(null);
        final String n = b.getUserName();
        assertNull(n);
    }

    @Test
    public void getUserName() {
        b.setUserName(NAME);
        final String n = b.getUserName();
        assertEquals(NAME, n);
    }

    @Test
    public void getUserNameFromUser() {
        final DbUser u = new DbUser();
        u.setLoginName(NAME);
        u.setDomain(DOMAIN);
        b.setCurrentUser(u);
        final String un = b.getUserName();
        assertEquals(String.format("%s@%s", NAME, DOMAIN), un);
    }

    @Test
    public void currentUserDefault() {
        final DbUser u = b.getCurrentUser();
        assertNull(u);
    }

    @Test
    public void currentUserNull() {
        final DbUser u = null;
        b.setCurrentUser(u);
        final DbUser cu = b.getCurrentUser();
        assertEquals(u, cu);
    }

    @Test
    public void currentUser() {
        final DbUser u = new DbUser();
        b.setCurrentUser(u);
        final DbUser cu = b.getCurrentUser();
        assertEquals(u, cu);
    }

    @Test
    public void vmTemplateIdDefault() {
        final Guid g = b.getVmTemplateId();
        assertEquals(Guid.Empty, g);
    }

    @Test
    public void vmTemplateId() {
        b.setVmTemplateId(GUID);
        final Guid g = b.getVmTemplateId();
        assertEquals(GUID, g);
    }

    @Test
    public void vmTemplateIdRefDefault() {
        final Guid g = b.getVmTemplateIdRef();
        assertNull(g);
    }

    @Test
    public void vmTemplateIdRef() {
        b.setVmTemplateId(GUID);
        final Guid g = b.getVmTemplateIdRef();
        assertEquals(GUID, g);
    }

    @Test
    public void vmTemplateIdRefWithVm() {
        final VM v = new VM();
        b.setVm(v);
        final Guid g = b.getVmTemplateIdRef();
        assertEquals(GUID, g);
    }

    @Test
    public void vmTemplateNameDefault() {
        final String n = b.getVmTemplateName();
        assertNull(n);
    }

    @Test
    public void vmTemplateName() {
        b.setVmTemplateName(NAME);
        final String nm = b.getVmTemplateName();
        assertEquals(NAME, nm);
    }

    @Test
    public void vmTemplateNameWithVm() {
        final VM v = new VM();
        b.setVm(v);
        final String n = b.getVmTemplateName();
        assertEquals(NAME, n);
    }

    @Test
    public void vmIdDefault() {
        final Guid i = b.getVmId();
        assertEquals(Guid.Empty, i);
    }

    @Test
    public void vmIdNull() {
        b.setVmId(null);
        final Guid i = b.getVmId();
        assertEquals(Guid.Empty, i);
    }

    @Test
    public void vmId() {
        b.setVmId(GUID);
        final Guid i = b.getVmId();
        assertEquals(GUID, i);
    }

    @Test
    public void snapshotNameDefault() {
        final String s = b.getSnapshotName();
        assertNull(s);
    }

    @Test
    public void snapshotNameNull() {
        b.setSnapshotName(null);
        final String s = b.getSnapshotName();
        assertNull(s);
    }

    @Test
    public void snapshotNameEmpty() {
        final String e = "";
        b.setSnapshotName(e);
        final String s = b.getSnapshotName();
        assertEquals(e, s);
    }

    @Test
    public void snapshotName() {
        b.setSnapshotName(NAME);
        final String s = b.getSnapshotName();
        assertEquals(NAME, s);
    }

    @Test
    public void vmIdRefDefault() {
        final Guid g = b.getVmIdRef();
        assertEquals(Guid.Empty, g);
    }

    @Test
    public void vmIdRefNullVmId() {
        b.setVmId(null);
        final Guid g = b.getVmIdRef();
        assertNull(g);
    }

    @Test
    public void vmIdRefNullVm() {
        b.setVmId(null);
        final VM v = new VM();
        v.setId(GUID);
        b.setVm(v);
        final Guid g = b.getVmIdRef();
        assertEquals(GUID, g);
    }

    @Test
    public void vmNameDefault() {
        final String n = b.getVmName();
        assertNull(n);
    }

    @Test
    public void vmNameNull() {
        b.setVmName(null);
        final String n = b.getVmName();
        assertNull(n);
    }

    @Test
    public void vmNameNullVm() {
        b.setVmName(null);
        final VM v = new VM();
        v.setName(NAME);
        b.setVm(v);
        final String n = b.getVmName();
        assertEquals(NAME, n);
    }

    @Test
    public void vmName() {
        b.setVmName(NAME);
        final String n = b.getVmName();
        assertEquals(NAME, n);
    }

    @Test
    public void vdsIdRefDefault() {
        final Guid g = b.getVdsIdRef();
        assertNull(g);
    }

    @Test
    public void vdsIdRefNull() {
        b.setVdsIdRef(null);
        final Guid g = b.getVdsIdRef();
        assertNull(g);
    }

    @Test
    public void vdsIdRef() {
        b.setVdsIdRef(GUID);
        final Guid g = b.getVdsIdRef();
        assertEquals(GUID, g);
    }

    @Test
    public void vdsIdRefVds() {
        b.setVdsIdRef(null);
        final VDS v = new VDS();
        v.setId(GUID);
        b.setVds(v);
        final Guid g = b.getVdsIdRef();
        assertEquals(GUID, g);
    }

    @Test
    public void vdsIdDefault() {
        final Guid g = b.getVdsId();
        assertEquals(Guid.Empty, g);
    }

    @Test
    public void vdsIdNull() {
        b.setVdsId(null);
        final Guid g = b.getVdsId();
        assertEquals(Guid.Empty, g);
    }

    @Test
    public void vdsId() {
        b.setVdsId(GUID);
        final Guid g = b.getVdsId();
        assertEquals(GUID, g);
    }

    @Test
    public void vdsNameDefault() {
        final String s = b.getVdsName();
        assertNull(s);
    }

    @Test
    public void vdsNameNull() {
        b.setVdsName(null);
        final String s = b.getVdsName();
        assertNull(s);
    }

    @Test
    public void vdsName() {
        b.setVdsName(NAME);
        final String s = b.getVdsName();
        assertEquals(NAME, s);
    }

    @Test
    public void vdsNameVds() {
        b.setVdsName(null);
        final VDS v = new VDS();
        v.setVdsName(NAME);
        b.setVds(v);
        final String s = b.getVdsName();
        assertEquals(NAME, s);
    }

    @Test
    public void storageDomainDefault() {
        final StorageDomain s = b.getStorageDomain();
        assertNull(s);
    }

    @Test
    public void storageDomainNull() {
        b.setStorageDomain(null);
        final StorageDomain s = b.getStorageDomain();
        assertNull(s);
    }

    @Test
    public void storageDomain() {
        final StorageDomain s = new StorageDomain();
        b.setStorageDomain(s);
        final StorageDomain st = b.getStorageDomain();
        assertEquals(s, st);
    }

    @Test
    public void storageDomainWithId() {
        b.setStorageDomainId(GUID);
        b.setStoragePoolId(GUID);
        final StorageDomain s = b.getStorageDomain();
        assertEquals(STORAGE_DOMAIN, s);
    }

    @Test
    public void storageDomainWithIdNullPool() {
        b.setStorageDomainId(GUID);
        b.setStoragePoolId(GUID2);
        final StorageDomain s = b.getStorageDomain();
        assertNull(s);
    }

    @Test
    public void storageDomainWithNullId() {
        b.setStorageDomainId(GUID2);
        final StorageDomain s = b.getStorageDomain();
        assertEquals(STORAGE_DOMAIN, s);
    }

    @Test
    public void storageDomainIdDefault() {
        final Guid g = b.getStorageDomainId();
        assertNull(g);
    }

    @Test
    public void storageDomainIdNull() {
        b.setStorageDomainId(null);
        final Guid g = b.getStorageDomainId();
        assertNull(g);
    }

    @Test
    public void storageDomainId() {
        b.setStorageDomainId(GUID);
        final Guid g = b.getStorageDomainId();
        assertEquals(GUID, g);
    }

    @Test
    public void storageDomainIdWithStorageDomain() {
        final StorageDomain s = new StorageDomain();
        s.setId(GUID);
        b.setStorageDomain(s);
        final Guid g = b.getStorageDomainId();
        assertEquals(GUID, g);
    }

    @Test
    public void storageDomainNameDefault() {
        final String s = b.getStorageDomainName();
        assertEquals("", s);
    }

    @Test
    public void storageDomainName() {
        final StorageDomain s = new StorageDomain();
        s.setStorageName(NAME);
        b.setStorageDomain(s);
        final String n = b.getStorageDomainName();
        assertEquals(NAME, n);
    }

    @Test
    public void storagePoolDefault() {
        final StoragePool p = b.getStoragePool();
        assertNull(p);
    }

    @Test
    public void storagePoolWithId() {
        b.setStoragePoolId(GUID);
        final StoragePool p = b.getStoragePool();
        assertNotNull(p);
    }

    @Test
    public void storagePool() {
        final StoragePool p = new StoragePool();
        b.setStoragePool(p);
        final StoragePool sp = b.getStoragePool();
        assertEquals(p, sp);
    }

    @Test
    public void storagePoolIdDefault() {
        final Guid n = b.getStoragePoolId();
        assertNull(n);
    }

    @Test
    public void storagePoolIdNull() {
        b.setStoragePoolId(null);
        final Guid n = b.getStoragePoolId();
        assertNull(n);
    }

    @Test
    public void storagePoolId() {
        b.setStoragePoolId(GUID);
        final Guid n = b.getStoragePoolId();
        assertEquals(GUID, n);
    }

    @Test
    public void storagePoolIdWithStoragePool() {
        b.setStoragePoolId(null);
        final StoragePool p = new StoragePool();
        p.setId(GUID);
        b.setStoragePool(p);
        final Guid n = b.getStoragePoolId();
        assertEquals(GUID, n);
    }

    @Test
    public void storagePoolIdWithStorageDomain() {
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
        final String s = b.getStoragePoolName();
        assertEquals("", s);
    }

    @Test
    public void storagePoolName() {
        final StoragePool p = new StoragePool();
        p.setName(NAME);
        b.setStoragePool(p);
        final String s = b.getStoragePoolName();
        assertEquals(NAME, s);
    }

    @Test
    public void auditLogTypeValue() {
        final AuditLogType t = b.getAuditLogTypeValue();
        assertEquals(AuditLogType.UNASSIGNED, t);
    }

    @Test
    public void getVdsDefault() {
        final VDS v = b.getVds();
        assertNull(v);
    }

    @Test
    public void getVdsNullAll() {
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
        final VM vm = new VM();
        vm.setRunOnVds(GUID2);
        b.setVdsId(GUID);
        b.setVm(vm);
        final VDS v = b.getVds();
        assertEquals(GUID, v.getId());
    }

    @Test
    public void getVdsWithVm() {
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
        final VDS vds = null;
        final VM vm = new VM();
        vm.setRunOnVds(GUID2);
        b.setVds(vds);
        b.setVdsId(GUID3);
        b.setVm(vm);
        final VDS v = b.getVds();
        assertNull(v);
    }

    @Test
    public void getVmDefault() {
        final VM v = b.getVm();
        assertNull(v);
    }

    @Test
    public void getVm() {
        final VM v = new VM();
        b.setVm(v);
        final VM vm = b.getVm();
        assertEquals(v, vm);
    }

    @Test
    public void getVmNullId() {
        final VM v = null;
        b.setVm(v);
        b.setVmId(null);
        final VM vm = b.getVm();
        assertNull(vm);
    }

    @Test
    public void getVmEmptyId() {
        final VM v = null;
        b.setVm(v);
        b.setVmId(Guid.Empty);
        final VM vm = b.getVm();
        assertNull(vm);
    }

    @Test
    public void getVmFromId() {
        final VM v = null;
        b.setVm(v);
        b.setVmId(GUID);
        final VM vm = b.getVm();
        assertNotNull(vm);
    }

    @Test
    public void getVmSwallowsExceptions() {
        final VM v = null;
        b.setVm(v);
        b.setVmId(GUID3);
        final VM vm = b.getVm();
        assertNull(vm);
    }

    @Test
    public void getVmTemplateDefault() {
        final VmTemplate t = b.getVmTemplate();
        assertNull(t);
    }

    @Test
    public void getVmTemplateNull() {
        b.setVmTemplate(null);
        final VmTemplate t = b.getVmTemplate();
        assertNull(t);
    }

    @Test
    public void getVmTemplateWithId() {
        b.setVmTemplate(null);
        b.setVmTemplateId(GUID);
        final VmTemplate t = b.getVmTemplate();
        assertNotNull(t);
    }

    @Test
    public void getVmTemplateWithVm() {
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
        final Guid g = b.getClusterId();
        assertEquals(Guid.Empty, g);
    }

    @Test
    public void getClusterId() {
        b.setClusterId(GUID);
        final Guid g = b.getClusterId();
        assertEquals(GUID, g);
    }

    @Test
    public void getClusterIdCluster() {
        final Cluster gr = new Cluster();
        gr.setId(GUID);
        b.setCluster(gr);
        final Guid g = b.getClusterId();
        assertEquals(GUID, g);
    }

    @Test
    public void getClusterDefault() {
        final Cluster g = b.getCluster();
        assertNull(g);
    }

    @Test
    public void getClusterNotNull() {
        final Cluster g = new Cluster();
        b.setCluster(g);
        final Cluster gr = b.getCluster();
        assertEquals(g, gr);
    }

    @Test
    public void getClusterWithId() {
        b.setClusterId(GUID);
        final Cluster g = b.getCluster();
        assertEquals(GUID, g.getId());
    }

    @Test
    public void getClusterWithVds() {
        final VDS v = new VDS();
        v.setClusterId(GUID);
        b.setVds(v);
        final Cluster g = b.getCluster();
        assertEquals(GUID, g.getId());
    }

    @Test
    public void getClusterWithVm() {
        final VM v = new VM();
        v.setClusterId(GUID);
        b.setVm(v);
        final Cluster g = b.getCluster();
        assertEquals(GUID, g.getId());
    }

    @Test
    public void getClusterNameDefault() {
        final String n = b.getClusterName();
        assertEquals("", n);
    }

    @Test
    public void getClusterNameNullVds() {
        final Cluster g = null;
        b.setCluster(g);
        final String n = b.getClusterName();
        assertEquals("", n);
    }

    @Test
    public void getClusterName() {
        final Cluster g = new Cluster();
        g.setName(NAME);
        b.setCluster(g);
        final String n = b.getClusterName();
        assertEquals(NAME, n);
    }

    @Test
    public void addCustomValueDoesNotHandleNullKeys() {
        final String key = null;
        final String value = NAME;
        assertThrows(NullPointerException.class, () -> b.addCustomValue(key, value));
    }

    @Test
    public void addCustomValueWillNotReturnANull() {
        final String key = NAME;
        final String value = null;
        b.addCustomValue(key, value);
        final String v = b.getCustomValue(key);
        assertEquals("", v);
    }

    @Test
    public void customValue() {
        final String key = "foo";
        final String value = NAME;
        b.addCustomValue(key, value);
        final String v = b.getCustomValue(key);
        assertEquals(value, v);
    }

    @Test
    public void getCustomValuesLeaksInternalStructure() {
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
        final String key = "foo";
        final String value = NAME;
        final String sep = "_";
        b.appendCustomValue(key, value, sep);
        final String s = b.getCustomValue(key);
        assertEquals(value, s);
    }

    @Test
    public void setCustomValues() {
        final String key = "foo";
        doSetCustomValuesTest(b, key);
    }

    @Test
    public void setCustomValuesOverridesExistingValues() {
        final String key = "foo";
        b.appendCustomValue(key, "test value", null);

        doSetCustomValuesTest(b, key);
    }

    @Test
    public void setCustomValuesOverridesDoesNotAffectsOtherKeys() {
        final String key1 = "foo";
        final String key2 = "bar";
        b.appendCustomValue(key2,  "test value 2", null);

        doSetCustomValuesTest(b, key1);

        assertThat(b.getCustomValue(key2), is("test value 2"));
    }

    private void doSetCustomValuesTest(AuditLogableBase underTest, String key) {
        final String value1 = NAME + 1;
        final String value2 = NAME + 2;
        final List<String> values = Arrays.asList(value1, value2);
        final String sep = "_";

        underTest.setCustomValues(key, values, sep);
        final String actual = underTest.getCustomValue(key);

        final String expected = String.format("%s%s%s", value1, sep, value2);
        assertEquals(expected, actual);
    }

    @Test
    public void setCustomCommaSeparatedValues() {
        final String key = "foo";
        final String s1 = NAME + 1;
        final String s2 = NAME + 2;
        final List<String> values = Arrays.asList(s1, s2);
        final String sep = ", ";

        b.setCustomCommaSeparatedValues(key, values);
        final String actual = b.getCustomValue(key);

        assertEquals(String.format("%s%s%s", s1, sep, s2), actual);
    }

    @Test
    public void appendCustomValueAppend() {
        final String key = "foo";
        final String value = NAME;
        final String newVal = "bar";
        final String sep = "_";
        b.addCustomValue(key, value);
        b.appendCustomValue(key, newVal, sep);
        final String s = b.getCustomValue(key);
        assertEquals(value + sep + newVal, s);
    }

    @Test
    public void appendCustomValueDoesntHandleNullKeys() {
        final String key = null;
        final String value = NAME;
        final String sep = "_";
        assertThrows(NullPointerException.class, () -> b.appendCustomValue(key, value, sep));
    }

    @Test
    public void appendCustomValueAppendsWithNull() {
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
        final String s = b.getCustomValue(NAME);
        assertEquals("", s);
    }
}
