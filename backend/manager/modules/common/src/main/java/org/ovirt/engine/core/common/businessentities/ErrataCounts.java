package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Erratum.ErrataSeverity;
import org.ovirt.engine.core.common.businessentities.Erratum.ErrataType;

public class ErrataCounts implements Serializable {

    private static final long serialVersionUID = -1790668006895698911L;
    private TypedErrataCounts<ErrataType> typedCounts;

    public ErrataCounts() {
        this.typedCounts = new TypedErrataCounts<>(ErrataType.class);
    }

    public TypedErrataCounts<ErrataType> getTypedCounts() {
        return typedCounts;
    }

    /**
     * Adds the erratum to the counts by its type and severity
     *
     * @param erratum
     *            the errata to add
     */
    public void addToCounts(Erratum erratum) {
        EnumCounter<ErrataSeverity> counterByType = typedCounts.getByType(erratum.getType());
        counterByType.incrementCounter(erratum.getSeverity());
    }

    /**
     * @return the overall count of the errata
     */
    public int getTotal() {
        int total = 0;

        for (ErrataType type : ErrataType.values()) {
            total += typedCounts.getByType(type).getTotal();
        }

        return total;
    }

    /**
     * The class represents a map between the errata type {@see ErrataType} to the counts of its severities.
     *
     * @param <E>
     *            The errata type
     */
    public static class TypedErrataCounts<E extends Enum<E>> implements Serializable {
        private static final long serialVersionUID = 3482928445330128097L;
        private Map<E, EnumCounter<ErrataSeverity>> countsBySeverity;
        private Class<E> enumClass;

        TypedErrataCounts() {
        }

        public TypedErrataCounts(Class<E> enumClass) {
            this.enumClass = enumClass;
            initCounts();
        }

        private void initCounts() {
            countsBySeverity = new HashMap<>();
            for (E e : EnumSet.allOf(enumClass)) {
                countsBySeverity.put(e, new EnumCounter<ErrataSeverity>(ErrataSeverity.class));
            }
        }

        public EnumCounter<ErrataSeverity> getByType(E type) {
            return countsBySeverity.get(type);
        }
    }

    /**
     * A generic class which represents a mapping between the enum constant to its counter
     *
     * @param <E>
     *            The type of the enum
     */
    public static class EnumCounter<E extends Enum<E>> implements Serializable {
        private static final long serialVersionUID = -7893563433079291004L;
        private Map<E, Integer> counterByType;
        private Class<E> enumClass;

        EnumCounter(){
        }

        public EnumCounter(Class<E> enumClass) {
            this.enumClass = enumClass;
            initCounters();
        }

        private void initCounters() {
            counterByType = new HashMap<>();
            for (E e : EnumSet.allOf(enumClass)) {
                counterByType.put(e, 0);
            }
        }

        /**
         * @param type
         *            the specific enum constant
         * @return the counter of a specific enum constant
         */
        public int getCounter(E type) {
            return counterByType.get(type);
        }

        /**
         * @return the total count of all enum constants
         */
        public int getTotal() {
            int total = 0;

            for (E e : EnumSet.allOf(enumClass)) {
                total += counterByType.get(e);
            }

            return total;
        }

        /**
         * Increases the count of a specific enum constant by 1
         *
         * @param type
         *            the enum constant to increment
         */
        public void incrementCounter(E type) {
            int counter = counterByType.get(type);
            counterByType.put(type, counter + 1);
        }
    }
}
