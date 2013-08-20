package org.ovirt.engine.ui.uicommonweb.models.gluster;

public class SizeConverter {

    public static final long CONVERT_FACTOR = 1024L;

    public static final long BYTES_IN_KB = 1024L;
    public static final long BYTES_IN_MB = 1024L * 1024L;
    public static final long BYTES_IN_GB = 1024L * 1024L * 1024L;

    public static enum SizeUnit {
        BYTES(1),
        KB(2),
        MB(3),
        GB(4);

        private long unitWeight;

        private SizeUnit(long unitWeight) {
            this.unitWeight = unitWeight;
        }

        public long getUnitWeight(){
            return unitWeight;
        }
    };

    public static long convert(long size, SizeUnit fromUnit, SizeUnit toUnit) {
        long fromType = fromUnit.getUnitWeight();
        long toType = toUnit.getUnitWeight();
        return (long) ((size) * ((Math.pow(CONVERT_FACTOR, fromType)) / (Math.pow(CONVERT_FACTOR, toType))));
    }
}
