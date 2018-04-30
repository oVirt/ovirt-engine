package org.ovirt.engine.core.bll.exportimport.vnics;


import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.ovirt.engine.core.common.flow.HandlerOutcome.NEUTRAL;
import static org.ovirt.engine.core.common.flow.HandlerOutcome.SUCCESS;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicsHandlers.MatchUserMappingToOvfVnic;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicsHandlers.ReportResults;
import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.network.VnicProfileViewDao;

public class MapVnicsFlowTest {

    private static final String NETWORK_NAME = "network name";
    private static final String PROFILE1_NAME = "vnic profile1 name";
    private static final String PROFILE2_NAME = "vnic profile2 name";
    private static final Guid PROFILE_ID = Guid.newGuid();

    private static final ExternalVnicProfileMapping NO_SOURCE_MAPPING = new ExternalVnicProfileMapping(null, null, PROFILE_ID);
    private static final ExternalVnicProfileMapping NO_TARGET_MAPPING = new ExternalVnicProfileMapping(NETWORK_NAME, PROFILE2_NAME, null);
    private static final ExternalVnicProfileMapping FULL_MAPPING =  new ExternalVnicProfileMapping(NETWORK_NAME, PROFILE1_NAME, PROFILE_ID);

    private MapVnicsFlow underTest;

    public static Stream<Arguments> flow() {

        Pair nullMappingsAndNullOvfs = testNullMappingsAndNullOvfs();
        Pair nullMappingsAndEmptyOvfs = testNullMappingsAndEmptyOvfs();
        Pair emptyMappingsAndEmptyOvfs = testEmptyMappingsAndEmptyOvfs();
        Pair emptyMappingsAndNullOvfs = testEmptyMappingsAndNullOvfs();
        Pair someMappingsAndNullOvfs = testSomeMappingsAndNullOvfs();

        return Stream.of(
            Arguments.of(nullMappingsAndNullOvfs.getFirst(),       nullMappingsAndNullOvfs.getSecond()  ),
            Arguments.of(nullMappingsAndEmptyOvfs.getFirst(),      nullMappingsAndEmptyOvfs.getSecond() ),
            Arguments.of(emptyMappingsAndEmptyOvfs.getFirst(),     emptyMappingsAndEmptyOvfs.getSecond()),
            Arguments.of(emptyMappingsAndNullOvfs.getFirst(),      emptyMappingsAndNullOvfs.getSecond() ),
            Arguments.of(someMappingsAndNullOvfs.getFirst(),       someMappingsAndNullOvfs.getSecond()  )
        );
    }

    /**
     * Init underTest with on the fly mocks. This test does not use these mocks,
     * but {@link MapVnicsFlow} must get some instantiated objects in order to work.
     */
    @BeforeEach
    public void before() {
        underTest = MapVnicsFlow.of(
                mock(VnicProfileViewDao.class), mock(VnicProfileDao.class), mock(NetworkClusterDao.class), mock(NetworkDao.class));
    }

    @ParameterizedTest
    @MethodSource
    public void flow(MapVnicsContext expected, MapVnicsContext actual) {
        // act
        underTest.getHead().process(actual);
        // assert
        assertEquals(expected, actual);
    }

    // test cases

    /** 0
     * When null OVFs are passed to the flow, there is nothing to map so there should
     * be no sub-flows or sub-contexts. the collection flow should exit with NOP
     */
    private static Pair<MapVnicsContext, MapVnicContext> testNullMappingsAndNullOvfs() {
        MapVnicsContext underTest = setupContext(null, null, "0");
        MapVnicsContext expected = setupContext(null, null, "0");
        expected.trace(NEUTRAL, MatchUserMappingToOvfVnic.class);
        expected.trace(SUCCESS, ReportResults.class);
        return new Pair(expected, underTest);
    }

    /** 1
     * When empty OVFs are passed to the flow, there is nothing to map so there should
     * be no sub-flows or sub-contexts. the collection flow should exit with NOP
     */
    private static Pair<MapVnicsContext, MapVnicContext> testNullMappingsAndEmptyOvfs() {
        MapVnicsContext underTest = setupContext(null, emptyList(), "1");
        MapVnicsContext expected = setupContext(null, emptyList(), "1");
        expected.trace(NEUTRAL, MatchUserMappingToOvfVnic.class);
        expected.trace(SUCCESS, ReportResults.class);
        return new Pair(expected, underTest);
    }

    /** 2
     * When null OVFs are passed to the flow, there is nothing to map so there should
     * be no sub-flows or sub-contexts. the collection flow should exit with NOP
     */
    private static Pair<MapVnicsContext, MapVnicContext> testEmptyMappingsAndEmptyOvfs() {
        MapVnicsContext underTest = setupContext(emptyList(), emptyList(), "2");
        MapVnicsContext expected = setupContext(emptyList(), emptyList(), "2");
        expected.trace(NEUTRAL, MatchUserMappingToOvfVnic.class);
        expected.trace(SUCCESS, ReportResults.class);
        return new Pair(expected, underTest);
    }

    /** 3
     * When null OVFs are passed to the flow, there is nothing to map so there should
     * be no sub-flows or sub-contexts. the collection flow should exit with NOP
     */
    private static Pair<MapVnicsContext, MapVnicContext> testEmptyMappingsAndNullOvfs() {
        MapVnicsContext underTest = setupContext(emptyList(), null, "3");
        MapVnicsContext expected = setupContext(emptyList(), null, "3");
        expected.trace(NEUTRAL, MatchUserMappingToOvfVnic.class);
        expected.trace(SUCCESS, ReportResults.class);
        return new Pair(expected, underTest);
    }

    /** 4
     * When null OVFs are passed to the flow, there is nothing to map so there should
     * be no sub-flows or sub-contexts. the collection flow should exit with NOP
     */
    private static Pair<MapVnicsContext, MapVnicContext> testSomeMappingsAndNullOvfs() {
        List<ExternalVnicProfileMapping> mappings = asList(FULL_MAPPING, NO_TARGET_MAPPING, NO_SOURCE_MAPPING);
        MapVnicsContext underTest = setupContext(mappings, null, "4");
        MapVnicsContext expected = setupContext(mappings, null, "4");
        expected.trace(NEUTRAL, MatchUserMappingToOvfVnic.class);
        expected.trace(SUCCESS, ReportResults.class);
        return new Pair(expected, underTest);
    }

    /**
     * setup a context with user input - mappings and ovf vnics - that would be under test
     */
    private static MapVnicsContext setupContext(List<ExternalVnicProfileMapping> mappings, List<VmNetworkInterface> vnics, String name){
        return new MapVnicsContext(name).setUserMappings(mappings).setOvfVnics(vnics);
    }
}
