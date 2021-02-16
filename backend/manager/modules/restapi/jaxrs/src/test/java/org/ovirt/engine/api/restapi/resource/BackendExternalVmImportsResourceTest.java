package org.ovirt.engine.api.restapi.resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.ExternalVmImport;
import org.ovirt.engine.api.model.ExternalVmProviderType;
import org.ovirt.engine.api.model.File;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportVmFromExternalUrlParameters;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendExternalVmImportsResourceTest extends AbstractBackendBaseTest {

    private static final Guid CLUSTER_ID = GUIDS[0];
    private static final Guid STORAGE_DOMAIN_ID = GUIDS[1];
    private static final Guid HOST_ID = GUIDS[2];

    private static final String VMWARE_URL = "vpx://administrator@1.2.3.4/DataCenter/Cluster/2.3.4.5?no_verify=1";
    private static final String USERNAME = "administrator";
    private static final String PASSWORD = "123456";
    private static final String EXTERNAL_VM_NAME = "external-name";
    private static final String CREATED_VM_NAME = "new-vm";
    private static final String DRIVERS_ISO = "drivers.iso";
    private static final VolumeType VOLUME_TYPE = VolumeType.Sparse;
    private static final OriginType ORIGIN_TYPE = OriginType.VMWARE;

    private final BackendExternalVmImportsResource resource = new BackendExternalVmImportsResource();

    @Override
    protected void init() {
        initBackendResource(resource);
        resource.setMappingLocator(mapperLocator);
    }

    @BeforeEach
    public void setup() {
        setUpBasicUriExpectations("/externalvmimports");
    }

    @Test
    public void testVmWareImport() {
        VM externalVm = new VM();
        externalVm.setName(EXTERNAL_VM_NAME);

        setUpActionExpectations(ActionType.ImportVmFromExternalUrl,
                ImportVmFromExternalUrlParameters.class,
                new String[] {
                    "OriginType",
                    "StorageDomainId",
                    "ClusterId",
                    "Url",
                    "Username",
                    "Password",
                    "ProxyHostId",
                    "VirtioIsoName",
                    "ExternalName",
                    "VolumeType",
                    "NewVmName"
                },
                new Object[] {
                    ORIGIN_TYPE,
                    STORAGE_DOMAIN_ID,
                    CLUSTER_ID,
                    VMWARE_URL,
                    USERNAME,
                    PASSWORD,
                    HOST_ID,
                    DRIVERS_ISO,
                    EXTERNAL_VM_NAME,
                    VOLUME_TYPE,
                    CREATED_VM_NAME
                },
                true,
                true,
                externalVm,
                true);

        ExternalVmImport vmImport = createVmImport();
        resource.add(vmImport);
    }

    private ExternalVmImport createVmImport() {
        File file = new File();
        file.setId(DRIVERS_ISO);
        ExternalVmImport vmImport = new ExternalVmImport();
        vmImport.setName(EXTERNAL_VM_NAME);
        vmImport.setVm(new Vm());
        vmImport.getVm().setName(CREATED_VM_NAME);
        vmImport.setCluster(new Cluster());
        vmImport.getCluster().setId(CLUSTER_ID.toString());
        vmImport.setStorageDomain(new StorageDomain());
        vmImport.getStorageDomain().setId(STORAGE_DOMAIN_ID.toString());
        vmImport.setHost(new Host());
        vmImport.getHost().setId(HOST_ID.toString());
        vmImport.setSparse(true);
        vmImport.setProvider(ExternalVmProviderType.VMWARE);
        vmImport.setDriversIso(file);
        vmImport.setUsername(USERNAME);
        vmImport.setPassword(PASSWORD);
        vmImport.setUrl(VMWARE_URL);

        return vmImport;
    }
}
