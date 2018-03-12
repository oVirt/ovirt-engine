package org.ovirt.engine.core.bll.exportimport.vnics;

import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.ApplyProfileById;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.NetworkAttachedToCluster;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.SourceNameExistsOnEngine;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.TargetIdExistsOnEngine;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.TargetNameExistsOnEngine;
import org.ovirt.engine.core.common.flow.Flow;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.network.VnicProfileViewDao;

@RunWith(Parameterized.class)
public class FlowDigraphTest {

    private static final boolean GENERATE_DEBUG_INFO = false;
    private final String flowName;
    @Rule
    public MockitoRule initMocks = MockitoJUnit.rule();

    private Flow<MapVnicContext> underTest;
    private final String circleSize;

    public FlowDigraphTest(String flowName, Flow<MapVnicContext> underTest, String circleSize) {
        this.flowName = flowName;
        this.underTest = underTest;
        this.circleSize = circleSize;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> params() {

        return Arrays.asList(new Object[][] {
                { "Map single vnic", getMapVnicFlow(), "19" },
                { "Map all vnics", getMapVnicsCollectionFlow(), "11" }
        });
    }

    /**
     * Print a digraph representation of the flow. The printout
     * of this test may be inserted into a digraph viewer (e.g.
     * http://www.webgraphviz.com) which displays the flow
     * graph visually in order to analyze its correctness.
     *
     */
    @Test
    public void printFlowAsDigraph() {
        if (!GENERATE_DEBUG_INFO) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        underTest.getHead().print(sb);
        String graph = sb.toString().replaceAll("([a-z]{1})([A-Z]{1})", "$1\\\\n$2");
        StringBuilder print = new StringBuilder();
        print.append("digraph \"" + flowName + "\" {\n");
        print.append("label=\"" + flowName + "\";\n");
        print.append("node [shape=egg,style=filled,color=\"0.650 0.200 1.000\"];\n");
        print.append("rankdir=LR;\n");
        print.append("size=\"" + circleSize + "\";\n");
        print.append(graph);
        print.append("}\n");
        System.out.println(print);
    }

    /**
     * Test that the flow is a Directed Acyclic Graph
     */
    @Test
    public void testDAG() {
        //TODO: test that there is no DAG in the flow
    }


    private static MapVnicFlow getMapVnicFlow() {
        return new MapVnicFlow(new SourceNameExistsOnEngine(mock(VnicProfileViewDao.class)), new TargetIdExistsOnEngine(mock(VnicProfileDao.class)),
                new TargetNameExistsOnEngine(mock(VnicProfileViewDao.class)), new NetworkAttachedToCluster(mock(NetworkClusterDao.class)),
                new NetworkAttachedToCluster(mock(NetworkClusterDao.class)), new NetworkAttachedToCluster(mock(NetworkClusterDao.class)),
                new ApplyProfileById(mock(NetworkDao.class)));
    }

    private static MapVnicsFlow getMapVnicsCollectionFlow () {
        return MapVnicsFlow.of(mock(VnicProfileViewDao.class), mock(VnicProfileDao.class),
                mock(NetworkClusterDao.class), mock(NetworkDao.class));
    }
}
