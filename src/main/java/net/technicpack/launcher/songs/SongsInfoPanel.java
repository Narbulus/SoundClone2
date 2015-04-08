package net.technicpack.launcher.songs;

import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import net.technicpack.launcher.ui.LauncherFrame;
import net.technicpack.ui.lang.ResourceLoader;

@SuppressWarnings("serial")
public class SongsInfoPanel extends JPanel {

	private ResourceLoader resources;

	public SongsInfoPanel(ResourceLoader resources) {

        this.resources = resources;

        initComponents();
    }

	private void initComponents() {
		setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(20,20,18,16));
        setBackground(LauncherFrame.COLOR_CENTRAL_BACK_OPAQUE);
	}
	
}
