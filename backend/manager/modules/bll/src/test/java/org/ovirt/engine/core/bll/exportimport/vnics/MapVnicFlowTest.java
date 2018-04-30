package org.ovirt.engine.core.bll.exportimport.vnics;


import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.exportimport.vnics.MapVnicDataPoints.CLUSTER;
import static org.ovirt.engine.core.bll.exportimport.vnics.MapVnicDataPoints.N1;
import static org.ovirt.engine.core.bll.exportimport.vnics.MapVnicDataPoints.N1_ID;
import static org.ovirt.engine.core.bll.exportimport.vnics.MapVnicDataPoints.P1;
import static org.ovirt.engine.core.bll.exportimport.vnics.MapVnicDataPoints.P1_ID;
import static org.ovirt.engine.core.bll.exportimport.vnics.MapVnicDataPoints.P2;
import static org.ovirt.engine.core.bll.exportimport.vnics.MapVnicFlowTestUtils.makeCtx;
import static org.ovirt.engine.core.bll.exportimport.vnics.MapVnicFlowTestUtils.mappingOf;
import static org.ovirt.engine.core.bll.exportimport.vnics.MapVnicFlowTestUtils.networkClusterOf;
import static org.ovirt.engine.core.bll.exportimport.vnics.MapVnicFlowTestUtils.networkOf;
import static org.ovirt.engine.core.bll.exportimport.vnics.MapVnicFlowTestUtils.printContexts;
import static org.ovirt.engine.core.bll.exportimport.vnics.MapVnicFlowTestUtils.printDataPointDetails;
import static org.ovirt.engine.core.bll.exportimport.vnics.MapVnicFlowTestUtils.profileOf;
import static org.ovirt.engine.core.bll.exportimport.vnics.MapVnicFlowTestUtils.profileViewOf;
import static org.ovirt.engine.core.bll.exportimport.vnics.MapVnicFlowTestUtils.vnicOf;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.ApplyProfileById;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.NetworkAttachedToCluster;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.SourceNameExistsOnEngine;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.TargetIdExistsOnEngine;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.TargetNameExistsOnEngine;
import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.network.VnicProfileViewDao;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MapVnicFlowTest {

    @Mock
    // queried when checking if target network is attached to target cluster
    private NetworkClusterDao mockNetworkClusterDao;
    @Mock
    // queried for the network name when applying a profile to the ovf vnic
    private NetworkDao mockNetworkDao;
    @Mock
    // queried when checking if target profile id exists on engine
    private VnicProfileDao mockVnicProfileDao;
    @Mock
    // queried when checking if target names or source names exist on engine
    private VnicProfileViewDao mockVnicProfileViewDao;
    // The flow to test
    private MapVnicFlow flowUnderTest;
    // test counter for printing debug info
    private static int testCount = 0;
    private static MapVnicDataPoints dataPoints;

    @BeforeAll
    public static void beforeClass() {
        dataPoints = new MapVnicDataPoints();
        dataPoints.prepareTestDataPoints();
    }

    @BeforeEach
    public void before() {
        testCount = 0;
        // instantiate flow once per test object instance because it requires the per-test-instance mocks
        flowUnderTest = new MapVnicFlow(new SourceNameExistsOnEngine(mockVnicProfileViewDao), new TargetIdExistsOnEngine(mockVnicProfileDao),
                new TargetNameExistsOnEngine(mockVnicProfileViewDao), new NetworkAttachedToCluster(mockNetworkClusterDao),
                new NetworkAttachedToCluster(mockNetworkClusterDao), new NetworkAttachedToCluster(mockNetworkClusterDao),
                new ApplyProfileById(mockNetworkDao));
    }

    /**
     * SET A
     * for the all the combinations in the two data point sets A below, the resulting profile
     * should always be an empty one. These combinations cover the following flows:
     * - search matching target specified by name or source specified by name in the dao. Either no profile is returned by
     *   the dao or the profile returned does not match the empty profile on the OVF, or matches it but does not have a
     *   network id on it.
     *
     * expected outcome: no profile is applied
     * tested in {@link MapVnicFlowTest#testSetA}
     * @return : set of no or bad (missing network id) profile views that are returned by {@link VnicProfileViewDao}
     */
    @SuppressWarnings("unchecked")
    public static List<VnicProfileView>[] daoProfileViewsSetA() {
        return new List[] {
            null,
            emptyList(),
            singletonList(VnicProfileView.EMPTY),
            singletonList(profileViewOf(null, null)),
            singletonList(profileViewOf(null, null)),
            singletonList(profileViewOf("", "")),
            singletonList(profileViewOf(N1, P2)) //bad profile - missing network id
        };
    }

    /**
     * @return set of mappings input by the user where there are either:
     * - no mappings
     * - mappings without target
     * - mappings where target profile not found on engine
     * - mappings where source profile not on engine
     */
    public static ExternalVnicProfileMapping[] mappingsSetA() {
        return new ExternalVnicProfileMapping[] {
            null,
            new ExternalVnicProfileMapping(null, null, null), // source is 'no profile', no target
            mappingOf(P1, N1, null), // no target
            mappingOf(null, null, P1, N1) // source is 'no profile', named target
        };
    }

    /**
    * This test runs over the product of the combinations of the two sets which are its inputs.
    * The expected profile on the vnic OVF is always 'no profile'. These combinations only trigger the vnicProfileViewDao,
    * when the a target specified by name or a source specified by name are searched in the dao.
    * The profile returned by the dao does not match the empty profile on the OVF, so no profile is applied to the OVF vnic.
    */
    @ParameterizedTest
    @MethodSource
    public void testSetA(List<VnicProfileView> daoProfileViews, ExternalVnicProfileMapping mapping) {

        printDataPointDetails(daoProfileViews, mapping, testCount++, "set A");
        when(mockVnicProfileViewDao.getAllForCluster(CLUSTER)).thenReturn(daoProfileViews);
        testInner(vnicOf(null, null), mapping, vnicOf(null, null));
        verify(mockVnicProfileDao, never()).get(any());
        verify(mockNetworkDao, never()).get(any());
    }

    public static Stream<Arguments> testSetA() {
        // Permute the two arrays
        return Arrays.stream(daoProfileViewsSetA()).flatMap(x -> Arrays.stream(mappingsSetA()).map(y -> Arguments.of(x, y)));
    }

    /**
     * use different combinations of source+target names+ids to test the content
     * of the vnic on the OVF after applying the flow under test to it
     */
    @Test
    public void testSetB() {

        mock();
        dataPoints.allProfileCombinations.forEach( dataPoint -> {
            printDataPointDetails(null, (ExternalVnicProfileMapping) dataPoint[1], testCount++, "set B");
            testInner((VmNetworkInterface) dataPoint[0], (ExternalVnicProfileMapping) dataPoint[1], (VmNetworkInterface) dataPoint[2]);
        });
    }

    private void testInner(VmNetworkInterface initialOvfVnic, ExternalVnicProfileMapping mapping, VmNetworkInterface expectedOvfVnic) {
        // arrange
        MapVnicContext underTestCtx = makeCtx(CLUSTER, mapping, initialOvfVnic);
        MapVnicContext expectedCtx = makeCtx(CLUSTER, expectedOvfVnic);
        // act
        flowUnderTest.getHead().process(underTestCtx);
        // assert
        printContexts(expectedCtx, underTestCtx);
        assertEquals(expectedCtx.getOvfVnic(), underTestCtx.getOvfVnic());
        assertTrue(expectedCtx.getException() == null && underTestCtx.getException() == null ||
                Objects.equals(expectedCtx.getException().getMessage(), underTestCtx.getException().getMessage()));
    }

    private void mock() {
        when(mockVnicProfileDao.get(P1_ID)).thenReturn(profileOf(P1_ID, P1, N1_ID));
        when(mockVnicProfileViewDao.getAllForCluster(CLUSTER)).thenReturn(singletonList(profileViewOf(P1_ID, P1, N1, N1_ID)));
        when(mockNetworkClusterDao.get(new NetworkClusterId(CLUSTER, N1_ID))).thenReturn(networkClusterOf(N1_ID));
        when(mockNetworkDao.get(N1_ID)).thenReturn(networkOf(N1_ID, N1));
    }
}
