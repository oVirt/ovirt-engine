package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class VMNamesListReturn {
    private static final String STATUS = "status";
    private static final String VM_NAMES = "vmNames";

    private Status status;
    private List<String> vmNamesList = Collections.emptyList();

    @SuppressWarnings("unchecked")
    public VMNamesListReturn(Map<String, Object> innerMap) {
        status = new Status((Map<String, Object>) innerMap.get(STATUS));
        Object[] vmNames = (Object[]) innerMap.get(VM_NAMES);
        if (vmNames != null) {
            vmNamesList = Arrays.stream(vmNames).map(Objects::toString).collect(Collectors.toList());
        }
    }

    public Status getStatus() {
        return status;
    }

    public List<String> getNamesList() {
        return vmNamesList;
    }
}
