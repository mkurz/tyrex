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
 * $Id: StatusDataFeed.java,v 1.1 2000/01/11 00:33:46 roro Exp $
 */

package org.exolab.fx;


/**
 * The abstract class used by the StatusPanel for retrieving data
 * @author <a href="mailto:kvisco@exoffice.com">Keith Visco</a>
 * @version $Revision: 1.1 $ $Date: 2000/01/11 00:33:46 $
**/
public abstract class StatusDataFeed {
    
    
    /**
     * Creates a new StatusDataFeed
    **/
    public StatusDataFeed() {
        super();
    } //-- StatusDataFeed
    
    /**
     * Returns the actual value that should be displayed below
     * the status bar, or null if no actual value should be 
     * displayed.
     * @return the actual value that should be displayed below
     * the status bar, or null if no actual value should be 
     * displayed.
     * <BR />
     * By default null will be returned.
    **/
    public Number getActual() { return null; }
    
    /**
     * Returns the description String that should be displayed 
     * below the status bar, or null if no description should be 
     * displayed.
     * @return the description String that should be displayed 
     * below the status bar, or null if no description should be 
     * displayed.
     * <BR />
     * By default null will be returned.
    **/
    public String getDescription() { return null; }
    
    /**
     * Returns the title that should be displayed above the
     * status bar, or null if no title should be displayed.
     * @return the title that should be displayed above the
     * status bar, or null if no title should be displayed.
     * <BR />
     * By default null will be returned.
    **/
    public String getTitle() { return null; }
    
    /**
     * Returns the unit information for the actual value. This
     * information will be displayed below the status bar 
     * to qualify the actual value.
     * @return the unit information for the actual value.
     * <BR />
     * By default null will be returned.
    **/
    public String getUnitInformation() { return null; }
    
    /**
     * Returns the percent value of the current status. The value
     * should be a number from 0 to 1. If the number is greater
     * than 1, a value of 1 will be used.
     * @return the percent value of the current status. The value
     * should be a number from 0 to 1.
    **/
    public abstract float getPercent();  

    
} //-- StatusDataFeed