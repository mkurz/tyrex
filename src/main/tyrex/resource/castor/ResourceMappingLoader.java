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

import org.exolab.castor.mapping.ClassDescriptor;
import org.exolab.castor.mapping.FieldDescriptor;
import org.exolab.castor.mapping.FieldHandler;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.mapping.ValidityException;
import org.exolab.castor.mapping.xml.FieldMapping;
import org.exolab.castor.xml.XMLMappingLoader;
import org.exolab.castor.xml.util.XMLFieldDescriptorImpl;

import tyrex.resource.ResourceConfig;
import tyrex.util.logging.Category;
import tyrex.util.logging.Priority;

/////////////////////////////////////////////////////////////////////
// ResourceMappingLoader
/////////////////////////////////////////////////////////////////////

/**
 * Implementation of org.exolab.castor.xml.XMLMappingLoader that 
 * overrides the class descriptor for non tyrex and java classes.
 * <P>
 * This class is not thread-safe.
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
public final class ResourceMappingLoader 
	extends XMLMappingLoader {

	/**
	 * Logging category
	 */
	static final Category CATEGORY = tyrex.util.logging.Logger.castor; //Category.getInstance("tyrex.resource.castor");

	/**
	 * The new class to create a class descriptor for
	 */
	private Class _newClass;

	/**
     * Creates the ResourceMappingLoader
     */
    public ResourceMappingLoader()
        throws MappingException {
        super(null, null);
   		CATEGORY.setPriority(Priority.ERROR );
		_newClass = null;
    }
    
	/**
     * Returns the class descriptor for the specified Java class.
     * In no such descriptor exists, returns null.
	 * <P>
	 * If the type comes from an object created by a field whose type
	 * is {@link tyrex.resource.ResourceConfig} then a special
	 * class descriptor is created and returned.
     *
     * @param type The Java class
     * @return A suitable class descriptor or null
     */
	public ClassDescriptor getDescriptor(Class type) {
		ClassDescriptor descriptor;
        
		descriptor = super.getDescriptor(type);

		if ((null == descriptor) && (type == _newClass)) {
			if (CATEGORY.isDebugEnabled()) {
				CATEGORY.debug("Creating resource class descriptor for " + type);
			}
			try {
				descriptor = new ResourceClassDescriptorImpl(type);
				addDescriptor(descriptor);
				_newClass = null;
			}
			catch(MappingException e) {
				// this should not happen
				throw new RuntimeException(e.toString());
			}
		}

		return descriptor;
	}


	/**
     * Creates a single field descriptor. The field mapping is used to
     * create a new stock {@link FieldDescriptor}. 
	 * <P>
	 * If the java class is a subclass of 
	 * {@link tyrex.resource.ResourceConfig} then the field handler
	 * of the default field descriptor is overridden with
	 * {@link #SpyFieldHandlerImpl}.
     *
     * @param javaClass The class to which the field belongs
     * @param fieldMap The field mapping information
     * @return The field descriptor
     * @throws MappingException The field or its accessor methods are not
     *  found, not accessible, not of the specified type, etc
     */
    protected FieldDescriptor createFieldDesc(Class javaClass, FieldMapping fieldMap)
        throws MappingException {
		FieldDescriptor descriptor;

		descriptor = super.createFieldDesc(javaClass, fieldMap);

		if (ResourceConfig.class.isAssignableFrom(javaClass)) {
			((XMLFieldDescriptorImpl)descriptor).setHandler(new SpyFieldHandlerImpl(descriptor.getHandler()));
		}

		return descriptor;
	}

	/**
	 * Implementation of org.exolab.castor.mapping.FieldHandler
	 * that intercepts calls to newInstance for 
	 * {@link tyrex.resource.ResourceConfig} classes.
	 */
	private class SpyFieldHandlerImpl
		implements FieldHandler
	{

		/**
		 * The actual field handler
		 */
		private final FieldHandler _handler;


		/**
		 * Create the SpyFieldHandlerImpl
		 *
		 * @param handler the actual field handler (required)
		 */
		private SpyFieldHandlerImpl(FieldHandler handler) {
			_handler = handler;
		}


		/**
		 * Returns the value of the field from the object.
		 *
		 * @param object The object
		 * @return The value of the field
		 * @throws IllegalStateException The Java object has changed and
		 *  is no longer supported by this handler, or the handler is not
		 *  compatiable with the Java object
		 */
		public Object getValue(Object object)
			throws IllegalStateException {
			return _handler.getValue(object);
		}
		
	
		/**
		 * Sets the value of the field on the object.
		 *
		 * @param object The object
		 * @param value The new value
		 * @throws IllegalStateException The Java object has changed and
		 *  is no longer supported by this handler, or the handler is not
		 *  compatiable with the Java object
		 * @thorws IllegalArgumentException The value passed is not of
		 *  a supported type
		 */
		public void setValue(Object object, Object value)
			throws IllegalStateException, IllegalArgumentException {
			_handler.setValue(object, value);
		}
	
	
		/**
		 * Sets the value of the field to a default value.
		 * <p>
		 * Reference fields are set to null, primitive fields are set to
		 * their default value, collection fields are emptied of all
		 * elements.
		 *
		 * @param object The object
		 * @throws IllegalStateException The Java object has changed and
		 *  is no longer supported by this handler, or the handler is not
		 *  compatiable with the Java object
		 */
		public void resetValue(Object object)
			throws IllegalStateException, IllegalArgumentException {
			_handler.resetValue(object);
		}
	
	
		/**
		 * @deprecated No longer supported
		 */
		public void checkValidity(Object object)
			throws ValidityException, IllegalStateException {
			_handler.checkValidity(object);
		}
	
	
		/**
		 * Creates a new instance of the object described by this field.
		 * <P>
		 * If a non-null object is created then set 
		 * {@link ResourceMappingLoader#_newClass} to the new class so that
		 * a {@link tyrex.resource.castor.ResourceClassDescriptorImpl} 
		 * can be created.
		 *
		 * @param parent The object for which the field is created
		 * @return A new instance of the field's value
		 * @throws IllegalStateException This field is a simple type and
		 *  cannot be instantiated
		 */
		public Object newInstance(Object parent)
			throws IllegalStateException {
			Object object;

			object = _handler.newInstance(parent);

			if (null != object) {
				_newClass = object.getClass();	
			}

			return object;
		}
	}

}
