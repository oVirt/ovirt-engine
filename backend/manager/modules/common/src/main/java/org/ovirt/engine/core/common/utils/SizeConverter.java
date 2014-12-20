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
    public static final long BYTES_IN_TB = BYTES_IN_GB * 1024L;

    public SizeConverter() {

    }

    public static enum SizeUnit {
        BYTES(1),
        KB(2),
        MB(3),
        GB(4),
        TB(5);

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
            return weightToUnit.get((int) weight).getSecond();
        }

        public static SizeUnit getMaxHandledUnit() {
            return weightToUnit.get(0).getSecond();
        }

        public static SizeUnit getMinHandledUnit() {
            return weightToUnit.get(weightToUnit.size() - 1).getSecond();
        }
    };

    public static Number convert(long size, SizeUnit fromUnit, SizeUnit toUnit) {
        long fromType = fromUnit.getUnitWeight();
        long toType = toUnit.getUnitWeight();
        return size * (Math.pow(CONVERT_FACTOR, fromType) / Math.pow(CONVERT_FACTOR, toType));
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

    public static SizeUnit leastUnitInList(List<Pair<SizeUnit, Double>> operands) {
        SizeUnit leastUnit = SizeUnit.getMaxHandledUnit();
        for(Pair<SizeUnit, Double> operand : operands) {
            if(operand.getFirst().getUnitWeight() < leastUnit.getUnitWeight()) {
                leastUnit = operand.getFirst();
            }
        }
        return leastUnit;
    }

    public static SizeUnit maxUnitInList(List<Pair<SizeUnit, Double>> operands) {
        SizeUnit maxUnit = SizeUnit.getMinHandledUnit();
        for(Pair<SizeUnit, Double> operand : operands) {
            if(operand.getFirst().getUnitWeight() > maxUnit.getUnitWeight()) {
                maxUnit = operand.getFirst();
            }
        }
        return maxUnit;
    }

    @SafeVarargs
    public static List<Pair<SizeUnit, Double>> getMathOperationSafeOperands(Pair<SizeUnit, Double>... operands) {
        List<Pair<SizeUnit, Double>>  operationReadyOperands = new ArrayList<>();
        List<Pair<SizeUnit, Double>> convertedOperands = new ArrayList<>();
        for (Pair<SizeUnit, Double> operand : operands) {
            convertedOperands.add(autoConvert(operand.getSecond().longValue(), operand.getFirst()));
        }

        SizeUnit finalUnit = leastUnitInList(convertedOperands);

        for (Pair<SizeUnit, Double> operand : convertedOperands) {
            if(operand.getFirst() != finalUnit) {
                operationReadyOperands.add(new Pair<SizeConverter.SizeUnit, Double>(finalUnit,
                        convert(operand.getSecond().longValue(), operand.getFirst(), finalUnit).doubleValue()));
            } else {
                operationReadyOperands.add(operand);
            }
        }
        return operationReadyOperands;
    }
}
