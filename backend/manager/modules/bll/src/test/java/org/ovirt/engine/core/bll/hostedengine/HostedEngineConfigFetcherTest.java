package org.ovirt.engine.core.bll.hostedengine;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

@RunWith(MockitoJUnitRunner.class)
public class HostedEngineConfigFetcherTest {

    private static final Guid DOMAIN_ID = Guid.newGuid();
    private static final Guid POOL_ID = Guid.newGuid();
    private static final Guid HOST_ID = Guid.newGuid();
    private static final Guid IMAGE_ID = Guid.newGuid();
    private static final Guid VOLUME_ID = Guid.newGuid();

    @Rule
    public MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.HostedEngineConfigDiskSizeInBytes, 20480));
    @Mock
    private ResourceManager resourceManager;
    @Mock
    private BackendInternal backend;
    @Mock
    private Instance<HostedEngineHelper> hostedEngineHelperInstance;
    @Mock
    private HostedEngineHelper hostedEngineHelper;
    @Spy
    @InjectMocks
    private HostedEngineConfigFetcher configFetcher;

    @Before
    public void setUp() throws Exception {
        when(hostedEngineHelperInstance.get()).thenReturn(hostedEngineHelper);
        when(hostedEngineHelper.getStorageDomainId()).thenReturn(DOMAIN_ID);
        when(hostedEngineHelper.getStoragePoolId()).thenReturn(POOL_ID);
        when(hostedEngineHelper.getRunningHostId()).thenReturn(HOST_ID);
        reset(configFetcher);
    }

    @Test
    public void fetchThrowException() {
        // given
        doThrow(NullPointerException.class)
                .when(resourceManager).runVdsCommand(any(), any());
        // when
        CompletableFuture<Map<String, String>> fetch = configFetcher.fetchPromise();
        // then
        assertThatThrownBy(() -> fetch.join())
                .isInstanceOf(CompletionException.class)
                .hasCauseInstanceOf(NullPointerException.class);
        assertTrue(fetch.isCompletedExceptionally());
        verifyCalled(VDSCommandType.GetImagesList, times(1));
        verifyNoMoreInteractions(resourceManager);
    }

    @Test
    public void imagesListIsNull() {
        // given
        mockVdsCommand(VDSCommandType.GetImagesList, unsuccessfulReturnValue(null));
        // when
        Map<String, String> config = fetchConfig();
        // then

        verifyCalled(VDSCommandType.GetImagesList, times(1));
        verifyCalled(VDSCommandType.GetVolumesList, never());
        assertThat(config, is(Collections.emptyMap()));
    }

    @Test
    public void imagesListIsEmpty() {
        // given
        mockVdsCommand(VDSCommandType.GetImagesList, successfulReturnValue(Collections.emptyList()));
        // when
        Map<String, String> config = fetchConfig();
        // then
        verifyCalled(VDSCommandType.GetImagesList, times(1));
        verifyCalled(VDSCommandType.GetVolumesList, never());
        assertThat(config, is(Collections.emptyMap()));
    }

    @Test
    public void volumesListIsEmpty() {
        // given
        mockVdsCommand(VDSCommandType.GetImagesList, successfulReturnValue(Arrays.asList(IMAGE_ID)));
        mockVdsCommand(VDSCommandType.GetVolumesList, successfulReturnValue(Collections.emptyList()));
        // when
        Map<String, String> config = fetchConfig();
        // then
        verifyCalled(VDSCommandType.GetImagesList, times(1));
        verifyCalled(VDSCommandType.GetVolumesList, times(1));
        assertThat(config, is(Collections.emptyMap()));
    }

    @Test
    public void volumeInfoIsEmpty() {
        // given
        givenListOfImagesAndVolumes();
        mockVdsCommand(VDSCommandType.GetImageInfo, unsuccessfulReturnValue(null));
        // when
        Map<String, String> config = fetchConfig();
        // then
        verifyCalled(VDSCommandType.GetImagesList, times(1));
        verifyCalled(VDSCommandType.GetVolumesList, times(1));
        verifyCalled(VDSCommandType.GetImageInfo, times(1));
        verifyCalled(VdcActionType.RetrieveImageData, never());
        assertThat(config, is(Collections.emptyMap()));
    }

    @Test
    public void configVolumeDescriptionNotMatch() {
        // given
        givenListOfImagesAndVolumes();
        mockVdsCommand(VDSCommandType.GetImageInfo, successfulReturnValue(newDisk("nonMatchingDesc")));
        // when
        Map<String, String> config = fetchConfig();
        // then
        verifyCalled(VDSCommandType.GetImagesList, times(1));
        verifyCalled(VDSCommandType.GetVolumesList, times(1));
        verifyCalled(VDSCommandType.GetImageInfo, times(1));
        verifyCalled(VdcActionType.RetrieveImageData, never());
        assertThat(config, is(Collections.emptyMap()));
    }

    @Test
    public void untarFails() {
        // given
        givenListOfImagesAndVolumes();
        givenTheWantedDiskImage();
        mockVdcCommand(VdcActionType.RetrieveImageData,
                successfulVdcReturnValue(null));
        // when
        Map<String, String> config = fetchConfig();
        // then
        verifyCalled(VDSCommandType.GetImagesList, times(1));
        verifyCalled(VDSCommandType.GetVolumesList, times(1));
        verifyCalled(VDSCommandType.GetImageInfo, times(1));
        verifyCalled(VdcActionType.RetrieveImageData, times(1));
        assertThat(config, is(Collections.emptyMap()));

    }

    @Test
    public void confFileNotExistInTar() {
        // given
        givenListOfImagesAndVolumes();
        givenTheWantedDiskImage();
        mockVdcCommand(VdcActionType.RetrieveImageData,
                successfulVdcReturnValue(new byte[10]));
        // when
        Map<String, String> config = fetchConfig();
        // then
        assertThat(config, is(Collections.emptyMap()));
    }

    @Test
    public void confFileConvertToMapFails() throws IOException, URISyntaxException {
        // given
        givenListOfImagesAndVolumes();
        givenTheWantedDiskImage();
        mockVdcCommand(VdcActionType.RetrieveImageData,
                successfulVdcReturnValue(load("not-a-valid-hosted-engine-config-tar.tar")));
        // when
        Map<String, String> config = fetchConfig();
        // then
        verifyCalled(VdcActionType.RetrieveImageData, times(1));
        assertThat(config, is(Collections.emptyMap()));
    }

    @Test
    public void successfulConfFileConvertToMap() throws IOException, URISyntaxException {
        // given
        givenListOfImagesAndVolumes();
        givenTheWantedDiskImage();
        mockVdcCommand(VdcActionType.RetrieveImageData,
                successfulVdcReturnValue(load("hosted-engine-config.tar")));
        // when
        Map<String, String> config = fetchConfig();
        // then
        verifyCalled(VdcActionType.RetrieveImageData, times(1));
        assertThat(config, hasKey("sdUUID"));
        assertThat(config, hasKey("host_id"));
    }

    private void givenListOfImagesAndVolumes() {
        mockVdsCommand(VDSCommandType.GetImagesList, successfulReturnValue(Arrays.asList(IMAGE_ID)));
        mockVdsCommand(VDSCommandType.GetVolumesList, successfulReturnValue(Arrays.asList(VOLUME_ID)));
    }

    private void givenTheWantedDiskImage() {
        mockVdsCommand(
                VDSCommandType.GetImageInfo,
                successfulReturnValue(newDisk(HostedEngineConfigFetcher.HOSTED_ENGINE_CONFIGURATION_IMAGE)));
    }

    private void mockVdsCommand(VDSCommandType cmdType, VDSReturnValue returnValue) {
        doReturn(returnValue)
                .when(resourceManager).runVdsCommand(eq(cmdType), any(VDSParametersBase.class));
    }

    private void mockVdcCommand(VdcActionType vdcActionType, VdcReturnValueBase returnValue) {
        doReturn(returnValue)
                .when(backend).runInternalAction(eq(vdcActionType), any(VdcActionParametersBase.class));
    }

    private VDSReturnValue verifyCalled(VDSCommandType vdsmCmd, VerificationMode times) {
        return verify(resourceManager, times).runVdsCommand(eq(vdsmCmd), any());
    }

    private VdcReturnValueBase verifyCalled(VdcActionType vdcActionType, VerificationMode times) {
        return verify(backend, times).runInternalAction(eq(vdcActionType), any(VdcActionParametersBase.class));
    }

    private VDSReturnValue successfulReturnValue(Object value) {
        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        vdsReturnValue.setSucceeded(true);
        vdsReturnValue.setReturnValue(value);
        return vdsReturnValue;
    }

    private VDSReturnValue unsuccessfulReturnValue(Object value) {
        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        vdsReturnValue.setSucceeded(false);
        vdsReturnValue.setReturnValue(value);
        return vdsReturnValue;
    }

    private VdcReturnValueBase successfulVdcReturnValue(Object value) {
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setSucceeded(true);
        returnValue.setActionReturnValue(value);
        return returnValue;
    }

    public Map<String, String> fetchConfig() {
        return configFetcher.fetch();
    }

    public DiskImage newDisk(String description) {
        DiskImage diskImage = new DiskImage();
        diskImage.setDescription(description);
        diskImage.setId(IMAGE_ID);
        diskImage.setImageId(VOLUME_ID);
        diskImage.setSize(1024 >> 20);
        return diskImage;
    }

    private byte[] load(String file) throws IOException, URISyntaxException {
        Path path = Paths.get(ClassLoader.getSystemResource(file).toURI());
        return Files.readAllBytes(path);
    }
}
