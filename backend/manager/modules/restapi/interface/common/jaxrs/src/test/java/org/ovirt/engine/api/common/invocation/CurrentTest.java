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

package org.ovirt.engine.api.common.invocation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CurrentTest extends Assert {

    private Current current;

    @Before
    public void setUp() {
        current = new Current();
    }

    @Test
    public void testPutGetSameThread() throws Exception {
        current.set(Integer.valueOf(7));
        current.set(Integer.valueOf(8));
        assertEquals(current.get(Integer.class), Integer.valueOf(8));
        assertNull(current.get(Short.class));
    }

    @Test
    public void testPutGetMultiThread() throws Exception {
        current.set(Integer.valueOf(7));
        Thread setter = new Thread(new Runnable() {
            public void run() {
                current.set(Integer.valueOf(8));
                current.set(Short.valueOf((short)9));
            }
        });
        setter.start();
        setter.join();
        assertEquals(current.get(Integer.class), Integer.valueOf(7));
        assertNull(current.get(Short.class));
    }
}
