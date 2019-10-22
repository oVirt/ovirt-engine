/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.ovirt.engine.core.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.utils.SizeConverter.SizeUnit;

public class SizeConverterTest {
    @Test
    public void testConvertMBToBytes() {
        long megabytes = 3L;
        long bytes = SizeConverter.convert(megabytes, SizeConverter.SizeUnit.MiB,
                SizeConverter.SizeUnit.BYTES).longValue();
        assertEquals(3145728, bytes);
    }

    @Test
    public void testCobvertGBToBytes() {
        long gigabytes = 3L;
        long bytes = SizeConverter.convert(gigabytes, SizeConverter.SizeUnit.GiB,
                SizeConverter.SizeUnit.BYTES).longValue();
        assertEquals(3221225472L, bytes);
    }

    @Test
    public void testConvertBytestoGB() {
        long bytes = 3221228000L;
        int gigabytes = SizeConverter.convert(bytes, SizeConverter.SizeUnit.BYTES,
                SizeConverter.SizeUnit.GiB).intValue();
        assertEquals(3, gigabytes);
    }

    @Test
    public void testConvertBytestoMB() {
        long bytes = 3160000L;
        int megabytes = SizeConverter.convert(bytes, SizeConverter.SizeUnit.BYTES,
                SizeConverter.SizeUnit.MiB).intValue();
        assertEquals(3, megabytes);
    }

    @Test
    public void testConvertMegaBytesToTB() {
        long mb = 5 * 1024 * 1024;
        int tbs = SizeConverter.convert(mb, SizeUnit.MiB, SizeUnit.TiB).intValue();
        assertEquals(5, tbs);
    }

    @Test
    public void testConvertTiBToYiB() {
        double tib = 100 * Math.pow(1024, 4);
        int yib = SizeConverter.convert((long)tib, SizeUnit.TiB, SizeUnit.YiB).intValue();
        assertEquals(100, yib);
    }

    @Test
    public void testConvertMiBToEiB() {
        double mib = 15 * Math.pow(1024, 4);
        int eib = SizeConverter.convert((long)mib, SizeUnit.MiB, SizeUnit.EiB).intValue();
        assertEquals(15, eib);
    }

    @Test
    public void testConvertPiBToZiB() {
        double pib = 15 * Math.pow(1024, 2);
        int zib = SizeConverter.convert((long)pib, SizeUnit.PiB, SizeUnit.ZiB).intValue();
        assertEquals(15, zib);
    }

    @Test
    public void testGetMathOperationSafeOperands() {
        List<Pair<SizeUnit, Double>> expected = Arrays.asList(
                new Pair<>(SizeUnit.KiB, 1D),
                new Pair<>(SizeUnit.KiB, 1D),
                new Pair<>(SizeUnit.KiB, 1024D),
                new Pair<>(SizeUnit.KiB, 1024D * 1024D));

        List<Pair<SizeUnit, Double>> actual = SizeConverter.getMathOperationSafeOperands(
                new Pair<>(SizeUnit.BYTES, 1024D),
                new Pair<>(SizeUnit.KiB, 1D),
                new Pair<>(SizeUnit.MiB, 1D),
                new Pair<>(SizeUnit.GiB, 1D)
                );
        assertEquals(expected, actual);
    }
}
