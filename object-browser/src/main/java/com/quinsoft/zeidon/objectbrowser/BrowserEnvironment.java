/**
    This file is part of the Zeidon Java Object Engine (Zeidon JOE).

    Zeidon JOE is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Zeidon JOE is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Zeidon JOE.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2009-2012 QuinSoft
 */

package com.quinsoft.zeidon.objectbrowser;

import java.awt.Component;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.SingleFrameApplication;

import com.quinsoft.zeidon.ObjectEngine;
import com.quinsoft.zeidon.View;
import com.quinsoft.zeidon.objectdefinition.ViewOd;

/**
 * This object contains info about the browser environment that can be passed around
 * to the different components.
 * 
 * @author DG
 *
 */
class BrowserEnvironment
{
    private static final String BROWSER_SESSION_FILE   = "BrowserState.xml";
    private static final String OIDISPLAY_SESSION_FILE = "OiDisplayState.xml";
    private static final String ATTRIBUTEDIALOG_SESSION_FILE = "AttributeDialog.xml";
    private static final String TWINDIALOG_SESSION_FILE = "TwinDialog.xml";
    
    private final ObjectEngine oe;
    private final ObjectBrowser objectBrowser;
    private final Map<ViewOd, ViewOdLayout> odLayouts;
    private final BrowserContext context;
    
    private int     painterScaleFactor = 8;
    private boolean showHiddenAttributes = false;
    private boolean showNullAttributes   = true;
    private boolean showUnnamedViews     = true;

    /**
     * @param oe
     * @param objectBrowser 
     */
    BrowserEnvironment( ObjectEngine oe, ObjectBrowser objectBrowser )
    {
        super();
        this.oe = oe;
        this.objectBrowser = objectBrowser;
        odLayouts = new HashMap<ViewOd, ViewOdLayout>();
        context = new BrowserContext();
    }

    ObjectEngine getOe()
    {
        return oe;
    }

    ViewOdLayout getOdLayout( View view )
    {
        return getOdLayout( view.getViewOd() );
    }
    
    synchronized ViewOdLayout getOdLayout( ViewOd viewOd )
    {
        ViewOdLayout layout = odLayouts.get( viewOd );
        if ( layout == null )
        {
            layout = new ViewOdLayout( this, viewOd );
            odLayouts.put( viewOd, layout );
        }
        
        return layout;
    }

    /**
     * @return the painterScaleFactor
     */
    int getPainterScaleFactor()
    {
        return painterScaleFactor;
    }
    
    int getDefaultPainterScale()
    {
        return 8;
    }

    BrowserEnvironment restore( Component component, String filename )
    {
        try
        {
            context.getSessionStorage().restore( component, filename );
        }
        catch ( IOException e )
        {
            error( "Error loading Browser session state from file %s: %s", filename, e.getMessage() );
        }

        return this;
    }

    BrowserEnvironment restore( ObjectBrowser browser )
    {
        return restore( browser.getMainFrame(), BROWSER_SESSION_FILE );
    }

    BrowserEnvironment restore( OiDisplayDialog display )
    {
        return restore( display, OIDISPLAY_SESSION_FILE );
    }

    BrowserEnvironment restore( TwinDialog dialog )
    {
        return restore( dialog, TWINDIALOG_SESSION_FILE );
    }

    void saveEnvironment()
    {
        objectBrowser.saveEnvironment();
    }
    
    BrowserEnvironment save( Component component, String filename )
    {
        try
        {
            context.getSessionStorage().save( component, filename );
        }
        catch ( IOException e )
        {
            error( "Error loading Browser session state from file %s: %s", filename, e.getMessage() );
        }

        return this;
    }

    BrowserEnvironment save( ObjectBrowser browser )
    {
        return save( browser.getMainFrame(), BROWSER_SESSION_FILE );
    }

    BrowserEnvironment save( OiDisplayDialog display )
    {
        return save( display, OIDISPLAY_SESSION_FILE );
    }

    BrowserEnvironment save( TwinDialog dialog )
    {
        return save( dialog, TWINDIALOG_SESSION_FILE );
    }

    /**
     * @param painterScaleFactor the painterScaleFactor to set
     */
    boolean setPainterScaleFactor( int painterScaleFactor )
    {
        // Make sure new factor is between 1 and 15.
        int n = Math.min( 15, Math.max( 1, painterScaleFactor ) );
        if ( n == this.painterScaleFactor )
            return false;
        
        this.painterScaleFactor = n;
        return true;
    }
    
    void warn( String format, Object...args )
    {
        oe.getSystemTask().log().warn( format, args );
    }
    
    void error( String format, Object...args )
    {
        oe.getSystemTask().log().error( format, args );
    }

    void restore( AttributeDialog attributeDialog )
    {
        restore( attributeDialog, ATTRIBUTEDIALOG_SESSION_FILE );
    }
    
    void save( AttributeDialog attributeDialog )
    {
        save( attributeDialog, ATTRIBUTEDIALOG_SESSION_FILE );
    }

    /**
     * @return the showHiddenAttributes
     */
    public boolean isShowHiddenAttributes()
    {
        return showHiddenAttributes;
    }

    /**
     * @param showHiddenAttributes the showHiddenAttributes to set
     */
    public void setShowHiddenAttributes( boolean showHiddenAttributes )
    {
        this.showHiddenAttributes = showHiddenAttributes;
    }

    /**
     * @return the showNullAttributes
     */
    public boolean isShowNullAttributes()
    {
        return showNullAttributes;
    }

    /**
     * @param showNullAttributes the showNullAttributes to set
     */
    public void setShowNullAttributes( boolean showNullAttributes )
    {
        this.showNullAttributes = showNullAttributes;
    }

    public void setShowUnnamedViews(boolean showUnnamedViews)
    {
        this.showUnnamedViews = showUnnamedViews;
    }

    public boolean isShowUnnamedViews()
    {
        return showUnnamedViews;
    }
    
    private class BrowserContext extends ApplicationContext
    {
        public BrowserContext()
        {
            super();
            setApplicationClass( ObjectBrowserState.class );
        }
    }
    
    /**
     * This is a dummy class so that we can use the Application context to save window state.
     */
    private static class ObjectBrowserState extends SingleFrameApplication
    {
        @Override
        protected void startup()
        {
        }
    }
}
