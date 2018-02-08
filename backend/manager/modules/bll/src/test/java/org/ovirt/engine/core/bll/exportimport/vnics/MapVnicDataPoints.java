package org.ovirt.engine.core.bll.exportimport.vnics;

import static org.ovirt.engine.core.bll.exportimport.vnics.MapVnicFlowTestUtils.mappingOf;
import static org.ovirt.engine.core.bll.exportimport.vnics.MapVnicFlowTestUtils.vnicOf;

import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

/**
 * <pre>
 * Data points for testing three flows:
 * - Map vnic according to user mapping when fallback is 'apply no profile'
 * - Map vnic according to user mapping when fallback is 'abort'
 * - Match ovf vnic profile to user mapping profile
 *
 * This class strives to achieve several usability goals:
 * - separate data points from tests and consolidate them in a single location
 * - reuse data point inputs in several tests
 * - make adding\editing data points easy
 * - make data points independent of each other
 * - display data points in a format close to tabular form for easy reading\debugging
 * - provide flexiblility for adding a data point to all tests or to some only
 *
 * Those goals are achieved with a fluent api of builder methods for specifying
 * the input of all tests once, and an expected outcome per test:
 *
 * <code>
 *      underTest(...inputs...).expectedTest1(..expected..).expectedTest2(...).expectedTest3(...);
 * </code>
 *
 * If a data point input is not relevant to a test, there is no need to specify it on this data point.
 * Once the data points have been constructed, a list containing all the input-expected records relevant
 * for each test is available for consumption by the test.
 *
 * The current Junit framework supports data points and parametrized tests but they have the
 * following shortcomings (IMHO):
 * - data points\parameters declaration must be in same file as tests
 * - parameters are run for all tests in file - cannot run some sets on some tests
 * - data points are difficult to reuse because spliting a data point (record) to
 *   input and output would create unwanted permutations to be tested.
 *
 * TODO: add formatter that will output the data points to an xml format that is consumable by
 * TODO: the engine REST-API module, so that it can be tested as well with these data points.
 * </pre>
 *
 *
 * @see MapVnicFlowTest
 * @see MatchUserMappingToOvfVnicTest
 *
 */
public class MapVnicDataPoints {

    // Profile, network names
    static final String P1 = "profile_name_1";
    static final String P2 = "profile_name_2";
    static final String N1 = "network_name_1";
    static final String N2 = "network_name_2";
    // Profile, network, cluster id's
    static final Guid P1_ID = Guid.newGuid();
    static final Guid P2_ID = Guid.newGuid();
    static final Guid N1_ID = Guid.newGuid();
    static final Guid CLUSTER = Guid.newGuid();

    // list of data points for tests with the 'apply no profile' fallback flow
    List<Object[]> allProfileCombinations;
    // list of data points for tests of {@link MatchUserMappingToOvfVnicTest}
    List<Object[]> matchUserMappingToOvfVnicDataPoints;
    // temp variables to hold the current under test input
    private ExternalVnicProfileMapping underTestUserMapping;
    private VmNetworkInterface underTestOvfVnic;

    MapVnicDataPoints() {
        allProfileCombinations = new LinkedList<>();
        matchUserMappingToOvfVnicDataPoints = new LinkedList<>();
    }

    /**
     * @param ovfVnic the vnic details found in the OVF of an imported VM - input to the test
     * @param mapping user mapping from request - input to the test
     */
    private MapVnicDataPoints underTest(VmNetworkInterface ovfVnic, ExternalVnicProfileMapping mapping) {
        // must not assign the parameters to fields, because they will override each other
        this.underTestOvfVnic = ovfVnic;
        this.underTestUserMapping = mapping;
        return this;
    }

    /**
     * @param expectedOvfVnicOnApply - the expected ovf vnic when the 'apply no profile' fallback is applied to the flow
     */
    private MapVnicDataPoints expectedVnic(VmNetworkInterface expectedOvfVnicOnApply) {
        allProfileCombinations.add(new Object[]{underTestOvfVnic, underTestUserMapping, expectedOvfVnicOnApply});
        return this;
    }

    /**
     * @param expectedMatch - expected value of whether the input ovf vnic values match the input user mapping values
     */
    private MapVnicDataPoints expectedMatch(boolean expectedMatch) {
        matchUserMappingToOvfVnicDataPoints.add(new Object[]{underTestOvfVnic, underTestUserMapping, expectedMatch});
        return this;
    }

    /**
     * Prepare lists of data points for different tests. The input for each data point is the same but several expected outcomes are
     * documented with a list for each.
     * Each list can be then provided to a different test and the expected of every data point in the list can be verified against its input.
     * @see MapVnicFlowTest
     * @see MatchUserMappingToOvfVnicTest
     *
     * A null entry in the vnic profile is equivalent to an empty string in the user mapping
     * Testing both for null and empty source names in the mapping
     * @see ExternalVnicProfileMapping
     */
    void prepareTestDataPoints() {

        sourceNoProfileTargetNotInDao(null, null);
        sourceNoProfileTargetNotInDao("", "");

        sourceNoProfileTargetInDao(null, null);
        sourceNoProfileTargetInDao("", "");

        sourceInDaoTargetInDao();
        sourceInDaoTargetNotInDao();

        invalidSourceTargetInDao(null, null);
        invalidSourceTargetInDao("", "");

        invalidSourceTargetNotInDao(null, null);
        invalidSourceTargetNotInDao("", "");

        sourceNotInDaoTargetNotSpecified();
        sourceNotInDaoAllTargets();
    }

    /**
     * source has 'no profile' which is a vnic with nulls
     */
    private void sourceNoProfileTargetNotInDao(String noProfile, String noNetwork) {
        // target not specified -> apply 'no profile'
        underTest(vnicOf(null, null), null).expectedVnic(vnicOf(null, null)).expectedMatch(true);
        // target id not specified -> apply 'no profile'
        underTest(vnicOf(null, null), mappingOf(noProfile, noNetwork, null)).expectedVnic(vnicOf(null, null)).expectedMatch(true);
        // target names not specified -> apply 'no profile'
        underTest(vnicOf(null, null), mappingOf(noProfile, noNetwork, null, null)).expectedVnic(vnicOf(null, null)).expectedMatch(true);
        // target id not found in dao -> apply 'no profile'
        underTest(vnicOf(null, null), mappingOf(noProfile, noNetwork, P2_ID)).expectedVnic(vnicOf(null, null)).expectedMatch(true);
        // target names not found in dao -> apply 'no profile'
        underTest(vnicOf(null, null), mappingOf(noProfile, noNetwork, N1, P2)).expectedVnic(vnicOf(null, null)).expectedMatch(true);
        // target names not found in dao -> apply 'no profile'
        underTest(vnicOf(null, null), mappingOf(noProfile, noNetwork, N2, P1)).expectedVnic(vnicOf(null, null)).expectedMatch(true);
        // target network name missing -> apply 'no profile'
        underTest(vnicOf(null, null), mappingOf(noProfile, noNetwork, P1, noNetwork)).expectedVnic(vnicOf(null, null)).expectedMatch(true);
        // target profile name missing -> apply 'no profile'
        underTest(vnicOf(null, null), mappingOf(noProfile, noNetwork, noProfile, N1)).expectedVnic(vnicOf(null, null)).expectedMatch(true);
    }

    /**
     * source has 'no profile' which is a vnic with nulls
     */
    private void sourceNoProfileTargetInDao(String noProfile, String noNetwork) {
        // target names are emtpy string -> apply target 'no profile'
        underTest(vnicOf(null, null), mappingOf(noProfile, noNetwork, "", "")).expectedVnic(vnicOf(null, null)).expectedMatch(true);
        // target id found in dao -> apply target profile
        underTest(vnicOf(null, null), mappingOf(noProfile, noNetwork, P1_ID)).expectedVnic(vnicOf(P1_ID, P1, N1)).expectedMatch(true);
        // target names found in dao -> apply target profile
        underTest(vnicOf(null, null), mappingOf(noProfile, noNetwork, P1, N1)).expectedVnic(vnicOf(P1_ID, P1, N1)).expectedMatch(true);
    }

    private void sourceInDaoTargetInDao() {
        // target names are empty string  -> apply target 'no profile'
        underTest(vnicOf(P1, N1), mappingOf(P1, N1, "", "")).expectedVnic(vnicOf(null, null)).expectedMatch(true);
        // target id found in dao - apply target profile
        underTest(vnicOf(P1, N1), mappingOf(P1, N1, P1_ID)).expectedVnic(vnicOf(P1_ID, P1, N1)).expectedMatch(true);
        // target names found in dao -> apply target profile
        underTest(vnicOf(P1, N1), mappingOf(P1, N1, P1, N1)).expectedVnic(vnicOf(P1_ID, P1, N1)).expectedMatch(true);
    }

    private void sourceInDaoTargetNotInDao() {
        // target not specified -> apply source profile
        underTest(vnicOf(P1, N1), null).expectedVnic(vnicOf(P1_ID, P1, N1)).expectedMatch(true);
        // target id not specified -> apply source profile
        underTest(vnicOf(P1, N1), mappingOf(P1, N1, null)).expectedVnic(vnicOf(P1_ID, P1, N1)).expectedMatch(true);
        // target names not specified, source profile found in dao -> apply source profile
        underTest(vnicOf(P1, N1), mappingOf(P1, N1, null, null)).expectedVnic(vnicOf(P1_ID, P1, N1)).expectedMatch(true);
        // target id not found in dao -> apply source profile
        underTest(vnicOf(P1, N1), mappingOf(P1, N1, P2_ID)).expectedVnic(vnicOf(P1_ID, P1, N1)).expectedMatch(true);
        // target names not found in dao -> apply source profile
        underTest(vnicOf(P1, N1), mappingOf(P1, N1, N1, P2)).expectedVnic(vnicOf(P1_ID, P1, N1)).expectedMatch(true);
        // target names not found in dao -> apply source profile
        underTest(vnicOf(P1, N1), mappingOf(P1, N1, N2, P1)).expectedVnic(vnicOf(P1_ID, P1, N1)).expectedMatch(true);
        // target network name missing -> apply source profile
        underTest(vnicOf(P1, N1), mappingOf(P1, N1, P1, null)).expectedVnic(vnicOf(P1_ID, P1, N1)).expectedMatch(true);
        // target profile name missing -> apply source profile
        underTest(vnicOf(P1, N1), mappingOf(P1, N1, null, N1)).expectedVnic(vnicOf(P1_ID, P1, N1)).expectedMatch(true);
    }

    private void sourceNotInDaoTargetNotSpecified() {
        // target not specified, source not found in dao -> apply 'no profile'
        underTest(vnicOf(P1, null), null).expectedVnic(vnicOf(null, null)).expectedMatch(true);
        // target not specified, source not found in dao -> apply 'no profile'
        underTest(vnicOf(null, N1), null).expectedVnic(vnicOf(null, null)).expectedMatch(true);
    }

    private void invalidSourceTargetInDao(String noProfile, String noNetwork) {
        // target id found in dao -> apply target profile
        underTest(vnicOf(P1, null), mappingOf(P1, noNetwork, P1_ID)).expectedVnic(vnicOf(P1_ID, P1, N1)).expectedMatch(true);
        // target id found in dao -> apply target profile
        underTest(vnicOf(null, N1), mappingOf(noProfile, N1, P1_ID)).expectedVnic(vnicOf(P1_ID, P1, N1)).expectedMatch(true);
        // target names found in dao -> apply target profile
        underTest(vnicOf(P1, null), mappingOf(P1, noNetwork, P1, N1)).expectedVnic(vnicOf(P1_ID, P1, N1)).expectedMatch(true);
        // target names found in dao -> apply target profile
        underTest(vnicOf(null, N1), mappingOf(noProfile, N1, P1, N1)).expectedVnic(vnicOf(P1_ID, P1, N1)).expectedMatch(true);
    }

    private void invalidSourceTargetNotInDao(String noProfile, String noNetwork) {
        // target id not found in dao -> apply 'no profile'
        underTest(vnicOf(P1, null), mappingOf(P1, noNetwork, P2_ID)).expectedVnic(vnicOf(null, null)).expectedMatch(true);
        // target id not found in dao -> apply 'no profile'
        underTest(vnicOf(null, N1), mappingOf(noProfile, N1, P2_ID)).expectedVnic(vnicOf(null, null)).expectedMatch(true);
        // target names not found in dao -> apply 'no profile'
        underTest(vnicOf(P1, null), mappingOf(P1, noNetwork, P2, N2)).expectedVnic(vnicOf(null, null)).expectedMatch(true);
        // target names not found in dao -> apply 'no profile'
        underTest(vnicOf(null, N1), mappingOf(noProfile, N1, P2, N2)).expectedVnic(vnicOf(null, null)).expectedMatch(true);
    }

    private void sourceNotInDaoAllTargets() {
        // target not specified, source profile not found in dao -> apply 'no profile'
        underTest(vnicOf(P2, N2), null).expectedVnic(vnicOf(null, null)).expectedMatch(true);
        // target id found in dao -> apply target profile
        underTest(vnicOf(P2, N2), mappingOf(P2, N2, P1_ID)).expectedVnic(vnicOf(P1_ID, P1, N1)).expectedMatch(true);
        // target names found in dao -> apply target profile
        underTest(vnicOf(P2, N2), mappingOf(P2, N2, P1, N1)).expectedVnic(vnicOf(P1_ID, P1, N1)).expectedMatch(true);
        // target names are emtpy string -> apply target 'no profile'
        underTest(vnicOf(P2, N2), mappingOf(P2, N2, "", "")).expectedVnic(vnicOf(null, null)).expectedMatch(true);
        // target id not found in dao -> apply 'no profile'
        underTest(vnicOf(P2, N2), mappingOf(P2, N2, P2_ID)).expectedVnic(vnicOf(null, null)).expectedMatch(true);
        // target names not found in dao -> apply 'no profile'
        underTest(vnicOf(P2, N2), mappingOf(P2, N2, P2, N2)).expectedVnic(vnicOf(null, null)).expectedMatch(true);
    }

    /**
     * more data points for {@link MatchUserMappingToOvfVnicTest}
     */
    public void prepareNonMatchingSourcesDataPoints() {
        underTest(vnicOf(P1, N1), mappingOf(P2, N2, null)).expectedMatch(false);
        underTest(vnicOf(P1, N2), mappingOf(P2, N2, null)).expectedMatch(false);
        underTest(vnicOf(P2, N1), mappingOf(P2, N2, null)).expectedMatch(false);
        underTest(vnicOf(P2, null), mappingOf(P2, N2, null)).expectedMatch(false);
        underTest(vnicOf(null, N2), mappingOf(P2, N2, null)).expectedMatch(false);

        underTest(vnicOf(P2, N2), mappingOf(P2, null, null)).expectedMatch(false);
        underTest(vnicOf(P2, N2), mappingOf(P2, "", null)).expectedMatch(false);
        underTest(vnicOf(P2, N2), mappingOf(null, N2, null)).expectedMatch(false);
        underTest(vnicOf(P2, N2), mappingOf("", N2, null)).expectedMatch(false);
    }
}
