package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.Translator;

public class AbstractGeneralModel<E> extends EntityModel<E> {

    private String graphicsType;

    public String getGraphicsType() {
        return graphicsType;
    }

    public void setGraphicsType(String graphicsType) {
        if (!Objects.equals(this.graphicsType, graphicsType)) {
            this.graphicsType = graphicsType;
            onPropertyChanged(new PropertyChangedEventArgs("GraphicsType")); //$NON-NLS-1$
        }
    }

    protected void updateProperties(Guid entityId) {
        if (entityId == null) {
            return;
        }

        Frontend.getInstance().runQuery(QueryType.GetGraphicsDevices,
                new IdQueryParameters(entityId).withoutRefresh(), new AsyncQuery<QueryReturnValue>(returnValue -> {
                            List<GraphicsDevice> graphicsDevices = returnValue.getReturnValue();
                            Set<GraphicsType> graphicsTypesCollection = new HashSet<>();

                            for (GraphicsDevice graphicsDevice : graphicsDevices) {
                                graphicsTypesCollection.add(graphicsDevice.getGraphicsType());
                            }

                            UnitVmModel.GraphicsTypes graphicsTypes = UnitVmModel.GraphicsTypes.fromGraphicsTypes(graphicsTypesCollection);
                            Translator translator = EnumTranslator.getInstance();
                            setGraphicsType(translator.translate(graphicsTypes));
                        }
                ));
    }

}
