/**
 *
 */
package com.quinsoft.zeidon.objectbrowser;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

/**
 * The main panel for the object browser.
 *
 * @author dgc
 *
 */
public class MainPanel extends JPanel
{
    private static final long serialVersionUID = 1L;

    private final BrowserEnvironment env;

    /**
     *
     */
    public MainPanel( BrowserEnvironment env )
    {
        super( new BorderLayout() );
        this.env = env;

        env.createAttributePanel();
        JTabbedPane leftTabbedPane = new JTabbedPane();
        leftTabbedPane.addTab( "Tasks/Views", new ViewChooser( env ) );
        leftTabbedPane.addTab( "Attributes", env.getAttributePanel() );

        OiDisplayPanel oiDisplay = env.createOiDisplay( this );
        JSplitPane splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, leftTabbedPane, oiDisplay );

        add( splitPane, BorderLayout.CENTER );

    }
}
