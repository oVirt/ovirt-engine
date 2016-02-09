package org.ovirt.engine.core.utils.timer;

import static org.junit.Assert.assertEquals;

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
        assertEquals(false, JobWrapper.methodAllowsConcurrent(new Dummy(), "serialOnly"));
        assertEquals(true, JobWrapper.methodAllowsConcurrent(new Dummy(), "normalJob"));
    }
}
