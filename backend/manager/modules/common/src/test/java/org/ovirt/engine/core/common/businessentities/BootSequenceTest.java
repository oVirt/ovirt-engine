package org.ovirt.engine.core.common.businessentities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BootSequenceTest {

    @Test
    public void nothingContainsNull() {
        assertFalse(BootSequence.C.containsSubsequence(null));
    }

    @Test
    public void leafContainsItself() {
        assertTrue(BootSequence.C.containsSubsequence(BootSequence.C));
    }

    @Test
    public void threeSequence_oneSubsequence() {
        assertTrue(BootSequence.CDN.containsSubsequence(BootSequence.C));
    }

    @Test
    public void threeSequence_twoSubsequence() {
        assertTrue(BootSequence.CDN.containsSubsequence(BootSequence.CD));
    }

    @Test
    public void threeSequence_twoSubsequence_secondPosition() {
        assertTrue(BootSequence.CDN.containsSubsequence(BootSequence.DN));
    }

    @Test
    public void threeSequence_oneSubsequence_secondPosition() {
        assertTrue(BootSequence.CDN.containsSubsequence(BootSequence.D));
    }

    @Test
    public void threeSequence_oneSubsequence_thirdPosition() {
        assertTrue(BootSequence.CDN.containsSubsequence(BootSequence.N));
    }

    @Test
    public void threeSequence_threeSubsequence() {
        assertTrue(BootSequence.CDN.containsSubsequence(BootSequence.CDN));
    }

    @Test
    public void oneSequence_oneWrongSubsequence() {
        assertFalse(BootSequence.C.containsSubsequence(BootSequence.D));
    }

    @Test
    public void oneSequence_twoSubsequence() {
        assertFalse(BootSequence.C.containsSubsequence(BootSequence.DC));
    }

    @Test
    public void oneSequence_threeSubsequence() {
        assertFalse(BootSequence.C.containsSubsequence(BootSequence.DCN));
    }

    @Test
    public void twoSequence_twoSubsequence_oppositeOrder() {
        assertTrue(BootSequence.CD.containsSubsequence(BootSequence.DC));
    }

    @Test
    public void twoSequence_oneSubsequence_notInItAtAll() {
        assertFalse(BootSequence.CD.containsSubsequence(BootSequence.N));
    }

}
