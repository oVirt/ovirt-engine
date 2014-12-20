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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.utils.SizeConverter.SizeUnit;

public class SizeConverterTest {
    @Test
    public void testConvertMBToBytes() {
        long megabytes = 3L;
        long bytes = SizeConverter.convert(megabytes, SizeConverter.SizeUnit.MB,
                SizeConverter.SizeUnit.BYTES).longValue();
        assertEquals(bytes, 3145728);
    }

    @Test
    public void testCobvertGBToBytes() {
        long gigabytes = 3L;
        long bytes = SizeConverter.convert(gigabytes, SizeConverter.SizeUnit.GB,
                SizeConverter.SizeUnit.BYTES).longValue();
        assertEquals(bytes, 3221225472L);
    }

    @Test
    public void testConvertBytestoGB() {
        long bytes = 3221228000L;
        int gigabytes = SizeConverter.convert(bytes, SizeConverter.SizeUnit.BYTES,
                SizeConverter.SizeUnit.GB).intValue();
        assertEquals(gigabytes, 3);
    }

    @Test
    public void testConvertBytestoMB() {
        long bytes = 3160000L;
        int megabytes = SizeConverter.convert(bytes, SizeConverter.SizeUnit.BYTES,
                SizeConverter.SizeUnit.MB).intValue();
        assertEquals(megabytes, 3);
    }

    @Test
    public void testConvertMegaBytesToTB() {
        long mb = 5 * 1024 * 1024;
        int tbs = SizeConverter.convert(mb, SizeUnit.MB, SizeUnit.TB).intValue();
        assertEquals(tbs, 5);
    }

    @Test
    public void testGetMathOperationSafeOperands() {
        List<Pair<SizeUnit, Double>> expected = new ArrayList<Pair<SizeUnit, Double>>() {
            private static final long serialVersionUID = 1L;

            {
                add(new Pair<SizeUnit, Double>(SizeUnit.KB, 1D));
                add(new Pair<SizeUnit, Double>(SizeUnit.KB, 1D));
                add(new Pair<SizeUnit, Double>(SizeUnit.KB, 1024D));
                add(new Pair<SizeUnit, Double>(SizeUnit.KB, 1024D * 1024D));
            }
        };
        List<Pair<SizeUnit, Double>> actual = SizeConverter.getMathOperationSafeOperands(
                new Pair<SizeUnit, Double>(SizeUnit.BYTES, 1024D),
                new Pair<SizeUnit, Double>(SizeUnit.KB, 1D),
                new Pair<SizeUnit, Double>(SizeUnit.MB, 1D),
                new Pair<SizeUnit, Double>(SizeUnit.GB, 1D)
                );
        assertEquals(expected, actual);
    }
}
