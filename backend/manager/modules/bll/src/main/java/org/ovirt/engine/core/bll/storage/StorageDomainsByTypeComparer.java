package org.ovirt.engine.core.bll.storage;

import java.util.Comparator;
import org.ovirt.engine.core.bll.*;

public class StorageDomainsByTypeComparer implements Comparator<CommandBase> {
    @Override
    public int compare(CommandBase x, CommandBase y) {
        return x.getStorageDomain().getstorage_domain_type().getValue()
                - y.getStorageDomain().getstorage_domain_type().getValue();
    }

}
