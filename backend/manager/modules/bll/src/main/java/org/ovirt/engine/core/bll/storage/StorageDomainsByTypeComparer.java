package org.ovirt.engine.core.bll.storage;

import java.util.Comparator;
import org.ovirt.engine.core.bll.*;

public class StorageDomainsByTypeComparer implements Comparator<CommandBase> {
    @Override
    public int compare(CommandBase x, CommandBase y) {
        return x.getStorageDomain().getStorageDomainType().getValue()
                - y.getStorageDomain().getStorageDomainType().getValue();
    }

}
