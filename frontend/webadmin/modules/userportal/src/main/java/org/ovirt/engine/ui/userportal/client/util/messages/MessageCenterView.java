package org.ovirt.engine.ui.userportal.client.util.messages;

import java.util.LinkedList;

import com.google.gwt.user.client.Timer;
import org.ovirt.engine.ui.userportal.client.UserPortal;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AnimationEffect;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.TitleOrientation;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.AnimationCallback;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ButtonItem;
import com.smartgwt.client.widgets.form.fields.FormItemIcon;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.LayoutSpacer;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.menu.IMenuButton;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;

public class MessageCenterView extends HLayout implements
		MessageCenter.MessageListener {

	public MessageCenterView() {
		setHeight100();
		setAlign(Alignment.LEFT);
		// setAlign(VerticalAlignment.CENTER);
		setOverflow(Overflow.HIDDEN);
	}

	@Override
	protected void onDraw() {
		super.onDraw();
		UserPortal.getMessageCenter().addMessageListener(this);

		final Menu recentEventsMenu = new Menu();

		IMenuButton recentEventsButton = new IMenuButton("messages",
				recentEventsMenu);
		recentEventsButton.setTop(5);
		recentEventsButton.setShowMenuBelow(false);
		recentEventsButton.setAutoFit(true);
		recentEventsButton.setValign(VerticalAlignment.CENTER);

		recentEventsButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent clickEvent) {
				LinkedList<Message> messages = UserPortal.getMessageCenter()
						.getMessages();
				if (messages.isEmpty()) {
					recentEventsMenu
							.setItems(new MenuItem("No recent messages"));
				} else {
					MenuItem[] items = new MenuItem[messages.size()];
					int i = 0;
					for (final Message message : messages) {
						MenuItem messageItem = new MenuItem(message.title,
								getSeverityIcon(message.severity));

						items[i++] = messageItem;

						messageItem
								.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {
									public void onClick(MenuItemClickEvent event) {
										showDetails(message);
									}
								});
					}
					recentEventsMenu.setItems(items);
				}
			}
		});

		VLayout vl = new VLayout();
		vl.setAutoWidth();
		vl.setAlign(Alignment.LEFT);
		vl.setAlign(VerticalAlignment.CENTER);
		vl.addMember(recentEventsButton);

		addMember(vl);
		addMember(new LayoutSpacer());
	}

	private void showDetails(Message message) {
		DynamicForm form = new DynamicForm();
		form.setWrapItemTitles(false);

		StaticTextItem title = new StaticTextItem("title", "Title");
		title.setValue(message.title);

		StaticTextItem severity = new StaticTextItem("severity", "Severity");
		FormItemIcon severityIcon = new FormItemIcon();
		severityIcon.setSrc(getSeverityIcon(message.severity));
		severity.setIcons(severityIcon);
		severity.setValue(message.severity.name());

		StaticTextItem date = new StaticTextItem("time", "Time");
		date.setValue(message.createdAt);

		StaticTextItem detail = new StaticTextItem("detail", "Detail");
		detail.setTitleOrientation(TitleOrientation.TOP);
		detail.setValue(message.createdAt);
		detail.setColSpan(2);

		ButtonItem okButton = new ButtonItem("Ok", "Ok");
		okButton.setColSpan(2);
		okButton.setAlign(Alignment.CENTER);

		form.setItems(title, severity, date, detail, okButton);

		final Window window = new Window();
		window.setTitle(message.title);
		window.setWidth(600);
		window.setHeight(400);
		window.setIsModal(true);
		window.setShowModalMask(true);
		window.setCanDragResize(true);
		window.centerInPage();
		window.addItem(form);
		window.show();
		okButton.focusInItem();
		okButton.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler() {
			public void onClick(
					com.smartgwt.client.widgets.form.fields.events.ClickEvent clickEvent) {
				window.destroy();
			}
		});
	}

	public void onMessage(final Message message) {
		final Label label = new Label(message.title);
		label.setMargin(5);
		label.setAutoFit(true);
		label.setHeight(25);
		label.setWrap(false);

		String iconSrc = getSeverityIcon(message.severity);

		label.setIcon(iconSrc);

		label.setTooltip(message.detailed);

		label.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent clickEvent) {
				showDetails(message);
			}
		});

		addMember(label, 1);
		redraw();

		Timer hideTimer = new Timer() {
			@Override
			public void run() {
				label.animateHide(AnimationEffect.FADE,
						new AnimationCallback() {
							public void execute(boolean b) {
								label.destroy();
							}
						});
			}
		};
		hideTimer.schedule(10000);
	}

	private String getSeverityIcon(Message.Severity severity) {
		String iconSrc = null;
		switch (severity) {
		case Info:
			iconSrc = "msg/msg_info.png";
			break;
		case Warning:
			iconSrc = "msg/msg_warn.png";
			break;
		case Error:
			iconSrc = "msg/msg_error.png";
			break;
		}
		return iconSrc;
	}
}
