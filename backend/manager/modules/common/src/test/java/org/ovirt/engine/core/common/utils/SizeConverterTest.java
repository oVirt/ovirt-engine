/*
 * Copyright (c) 2010 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ovirt.engine.core.common.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.utils.SizeConverter.SizeUnit;

public class SizeConverterTest {
    @Test
    public void testConvertMBToBytes() {
        long megabytes = 3L;
        long bytes = SizeConverter.convert(megabytes, SizeConverter.SizeUnit.MiB,
                SizeConverter.SizeUnit.BYTES).longValue();
        assertEquals(bytes, 3145728);
    }

    @Test
    public void testCobvertGBToBytes() {
        long gigabytes = 3L;
        long bytes = SizeConverter.convert(gigabytes, SizeConverter.SizeUnit.GiB,
                SizeConverter.SizeUnit.BYTES).longValue();
        assertEquals(bytes, 3221225472L);
    }

    @Test
    public void testConvertBytestoGB() {
        long bytes = 3221228000L;
        int gigabytes = SizeConverter.convert(bytes, SizeConverter.SizeUnit.BYTES,
                SizeConverter.SizeUnit.GiB).intValue();
        assertEquals(gigabytes, 3);
    }

    @Test
    public void testConvertBytestoMB() {
        long bytes = 3160000L;
        int megabytes = SizeConverter.convert(bytes, SizeConverter.SizeUnit.BYTES,
                SizeConverter.SizeUnit.MiB).intValue();
        assertEquals(megabytes, 3);
    }

    @Test
    public void testConvertMegaBytesToTB() {
        long mb = 5 * 1024 * 1024;
        int tbs = SizeConverter.convert(mb, SizeUnit.MiB, SizeUnit.TiB).intValue();
        assertEquals(tbs, 5);
    }

    @Test
    public void testConvertTiBToYiB() {
        double tib = 100 * Math.pow(1024, 4);
        int yib = SizeConverter.convert((long)tib, SizeUnit.TiB, SizeUnit.YiB).intValue();
        assertEquals(yib, 100);
    }

    @Test
    public void testConvertMiBToEiB() {
        double mib = 15 * Math.pow(1024, 4);
        int eib = SizeConverter.convert((long)mib, SizeUnit.MiB, SizeUnit.EiB).intValue();
        assertEquals(eib, 15);
    }

    @Test
    public void testConvertPiBToZiB() {
        double pib = 15 * Math.pow(1024, 2);
        int zib = SizeConverter.convert((long)pib, SizeUnit.PiB, SizeUnit.ZiB).intValue();
        assertEquals(zib, 15);
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
