package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.HostedEngineDeployConfiguration;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

/**<pre>
 * GWT usage note:
 * The hosted engine tab model implementation. To keep it DRY this model is used in Host install and new host views by
 * connecting it to a tab using the xml.
 * This model has currenly a list of {@linkplain org.ovirt.engine.core.common.businessentities.HostedEngineDeployConfiguration.Action}
 * which is translated into radio buttons by each of the view classes, by using a ListModelRadioGroupEditor
 *
 * The {@linkplain #actions} are the set of deploy operations options presented by the radio button, essentially its meant
 * to DEPLOY the hosted engine agent, or UNDEPLOY it. NONE means the host installation will not touch hosted engine components.
 * See {@link org.ovirt.engine.core.common.businessentities.HostedEngineDeployConfiguration.Action}
 *
 * Future additions to this model may include more customizations to pass to the installation.
 * </pre>
 */
public class HostedEngineHostModel extends ListModel<HostedEngineDeployConfiguration.Action> {

    public HostedEngineHostModel() {
        setItems(Arrays.asList(HostedEngineDeployConfiguration.Action.values()));
    }

    public void removeActionFromList(HostedEngineDeployConfiguration.Action action) {
        List<HostedEngineDeployConfiguration.Action> actions = new ArrayList<>();

        for (HostedEngineDeployConfiguration.Action deployAction: HostedEngineDeployConfiguration.Action.values()) {
            if (!deployAction.equals(action)) {
                actions.add(deployAction);
            }
        }

        setItems(actions);
    }
}
