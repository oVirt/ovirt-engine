package org.ovirt.engine.core.utils.timer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
    public void testMethodAllowsConcurrent() {
        assertFalse(JobWrapper.methodAllowsConcurrent(new Dummy(), "serialOnly"));
        assertTrue(JobWrapper.methodAllowsConcurrent(new Dummy(), "normalJob"));
    }
}
