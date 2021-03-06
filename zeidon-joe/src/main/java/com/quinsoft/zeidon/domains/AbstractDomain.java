/**
    This file is part of the Zeidon Java Object Engine (Zeidon JOE).

    Zeidon JOE is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Zeidon JOE is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Zeidon JOE.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2009-2012 QuinSoft
 */

package com.quinsoft.zeidon.domains;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.quinsoft.zeidon.Application;
import com.quinsoft.zeidon.Blob;
import com.quinsoft.zeidon.InvalidAttributeConversionException;
import com.quinsoft.zeidon.InvalidAttributeValueException;
import com.quinsoft.zeidon.Task;
import com.quinsoft.zeidon.ZeidonException;
import com.quinsoft.zeidon.objectdefinition.DomainType;
import com.quinsoft.zeidon.objectdefinition.InternalType;
import com.quinsoft.zeidon.objectdefinition.ViewAttribute;


/**
 * @author DG
 *
 */
public abstract class AbstractDomain implements Domain
{
    private final Application application;
    private final String name;
    private final Map<String, DomainContext> contextList = new HashMap<String, DomainContext>();
    private DomainContext defaultContext = null;
    
    /**
     * Internal domain type read from the XDM.
     */
    private final DomainType    domainType;
    private final InternalType  dataType;
    private final String        description;
    
    public AbstractDomain( Application app, Map<String, Object> domainProperties, Task task )
    {
        application = app;
        name = ((String) domainProperties.get( "Name" ) ).intern();
        domainType = DomainType.mapCode( ((String) domainProperties.get( "DomainType" )).charAt( 0 ) );
        dataType = InternalType.mapCode( (String) domainProperties.get( "DataType" ) );
        description = (String) domainProperties.get( "Desc" );
    }
    
    @Override
    public Object convertExternalValue(Task task, ViewAttribute viewAttribute, String contextName, Object externalValue)
    {
        validateInternalValue( task, viewAttribute, externalValue );
        return externalValue;
    }

    /* (non-Javadoc)
     * @see com.quinsoft.zeidon.domains.Domain#convertInternalValue(com.quinsoft.zeidon.Task, com.quinsoft.zeidon.objectdefinition.ViewAttribute, java.lang.Object)
     */
    @Override
    public Object convertInternalValue(Task task, ViewAttribute viewAttribute, Object value) throws InvalidAttributeValueException
    {
        // For most domains converting an internal value is the same as converting an external value.
        return convertExternalValue( task, viewAttribute, null, value );
    }
    
    @Override
    public void validateInternalValue( Task task, ViewAttribute viewAttribute, Object internalValue ) throws InvalidAttributeValueException
    {
        if ( internalValue instanceof String )
            return;
        
        throw new InvalidAttributeValueException( viewAttribute, internalValue, "Attribute isn't expecting a String, got %s", internalValue.getClass() );
    }

    @Override
    public Blob convertToBlob(Task task, ViewAttribute viewAttribute, Object internalValue)
    {
        if ( internalValue == null )
            return null;

        if ( internalValue instanceof Blob )
            return (Blob) internalValue;
        
        throw new InvalidAttributeConversionException( viewAttribute, "Cannot convert internal value of %s to Blob", viewAttribute.toString() );
    }

    @Override
    public DateTime convertToDate(Task task, ViewAttribute viewAttribute, Object internalValue)
    {
    	if ( internalValue == null )
    		return null;

    	if ( internalValue instanceof DateTime )
            return (DateTime) internalValue;

        if ( internalValue instanceof Date )
            return new DateTime( internalValue );
        
        throw new InvalidAttributeConversionException( viewAttribute, "Cannot convert internal value of %s to Date", viewAttribute.toString() );
    }

    @Override
    public Double convertToDouble(Task task, ViewAttribute viewAttribute, Object internalValue)
    {
    	if ( internalValue == null )
    		return null;

        if ( internalValue instanceof Double )
            return (Double) internalValue;

        try
        {
            return new Double( internalValue.toString() );
        }
        catch ( NumberFormatException e )
        {
            throw new InvalidAttributeConversionException( viewAttribute, "Cannot convert internal value of %s (%s) to Double", internalValue.toString(), viewAttribute.toString() );
        }
    }

    @Override
    public Integer convertToInteger(Task task, ViewAttribute viewAttribute, Object internalValue)
    {
        if ( internalValue == null )
            return null;

        if ( internalValue instanceof Integer )
            return (Integer) internalValue;

        try
        {
            return new Integer( internalValue.toString() );
        }
        catch ( NumberFormatException e )
        {
            throw new InvalidAttributeConversionException( viewAttribute, "Cannot convert internal value of %s (%s) to Integer", internalValue.toString(), viewAttribute.toString() );
        }
    }

    @Override
    public String convertToString(Task task, ViewAttribute viewAttribute, Object internalValue)
    {
        if ( internalValue == null )
            return StringDomain.checkNullString( viewAttribute.getDomain().getApplication(), null );

        return internalValue.toString();
    }

    @Override
    public Blob convertToBlob(Task task, ViewAttribute viewAttribute, Object internalValue, String contextName)
    {
        if ( internalValue == null )
            return null;

        if ( internalValue instanceof Blob )
            return (Blob) internalValue;
        
        throw new InvalidAttributeConversionException( viewAttribute, "Cannot convert %s to Blob", viewAttribute.toString() );
    }

    @Override
    public DateTime convertToDate(Task task, ViewAttribute viewAttribute, Object internalValue, String contextName)
    {
        if ( internalValue == null )
            return null;

        if ( internalValue instanceof DateTime )
            return (DateTime) internalValue;
        
        if ( internalValue instanceof Date )
            return new DateTime( internalValue );
        
        throw new InvalidAttributeConversionException( viewAttribute, "Cannot convert %s to Date", viewAttribute.toString() );
    }

    @Override
    public Double convertToDouble(Task task, ViewAttribute viewAttribute, Object internalValue, String contextName)
    {
        if ( internalValue == null )
            return null;

        if ( internalValue instanceof Double )
            return (Double) internalValue;
        
        throw new InvalidAttributeConversionException( viewAttribute, "Cannot convert %s to Double", viewAttribute.toString() );
    }

    @Override
    public Integer convertToInteger(Task task, ViewAttribute viewAttribute, Object internalValue, String contextName)
    {
        // TODO: Should we just use the context like in convertToString()?
        if ( internalValue == null )
            return null;

        if ( internalValue instanceof Integer )
            return (Integer) internalValue;
        
        throw new InvalidAttributeConversionException( viewAttribute, "Cannot convert %s to Integer", viewAttribute.toString() );
    }

    @Override
    public String convertToString(Task task, ViewAttribute viewAttribute, Object internalValue, String contextName)
    {
        DomainContext context = getContext( task, contextName );
        return context.convertToString( task, viewAttribute, internalValue );
    }

    @Override
    public Object addToAttribute( Task task, ViewAttribute viewAttribute, Object currentValue, Object operand )
    {
        throw new ZeidonException( "addToAttribute not supported for this domain" );
    }

    @Override
    public Object multiplyAttribute( Task task, ViewAttribute viewAttribute, Object currentValue, Object operand )
    {
        throw new ZeidonException( "multiplyAttribute not supported for this domain" );
    }
    
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * Looks for the context in contextList.  Returns null if it isn't found.
     * @param contextName
     * @return
     */
    protected DomainContext getContext( String contextName )
    {
        // Convert contextName to lower case so we can find the context irrespective of case.
        String lowerName; 
        if ( StringUtils.isBlank( contextName ) )
            lowerName = null;
        else
            lowerName = contextName.toLowerCase();

        if ( lowerName == null )
        {
            if ( defaultContext != null )
                return defaultContext;

            // We'll support domains that don't specify the default context if
            // there is only one context.
            if ( contextList.size() == 1 )
            {
                for ( DomainContext dc : contextList.values() )
                {
                    // Get the first context and return it.
                    defaultContext = dc;
                    return defaultContext;
                }
            }
        }

        DomainContext context = contextList.get( lowerName );
        return context;
    }
    
    @Override
    public DomainContext getContext(Task task, String contextName)
    {
        DomainContext context = getContext( contextName );
        if ( context == null )
        {
            if ( StringUtils.isBlank( contextName ) )
                throw new ZeidonException("Domain '%s' does not have a default context defined.", getName() );
            
            throw new ZeidonException("Couldn't find context '%s' for domain '%s'", contextName, getName() );
        }

        return context;
    }

    @Override
    public void addContext(Task task, DomainContext context)
    {
        contextList.put( context.getName().toLowerCase(), context );
        if ( context.isDefault() )
            defaultContext = context;
    }

    @Override
    public DomainType getDomainType()
    {
        return domainType;
    }

    @Override
    public InternalType getDataType()
    {
        return dataType;
    }

    protected Integer compareNull(Task task, ViewAttribute viewAttribute, Object internalValue, Object externalValue)
    {
        if ( isNull( task, viewAttribute, internalValue ) )
        {
            if ( isNull( task, viewAttribute, externalValue ) )
                return 0;

            return -1;
        }
        
        if ( isNull( task, viewAttribute, externalValue ) )
            return 1;
        
        return null;
    }
    
    @Override
    public int compare(Task task, ViewAttribute viewAttribute, Object internalValue, Object externalValue)
    {
        try
        {
            Object value = convertExternalValue( task, viewAttribute, null, externalValue );
            Integer rc = compareNull( task, viewAttribute, internalValue, value);
            if ( rc != null )
                return rc;
            
            if ( internalValue instanceof Comparable )
            {
                assert internalValue.getClass() == value.getClass();
                
                @SuppressWarnings("unchecked")
                Comparable<Object> c = (Comparable<Object>) internalValue;
                return c.compareTo( value );
            }
            
            DomainContext context = getContext( task, null ); // Get the default context.
            return context.compare( task, internalValue, value );
        }
        catch ( Throwable t )
        {
            throw ZeidonException.wrapException( t ).prependViewAttribute( viewAttribute );
        }
    }

    @Override
    public DomainContext newContext(Task task)
    {
        return new BaseDomainContext( this );
    }

    @Override
    public Application getApplication()
    {
        return application;
    }

    @Override
    public String toString()
    {
        return getApplication().getName() + "." + getName() + " [" + this.getClass().getName() + "]";
    }

    public String getDescription()
    {
        return description;
    }
    
    protected String getDefaultedProperty( Map<String, Object> domainProperties, String propName, String defaultValue )
    {
        if ( domainProperties.containsKey( propName ) )
            return (String) domainProperties.get( propName );
        
        return defaultValue;
    }
    
    @Override
    public boolean isNull( Task task, ViewAttribute viewAttribute, Object value )
    {
        if ( value == null )  // Null values are always null (duh).
            return true;
        
        if ( value instanceof String &&
             viewAttribute.getViewEntity().getViewOd().getApplication().nullStringEqualsEmptyString() &&
             StringUtils.isBlank( (String) value ) )
        {
            return true;
        }
        
        return false;
        // old isNull code...
        //return value == null;
    }
}
