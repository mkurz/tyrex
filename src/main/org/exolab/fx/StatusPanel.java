/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: StatusPanel.java,v 1.1.1.1 2000/01/11 00:33:46 roro Exp $
 */

package org.exolab.fx;

import java.awt.*;
import com.kvisco.gui.Panel3D;
import java.util.Vector;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * An AWT component for showing graphical Status information
 * @author <a href="mailto:kvisco@exoffice.com">Keith Visco</a>
 * @version $Revision: 1.1.1.1 $ $Date: 2000/01/11 00:33:46 $
**/
public class StatusPanel extends Panel3D 
    implements Runnable, MouseListener
{
    
    /**
     * The default polling interval for DataFeeds
     * specified in milliseconds
    **/
    public static final int DEFAULT_POLL_INTERVAL = 500;
    
    private Label lblTitle = null;
    
    private Panel sPanel = null;
    
    private Panel descriptions  = null;
    
    /**
     * A list of StatusDataFeed objects
    **/
    private Vector feeders = null;
    
    /**
     * The poll interval for DataFeeds, specified in milliseconds
    **/
    private int pollInterval = DEFAULT_POLL_INTERVAL;
    
    /**
     * The list of DetailedStatusBars for this Panel
    **/
    private Vector displays = null;
    
    /**
     * The boolean indicating whether to stop polling
    **/
    private boolean stop = false;
    
    
    //----------------/
    //- Constructors -/
    //----------------/
    
    /**
     * Creates a new StatusPanel
    **/
    public StatusPanel(String title) {
        super(Panel3D.SUNKEN);
        
        
        //-- Exoffice Blue
        Color color = new Color(102, 102, 153);
        setBackground(color);
        setForeground(Color.white);
        setBorder(4); //-- Panel3D feature
        
        //-- define layout
        setLayout(new BorderLayout());
        
        //-- set title
        lblTitle = new Label(title);
        lblTitle.setAlignment(Label.CENTER);
        add(lblTitle,BorderLayout.NORTH);
        
        sPanel = new Panel();
        sPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        add(sPanel, BorderLayout.CENTER);
        
        descriptions = new Panel();
        descriptions.setLayout(new GridLayout(2,1));
        add(descriptions, BorderLayout.SOUTH);
        
        
        feeders    = new Vector(3);
        displays   = new Vector(3);
        
    } //-- StatusPanel
    
    public void run() {
        
        stop = false;
        
        while(!stop) {
            
            for (int i = 0; i < feeders.size(); i++) {
                StatusDataFeed dataFeed 
                    = (StatusDataFeed)feeders.elementAt(i);
                    
                DetailedStatusDisplay display
                    = (DetailedStatusDisplay) displays.elementAt(i);
                    
                float p = dataFeed.getPercent();
                display.setPercent(p);
            }
            
            try {
                Thread.sleep(200);
            }
            catch(java.lang.InterruptedException ex) {
                stop = true;
            };
        }
    } //-- run
    
    /**
     * Adds the given DataFeed to this StatusPanel
     * @param dataFeed the StatusDataFeed to add
    **/
    public void addDataFeed(StatusDataFeed dataFeed) {
        if (dataFeed == null) return;


        String title = dataFeed.getTitle();
        if (title == null) title = "";
        DetailedStatusDisplay display = new DetailedStatusDisplay(title);
        
        display.getDisplay().addMouseListener(this);
        
        synchronized (feeders) {
            feeders.addElement(dataFeed);
            displays.addElement(display);
        }
        
        String desc = dataFeed.getDescription();
        if (desc != null) {
            Label lbl = new Label(desc);
            lbl.setAlignment(Label.CENTER);
            descriptions.add(lbl);
        }
        sPanel.add(display);
    } //-- addDataFeed
    
    /**
     * Sets the display to use for a DataFeed
     * @param dataFeed the StatusDataFeed to set the display for
     * @param display the StatusDisplay to use for the given DataFeed
    **/
    public void setDisplay(StatusDataFeed dataFeed, StatusDisplay display) {
        for (int i = 0; i < feeders.size(); i++) {
            if (dataFeed == feeders.elementAt(i)) {
                DetailedStatusDisplay dsd = 
                    (DetailedStatusDisplay) displays.elementAt(i);
                dsd.setDisplay(display);
                return;
            }
        }
    } //-- setDisplay
    
    /**
     * Sets the polling interval for DataFeeds
     * @param millis the number of milliseconds between polling
    **/
    public void setPollInterval(int millis) {
        this.pollInterval = millis;
    } //-- setPollInterval
    
    /**
     * Sets the title of this DetailedStatusBar
     * @param title the title to display above the status bar
    **/
    public void setTitle(String title) {
        lblTitle.setText(title);
    } //-- setTitle
    //-------------------------/
    //- MouseListener methods -/
    //-------------------------/
    
    public void mouseClicked(MouseEvent e) {
        
        /*
        Object target = e.getComponent();
        if (target instanceof StatusDisplay) {
            
            //-- find DetailedDisplayStatus
            int idx = -1;
            DetailedStatusDisplay dsd = null;
            for (int i = 0; i < displays.size(); i++) {
                dsd = (DetailedStatusDisplay) displays.elementAt(i);
                if (dsd.getDisplay() == target){
                    idx = 1;
                    break;
                }
            }
            
            if (idx >= 0) {
                // toggle display
                if (target instanceof StatusBar) {
                    dsd.setDisplay(new StatusGraph());
                }
                else if (target instanceof StatusGraph) {
                    dsd.setDisplay(new StatusBar());
                }
                dsd.getDisplay().addMouseListener(this);
            }
            update(getGraphics());
        }
        */
    } //-- mouseClicked
    
    public void mouseEntered(MouseEvent e)  { /* do nothing */ };
    public void mouseExited(MouseEvent e)   { /* do nothing */ };
    public void mousePressed(MouseEvent e)  { /* do nothing */ };;
    public void mouseReleased(MouseEvent e) { /* do nothing */ };
    
} //-- StatusPanel