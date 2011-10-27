package org.ovirt.engine.ui.userportal.client.modalpanels;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.frontend.Message;
import org.ovirt.engine.ui.userportal.client.common.Severity;
import org.ovirt.engine.ui.userportal.client.components.NonDraggableModalPanel;
import org.ovirt.engine.ui.userportal.client.components.Button;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

public class MessageDialog extends NonDraggableModalPanel {

	private final Button okButton;

    private MessageDialog(String title, Severity severity) {
		super(500, 400, title, severity);

		okButton = new Button("OK");
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				destroy();
			}
		});
		
		setFooterButtons(Alignment.RIGHT, okButton);
	}

	public MessageDialog(String title, final Message message, Severity severity) {
		this(title, new ArrayList<Message>(1) {{this.add(message);}}, severity);
	}

	public MessageDialog(String title, List<Message> messages, Severity severity) {
		this(title, severity);
		
		for (Message message : messages) {
			if (message.getDescription() != null && !message.getDescription().isEmpty()) {
				Label desc = new Label(message.getDescription() + ":");
				desc.setAutoHeight();
				desc.setWidth100();
				desc.setStyleName("errorDialogDescription");
				addItem(desc);
			}

			Label msg = new Label("<li>" + message.getText() + "</li>");
			msg.setAutoHeight();
			msg.setWidth100();
			msg.setCanSelectText(true);
			msg.setStyleName("errorDialogText");
			addItem(msg);
		}
	}
	
    @Override
    protected void onDraw() {
        super.onDraw();
        okButton.focus();
    }

}
