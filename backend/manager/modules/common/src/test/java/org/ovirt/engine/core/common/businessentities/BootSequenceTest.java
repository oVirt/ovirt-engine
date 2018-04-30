package org.ovirt.engine.core.common.businessentities;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
    public void threeSequenceOneSubsequence() {
        assertTrue(BootSequence.CDN.containsSubsequence(BootSequence.C));
    }

    @Test
    public void threeSequenceTwoSubsequence() {
        assertTrue(BootSequence.CDN.containsSubsequence(BootSequence.CD));
    }

    @Test
    public void threeSequenceTwoSubsequenceSecondPosition() {
        assertTrue(BootSequence.CDN.containsSubsequence(BootSequence.DN));
    }

    @Test
    public void threeSequenceOneSubsequenceSecondPosition() {
        assertTrue(BootSequence.CDN.containsSubsequence(BootSequence.D));
    }

    @Test
    public void threeSequenceOneSubsequenceThirdPosition() {
        assertTrue(BootSequence.CDN.containsSubsequence(BootSequence.N));
    }

    @Test
    public void threeSequenceThreeSubsequence() {
        assertTrue(BootSequence.CDN.containsSubsequence(BootSequence.CDN));
    }

    @Test
    public void oneSequenceOneWrongSubsequence() {
        assertFalse(BootSequence.C.containsSubsequence(BootSequence.D));
    }

    @Test
    public void oneSequenceTwoSubsequence() {
        assertFalse(BootSequence.C.containsSubsequence(BootSequence.DC));
    }

    @Test
    public void oneSequenceThreeSubsequence() {
        assertFalse(BootSequence.C.containsSubsequence(BootSequence.DCN));
    }

    @Test
    public void twoSequenceTwoSubsequenceOppositeOrder() {
        assertTrue(BootSequence.CD.containsSubsequence(BootSequence.DC));
    }

    @Test
    public void twoSequenceOneSubsequenceNotInItAtAll() {
        assertFalse(BootSequence.CD.containsSubsequence(BootSequence.N));
    }

}
