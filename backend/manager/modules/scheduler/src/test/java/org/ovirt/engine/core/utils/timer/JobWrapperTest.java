package org.ovirt.engine.core.utils.timer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class JobWrapperTest {

    class Dummy {
        @OnTimerMethodAnnotation(value = "serialOnly", allowsConcurrent = false)
        public void serialMethod() {

        }

        @OnTimerMethodAnnotation("normalJob")
        public void concurrentMethod() {

        }
    }

    @Test
    public void testMethodAllowsConcurrent() throws Exception {
        assertFalse(JobWrapper.methodAllowsConcurrent(new Dummy(), "serialOnly"));
        assertTrue(JobWrapper.methodAllowsConcurrent(new Dummy(), "normalJob"));
    }
}
