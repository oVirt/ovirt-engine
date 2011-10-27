package org.ovirt.engine.ui.userportal.client.components;

import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.layout.HLayout;

public class MonitorBarsViewer extends HLayout {
	
	
	private static final int barHeight = 15;
	private static final int barWidth = 200;
	
	MonitorBar cpuBar = new MonitorBar("CPU Usage", "#FF6600", barWidth, barHeight); 
	MonitorBar memoryBar = new MonitorBar("Memory Usage", "#00CC33", barWidth, barHeight);
	MonitorBar networkBar = new MonitorBar("Network usage", "#3A5F7C", barWidth, barHeight); 
	
	
	public MonitorBarsViewer() {
		setWidth100();
		setHeight("90%");
		
		Img cpuIcon = new Img("general/cpu_icon.png", 32, 32);
		Img memoryIcon = new Img("general/memory_icon.png", 32, 32);
		Img networkIcon = new Img("general/network_icon.png", 32, 32);
		
        HLayout cpuLayout = new HLayout(5); 
    	cpuLayout.setHeight("50%");
    	cpuLayout.setWidth("33%");
    	cpuLayout.setLayoutAlign(VerticalAlignment.CENTER);
        cpuLayout.setAlign(Alignment.CENTER);
    	cpuLayout.setMembers(cpuIcon, cpuBar);
    	
        HLayout memoryLayout = new HLayout(5); 
    	memoryLayout.setHeight("50%");
    	memoryLayout.setWidth("33%");
    	memoryLayout.setLayoutAlign(VerticalAlignment.CENTER);
    	memoryLayout.setAlign(Alignment.CENTER);
    	memoryLayout.setMembers(memoryIcon, memoryBar);
    	
        HLayout networkLayout = new HLayout(5); 
        networkLayout.setWidth("33%");
    	networkLayout.setHeight("50%");
    	networkLayout.setLayoutAlign(VerticalAlignment.CENTER);
    	networkLayout.setAlign(Alignment.CENTER);
    	networkLayout.setMembers(networkIcon, networkBar);    	
    	
    	addMember(cpuLayout);
		addMember(memoryLayout);
		addMember(networkLayout);
	}

	public void setData(RecordList records) {
		if (records == null || records.getLength()<1)
			return;
			
		Record r = records.get(0);
	
		cpuBar.setBarPercentage(r.getAttributeAsInt("cpuUsage"));
		memoryBar.setBarPercentage(r.getAttributeAsInt("memoryUsage"));
		networkBar.setBarPercentage(r.getAttributeAsInt("networkUsage"));
	}
}
