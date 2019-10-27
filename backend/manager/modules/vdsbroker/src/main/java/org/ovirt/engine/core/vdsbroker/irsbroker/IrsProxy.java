package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSDomainsData;
import org.ovirt.engine.core.compat.Guid;

public interface IrsProxy {

    void dispose();

    List<Guid> obtainDomainsReportedAsProblematic(List<VDSDomainsData> vdsDomainsData);

    void clearVdsFromCache(Guid vdsId, String vdsName);

    void updateVdsDomainsData(VDS vds, final ArrayList<VDSDomainsData> data);

    boolean getHasVdssForSpmSelection();

    IIrsServer getIrsProxy();

    void runInControlledConcurrency(Runnable codeblock);

    boolean failover();

    Guid getCurrentVdsId();

    void setCurrentVdsId(Guid value);

    Guid getPreferredHostId();

    void setPreferredHostId(Guid preferredHostId);

    Set<Guid> getTriedVdssList();

    void clearPoolTimers();

    void clearCache();

    String getIsoDirectory();

    void setFencedIrs(Guid fencedIrs);

    void resetIrs();
}
