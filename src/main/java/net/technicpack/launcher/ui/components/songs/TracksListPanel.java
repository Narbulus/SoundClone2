package net.technicpack.launcher.ui.components.songs;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.technicpack.launcher.ui.LauncherFrame;
import net.technicpack.ui.controls.TintablePanel;
import net.technicpack.ui.lang.ResourceLoader;

@SuppressWarnings("serial")
public class TracksListPanel extends TintablePanel {

	private ResourceLoader resources;
	private JPanel trackList;
	private JScrollPane scrollPane;

	public TracksListPanel(ResourceLoader resources) {

		this.resources = resources;

		initComponents();
	}

	private void initComponents() {
        setBackground(LauncherFrame.COLOR_TRACKS_LIST);
        
        trackList = new JPanel();
        scrollPane = new JScrollPane(trackList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        		JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        
	}
}
