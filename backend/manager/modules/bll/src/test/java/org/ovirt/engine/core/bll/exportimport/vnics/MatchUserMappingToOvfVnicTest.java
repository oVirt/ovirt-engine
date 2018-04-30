package org.ovirt.engine.core.bll.exportimport.vnics;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicsHandlers.MatchUserMappingToOvfVnic;
import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;

public class MatchUserMappingToOvfVnicTest {

    private static final boolean GENERATE_DEBUG_INFO = false;
    private static MapVnicDataPoints dataPoints;
    private static MatchUserMappingToOvfVnic matcher;

    @BeforeAll
    public static void beforeClass() {
        dataPoints = new MapVnicDataPoints();
        dataPoints.prepareTestDataPoints();
        dataPoints.prepareNonMatchingSourcesDataPoints();
        matcher = new MatchUserMappingToOvfVnic();
    }

    @Test
    public void test() {
        AtomicInteger count = new AtomicInteger(0);
        dataPoints.matchUserMappingToOvfVnicDataPoints.forEach( dataPoint -> {
            test(dataPoint, count.get());
            count.getAndIncrement();
        });
    }

    private void test(Object[] dataPoint, int count) {
        // Translate input to human readable
        VmNetworkInterface ovfVnicUnderTest = (VmNetworkInterface) dataPoint[0];
        ExternalVnicProfileMapping userMappingUnderTest = (ExternalVnicProfileMapping) dataPoint[1];
        Boolean expectedIsMatch = (Boolean) dataPoint[2];
        // Arrange
        MapVnicsContext ctx = new MapVnicsContext("test");
        ctx.setOvfVnics(singletonList(ovfVnicUnderTest));
        ctx.setUserMappings(singletonList(userMappingUnderTest));
        // Act
        matcher.handle(ctx);
        // Assert
        print(ovfVnicUnderTest, ctx, count);
        ExternalVnicProfileMapping expectedMatchingMapping = expectedIsMatch ? userMappingUnderTest : null;
        assertEquals(expectedMatchingMapping, ctx.getMatched().get(ovfVnicUnderTest));
        assertTrue(ctx.getMatched().size() > 0);
        assertNull(ctx.getException());
    }

    private void print(VmNetworkInterface ovfVnicUnderTest, MapVnicsContext ctx, int count) {
        if (!GENERATE_DEBUG_INFO) {
            return;
        }
        System.out.println("---------------- test #" + count + " ----------------");
        System.out.println("ovf vnic:" + ovfVnicUnderTest.getVnicProfileName() + " " + ovfVnicUnderTest.getNetworkName());
        ExternalVnicProfileMapping matched = ctx.getMatched().get(ovfVnicUnderTest);
        if (matched != null) {
            System.out.println("matched:" + matched.getSourceProfileName() + " " + matched.getSourceNetworkName());
        } else {
            System.out.println("matched is null");
        }
    }
}
