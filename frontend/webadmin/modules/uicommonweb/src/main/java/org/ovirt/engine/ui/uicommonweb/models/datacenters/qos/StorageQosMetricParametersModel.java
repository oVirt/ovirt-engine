package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;

import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

public class StorageQosMetricParametersModel extends Model {
    // TODO(sr) changed from EntityModel<Integer> to EntityModel<String>
    //  to work around BZ #1455944
    //
    //  Once we fix that BZ, we should switch back to
    //
    //  EntityModel<Integer>
    //
    //
    private EntityModel<String> total;
    private EntityModel<String> read;
    private EntityModel<String> write;
    private EntityModel<Boolean> choiceGroupNone;
    private EntityModel<Boolean> choiceGroupTotal;
    private EntityModel<Boolean> choiceGroupReadWrite;

    private final ConfigValues maxTotal;
    private final ConfigValues maxRead;
    private final ConfigValues maxWrite;

    public StorageQosMetricParametersModel(ConfigValues maxTotal, ConfigValues maxRead, ConfigValues maxWrite) {
        this.maxTotal = maxTotal;
        this.maxRead = maxRead;
        this.maxWrite = maxWrite;
        setTotal(new EntityModel<String>());
        setRead(new EntityModel<String>());
        setWrite(new EntityModel<String>());
        setChoiceGroupNone(new EntityModel<Boolean>());
        setChoiceGroupTotal(new EntityModel<Boolean>());
        setChoiceGroupReadWrite(new EntityModel<Boolean>());

        getChoiceGroupNone().getEntityChangedEvent().addListener(this);
        getChoiceGroupTotal().getEntityChangedEvent().addListener(this);
        getChoiceGroupReadWrite().getEntityChangedEvent().addListener(this);

    }

    public EntityModel<String> getTotal() {
        return total;
    }

    public void setTotal(EntityModel<String> total) {
        this.total = total;
    }

    public EntityModel<String> getRead() {
        return read;
    }

    public void setRead(EntityModel<String> read) {
        this.read = read;
    }

    public EntityModel<String> getWrite() {
        return write;
    }

    public void setWrite(EntityModel<String> write) {
        this.write = write;
    }

    public EntityModel<Boolean> getChoiceGroupNone() {
        return choiceGroupNone;
    }

    public EntityModel<Boolean> getChoiceGroupTotal() {
        return choiceGroupTotal;
    }

    public EntityModel<Boolean> getChoiceGroupReadWrite() {
        return choiceGroupReadWrite;
    }


    public void setChoiceGroupNone(EntityModel<Boolean> choice_group_none) {
        this.choiceGroupNone = choice_group_none;
    }

    public void setChoiceGroupTotal(EntityModel<Boolean> choice_group_total) {
        this.choiceGroupTotal = choice_group_total;
    }

    public void setChoiceGroupReadWrite(EntityModel<Boolean> choice_group_read_write) {
        this.choiceGroupReadWrite = choice_group_read_write;
    }

    public boolean validate() {
        if (getChoiceGroupNone().getEntity()) {
            // If the Radio Button has been set to None, ensure the object is valid.
            setIsValid(true);
            return true;
        }

        validateValue(getTotal(), (Integer) AsyncDataProvider.getInstance().getConfigValuePreConverted(maxTotal));
        validateValue(getRead(), (Integer) AsyncDataProvider.getInstance().getConfigValuePreConverted(maxRead));
        validateValue(getWrite(), (Integer) AsyncDataProvider.getInstance().getConfigValuePreConverted(maxWrite));

        if (getTotal().getEntity() != null && (getRead().getEntity() != null || getWrite().getEntity() != null)) {
            setErrorMsg(getTotal());
            setErrorMsg(getRead());
            setErrorMsg(getWrite());
        }

        setIsValid(getTotal().getIsValid() && getRead().getIsValid() && getWrite().getIsValid());
        return getIsValid();
    }

    private void setErrorMsg(EntityModel<String> entityModel) {
        if (entityModel.getEntity() != null) {
            entityModel.setIsValid(false);
            entityModel.getInvalidityReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .eitherTotalOrReadWriteCanHaveValues());
        }
    }

    private void validateValue(EntityModel<String> entity, Integer maxValue) {
        entity.validateEntity(new IValidation[] {
                new IntegerValidation(0, maxValue) });
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (this.equals(sender)) {
            getChoiceGroupNone().setIsChangeable(getIsChangable());
            getChoiceGroupTotal().setIsChangeable(getIsChangable());
            getChoiceGroupReadWrite().setIsChangeable(getIsChangable());
        } else if ((sender instanceof EntityModel) && Boolean.TRUE.equals(((EntityModel) sender).getEntity())) {
            if (!getChoiceGroupNone().equals(sender)) {
                getChoiceGroupNone().setEntity(false);
            }

            if (!getChoiceGroupTotal().equals(sender)) {
                getChoiceGroupTotal().setEntity(false);
                getTotal().setEntity(null);
            }

            if (!getChoiceGroupReadWrite().equals(sender)) {
                getChoiceGroupReadWrite().setEntity(false);
                getRead().setEntity(null);
                getWrite().setEntity(null);
            }

            updateChangeability();
        }
    }

    private void updateChangeability() {
        //Suppress update of changeability when entities weren't constructed yet.
        if(getChoiceGroupNone() == null || getChoiceGroupNone().getEntity() == null ||
           getChoiceGroupTotal() == null || getChoiceGroupTotal().getEntity() == null ||
           getChoiceGroupReadWrite() == null || getChoiceGroupReadWrite().getEntity() == null) {
            return;
        }

        boolean total_available = getIsChangable() && getChoiceGroupTotal().getEntity();
        boolean read_write_available = getIsChangable() && getChoiceGroupReadWrite().getEntity();

        getTotal().setIsChangeable(total_available);
        getRead().setIsChangeable(read_write_available);
        getWrite().setIsChangeable(read_write_available);
    }
}
