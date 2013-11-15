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

import org.junit.Test;

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
}
