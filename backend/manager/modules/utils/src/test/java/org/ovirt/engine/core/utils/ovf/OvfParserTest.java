package org.ovirt.engine.core.utils.ovf;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.ovirt.engine.core.compat.RefObject;

public class OvfParserTest {

    @Test
    public void UtcDateStringToLocaDate() {
        RefObject<Date> ref = new RefObject<Date>(null);
        OvfParser.UtcDateStringToLocaDate("1984/06/19 14:25:11", ref);
        Assert.assertEquals(456503111000l, ref.argvalue.getTime());
    }

    static class Checker implements Runnable {

        public Checker(String dateStr, long dateVal, long count) {
            super();
            this.dateStr = dateStr;
            this.dateVal = dateVal;
            this.count = count;
        }

        final String dateStr;
        final long dateVal;
        final long count;

        long errors;

        @Override
        public void run() {
            RefObject<Date> ref = new RefObject<Date>(null);
            for (long l = 0; l < count; l++) {
                OvfParser.UtcDateStringToLocaDate(dateStr, ref);
                if (dateVal != ref.argvalue.getTime()) {
                    errors++;
                }
            }
        }

    }

    @Test
    public void UtcDateStringToLocaDateMultiThread() throws InterruptedException {
        final Thread[] threads = new Thread[8];
        final Checker[] checkers = new Checker[threads.length];
        for (int i = 0; i < threads.length; i++) {
            checkers[i] = new Checker("1984/06/19 14:25:11", 456503111000l, 100000);
            threads[i] = new Thread(checkers[i], "ovf-checker-" + i);
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        for (Checker checker : checkers) {
            Assert.assertEquals(checker.errors, 0);
        }
    }

}
