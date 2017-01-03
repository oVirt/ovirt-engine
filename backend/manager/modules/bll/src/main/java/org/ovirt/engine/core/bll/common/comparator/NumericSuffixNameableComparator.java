package org.ovirt.engine.core.bll.common.comparator;

import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.ovirt.engine.core.bll.common.NumericSuffixNormalizer;
import org.ovirt.engine.core.common.businessentities.Nameable;

public class NumericSuffixNameableComparator implements Comparator<Nameable> {

    private final NumericSuffixNormalizer numericSuffixNormalizer = new NumericSuffixNormalizer();

    @Override
    public int compare(Nameable nameable1, Nameable nameable2) {
        final String name1 = nameable1.getName();
        final String name2 = nameable2.getName();

        final List<String> normalizedNames = numericSuffixNormalizer.normalize(name1, name2);
        return ObjectUtils.compare(normalizedNames.get(0), normalizedNames.get(1));
    }
}
