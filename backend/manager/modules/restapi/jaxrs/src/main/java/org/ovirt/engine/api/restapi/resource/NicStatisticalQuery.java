package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.restapi.util.RxTxCalculator;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;


public class NicStatisticalQuery extends AbstractStatisticalQuery<Nic, VmNetworkInterface> {

    private static final Statistic DATA_RX = create("data.current.rx", "Receive data rate",  GAUGE,   BYTES_PER_SECOND, DECIMAL);
    private static final Statistic DATA_TX = create("data.current.tx", "Transmit data rate", GAUGE,   BYTES_PER_SECOND, DECIMAL);
    private static final Statistic DATA_RX_BITS = create("data.current.rx.bps", "Receive data rate", GAUGE, BITS_PER_SECOND, DECIMAL);
    private static final Statistic DATA_TX_BITS = create("data.current.tx.bps", "Transmit data rate", GAUGE, BITS_PER_SECOND, DECIMAL);
    private static final Statistic ERRS_RX = create("errors.total.rx", "Total receive errors", COUNTER, NONE, INTEGER);
    private static final Statistic ERRS_TX = create("errors.total.tx", "Total transmit errors", COUNTER, NONE, INTEGER);
    private static final Statistic TOTAL_RX = create("data.total.rx", "Total received data", COUNTER, BYTES, INTEGER);
    private static final Statistic TOTAL_TX = create("data.total.tx", "Total transmitted data", COUNTER, BYTES, INTEGER);

    protected NicStatisticalQuery(Nic parent) {
        this(null, parent);
    }

    protected NicStatisticalQuery(AbstractBackendResource<Nic, VmNetworkInterface>.EntityIdResolver<Guid> resolver, Nic parent) {
        super(Nic.class, parent, resolver);
    }

    @Override
    public List<Statistic> getStatistics(VmNetworkInterface iface) {
        NetworkStatistics s = iface.getStatistics();
        return asList(setDatum(clone(DATA_RX), RxTxCalculator.percent2bytes(iface.getSpeed(), s.getReceiveRate())),
                setDatum(clone(DATA_TX), RxTxCalculator.percent2bytes(iface.getSpeed(), s.getTransmitRate())),
                setDatum(clone(DATA_RX_BITS), RxTxCalculator.percent2bits(iface.getSpeed(), s.getReceiveRate())),
                setDatum(clone(DATA_TX_BITS), RxTxCalculator.percent2bits(iface.getSpeed(), s.getTransmitRate())),
                setDatum(clone(ERRS_RX), s.getReceiveDrops()),
                setDatum(clone(ERRS_TX), s.getTransmitDrops()),
                setDatum(clone(TOTAL_RX), s.getReceivedBytes()),
                setDatum(clone(TOTAL_TX), s.getTransmittedBytes()));
    }

    @Override
    public Statistic adopt(Statistic statistic) {
        // clone required because LinkHelper unsets the grandparent
        statistic.setNic(clone(parent));
        return statistic;
    }

    private Nic clone(Nic parent) {
        Nic nic = new Nic();
        nic.setId(parent.getId());
        nic.setVm(new Vm());
        nic.getVm().setId(parent.getVm().getId());
        return nic;
    }
}
