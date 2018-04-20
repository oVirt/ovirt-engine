package org.ovirt.engine.core.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ovirt.engine.core.common.businessentities.VmPool;

public class NameForVmInPoolGenerator {

    private String poolName;
    private int lastUsedIndex;

    private static final Pattern PATTERNED_POOL_NAME_PATTERN = Pattern.compile("^.*?([" + VmPool.MASK_CHARACTER + "]+).*?$");

    public NameForVmInPoolGenerator(String poolName) {
        this.poolName = poolName;
    }

    public String generateVmName() {
        Matcher matcher = PATTERNED_POOL_NAME_PATTERN.matcher(poolName);
        if (matcher.find()) {
            String numberPart = matcher.group(1);
            return String.format(poolName.replace(numberPart, "%0" + numberPart.length() + "d"), ++lastUsedIndex);
        } else {
            return String.format("%1$s-%2$s", poolName, ++lastUsedIndex);
        }
    }
}
