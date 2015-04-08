package net.technicpack.launcher.ui.components.songs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import net.technicpack.launcher.ui.LauncherFrame;
import net.technicpack.ui.controls.TintablePanel;
import net.technicpack.ui.controls.list.SimpleScrollbarUI;
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
        setLayout(new BorderLayout());
        
        trackList = new JPanel();
        trackList.setLayout(new GridBagLayout());
        
        scrollPane = new JScrollPane(trackList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        		JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUI(new SimpleScrollbarUI(LauncherFrame.COLOR_SCROLL_TRACK, LauncherFrame.COLOR_SCROLL_THUMB));
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(10, 10));
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        
        add(scrollPane, BorderLayout.CENTER);
        trackList.add(Box.createHorizontalStrut(294), new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0),0,0));
        trackList.add(Box.createGlue(), new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        
        int r = 12;
        int g =	94;
        int b = 100;
        for (int i = 2; i < 50; i++) {
        	JLabel label = new JLabel();	
        	
        	b += 1;
        	label.setText("Track " + (i - 1));
        	label.setBorder(new LineBorder(new Color(0,0,0)));
        	label.setOpaque(true);
        	label.setBackground(new Color(r, g, b));
            trackList.add(label, new GridBagConstraints(0, i, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
            
        }
        
	}
}
