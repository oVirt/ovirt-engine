package org.ovirt.engine.core.bll.storage.domain;

import java.io.Serializable;
import java.util.Comparator;

import org.ovirt.engine.core.bll.CommandBase;

public class StorageDomainsByTypeComparer implements Comparator<CommandBase>, Serializable {
    @Override
    public int compare(CommandBase x, CommandBase y) {
        return x.getStorageDomain().getStorageDomainType().getValue()
                - y.getStorageDomain().getStorageDomainType().getValue();
    }

}
