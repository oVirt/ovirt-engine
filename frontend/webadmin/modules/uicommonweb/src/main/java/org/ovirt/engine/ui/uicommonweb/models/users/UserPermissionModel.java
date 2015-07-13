package org.ovirt.engine.ui.uicommonweb.models.users;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class UserPermissionModel extends Model {

    private Guid privateId = Guid.Empty;

    public Guid getId() {
        return privateId;
    }

    public void setId(Guid value) {
        privateId = value;
    }

    private ListModel privateRole;

    public ListModel getRole() {
        return privateRole;
    }

    public void setRole(ListModel value) {
        privateRole = value;
    }

    private List<TagModel> tags;

    public List<TagModel> getTags() {
        return tags;
    }

    public void setTags(List<TagModel> value) {
        if (tags != value) {
            tags = value;
            onPropertyChanged(new PropertyChangedEventArgs("Tags")); //$NON-NLS-1$
        }
    }

    public UserPermissionModel() {
        setRole(new ListModel());
    }

    public boolean validate() {
        getRole().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        return getRole().getIsValid();
    }
}
