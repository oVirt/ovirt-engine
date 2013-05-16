package org.ovirt.engine.core.utils.ovf;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

public class OvfParserTest {

    @Test
    public void UtcDateStringToLocaDate_nodep() {
        Date date =
                OvfParser.UtcDateStringToLocaDate("1984/06/19 14:25:11");
        Assert.assertEquals(456503111000L, date.getTime());
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
            for (long l = 0; l < count; l++) {
                final Date date = OvfParser.UtcDateStringToLocaDate(dateStr);
                if (dateVal != date.getTime()) {
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
            checkers[i] = new Checker("1984/06/19 14:25:11", 456503111000L, 100000);
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
