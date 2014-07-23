package org.ovirt.engine.core.bll;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.io.IOException;
import java.nio.charset.Charset;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.validator.ImportValidator;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class ImportVMFromConfigurationCommandTest {
    private Guid vmId;
    private Guid storageDomainId;
    private Guid storagePoolId;
    private Guid clusterId;
    private static final String VM_OVF_XML_DATA = "src/test/resources/vmOvfData.xml";
    private String xmlOvfData;
    private VDSGroup vdsGroup;
    private StoragePool storagePool;

    private ImportVmFromConfigurationCommand<ImportVmParameters> cmd;
    private ImportValidator validator;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.VirtIoScsiEnabled, Version.v3_2.toString(), false)
            );

    @Mock
    private OsRepository osRepository;

    @Mock
    private UnregisteredOVFDataDAO unregisteredOVFDataDao;

    @Before
    public void setUp() throws IOException {
        vmId = Guid.newGuid();
        storageDomainId = Guid.createGuidFromString("7e2a7eac-3b76-4d45-a7dd-caae8fe0f588");
        storagePoolId = Guid.newGuid();
        clusterId = Guid.newGuid();

        // init the injector with the osRepository instance
        SimpleDependecyInjector.getInstance().bind(OsRepository.class, osRepository);
        final int osId = 0;
        Map<Integer, Map<Version, List<Pair<GraphicsType, DisplayType>>>> graphicsAndDisplays = new HashMap<>();
        graphicsAndDisplays.put(osId, new HashMap());
        graphicsAndDisplays.get(osId).put(null, Arrays.asList(new Pair<>(GraphicsType.SPICE, DisplayType.qxl)));
        when(osRepository.getGraphicsAndDisplays()).thenReturn(
                Collections.singletonMap(osId,
                        Collections.singletonMap(Version.getLast(),
                                Arrays.asList(new Pair<>(GraphicsType.SPICE, DisplayType.qxl)))));
        when(osRepository.isBalloonEnabled(anyInt(), any(Version.class))).thenReturn(true);
        mockVdsGroup();
        mockDisplayTypes();
        setXmlOvfData();
    }

    private void mockDisplayTypes() {
        Integer osId = 0;
        Version clusterVersion = null;
        Map<Integer, Map<Version, List<Pair<GraphicsType, DisplayType>>>> displayTypeMap = new HashMap<>();
        displayTypeMap.put(osId, new HashMap<Version, List<Pair<GraphicsType, DisplayType>>>());
        displayTypeMap.get(osId).put(clusterVersion, Arrays.asList(new Pair<>(GraphicsType.SPICE, DisplayType.qxl)));
        when(osRepository.getGraphicsAndDisplays()).thenReturn(displayTypeMap);
    }

    private void setXmlOvfData() throws IOException {
        xmlOvfData = new String(Files.readAllBytes(Paths.get(VM_OVF_XML_DATA)), Charset.forName("UTF-8"));
    }

    @Test
    public void testPositiveImportVmFromConfiguration() {
        initCommand(getOvfEntityData());
        final StorageDomainDAO dao = mock(StorageDomainDAO.class);
        doReturn(dao).when(cmd).getStorageDomainDAO();
        when(dao.getForStoragePool(storageDomainId, storagePoolId)).thenReturn(createStorageDomain());
        doReturn(storagePool).when(cmd).getStoragePool();
        doReturn(Boolean.TRUE).when(cmd).canDoActionAfterCloneVm(anyMap());
        doReturn(Boolean.TRUE).when(cmd).canDoActionBeforeCloneVm(anyMap());

        when(validator.validateUnregisteredEntity(any(IVdcQueryable.class), any(OvfEntityData.class), anyList())).thenReturn(ValidationResult.VALID);
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(cmd);
    }

    @Test
    public void testImportVMFromConfigurationWhenStorageDomainIsInMaintenance() {
        initCommand(getOvfEntityData());
        StorageDomain storageDomain = createStorageDomain();
        storageDomain.setStatus(StorageDomainStatus.Maintenance);

        // Mock Storage Domain.
        final StorageDomainDAO dao = mock(StorageDomainDAO.class);
        doReturn(dao).when(cmd).getStorageDomainDAO();
        when(dao.getForStoragePool(storageDomainId, storagePoolId)).thenReturn(storageDomain);

        doReturn(storageDomain).when(cmd).getStorageDomain();
        when(validator.validateUnregisteredEntity(any(IVdcQueryable.class), any(OvfEntityData.class), anyList())).thenReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2));
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);
    }

    @Test
    public void testImportVMFromConfigurationWhenStorageDomainIsInactive() {
        initCommand(getOvfEntityData());
        StorageDomain storageDomain = createStorageDomain();
        storageDomain.setStatus(StorageDomainStatus.Inactive);

        // Mock Storage Domain.
        final StorageDomainDAO dao = mock(StorageDomainDAO.class);
        when(validator.validateUnregisteredEntity(any(IVdcQueryable.class), any(OvfEntityData.class), anyList())).thenReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2));
        when(dao.getForStoragePool(storageDomainId, storagePoolId)).thenReturn(storageDomain);

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);
    }

    @Test
    public void testImportVMFromConfigurationWhenVMDoesNotExists() {
        initCommand(null);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd, VdcBllMessages.ACTION_TYPE_FAILED_UNSUPPORTED_OVF);
    }

    @Test
    public void testImportVMFromConfigurationXMLCouldNotGetParsed() {
        OvfEntityData ovfEntity = getOvfEntityData();
        ovfEntity.setOvfData("This is not a valid XML");
        initCommand(ovfEntity);
        List<OvfEntityData> ovfEntityDataList = new ArrayList<>();
        ovfEntityDataList.add(ovfEntity);
        when(unregisteredOVFDataDao.getByEntityIdAndStorageDomain(vmId, storageDomainId)).thenReturn(ovfEntityDataList);
        when(validator.validateUnregisteredEntity(any(IVdcQueryable.class), any(OvfEntityData.class), anyList())).thenReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_OVF_CONFIGURATION_NOT_SUPPORTED));
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_OVF_CONFIGURATION_NOT_SUPPORTED);
    }

    private ImportVmParameters createParametersWhenImagesExistOnTargetStorageDomain() {
        ImportVmParameters params = new ImportVmParameters();
        params.setContainerId(vmId);
        params.setStorageDomainId(storageDomainId);
        params.setVdsGroupId(clusterId);
        params.setImagesExistOnTargetStorageDomain(true);
        return params;
    }

    private void initCommand(OvfEntityData resultOvfEntityData) {
        ImportVmParameters parameters = createParametersWhenImagesExistOnTargetStorageDomain();
        initUnregisteredOVFData(resultOvfEntityData);
        cmd = spy(new ImportVmFromConfigurationCommand<ImportVmParameters>(parameters) {
            // Overridden here and not during spying, since it's called in the constructor
            @SuppressWarnings("synthetic-access")
            @Override
            protected UnregisteredOVFDataDAO getUnregisteredOVFDataDao() {
                return unregisteredOVFDataDao;
            }

            public VDSGroup getVdsGroup() {
                return vdsGroup;
            }

            @Override
            protected List<DiskImage> getImages() {
                return Collections.emptyList();
            }
        });
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

    private void mockVdsGroup() {
        vdsGroup = new VDSGroup();
        vdsGroup.setId(clusterId);
        vdsGroup.setStoragePoolId(storagePoolId);
        vdsGroup.setArchitecture(ArchitectureType.x86_64);
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
