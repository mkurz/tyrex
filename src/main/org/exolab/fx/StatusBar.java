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
 * $Id: StatusBar.java,v 1.2 2000/01/17 22:16:03 arkin Exp $
 */

package org.exolab.fx;

import com.kvisco.gui.Panel3D;

import java.awt.*;

public class StatusBar extends StatusDisplay {
    
    private static final int DEFAULT_STEPS = 10;
    
    private int warningLevel = 80;
    
    private Dimension stepDim = null;
    
    private Color fill    = Color.green;
    private Color empty   = Color.darkGray;
    private Color warning = Color.red;
    private Color top     = empty;
    
    private int sepSize    = 1; //-- separator size
    private int stepHeight = 10;
    private int stepWidth  = 15;
    private int warnHeight = 0;
    
    private int   stepCount = DEFAULT_STEPS;
    private float stepPercent = 100/stepCount;

    
    private Image doubleBuffer = null;
    
    /**
     * The current status of this status bar
    **/
    private float status = 0;
    
    private float oldStatus = 0;
    
    /**
     * Creates a new Status Bar
    **/
    public StatusBar() {
        super();
        //-- we need to move this block into an init method for
        //-- reuse
        Dimension dim = new Dimension(stepWidth, stepCount*stepHeight);
        setSize(dim);
        warnHeight = dim.height -(int) (stepHeight*(warningLevel/stepPercent));
    }
    
    public Dimension getMaximumSize() {
        return getSize();
    }
    public Dimension getMinimumSize() {
        return getSize(); 
    } //-- getMinimumSize
    
    /**
     * Sets the number of steps to use.
    **/
    public void setSteps(int steps) {
    } //-- setSteps

    public void paint(Graphics g) {
        Dimension dim = getSize();
        //-- clearAll
        //-- draw level
        
        int x = (dim.width-stepWidth)/2;
        int width = stepWidth;
        
        int height = dim.height - (int) (stepHeight*(status/stepPercent));
        if (height != 0) {
            g.setColor(empty);
            g.fillRect(x,0,width,height);
        }
        
        //-- show old level
        if (oldStatus > status) {
            g.setColor(top);
            int oldHeight 
                = dim.height - (int)(stepHeight*(oldStatus/stepPercent));
            g.drawLine(x, oldHeight, x+width, oldHeight);
        }
        
        //-- show current level
        if (status < warningLevel) {
            g.setColor(fill);
            g.fillRect(x, height, width, dim.height);
        }
        else {
            g.setColor(fill);
            g.fillRect(x, warnHeight, width, dim.height);
            g.setColor(warning);
            g.fillRect(x,height, width,warnHeight-height);
        }
        top = g.getColor();
        
        
        g.setColor(Color.black);
        g.drawRect(x, 0, width, dim.height);
    } //-- paint
    
    /**
     * Sets the current percentage for display. 
     * @param percent the percent value to display. This value
     * should be a number between 0 and 1.
    **/
    public void setPercent(float status) {
        
        oldStatus = this.status;
        this.status = status*100;
        update(getGraphics());
    } //-- setPercent
    
    public void update(Graphics g) {
        if (doubleBuffer == null) initDoubleBuffer();        
        //-- protect against unable to create double buffer
        if (doubleBuffer == null) return;
        Graphics dbg = doubleBuffer.getGraphics();
        Dimension d = getSize();
        dbg.clearRect(0,0,d.width,d.height);
        paint(dbg);
        g.drawImage(doubleBuffer, 0, 0, this);
    } //-- update
    
    /**
     * Initializes the double buffer
    **/
    private void initDoubleBuffer() {
        Dimension d = getSize();
        doubleBuffer = createImage(d.width,d.height);
    }
    
} //--