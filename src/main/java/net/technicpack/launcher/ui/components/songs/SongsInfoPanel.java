package net.technicpack.launcher.ui.components.songs;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.technicpack.launcher.ui.LauncherFrame;
import net.technicpack.ui.controls.TintablePanel;
import net.technicpack.ui.controls.WatermarkTextField;
import net.technicpack.ui.controls.borders.RoundBorder;
import net.technicpack.ui.lang.ResourceLoader;

public class SongsInfoPanel extends TintablePanel {
	private ResourceLoader resources;
	private JPanel songsInfoContainer;
	private JPanel userInfo;
	private JPanel trackInfo;
	private JTextField usernameField;
	
	public static final int SONGS_INFO_WIDTH = 400;
	public static final int SONGS_INFO_HEIGHT = 180;

	public SongsInfoPanel(ResourceLoader resources) {

        this.resources = resources;

        initComponents();
    }

	private void initComponents() {
        setBackground(LauncherFrame.COLOR_SONGS_INFO);
        setLayout(new BorderLayout());
        setMaximumSize(new Dimension(SONGS_INFO_WIDTH, SONGS_INFO_HEIGHT));
        
        trackInfo = new JPanel();
        userInfo = new JPanel();
        
        add(userInfo, BorderLayout.PAGE_START);
        add(trackInfo, BorderLayout.CENTER);
        
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.X_AXIS));
        userInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        userInfo.setPreferredSize(new Dimension(SONGS_INFO_WIDTH, SONGS_INFO_HEIGHT));
        userInfo.setBackground(LauncherFrame.COLOR_BLUE_DARKER);
        
        usernameField = new WatermarkTextField("username", LauncherFrame.COLOR_BLUE_DARKER);
        usernameField.setBorder(null);
        usernameField.setPreferredSize(new Dimension(150,30));
        
        ImageIcon userDefaultImg = resources.getIcon("default_user.png");
        JButton userIcon = new JButton(userDefaultImg);
        userIcon.setContentAreaFilled(false);
        userIcon.setFocusPainted(false);
        
        JPanel usernameContainer = new JPanel();
        usernameContainer.setBorder(BorderFactory.createEmptyBorder(60, 0, 50, 0));
        usernameContainer.add(usernameField);
        usernameContainer.setOpaque(false);
        userInfo.add(userIcon);
        userInfo.add(usernameContainer);

        
        trackInfo.setLayout(new BoxLayout(trackInfo, BoxLayout.Y_AXIS));
        trackInfo.add(Box.createVerticalStrut(SONGS_INFO_HEIGHT));
        
	}
}
