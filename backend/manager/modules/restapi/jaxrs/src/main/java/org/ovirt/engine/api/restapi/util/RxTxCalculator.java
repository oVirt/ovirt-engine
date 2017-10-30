package org.ovirt.engine.api.restapi.util;

public class RxTxCalculator {
    public static Double percent2bytes(Integer speedValueInMegaBits, Double rxTxValueInPrecent) {
        return megaBitToByte(percent2megaBits(speedValueInMegaBits, rxTxValueInPrecent));
    }

    public static Double percent2bits(Integer speedValueInMegaBits, Double rxTxValueInPercent) {
        return megaBitToBit(percent2megaBits(speedValueInMegaBits, rxTxValueInPercent));
    }

    private static double megaBitToByte(double megaBit) {
        return megaBit * 125000;
    }

    private static double percent2megaBits(Integer speedValueInMegaBits, Double rxTxValueInPrecent) {
        if (speedValueInMegaBits == null || rxTxValueInPrecent == null) {
            return 0.0;
        }
        return speedValueInMegaBits * rxTxValueInPrecent / 100;
    }

    private static double megaBitToBit(double megaBit) {
        return megaBit * 1_000_000;
    }

}
