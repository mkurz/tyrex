/*
 * (C) Copyright Keith Visco 1997-1999  All rights reserved.
 *
 * The contents of this file are released under an Open Source 
 * Definition (OSD) compliant license; you may not use this file 
 * execpt in compliance with the license. Please see license.txt, 
 * distributed with this file. You may also obtain a copy of the
 * license at http://www.clc-marketing.com/xslp/license.txt
 *
 * The program is provided "as is" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * The Copyright owner will not be liable for any damages suffered by
 * you as a result of using the Program. In no event will the Copyright
 * owner be liable for any special, indirect or consequential damages or
 * lost profits even if the Copyright owner has been advised of the
 * possibility of their occurrence.
 *
 * $Id: Panel3D.java,v 1.1.1.1 2000/01/11 00:33:46 roro Exp $
 */

package com.kvisco.gui;

import java.awt.*;

/**
 * @author <a href="mailto:kvisco@ziplink.net">Keith Visco</a>
 * @version $Revision: 1.1.1.1 $ $Date: 2000/01/11 00:33:46 $
**/
public class Panel3D extends Panel {

    // Static data members
    public final static int RAISED_BORDER = 0;
    public final static int SUNKEN_BORDER = 1;
    public final static int RAISED = 2;
    public final static int SUNKEN = 3;
    public final static int FLAT = 4;

    /* private data members */
    
    // Insets
    private final int iTop    = 4;
    private final int iLeft   = 4;
    private final int iBottom = 4;
    private final int iRight  = 4;
    
    private int style = 0;
    private Image doubleBuffer = null;
    
    /* public data members */
    public boolean raised = true; // boolean indicating raised or sunken   
    public int lw = 1;            // line width

    public Panel3D() {
        super();
        this.style = RAISED;
    }
    
    public Panel3D(int style) {
        super();
        this.style = style;
    }
    
    private void draw3D (Graphics g, boolean raised) {
        Dimension d = size();
        Color oldColor = g.getColor();
        Color cMain = getBackground();
        Color cLight;
        Color cDark;

        cLight = lighter(cMain);
        cDark = darker(cMain);
        
        //-- clear all;
        g.setColor(cMain);
        g.clearRect(0, 0, d.width, d.height);
        
        // Draw Top and Left sides
        if (raised) g.setColor(lighter(cLight));
        else g.setColor(darker(cDark));
        g.drawLine(0,0,d.width-2,0); // Top outside
        g.drawLine(0,0,0,d.height-2); // left outside
        if (raised) g.setColor(cLight);
        else g.setColor(cDark);
        g.drawLine(1,1,d.width-3,1); // Top inside
        g.drawLine(1,1,1,d.height-3); // Left inside

        // Draw Right and Bottom sides
        if (raised) g.setColor(cDark);
        else g.setColor(cLight);
        g.drawLine(1,d.height-2,d.width-3,d.height-2); // Bottom inside
        g.drawLine(d.width-2,2,d.width-2,d.height-2); // Right inside
        if (raised) g.setColor(darker(cDark));
        else g.setColor(lighter(cLight));
        g.drawLine(0,d.height-1,d.width-2,d.height-1); // Bottom
        g.drawLine(d.width-1,1,d.width-1,d.height-1); // Right

        g.setColor(oldColor);
    }
    
    private void drawRaisedBorder(Graphics g) {
        Dimension d = size();
        Color oldColor = g.getColor();
        Color cMain = getBackground();
        Color cLight = cMain.brighter();
        Color cDark = cMain.darker();
        //-- clear all;
        g.setColor(cMain);
        g.clearRect(0, 0, d.width, d.height);
        //-- draw Border
        g.setColor(cDark);
        g.drawRect(0,0,d.width-2,d.height-2);
        g.setColor(cLight);
        g.drawRect(1,1,d.width-2,d.height-2);
        g.setColor(oldColor);
    }

    private void drawSunkenBorder(Graphics g) {
        Dimension d = size();
        Color oldColor = g.getColor();
        Color cMain = getBackground();
        Color cLight = cMain.brighter();
        Color cDark = cMain.darker();
        
        //-- clear all;
        g.setColor(cMain);
        g.clearRect(0, 0, d.width, d.height);
        //-- draw Border
        g.setColor(cLight);
        g.drawRect(0,0,d.width-2,d.height-2);
        g.setColor(cDark);
        g.drawRect(1,1,d.width-2, d.height-2);
        g.setColor(oldColor);
    }
    
    public int getBorder() { return lw; }
    
    public Insets getInsets() {
        return new Insets(iTop, iLeft, iBottom, iRight);
    }
    
    public void setBorder(int border) {
        lw = border;
        repaint();
    }
    public void setStyle(int x) {
        style = x;
    }
    public void paint(Graphics g) {
        
        if (doubleBuffer == null) initDoubleBuffer();
        
        //-- protect against unable to create double buffer
        if (doubleBuffer == null) return;
        
        Graphics dbg = doubleBuffer.getGraphics();
        super.paint(dbg);
        switch (style) {
            case RAISED_BORDER:
                drawRaisedBorder(dbg);
                break;
            case RAISED:
                draw3D(dbg, true);
                break;
            case SUNKEN_BORDER:
                drawSunkenBorder(dbg);
                break;
            case SUNKEN:
                draw3D(dbg, false);
                break;
            default:
                break;
        }
        g.drawImage(doubleBuffer,0,0,this);
    }
    
    public void update(Graphics g) {
        if (doubleBuffer == null) initDoubleBuffer();        
        //-- protect against unable to create double buffer
        if (doubleBuffer == null) return;
        Graphics dbg = doubleBuffer.getGraphics();
        Dimension d = getSize();
        dbg.clearRect(0,0,d.width,d.height);
        paint(g);
    }
    
    /**
     * Returns a darker Color than the given Color
     * @param c the Color in which to return a darker version of
     * @return a darker Color than the given Color
    **/
    private Color darker (Color c) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        if ((r -= ((r + 255) / 10)) < 0) r = 0;
        if ((g -= ((g + 255) / 10)) < 0) g = 0;
        if ((b -= ((b + 255) / 10)) < 0) b = 0;
        return new Color(r,g,b);
    }
    
    /**
     * Initializes the double buffer
    **/
    private void initDoubleBuffer() {
        Dimension d = getSize();
        doubleBuffer = createImage(d.width,d.height);
    }
    
    
    /**
     * @param c the Color in which to return a lighter version of
     * @return a lighter Color than the given Color
    **/
    private Color lighter(Color c) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        if ((r = r + ((255 + r) / 10)) > 255) r = 255;
        if ((g = g + ((255 + g) / 10)) > 255) g = 255;
        if ((b += ((255 + b) / 10)) > 255) b = 255;
        return new Color(r, g, b);
    }

    


}
