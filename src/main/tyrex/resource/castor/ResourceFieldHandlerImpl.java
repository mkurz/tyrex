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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.exolab.castor.mapping.FieldHandler;
import org.exolab.castor.mapping.ValidityException;
import org.exolab.castor.types.AnyNode;
import org.exolab.castor.xml.NodeType;

/////////////////////////////////////////////////////////////////////
// ResourceFieldHandlerImpl
/////////////////////////////////////////////////////////////////////

/**
 * Implementation of 
 * {@link org.exolab.castor.mapping.FieldHandler}.
 */
class ResourceFieldHandlerImpl
	implements FieldHandler {

	/**
	 * The set method
	 */
	private final Method _method;

	/**
	 * The type of the set method parameter. It is 
	 * a primitive type, primitive wrapper type or
	 * a string type.
	 */
	private Class _type;

	/**
	 * Create the ResourceFieldHandlerImpl
	 *
	 * @param method the set method (required)
	 * @param type the type of the set method parameter (optional)
	 */
	ResourceFieldHandlerImpl(Method method, Class type) {
		_method = method;
		_type = type;
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
		// don't care
		return null;
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
		Object actualValue;
		try {
			actualValue = getValueToBeSet(value);

			if (ResourceMappingLoader.CATEGORY.isDebugEnabled()) {
				ResourceMappingLoader.CATEGORY.debug("Setting value " + actualValue + " in object " + object);	
			}
			_method.invoke(object, new Object[]{actualValue});
		}
		catch (IllegalAccessException except) {
			throw new IllegalStateException(except.toString());
		} 
		catch (InvocationTargetException except) {
			throw new IllegalStateException(except.toString());
		}
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
		// don't care
	}


	/**
	 * @deprecated No longer supported
	 */
	public void checkValidity(Object object)
		throws ValidityException, IllegalStateException {
		// don't care
	}


	/**
	 * Creates a new instance of the object described by this field.
	 *
	 * @param parent The object for which the field is created
	 * @return A new instance of the field's value
	 * @throws IllegalStateException This field is a simple type and
	 *  cannot be instantiated
	 */
	public Object newInstance(Object parent)
		throws IllegalStateException {
		// don't care
		return null;
	}



	/**
	 * Return the actual value to be set in field from
	 * the specified value. If the value is of type
	 * org.castor.types.AnyNode then the underlying
	 *
	 * @param value the value (optional)
	 * @return the actual value to be set in field from
	 *		the specified value.
	 */
	private Object getValueToBeSet(Object value) {
		String string;

		if (null == value) {
			return null;	
		}
		else if (value instanceof AnyNode) {
			string = ((AnyNode)value).getStringValue();		
		}
		else {
			// this should not happen
			string = value.toString();
		}

		if ((Boolean.class == _type) ||
			(Boolean.TYPE == _type)) {
			return Boolean.valueOf(string);
        }
		if (String.class == _type) {
			return string;
		}
		if ((Integer.class == _type) ||
			(Integer.TYPE == _type)) {
			return Integer.valueOf(string);
		}
		if ((Double.class == _type) ||
			(Double.TYPE == _type)) {
			return Double.valueOf(string);
		}
		if ((Byte.class == _type) ||
			(Byte.TYPE == _type)) {
			return Byte.valueOf(string);
		}
		if ((Short.class == _type) ||
			(Short.TYPE == _type)) {
			return Short.valueOf(string);
		}
		if ((Long.class == _type) ||
			(Long.TYPE == _type)) {
			return Long.valueOf(string);
		}
		if ((Float.class == _type) ||
			(Float.TYPE == _type)) {
			return Float.valueOf(string);
		}
		if ((Character.class == _type) ||
			(Character.TYPE == _type)) {
			if (1 != string.length()) {
				throw new IllegalArgumentException( "The argument is not valid for a character - '" + string + "'");
			}
			return new Character(string.charAt(0));
		}

		throw new IllegalArgumentException("");
	}
}
