package org.ovirt.engine.ui.uicommonweb.models.vms.register;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class VnicProfileMappingItem extends EntityModel<VnicProfileMappingEntity> {

    private final ListModel<VnicProfileView> targetVnicProfile;

    public VnicProfileMappingItem(VnicProfileMappingEntity entity, List<VnicProfileView> targetVnicProfiles) {
        setEntity(new VnicProfileMappingEntity(entity));
        this.targetVnicProfile = new ListModel<>();
        this.targetVnicProfile.setItems(targetVnicProfiles);
    }

    @Override
    public void initialize() {
        super.initialize();

        this.targetVnicProfile.getSelectedItemChangedEvent().addListener((ev, sender, args) -> getEntity().setVnicProfileId(getTargetVnicProfileId()));
        selectInitialTargetVnicProfile();
    }

    private Guid getTargetVnicProfileId() {
        final VnicProfileView selectedVnicProfile = targetVnicProfile.getSelectedItem();
        if (selectedVnicProfile == null || selectedVnicProfile == VnicProfileView.EMPTY) {
            return null;
        } else {
            return selectedVnicProfile.getId();
        }
    }

    private void selectInitialTargetVnicProfile() {
        final Predicate<VnicProfileView> predicate;
        if (getEntity().isChanged()) {
            predicate = vnicProfile -> Objects.equals(getEntity().getVnicProfileId(), vnicProfile.getId());
        } else {
            predicate = vnicProfile ->
                    Objects.equals(getEntity().getExternalNetworkName(), vnicProfile.getNetworkName())
                    && Objects.equals(getEntity().getExternalNetworkProfileName(), vnicProfile.getName());
        }
        selectTargetVnicProfileByPredicate(predicate);
    }

    private void selectTargetVnicProfileByPredicate(Predicate<VnicProfileView> predicate) {
        final VnicProfileView vnicProfile =
                Linq.firstOrDefault(targetVnicProfile.getItems(), predicate, VnicProfileView.EMPTY);
        targetVnicProfile.setSelectedItem(vnicProfile);
    }

    public ListModel<VnicProfileView> getTargetVnicProfile() {
        return targetVnicProfile;
    }
}
