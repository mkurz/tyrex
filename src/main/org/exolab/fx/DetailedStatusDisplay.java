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
 * $Id: DetailedStatusDisplay.java,v 1.1.1.1 2000/01/11 00:33:46 roro Exp $
 */

package org.exolab.fx;

import java.awt.*;

public class DetailedStatusDisplay extends Panel {
    
    
    /**
     * The actual visual status display
    **/
    private StatusDisplay status = null;
    
    /**
     * The title
    **/
    private Label lblTitle = null;
    
    
    /**
     * The display for the percentage
    **/
    Label lblPercent = null;
    
    //int captionCount = 0;
    
    /**
     * Creates a new DetailedStatusDisplay
    **/
    public DetailedStatusDisplay(String title) {
        this(title, null);
    } //-- DetailedStatusBar

    /**
     * Creates a new DetailedStatusDisplay, using the given StatusDisplay
     * @param title the title for this DetailedStatusDisplay
     * @param display the actual graphical component in which to use
     * for displaying the status information
    **/
    public DetailedStatusDisplay(String title, StatusDisplay display) {
        super();
        
        if (display == null) this.status = new StatusBar();
        else this.status = display;

        //-- define layout
        setLayout(new BorderLayout());
        
        //-- set title
        lblTitle = new Label(title);
        lblTitle.setAlignment(Label.CENTER);
        Panel panel = new Panel(new FlowLayout(FlowLayout.CENTER));
        panel.add(lblTitle);
        add(panel, BorderLayout.NORTH);
        add(status, BorderLayout.CENTER); 
        
        lblPercent = new Label();
        lblPercent.setAlignment(Label.CENTER);
        add(lblPercent, BorderLayout.SOUTH);
        
    } //-- DetailedStatusBar
    
    public void addCaption(Label label) {
        add(label, BorderLayout.SOUTH);
    }
    
    public void addCaption(String caption) {
        Label label = new Label(caption);
        label.setAlignment(Label.CENTER);
        addCaption(label);
    }
    
    /**
     * Returns the actual graphical component used for displaying
     * the status information
     * @return the actual graphical component used for displaying
     * the status information
    **/
    public StatusDisplay getDisplay() {
        return this.status;
    } //-- setDisplay
    
    /**
     * Sets the actual graphical component to use for displaying
     * the status information
     * @param statusDisplay the StatusDisplay in which to display
     * the status information
    **/
    public void setDisplay(StatusDisplay statusDisplay) {
        
        if (statusDisplay != null) {
            remove(this.status);
            this.status = statusDisplay;
            add(this.status, BorderLayout.CENTER);
        }
        
    } //-- setDisplay
    
    /**
     * Sets the current percent to display on the StatusBar
     * @param percent the percent to display the statusBar at
     * this is a number from 0 to 1
    **/
    public void setPercent(float percent) {
        status.setPercent(percent);
        int p = (int) (percent*100);
        lblPercent.setText(Integer.toString(p)+"%");
    } //-- setPercent
    
    /**
     * Sets the title of this DetailedStatusBar
     * @param title the title to display above the status bar
    **/
    public void setTitle(String title) {
        lblTitle.setText(title);
    } //-- setTitle
    
    

} //-- DetailedStatusDisplay