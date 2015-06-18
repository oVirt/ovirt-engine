package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.HostedEngineDeployConfiguration;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
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
public class HostedEngineHostModel extends EntityModel {

    private ListModel<HostedEngineDeployConfiguration.Action> actions = new ListModel<>();

    public HostedEngineHostModel() {
        actions.setItems(
                Arrays.asList(HostedEngineDeployConfiguration.Action.values()),
                HostedEngineDeployConfiguration.Action.NONE);
    }

    public ListModel<HostedEngineDeployConfiguration.Action> getActions() {
        return actions;
    }

    public void setActions(ListModel<HostedEngineDeployConfiguration.Action> actions) {
        this.actions = actions;
    }
}
