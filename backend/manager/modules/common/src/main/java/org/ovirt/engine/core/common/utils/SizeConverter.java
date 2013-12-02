package org.ovirt.engine.core.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SizeConverter {

    public static final long CONVERT_FACTOR = 1024L;

    public static final long BYTES_IN_KB = 1024L;
    public static final long BYTES_IN_MB = 1024L * 1024L;
    public static final long BYTES_IN_GB = 1024L * 1024L * 1024L;

    public SizeConverter() {

    }

    public static enum SizeUnit {
        BYTES(1),
        KB(2),
        MB(3),
        GB(4);

        private long unitWeight;

        private static List<Pair<Long, SizeUnit>> weightToUnit = new ArrayList<Pair<Long, SizeUnit>>();

        private SizeUnit(long unitWeight) {
            this.unitWeight = unitWeight;
        }

        static {
            for (SizeUnit unit : SizeUnit.values()) {
                weightToUnit.add(new Pair<Long, SizeConverter.SizeUnit>(unit.getUnitWeight(), unit));
            }
            Collections.sort(weightToUnit, Collections.reverseOrder(new Comparator<Pair <Long, SizeUnit>>() {

                @Override
                public int compare(Pair<Long, SizeUnit> unit1, Pair<Long, SizeUnit> unit2) {
                    return unit1.getFirst().compareTo(unit2.getFirst());
                }
            }));
        }

        public long getUnitWeight() {
            return unitWeight;
        }

        public SizeUnit getUnit(long weight) {
            return (SizeUnit) weightToUnit.get((int) weight).getSecond();
        }
    };

    public static Number convert(long size, SizeUnit fromUnit, SizeUnit toUnit) {
        long fromType = fromUnit.getUnitWeight();
        long toType = toUnit.getUnitWeight();
        return (size) * ((Math.pow(CONVERT_FACTOR, fromType)) / (Math.pow(CONVERT_FACTOR, toType)));
    }

    public static Pair<SizeUnit, Double> autoConvert(long size, SizeUnit inUnit) {
        for (Pair<Long, SizeUnit> currentUnitPair : SizeUnit.weightToUnit) {
            if (size / Math.pow(CONVERT_FACTOR, currentUnitPair.getFirst() - inUnit.getUnitWeight()) >= 1) {
                return new Pair<SizeConverter.SizeUnit, Double>(currentUnitPair.getSecond(),
                        SizeConverter.convert(size, inUnit, currentUnitPair.getSecond()).doubleValue());
            }
        }
        return new Pair<SizeConverter.SizeUnit, Double>(SizeUnit.BYTES, (double)size);
    }
}
