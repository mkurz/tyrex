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
 *    permission of Intalio Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio Technologies. Exolab is a registered
 *    trademark of Intalio Technologies.
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
 * Copyright 2001 (C) Intalio Technologies Inc. All Rights Reserved.
 *
 */

package tyrex.resource.castor;

import java.lang.reflect.Method;
import org.exolab.castor.mapping.ClassDescriptor;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.mapping.ValidityException;
import org.exolab.castor.types.AnyNode;
import org.exolab.castor.xml.NodeType;
import org.exolab.castor.xml.XMLFieldDescriptor;
import org.exolab.castor.xml.util.DefaultNaming;
import org.exolab.castor.xml.util.XMLClassDescriptorImpl;
import org.exolab.castor.xml.util.XMLFieldDescriptorImpl;

/////////////////////////////////////////////////////////////////////
// ResourceClassDescriptorImpl
/////////////////////////////////////////////////////////////////////

/**
 * Implementation of 
 * {@link org.exolab.castor.xml.XMLClassDescriptor} that returns
 * field descriptors that use 
 * {@link #ResourceFieldHandlerImpl resource field handlers}.
 */
class ResourceClassDescriptorImpl
	extends XMLClassDescriptorImpl {

	/**
	 * The naming
	 */
	private static final DefaultNaming NAMING = new DefaultNaming();

	static {
		NAMING.setStyle(DefaultNaming.MIXED_CASE_STYLE);
	}

	ResourceClassDescriptorImpl(Class type) {
		super(type);
		// we want wild card look up of field descriptors
		setIntrospected(true); 
		
		initialize();
	}

	/**
	 * Initialize the field descriptors for the class
	 */
	private void initialize() {
		Method[] methods;
		Method method;
		Class[] types;
		XMLFieldDescriptorImpl fieldDescriptor;
		String name;
		String xmlName;

		methods = getJavaClass().getMethods();

		for (int i = methods.length; --i >= 0;) {
			method = methods[i];

			if ((method.getReturnType() == Void.TYPE) &&
				(method.getName().length() > 3) &&
				method.getName().startsWith("set")) {
				types = method.getParameterTypes();		

				if ((1 == types.length) &&
					isValidType(types[0])) {
					name = method.getName().substring(3);
					xmlName = NAMING.toXMLName(name);

					if (ResourceMappingLoader.CATEGORY.isDebugEnabled()) {
						ResourceMappingLoader.CATEGORY.debug("Creating resource field descriptor for " + 
											  method.getName() + " and xml-name " + xmlName);	
					}

					fieldDescriptor = new XMLFieldDescriptorImpl(java.lang.Object.class, name, xmlName, null);
					fieldDescriptor.setHandler(new ResourceFieldHandlerImpl(method, types[0]));
					fieldDescriptor.setContainingClassDescriptor(this);
					addFieldDescriptor(fieldDescriptor);		
				}
			}
		}
	}

	/**
	 * Return true if the type is a primitive type, primitive 
	 * wrapper type or string type.
	 *
	 * @param type the type (required)
	 * @return true if the type is a primitive type, primitive
	 *		wrapper type or string type.
	 */
	private boolean isValidType(Class type) {
		return 	(type.isPrimitive() || 
				(String.class == type) ||
				(Boolean.class == type) ||
				(Integer.class == type) ||
				(Double.class == type) ||
				(Byte.class == type) ||
				(Short.class == type) ||
				(Long.class == type) ||
				(Float.class == type) ||
				(Character.class == type));
	}
}
