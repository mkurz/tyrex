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
 *    permission of Intalio.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio. Exolab is a registered
 *    trademark of Intalio.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY INTALIO AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * INTALIO OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999-2001 (C) Intalio Inc. All Rights Reserved.
 *
 * Contributions by MetaBoss team are Copyright (c) 2003-2004, Softaris Pty. Ltd. All Rights Reserved.
 *
 * $Id: Category.java,v 1.1 2004/05/06 06:04:41 metaboss Exp $
 */


package tyrex.util.logging;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author <a href="rost.vashevnik@metaboss.com">Rost Vashevnik</a>
 * @version $Revision: 1.1 $ $Date: 2004/05/06 06:04:41 $
 */
public class Category
{
	private static Map sCategories = new HashMap();
	private String mName;
	private Priority mPriority;
	private Log mLog;
	
	public static Category getInstance(String pCategoryName)
	{
		synchronized(sCategories)
		{
			Category lCategory = (Category)sCategories.get(pCategoryName);
			if (lCategory == null)
				sCategories.put(pCategoryName, lCategory = new Category(pCategoryName));
			return lCategory;
		}	
	}
	
	private Category (String pName)
	{
		mName = pName;
		mLog = LogFactory.getLog(pName);
	}
	
	public String getName()
	{
		return mName;
	}
	
	public void setPriority(Priority pPriority)
	{
		mPriority = pPriority;
	}

    public boolean isDebugEnabled()
    {
    	return mLog.isDebugEnabled();
    }
    
	public void log(Priority pPriority, String pMessage)
	{
		if (pPriority.equals(Priority.DEBUG))
			mLog.debug(pMessage);
		else	
		if (pPriority.equals(Priority.INFO))
			mLog.info(pMessage);
		else	
		if (pPriority.equals(Priority.ERROR))
			mLog.error(pMessage);
	}

	public void info(String pMessage)
	{
		mLog.info(pMessage);
	}

	public void debug(String pMessage)
	{
		mLog.debug(pMessage);
	}
	public void debug(String pMessage, Throwable pThrowable)
	{
		mLog.debug(pMessage,pThrowable);
	}

	public void warn(String pMessage)
	{
		mLog.warn(pMessage);
	}

	public void error(String pMessage)
	{
		mLog.error(pMessage);
	}
	public void error(String pMessage, Throwable pThrowable)
	{
		mLog.error(pMessage, pThrowable);
	}
}
