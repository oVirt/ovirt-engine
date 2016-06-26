package org.ovirt.engine.core.bll.exportimport;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.InjectorRule;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerDc;
import org.ovirt.engine.core.bll.network.vm.ExternalVmMacsFinder;
import org.ovirt.engine.core.bll.validator.ImportValidator;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.VmIconIdSizePair;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDao;
import org.ovirt.engine.core.utils.ovf.OvfVmIconDefaultsProvider;

public class ImportVMFromConfigurationCommandTest extends BaseCommandTest {
    private Guid vmId;
    private Guid storageDomainId;
    private Guid storagePoolId;
    private Guid clusterId;
    private static final String VM_OVF_XML_DATA = "src/test/resources/vmOvfData.xml";
    private String xmlOvfData;
    @Mock
    private Cluster cluster;
    private StoragePool storagePool;

    private ImportVmFromConfigurationCommand<ImportVmParameters> cmd;
    private ImportValidator validator;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule();

    @Rule
    public InjectorRule injectorRule = new InjectorRule();


    @Mock
    private OsRepository osRepository;

    @Mock
    private UnregisteredOVFDataDao unregisteredOVFDataDao;

    @Mock
    private OvfVmIconDefaultsProvider iconDefaultsProvider;

    @Mock
    private MacPoolPerDc macPoolPerDc;

    @Mock
    private ExternalVmMacsFinder externalVmMacsFinder;

    @Before
    public void setUp() throws IOException {
        vmId = Guid.newGuid();
        storageDomainId = Guid.createGuidFromString("7e2a7eac-3b76-4d45-a7dd-caae8fe0f588");
        storagePoolId = Guid.newGuid();
        clusterId = Guid.newGuid();
        injectorRule.bind(MacPoolPerDc.class, mock(MacPoolPerDc.class));


        // init the injector with the osRepository instance
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);
        SimpleDependencyInjector.getInstance().bind(OvfVmIconDefaultsProvider.class, iconDefaultsProvider);
        final int osId = 0;
        Map<Integer, Map<Version, List<Pair<GraphicsType, DisplayType>>>> graphicsAndDisplays = new HashMap<>();
        graphicsAndDisplays.put(osId, new HashMap());
        graphicsAndDisplays.get(osId).put(null, Arrays.asList(new Pair<>(GraphicsType.SPICE, DisplayType.qxl)));
        when(osRepository.getGraphicsAndDisplays()).thenReturn(
                Collections.singletonMap(osId,
                        Collections.singletonMap(Version.getLast(),
                                Arrays.asList(new Pair<>(GraphicsType.SPICE, DisplayType.qxl)))));
        when(osRepository.isBalloonEnabled(anyInt(), any(Version.class))).thenReturn(true);
        when(osRepository.isSoundDeviceEnabled(anyInt(), any(Version.class))).thenReturn(true);
        when(iconDefaultsProvider.getVmIconDefaults()).thenReturn(new HashMap<Integer, VmIconIdSizePair>(){{
            put(osId, new VmIconIdSizePair(
                    Guid.createGuidFromString("00000000-0000-0000-0000-00000000000a"),
                    Guid.createGuidFromString("00000000-0000-0000-0000-00000000000b")));
        }});
        mockCluster();
        mockDisplayTypes();
        setXmlOvfData();
    }

    private void mockDisplayTypes() {
        Integer osId = 0;
        Version clusterVersion = null;
        Map<Integer, Map<Version, List<Pair<GraphicsType, DisplayType>>>> displayTypeMap = new HashMap<>();
        displayTypeMap.put(osId, new HashMap<>());
        displayTypeMap.get(osId).put(clusterVersion, Arrays.asList(new Pair<>(GraphicsType.SPICE, DisplayType.qxl)));
        when(osRepository.getGraphicsAndDisplays()).thenReturn(displayTypeMap);
    }

    private void setXmlOvfData() throws IOException {
        xmlOvfData = new String(Files.readAllBytes(Paths.get(VM_OVF_XML_DATA)), StandardCharsets.UTF_8);
    }

    @Test
    public void testPositiveImportVmFromConfiguration() {
        initCommand(getOvfEntityData());
        final StorageDomainDao dao = mock(StorageDomainDao.class);
        doReturn(dao).when(cmd).getStorageDomainDao();
        when(dao.getForStoragePool(storageDomainId, storagePoolId)).thenReturn(createStorageDomain());
        doReturn(storagePool).when(cmd).getStoragePool();
        doReturn(Boolean.TRUE).when(cmd).validateAfterCloneVm(anyMap());
        doReturn(Boolean.TRUE).when(cmd).validateBeforeCloneVm(anyMap());
        final VM expectedVm = cmd.getVm();
        when(externalVmMacsFinder.findExternalMacAddresses(eq(expectedVm), any(CommandContext.class)))
                .thenReturn(Collections.emptySet());
        when(validator.validateUnregisteredEntity(any(IVdcQueryable.class), any(OvfEntityData.class), anyList()))
                .thenReturn(ValidationResult.VALID);
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testImportVMFromConfigurationWhenStorageDomainIsInMaintenance() {
        initCommand(getOvfEntityData());
        StorageDomain storageDomain = createStorageDomain();
        storageDomain.setStatus(StorageDomainStatus.Maintenance);

        // Mock Storage Domain.
        final StorageDomainDao dao = mock(StorageDomainDao.class);
        doReturn(dao).when(cmd).getStorageDomainDao();
        when(dao.getForStoragePool(storageDomainId, storagePoolId)).thenReturn(storageDomain);

        doReturn(storageDomain).when(cmd).getStorageDomain();
        when(validator.validateUnregisteredEntity(any(IVdcQueryable.class), any(OvfEntityData.class), anyList())).thenReturn(new ValidationResult(
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2));
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);
    }

    @Test
    public void testImportVMFromConfigurationWhenStorageDomainIsInactive() {
        initCommand(getOvfEntityData());
        StorageDomain storageDomain = createStorageDomain();
        storageDomain.setStatus(StorageDomainStatus.Inactive);

        // Mock Storage Domain.
        final StorageDomainDao dao = mock(StorageDomainDao.class);
        when(validator.validateUnregisteredEntity(any(IVdcQueryable.class), any(OvfEntityData.class), anyList())).thenReturn(new ValidationResult(
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2));
        when(dao.getForStoragePool(storageDomainId, storagePoolId)).thenReturn(storageDomain);

        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);
    }

    @Test
    public void testImportVMFromConfigurationWhenVMDoesNotExists() {
        initCommand(null);
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_UNSUPPORTED_OVF);
    }

    @Test
    public void testImportVMFromConfigurationXMLCouldNotGetParsed() {
        OvfEntityData ovfEntity = getOvfEntityData();
        ovfEntity.setOvfData("This is not a valid XML");
        initCommand(ovfEntity);
        List<OvfEntityData> ovfEntityDataList = new ArrayList<>();
        ovfEntityDataList.add(ovfEntity);
        when(unregisteredOVFDataDao.getByEntityIdAndStorageDomain(vmId, storageDomainId)).thenReturn(ovfEntityDataList);
        when(validator.validateUnregisteredEntity(any(IVdcQueryable.class), any(OvfEntityData.class), anyList())).thenReturn(new ValidationResult(
                EngineMessage.ACTION_TYPE_FAILED_OVF_CONFIGURATION_NOT_SUPPORTED));
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_OVF_CONFIGURATION_NOT_SUPPORTED);
    }

    private ImportVmParameters createParametersWhenImagesExistOnTargetStorageDomain() {
        ImportVmParameters params = new ImportVmParameters();
        params.setContainerId(vmId);
        params.setStorageDomainId(storageDomainId);
        params.setClusterId(clusterId);
        params.setImagesExistOnTargetStorageDomain(true);
        return params;
    }

    private void initCommand(OvfEntityData resultOvfEntityData) {
        ImportVmParameters parameters = createParametersWhenImagesExistOnTargetStorageDomain();
        initUnregisteredOVFData(resultOvfEntityData);
        cmd = spy(new ImportVmFromConfigurationCommand<ImportVmParameters>(
                parameters, CommandContext.createContext(parameters.getSessionId())) {

            {
                this.externalVmMacsFinder = ImportVMFromConfigurationCommandTest.this.externalVmMacsFinder;
            }

            // Overridden here and not during spying, since it's called in the constructor
            @SuppressWarnings("synthetic-access")
            @Override
            public UnregisteredOVFDataDao getUnregisteredOVFDataDao() {
                return unregisteredOVFDataDao;
            }

            @Override
            protected void initUser() {
            }

            public Cluster getCluster() {
                return cluster;
            }

            @Override
            protected List<DiskImage> getImages() {
                return Collections.emptyList();
            }
        });
        cmd.init();
        doReturn(mock(MacPool.class)).when(cmd).getMacPool();
        validator = spy(new ImportValidator(parameters));
        doReturn(validator).when(cmd).getImportValidator();
        mockStoragePool();
        doReturn(storagePool).when(validator).getStoragePool();
    }

    private void initUnregisteredOVFData(OvfEntityData resultOvfEntityData) {
        List<OvfEntityData> ovfEntityDataList = new ArrayList<>();
        if (resultOvfEntityData != null) {
            ovfEntityDataList.add(resultOvfEntityData);
        }
        when(unregisteredOVFDataDao.getByEntityIdAndStorageDomain(vmId, storageDomainId)).thenReturn(ovfEntityDataList);
    }

    private OvfEntityData getOvfEntityData() {
        OvfEntityData ovfEntity = new OvfEntityData();
        ovfEntity.setEntityId(vmId);
        ovfEntity.setEntityName("Some VM");
        ovfEntity.setOvfData(xmlOvfData);
        return ovfEntity;
    }

    private void mockCluster() {
        cluster = mock(Cluster.class);
        doReturn(clusterId).when(cluster).getId();
        doReturn(storagePoolId).when(cluster).getStoragePoolId();
        doReturn(ArchitectureType.x86_64).when(cluster).getArchitecture();
        doReturn(null).when(cluster).getCompatibilityVersion();
    }

    private void mockStoragePool() {
        storagePool = new StoragePool();
        storagePool.setId(storagePoolId);
    }

    protected StorageDomain createStorageDomain() {
        StorageDomain sd = new StorageDomain();
        sd.setId(storageDomainId);
        sd.setStorageDomainType(StorageDomainType.Data);
        sd.setStatus(StorageDomainStatus.Active);
        sd.setStoragePoolId(storagePoolId);
        return sd;
    }
}
