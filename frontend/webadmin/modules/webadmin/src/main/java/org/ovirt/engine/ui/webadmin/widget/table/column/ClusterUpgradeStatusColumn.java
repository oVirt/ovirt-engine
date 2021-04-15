package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.ui.common.widget.table.cell.CompositeCell;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.safehtml.shared.SafeHtml;

public class ClusterUpgradeStatusColumn extends AbstractColumn<Cluster, Cluster> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public ClusterUpgradeStatusColumn() {
        super(new CompositeCell<Cluster>(Arrays.asList(
            new StatusTextColumn(),
            new UpgradeProgressColumn()
        )));
    }

    @Override
    public Cluster getValue(Cluster object) {
      return object;
    }

    @Override
    public SafeHtml getTooltip(Cluster object) {
        return null;
    }

    private static class StatusTextColumn extends AbstractTextColumn<Cluster> {
        public StatusTextColumn() {
          super(true);
        }

        @Override
        public String getValue(Cluster object) {
            if (object.isUpgradeRunning()) {
                return null; // will be rendered by progress column
            }

            return object.getClusterHostsAndVms() != null
                    && object.getClusterHostsAndVms().getHostsWithUpdateAvailable() > 0
                            ? constants.clusterHasUpgradableHosts()
                            : constants.empty();
        }
    }

    private static class UpgradeProgressColumn extends AbstractTextColumn<Cluster> {
        @Override
        public String getValue(Cluster object) {
          if (!object.isUpgradeRunning()) {
              return null;
          }

          return constants.clusterUpgradeInProgress();
        }
    }


}
