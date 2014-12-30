package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostNIC;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.restapi.util.RxTxCalculator;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public class HostNicStatisticalQuery extends AbstractStatisticalQuery<HostNIC, VdsNetworkInterface> {

    private final static Statistic DATA_RX = create("data.current.rx", "Receive data rate",  GAUGE, BYTES_PER_SECOND, DECIMAL);
    private final static Statistic DATA_TX = create("data.current.tx", "Transmit data rate", GAUGE, BYTES_PER_SECOND, DECIMAL);
    private final static Statistic TOTAL_RX = create("data.total.rx", "Total received data", COUNTER, BYTES, INTEGER);
    private final static Statistic TOTAL_TX = create("data.total.tx", "Total transmitted data", COUNTER, BYTES, INTEGER);
    private final static Statistic ERRS_RX = create("errors.total.rx", "Total receive errors", COUNTER, NONE, INTEGER);
    private final static Statistic ERRS_TX = create("errors.total.tx", "Total transmit errors", COUNTER, NONE, INTEGER);

    protected HostNicStatisticalQuery(HostNIC parent) {
        this(null, parent);
    }

    protected HostNicStatisticalQuery(AbstractBackendResource<HostNIC, VdsNetworkInterface>.EntityIdResolver<Guid> entityResolver,
            HostNIC parent) {
        super(HostNIC.class, parent, entityResolver);
    }

    @Override
    public List<Statistic> getStatistics(VdsNetworkInterface iface) {
        NetworkStatistics s = iface.getStatistics();

        return asList(setDatum(clone(DATA_RX), RxTxCalculator.percent2bytes(iface.getSpeed(), s.getReceiveRate())),
                setDatum(clone(DATA_TX), RxTxCalculator.percent2bytes(iface.getSpeed(), s.getTransmitRate())),
                setDatum(clone(TOTAL_RX), s.getReceivedBytes()),
                setDatum(clone(TOTAL_TX), s.getTransmittedBytes()),
                setDatum(clone(ERRS_RX), s.getReceiveDropRate()),
                setDatum(clone(ERRS_TX), s.getTransmitDropRate()));
    }

    @Override
    public Statistic adopt(Statistic statistic) {
        // clone required because LinkHelper unsets the grandparent
        statistic.setHostNic(clone(parent));
        return statistic;
    }

    private HostNIC clone(HostNIC parent) {
        HostNIC nic = new HostNIC();
        nic.setId(parent.getId());
        nic.setHost(new Host());
        nic.getHost().setId(parent.getHost().getId());
        return nic;
    }
};
