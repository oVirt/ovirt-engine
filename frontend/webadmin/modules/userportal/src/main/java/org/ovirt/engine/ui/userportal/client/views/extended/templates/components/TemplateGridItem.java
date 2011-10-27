package org.ovirt.engine.ui.userportal.client.views.extended.templates.components;

import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Translator;
import org.ovirt.engine.ui.userportal.client.components.GridElement;
import org.ovirt.engine.ui.userportal.client.components.UPLabel;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.layout.HLayout;

public class TemplateGridItem extends HLayout implements GridElement<VmTemplate> {

	public static final int TEMPLATE_GRID_ITEM_HEIGHT = 50;
	
	private Img image;
	private UPLabel nameLabel;
	private UPLabel descriptionLabel;
	
	private static final int IMAGE_WIDTH = 51;
	private static final int IMAGE_HEIGHT = 41;

	private Object entityGuid;
	
	private Translator osTranslator = EnumTranslator.Create(VmOsType.class);
	
	public TemplateGridItem(VmTemplate item) {
		
		setItemId(item.getId());
		
		setHeight(TEMPLATE_GRID_ITEM_HEIGHT);
		setWidth100();
		setMembersMargin(7);
		setCanHover(true);
		setCanFocus(true);
		setStyleName("mainGrid-row");
		setLayoutLeftMargin(7);
		
		image = new Img();
		image.setWidth(IMAGE_WIDTH);
		image.setHeight(IMAGE_HEIGHT);
		image.setLayoutAlign(VerticalAlignment.CENTER);		
		image.setBorder("1px solid white");		
		image.setHoverOpacity(75);
		image.setHoverStyle("gridToolTipStyle");
		image.setHoverWidth(1);
		image.setHoverWrap(false);
		image.setHoverDelay(500);
		
		nameLabel = new UPLabel("mainGrid-VMname");
		nameLabel.setLayoutAlign(VerticalAlignment.CENTER);
		nameLabel.setAutoFit(true);
		
		descriptionLabel = new UPLabel("mainGrid-VMdescription");
		descriptionLabel.setLayoutAlign(VerticalAlignment.CENTER);
		descriptionLabel.setAutoFit(true);
		
		HLayout nameAndDescriptionLayout = new HLayout(5);
		nameAndDescriptionLayout.setMembers(nameLabel, descriptionLabel);
		nameAndDescriptionLayout.setWidth100();
		nameAndDescriptionLayout.setOverflow(Overflow.HIDDEN);
		
		updateValues(item);
		
		setMembers(image, nameAndDescriptionLayout);
	}
	
	@Override
	public void updateValues(VmTemplate item) {
		setImage(item);
		setName(item.getname());
		setDescription(item.getdescription());
	}

	public void setImage(VmTemplate item) {
		String imageName = "os/large/" + item.getos().name() + ".png";
		if (!image.getSrc().equals(imageName)) {
			image.setTooltip(osTranslator.get(item.getos()));
			image.setSrc(imageName);
		}
	}
	
	public void setName(String name) {
		nameLabel.setContents(name);
	}
	public void setDescription(String description) {
		if ((description == null || description.isEmpty())) {
			descriptionLabel.setContents(null, true);
			return;
		}

		String descriptionString = '(' + description + ')';
		descriptionLabel.setContents(descriptionString, true);	
	}
	
	@Override
	public void select() {
		setStyleName("mainGrid-rowSelected");
	}

	@Override
	public void deselect() {
		setStyleName("mainGrid-row");
	}

	@Override
	public void setItemId(Object id) {
		entityGuid = id;
	}

	@Override
	public Object getItemId() {
		return entityGuid;
	}
}