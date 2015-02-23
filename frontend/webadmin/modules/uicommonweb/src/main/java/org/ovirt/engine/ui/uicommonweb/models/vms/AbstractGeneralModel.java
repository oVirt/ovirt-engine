package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.Translator;

public class AbstractGeneralModel extends EntityModel {

    private String graphicsType;

    public String getGraphicsType() {
        return graphicsType;
    }

    public void setGraphicsType(String graphicsType) {
        if (!ObjectUtils.objectsEqual(this.graphicsType, graphicsType)) {
            this.graphicsType = graphicsType;
            onPropertyChanged(new PropertyChangedEventArgs("GraphicsType")); //$NON-NLS-1$
        }
    }

    protected void updateProperties(Guid entityId) {
        if (entityId == null) {
            return;
        }

        Frontend.getInstance().runQuery(VdcQueryType.GetGraphicsDevices, new IdQueryParameters(entityId), new AsyncQuery(
            this,
            new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object returnValue) {
                    List<GraphicsDevice> graphicsDevices = ((VdcQueryReturnValue) returnValue).getReturnValue();
                    Set<GraphicsType> graphicsTypesCollection = new HashSet<GraphicsType>();

                    for (GraphicsDevice graphicsDevice : graphicsDevices) {
                        graphicsTypesCollection.add(graphicsDevice.getGraphicsType());
                    }

                    UnitVmModel.GraphicsTypes graphicsTypes = UnitVmModel.GraphicsTypes.fromGraphicsTypes(graphicsTypesCollection);
                    Translator translator = EnumTranslator.getInstance();
                    setGraphicsType(translator.translate(graphicsTypes));
                }
            }
        ));
    }

}
