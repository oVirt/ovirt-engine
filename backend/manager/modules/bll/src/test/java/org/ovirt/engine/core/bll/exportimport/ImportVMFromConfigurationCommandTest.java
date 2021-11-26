package org.ovirt.engine.core.bll.exportimport;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.network.vm.ExternalVmMacsFinder;
import org.ovirt.engine.core.bll.storage.ovfstore.DrMappingHelper;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfHelper;
import org.ovirt.engine.core.bll.storage.utils.BlockStorageDiscardFunctionalityHelper;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.ImportValidator;
import org.ovirt.engine.core.common.action.ImportVmFromConfParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.VmIconIdSizePair;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.LabelDao;
import org.ovirt.engine.core.dao.UnregisteredDisksDao;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmStatisticsDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.ovf.OvfProperties;
import org.ovirt.engine.core.utils.ovf.OvfVmIconDefaultsProvider;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.ovirt.engine.core.utils.ovf.xml.XmlNode;
import org.ovirt.engine.core.vdsbroker.vdsbroker.CloudInitHandler;
import org.w3c.dom.Element;

@ExtendWith(MockConfigExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ImportVMFromConfigurationCommandTest extends BaseCommandTest implements OvfProperties {
    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.DefaultGeneralTimeZone, "Etc/GMT"),
                MockConfigDescriptor.of(ConfigValues.BiosTypeSupported, Version.getLast(), true)
        );
    }

    private Guid vmId = Guid.newGuid();
    private static final Guid storageDomainId = new Guid("7e2a7eac-3b76-4d45-a7dd-caae8fe0f588");
    private final Guid storagePoolId = Guid.newGuid();
    private final Guid clusterId = Guid.newGuid();
    private static final String VM_OVF_XML_DATA = "src/test/resources/vmOvfData.xml";
    private static final String OVF_URI = "http://schemas.dmtf.org/ovf/envelope/1/";
    private String xmlOvfData;
    private Cluster cluster;
    private StoragePool storagePool;

    @Spy
    @InjectMocks
    private ImportVmFromConfigurationCommand<ImportVmFromConfParameters> cmd =
            new ImportVmFromConfigurationCommand<>(createParametersWhenImagesExistOnTargetStorageDomain(), null);

    private ImportValidator validator;

    @Mock
    private UnregisteredOVFDataDao unregisteredOVFDataDao;

    @Mock
    private UnregisteredDisksDao unregisteredDisksDao;

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private ExternalVmMacsFinder externalVmMacsFinder;

    @Mock
    private DrMappingHelper drMappingHelper;

    @Spy
    @InjectMocks
    private OvfHelper ovfHelper;

    // Using @Spy, so the object will be injected into ovfHelper
    @Spy
    private OvfManager ovfManager = new OvfManager();

    @Mock
    private OsRepository osRepository;

    @Mock
    private VmHandler vmHandler;

    @Mock
    private CloudInitHandler cloudInitHandler;

    @Mock
    private AffinityGroupDao affinityGroupDao;

    @Mock
    private LabelDao labelDao;

    @Mock
    private AuditLogDirector auditLogDirector;

    @Mock
    private VmStaticDao vmStaticDao;

    @Mock
    private VmDynamicDao vmDynamicDao;

    @Mock
    private VmStatisticsDao vmStatisticsDao;

    @Mock
    private BlockStorageDiscardFunctionalityHelper discardHelper;

    @Mock
    private VmDeviceUtils vmDeviceUtils;

    private ArgumentCaptor<VmStatic> vmStaticArgumentCaptor;

    @BeforeEach
    public void setUp() throws IOException {
        int osId = 30;

        when(osRepository.getOsIdByUniqueName(any())).thenReturn(osId);
        when(osRepository.getArchitectureFromOS(anyInt())).thenReturn(ArchitectureType.x86_64);
        when(osRepository.isWindows(anyInt())).thenReturn(false);
        when(osRepository.isSoundDeviceEnabled(anyInt(), any())).thenReturn(true);
        when(osRepository.isQ35Supported(anyInt())).thenReturn(true);
        when(osRepository.isSecureBootSupported(anyInt())).thenReturn(true);
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);

        doReturn(osRepository).when(ovfManager).getOsRepository();

        var ovfVmIconDefaultsProvider = mock(OvfVmIconDefaultsProvider.class);
        when(ovfVmIconDefaultsProvider.getVmIconDefaults()).thenReturn(Map.of(
                osId, new VmIconIdSizePair(Guid.Empty, Guid.Empty)
        ));
        doReturn(ovfVmIconDefaultsProvider).when(ovfManager).getIconDefaultsProvider();

        cluster = new Cluster();
        cluster.setId(clusterId);
        cluster.setStoragePoolId(storagePoolId);
        cluster.setArchitecture(ArchitectureType.x86_64);
        cluster.setCompatibilityVersion(Version.getLast());
        cluster.setBiosType(BiosType.Q35_SEA_BIOS);

        storagePool = new StoragePool();
        storagePool.setId(storagePoolId);
        storagePool.setCompatibilityVersion(Version.getLast());

        doNothing().when(cmd).updateVmVersion();
        doReturn(cluster).when(cmd).getCluster();
        doReturn(storagePool).when(cmd).getStoragePool();
        doReturn(emptyList()).when(cmd).getImages();
        doReturn(emptyList()).when(cloudInitHandler).validate(any());

        doReturn(null).when(affinityGroupDao).getByName(any());
        doReturn(null).when(labelDao).getByName(any());

        setXmlOvfData();
    }

    private void setXmlOvfData() throws IOException {
        xmlOvfData = new String(Files.readAllBytes(Paths.get(VM_OVF_XML_DATA)), StandardCharsets.UTF_8);
    }

    private void appendBiosTypeTag(XmlDocument document, int biosTypeId, Boolean custom) {
        XmlNode content = document.selectSingleNode("//*/Content");
        Element element = document.createElement(BIOS_TYPE);
        if (custom != null) {
            element.setAttributeNS(OVF_URI, "ovf:custom", custom.toString());
        }
        element.setTextContent(String.valueOf(biosTypeId));
        content.appendChild(element);
    }

    private void setXmlOvfData(BiosType biosType, Boolean custom) throws Exception {
        XmlDocument document = new XmlDocument(xmlOvfData);
        if (biosType != null) {
            appendBiosTypeTag(document, biosType.getValue() - 1, custom);
        }
        xmlOvfData = document.convertToString();
    }

    @Test
    public void testPositiveImportVmFromConfiguration() {
        initCommand(getOvfEntityData());
        doReturn(Boolean.TRUE).when(cmd).validateAfterCloneVm(any());
        doReturn(Boolean.TRUE).when(cmd).validateBeforeCloneVm(any());
        when(validator.validateUnregisteredEntity(any())) .thenReturn(ValidationResult.VALID);
        when(validator.validateStorageExistForUnregisteredEntity(anyList(), anyBoolean(), any(), any()))
                .thenReturn(ValidationResult.VALID);
        doReturn(ValidationResult.VALID).when(validator)
                .validateStorageExistsForMemoryDisks(anyList(), anyBoolean(), anyMap());

        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testImportVMFromConfigurationWhenStorageDomainIsInMaintenance() {
        initCommand(getOvfEntityData());
        doReturn(true).when(cmd).validateBeforeCloneVm(any());
        doReturn(true).when(cmd).validateAfterCloneVm(any());
        StorageDomain storageDomain = createStorageDomain();
        storageDomain.setStatus(StorageDomainStatus.Maintenance);

        doReturn(storageDomain).when(cmd).getStorageDomain();
        when(validator.validateUnregisteredEntity(any())).thenReturn(ValidationResult.VALID);
        when(validator.validateStorageExistForUnregisteredEntity(anyList(), anyBoolean(), any(), any())).
                thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2));

        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);
    }

    @Test
    public void testImportVMFromConfigurationWhenStorageDomainIsInactive() {
        initCommand(getOvfEntityData());
        doReturn(true).when(cmd).validateBeforeCloneVm(any());
        doReturn(true).when(cmd).validateAfterCloneVm(any());
        doReturn(new StoragePool()).when(cmd).getStoragePool();
        StorageDomain storageDomain = createStorageDomain();
        storageDomain.setStatus(StorageDomainStatus.Inactive);

        when(validator.validateUnregisteredEntity(any())).thenReturn(ValidationResult.VALID);
        when(validator.validateStorageExistForUnregisteredEntity(anyList(), anyBoolean(), any(), any())).
                thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2));

        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);
    }

    @Test
    public void testImportVMFromConfigurationWhenVMDoesNotExists() {
        initCommand(null);
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
    }

    @Test
    public void testImportVMFromConfigurationXMLCouldNotGetParsed() {
        OvfEntityData ovfEntity = getOvfEntityData();
        ovfEntity.setOvfData("This is not a valid XML");
        initCommand(ovfEntity);
        List<OvfEntityData> ovfEntityDataList = new ArrayList<>();
        ovfEntityDataList.add(ovfEntity);
        doReturn(true).when(cmd).validateBeforeCloneVm(any());
        doReturn(true).when(cmd).validateAfterCloneVm(any());
        when(unregisteredOVFDataDao.getByEntityIdAndStorageDomain(vmId, storageDomainId)).thenReturn(ovfEntityDataList);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_OVF_CONFIGURATION_NOT_SUPPORTED);
    }

    private void testBiosType(BiosType biosType,
            Boolean custom,
            BiosType clusterBiosType,
            BiosType expectedBiosType) throws Exception {
        cluster.setBiosType(clusterBiosType);
        doNothing().when(cmd).processImages();
        doReturn(mock(CompensationContext.class)).when(cmd).getCompensationContext();

        setXmlOvfData(biosType, custom);
        initCommand(getOvfEntityData());
        doReturn(Boolean.TRUE).when(cmd).validateAfterCloneVm(any());
        doReturn(Boolean.TRUE).when(cmd).validateBeforeCloneVm(any());
        when(validator.validateUnregisteredEntity(any())).thenReturn(ValidationResult.VALID);
        when(validator.validateStorageExistForUnregisteredEntity(anyList(), anyBoolean(), any(), any()))
                .thenReturn(ValidationResult.VALID);
        doReturn(ValidationResult.VALID).when(validator)
                .validateStorageExistsForMemoryDisks(anyList(), anyBoolean(), anyMap());

        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
        cmd.executeVmCommand();
        vmStaticArgumentCaptor = ArgumentCaptor.forClass(VmStatic.class);
        verify(vmStaticDao).save(vmStaticArgumentCaptor.capture());
        assertEquals(expectedBiosType,
                vmStaticArgumentCaptor.getValue().getBiosType(),
                "Effective bios type");
    }

    @Test
    public void testBiosTypeDefault() throws Exception {
        testBiosType(null, null, BiosType.I440FX_SEA_BIOS, BiosType.I440FX_SEA_BIOS);
    }

    @Test
    public void testBiosTypeDefaultWithChipsetChange() throws Exception {
        testBiosType(null, null, BiosType.Q35_SEA_BIOS, BiosType.I440FX_SEA_BIOS);
    }

    @Test
    public void testBiosTypeDefaultQ35SeaBios() throws Exception {
        testBiosType(BiosType.Q35_SEA_BIOS, null, BiosType.Q35_SEA_BIOS, BiosType.Q35_SEA_BIOS);
    }

    @Test
    public void testBiosTypeDefaultQ35SeaBiosWithChipsetChange() throws Exception {
        testBiosType(BiosType.Q35_SEA_BIOS, null, BiosType.I440FX_SEA_BIOS, BiosType.Q35_SEA_BIOS);
    }

    @Test
    public void testBiosTypeDefaultQ35SecureBoot() throws Exception {
        testBiosType(BiosType.Q35_SECURE_BOOT, null, BiosType.Q35_SEA_BIOS, BiosType.Q35_SECURE_BOOT);
    }

    @Test
    public void testBiosTypeNotCustomQ35SeaBios() throws Exception {
        testBiosType(BiosType.Q35_SEA_BIOS, false, BiosType.Q35_SEA_BIOS, BiosType.Q35_SEA_BIOS);
    }

    @Test
    public void testBiosTypeNotCustomQ35SeaBiosWithChipsetChange() throws Exception {
        testBiosType(BiosType.Q35_SEA_BIOS, false, BiosType.I440FX_SEA_BIOS, BiosType.Q35_SEA_BIOS);
    }

    @Test
    public void testBiosTypeCustomQ35SeaBios() throws Exception {
        testBiosType(BiosType.Q35_SEA_BIOS, true, BiosType.I440FX_SEA_BIOS, BiosType.Q35_SEA_BIOS);
    }

    @Test
    public void testBiosTypeNotCustomQ35SecureBoot() throws Exception {
        testBiosType(BiosType.Q35_SECURE_BOOT, false, BiosType.Q35_SEA_BIOS, BiosType.Q35_SECURE_BOOT);
    }

    @Test
    public void testBiosTypeNotCustomQ35SecureBootWithChipsetChagned() throws Exception {
        testBiosType(BiosType.Q35_SECURE_BOOT, false, BiosType.I440FX_SEA_BIOS, BiosType.Q35_SECURE_BOOT);
    }

    @Test
    public void testBiosTypeCustomQ35SecureBoot() throws Exception {
        testBiosType(BiosType.Q35_SECURE_BOOT, true, BiosType.I440FX_SEA_BIOS, BiosType.Q35_SECURE_BOOT);
    }

    private ImportVmFromConfParameters createParametersWhenImagesExistOnTargetStorageDomain() {
        ImportVmFromConfParameters params = new ImportVmFromConfParameters(emptyList(), true);
        params.setContainerId(vmId);
        params.setStorageDomainId(storageDomainId);
        params.setClusterId(clusterId);
        params.setImagesExistOnTargetStorageDomain(true);
        return params;
    }

    private void initCommand(OvfEntityData resultOvfEntityData) {
        initUnregisteredOVFData(resultOvfEntityData);
        doReturn(mock(MacPool.class)).when(cmd).getMacPool();
        validator = spy(new ImportValidator(cmd.getParameters()));
        doReturn(validator).when(cmd).getImportValidator();
        doReturn(storagePool).when(validator).getStoragePool();
        cmd.init();
    }

    private void initUnregisteredOVFData(OvfEntityData resultOvfEntityData) {
        List<OvfEntityData> ovfEntityDataList = new ArrayList<>();
        if (resultOvfEntityData != null) {
            ovfEntityDataList.add(resultOvfEntityData);
        }
        when(unregisteredOVFDataDao.getByEntityIdAndStorageDomain(vmId, storageDomainId)).thenReturn(ovfEntityDataList);
        when(clusterDao.getByName(any())).thenReturn(null);
    }

    private OvfEntityData getOvfEntityData() {
        OvfEntityData ovfEntity = new OvfEntityData();
        ovfEntity.setEntityId(vmId);
        ovfEntity.setEntityName("Some VM");
        ovfEntity.setOvfData(xmlOvfData);
        return ovfEntity;
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
