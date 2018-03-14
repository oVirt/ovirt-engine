package org.ovirt.engine.core.common.businessentities;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * C - HardDisk, D - CDROM, N - Network first 3 numbers for backward compatibility
 */
public enum BootSequence {
    C(0),
    D(10),
    N(2),
    DC(1, D, C),
    CDN(3, C, D, N),
    CND(4, C, N, D),
    DCN(5, D, C, N),
    DNC(6, D, N, C),
    NCD(7, N, C, D),
    NDC(8, N, D, C),
    CD(9, C, D),
    CN(11, C, N),
    DN(12, D, N),
    NC(13, N, C),
    ND(14, N, D);

    private int intValue;
    private final List<BootSequence> components;
    private static Map<Integer, BootSequence> mappings =
            Stream.of(values()).collect(Collectors.toMap(BootSequence::getValue, Function.identity()));

    private BootSequence(int value, BootSequence... composedOf) {
        intValue = value;

        if (composedOf == null || composedOf.length == 0) {
            // leaf contains itself
            this.components = Arrays.asList(this);
        } else {
            this.components = Arrays.asList(composedOf);
        }

    }

    public int getValue() {
        return intValue;
    }

    public static BootSequence forValue(int value) {
        return mappings.get(value);
    }

    /**
     * Returns true if and only if all the components of the subsequence are in this sequence (ignoring order)
     * <p>
     * For example, D is a subsequence of D, DN, CD..., DN is a subsequence of DNC etc
     * <p>
     */
    public boolean containsSubsequence(BootSequence subsequence) {
        if (subsequence == null) {
            return false;
        }

        return components.containsAll(subsequence.components);
    }
}
