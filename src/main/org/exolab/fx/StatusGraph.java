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
 * $Id: StatusGraph.java,v 1.2 2000/01/17 22:16:03 arkin Exp $
 */

package org.exolab.fx;

import com.kvisco.gui.Panel3D;

import java.awt.*;

public class StatusGraph extends StatusDisplay {
    

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
    
    /**
     * The image used for double buffering, we paint
     * to this image and then paint it all at once to 
     * the screen
    **/
    private Image _dblBuffer = null;
    
    /**
     * The current status of this status bar
    **/
    private float status = 0;
    
    private float oldStatus = 0;
    
    private float[] statusList;
    private int statusCount = 10;
    
    /**
     * Creates a new Status Bar
    **/
    public StatusGraph() {
        super();
        
        setBackground(Color.black);
        
        statusList = new float[statusCount];
        
        for (int i = 0; i < statusCount; i++) 
            statusList[i] = 0;
        
        //-- we need to move this block into an init method for
        //-- reuse
        Dimension dim = new Dimension(stepWidth*(statusCount-1), 
                                      stepCount*stepHeight);
        setSize(dim);
        
        warnHeight = dim.height -(int) (stepHeight*(warningLevel/stepPercent));
    }

    public void paint(Graphics g) {
        Dimension dim = getSize();
        
        //-- drawGrid
        int x = 0;
        int y = 0;
        
        g.setColor(Color.magenta);
        //g.drawRect(0,0,dim.width-1, dim.height-1);
        
        for (x = stepWidth; x < dim.width; x += stepWidth)
            g.drawLine(x, 0, x, dim.height); 
        for (y = stepHeight; y < dim.height; y += stepHeight)
            g.drawLine(0, y, dim.width, y); 
        
        //-- warn line
        g.setColor(warning);
        g.drawLine(0, warnHeight, dim.width, warnHeight);
       
        
        x = 0;
        y = dim.height - (int) (stepHeight*(statusList[0]/stepPercent));
        g.setColor(fill);
        for (int i = 1; i < statusList.length; i++) {
            int old_x = x;
            int old_y = y;
            x += stepWidth;
            y = dim.height - (int) (stepHeight*(statusList[i]/stepPercent));
            g.drawLine(old_x, old_y, x, y);
        }
    } //-- paint
    
    /**
     * Sets the current percentage for display. 
     * @param percent the percent value to display. This value
     * should be a number between 0 and 1.
    **/
    public void setPercent(float status) {
        moveUp();
        statusList[0] = (int)(status*100);
        update(getGraphics());
    } //-- setPercent
    
    public void update(Graphics g) {
        if (_dblBuffer == null) initDoubleBuffer();        
        //-- protect against unable to create double buffer
        if (_dblBuffer == null) return;
        Graphics dbg = _dblBuffer.getGraphics();
        Dimension d = getSize();
        dbg.clearRect(0,0,d.width,d.height);
        paint(dbg);
        g.drawImage(_dblBuffer, 0, 0, this);
    } //-- update
    
    /**
     * Initializes the double buffer
    **/
    private void initDoubleBuffer() {
        Dimension d = getSize();
        _dblBuffer = createImage(d.width,d.height);
    }
    
    private void moveUp() {
        for (int i = statusList.length-1; i > 0; i--) 
            statusList[i] = statusList[i-1];
    }
    
} //-- StatusGraph