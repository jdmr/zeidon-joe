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
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.quinsoft.zeidon.Application;
import com.quinsoft.zeidon.InvalidAttributeValueException;
import com.quinsoft.zeidon.ObjectEngine;
import com.quinsoft.zeidon.Task;
import com.quinsoft.zeidon.ZeidonException;
import com.quinsoft.zeidon.objectdefinition.ViewAttribute;
import com.quinsoft.zeidon.utils.JoeUtils;
import com.quinsoft.zeidon.utils.PortableFileReader;

/**
 * @author DG
 *
 */
public class DateDomain extends AbstractDomain
{
    protected DateTimeFormatter defaultDateFormatter = DateTimeFormat.forPattern( ObjectEngine.INTERNAL_DATE_STRING_FORMAT );
    
    public DateDomain(Application application, Map<String, Object> domainProperties, Task task)
    {
        super( application, domainProperties, task );
    }

    @Override
    public Object convertExternalValue(Task task, ViewAttribute viewAttribute, String contextName, Object externalValue)
    {
    	// KJS - Added 01/27/11 because of line 2836 in lTrnscpt_Object.java
    	// OrderEntityForView( lTrnscpt, "StudentMajorDegreeTrack", "wPrimarySortOrder A GraduationDate A" );
    	// value = null so we are getting to the exception.  Will try returning null, see what happens.
    	if ( externalValue == null )
    		return null;
    	
        if ( externalValue instanceof DateTime )
            return externalValue;
    	
        if ( externalValue instanceof Date )
            return new DateTime( externalValue );

        // VML operations use "" as synonymous with null.
        if ( externalValue instanceof String && StringUtils.isBlank( (String) externalValue ) )
            return null;

        if ( externalValue instanceof CharSequence )
        {
            // If string is "NOW" then we'll use current datetime.
            if ( externalValue.toString().equals( "NOW" ) )
                return new DateTime();
                        
            DomainContext context = getContext( task, contextName );
            return context.convertExternalValue( task, viewAttribute, externalValue.toString() );
        }

        throw new InvalidAttributeValueException( viewAttribute, externalValue, 
                                                  "Invalid object: Domain %s cannot convert value for context %s.",
                                                  this.getClass().getName(), contextName );
    }

    
    @Override
    public int compare(Task task, ViewAttribute viewAttribute, Object internalValue, Object externalValue)
    {
        
        try
        {
        	// Had to add this so that value.getClass is DateTime.
            Object value = convertExternalValue( task, viewAttribute, null, externalValue );
            Integer rc = compareNull( task, viewAttribute, internalValue, value);
            if ( rc != null )
                return rc;
            
            if ( internalValue instanceof Comparable )
            {
                assert internalValue.getClass() == value.getClass();
                
                // KJS 07/18/14 - When we are comparing Dates (as opposed to DateTime) we don't
                // want to compare with the Time portion (which is what internalValue and value have).
                // Only compare the Date portion.
                return DateTimeComparator.getDateOnlyInstance().compare(internalValue, value);                
            }
            
            DomainContext context = getContext( task, null ); // Get the default context.
            return context.compare( task, internalValue, value );
        }
        catch ( Throwable t )
        {
            throw ZeidonException.wrapException( t ).prependViewAttribute( viewAttribute );
        }

   }
    
    /**
     * Attempt to get the context.  This differs from the normal getContext() by attempting to create
     * a SimpleDateFormatter using the contextName as the format if a context is not found by name.
     */
    @Override
    public DomainContext getContext(Task task, String contextName)
    {
        DomainContext context = getContext( contextName );
        if ( context != null )
            return context;  // We found one by name.
        
        if ( StringUtils.isBlank( contextName ) )
            throw new ZeidonException("Domain '%s' does not have a default context defined.", getName() );

        // Create a temporary new one and set its edit string to the context name.
        DateContext dateContext = new DateContext( this );
        dateContext.setName( contextName );
        dateContext.setEditString( contextName );
        return dateContext;
    }

    @Override
    public void validateInternalValue( Task task, ViewAttribute viewAttribute, Object internalValue ) throws InvalidAttributeValueException
    {
      if ( internalValue instanceof DateTime )
            return;
        
        throw new InvalidAttributeValueException( viewAttribute, internalValue, "Internal value must be Joda DateTime, not %s",
                                                  internalValue.getClass().getName() );
    }

    @Override
    public String convertToString(Task task, ViewAttribute viewAttribute, Object internalValue)
    {
        if ( internalValue == null )
            return StringDomain.checkNullString( viewAttribute.getDomain().getApplication(), null );
  	
    	if ( internalValue.toString().isEmpty())
    		return internalValue.toString();
        
        return defaultDateFormatter.print( (DateTime) internalValue );
    }

    /**
     * Adds milliseconds to the datetime value.
     */
    @Override
    public Object addToAttribute( Task task, ViewAttribute viewAttribute, Object currentValue, Object addValue )
    {
        DateTime date1 = (DateTime) convertExternalValue( task, viewAttribute, null, currentValue );
        
        if ( addValue == null )
            return date1;
        
        if ( addValue instanceof Number )
        {
            int millis = ((Number) addValue).intValue();
            return date1.plusMillis( millis );
        }
        
        throw new ZeidonException( "Value type of %s not supported for add to DateDomain", addValue.getClass().getName() );
    }

    @Override
    public DomainContext newContext(Task task)
    {
        return new DateContext( this );
    }

    private class DateContext extends BaseDomainContext
    {
        /**
         * URL for Java SimpleDateFormat help.
         */
        private static final String JAVA_DATE_FORMATTING_URL = "http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html";

        public DateContext(Domain domain)
        {
            super( domain );
        }

        private String            editString;
        private DateTimeFormatter formatter;
        
        @Override
        public String convertToString(Task task, ViewAttribute viewAttribute, Object internalValue) throws ZeidonException
        {
        	if ( internalValue == null )
        		return StringDomain.checkNullString(task.getApplication(), null);
        	
        	if ( internalValue.toString().isEmpty())
        		return internalValue.toString();
        	
        	if ( formatter == null )
        	    throw new ZeidonException( "JaveEditString is not set for context %s", this.toString() );

            return formatter.print( (DateTime) internalValue );
        }

        /**
         * Assumes a string.
         */
        @Override
        public Object convertExternalValue(Task task, ViewAttribute viewAttribute, Object value) throws InvalidAttributeValueException
        {
        	if ( value == null )
        		return null;
        	
            String s = (String) value;

            // VML operations use "" as synonymous with null.
        	if ( StringUtils.isBlank( s ) )
        		return null;
       	
        	// A common internal date/time format is the DefaultTimeFormat.  Let's try
        	// a quick check to see if 's' is the same length and only digits.
        	if ( s.length() >= ObjectEngine.INTERNAL_DATE_STRING_FORMAT.length() && JoeUtils.onlyDigits( s ) )
                return JoeUtils.parseStandardDateString( s );
        	
        	if ( formatter == null )
        	    throw new ZeidonException( "JaveEditString is not set for context %s", this.toString() );
        	
        	try
        	{
        	    return formatter.parseDateTime( s );
        	}
        	catch ( Exception e )
        	{
        	    throw new InvalidAttributeValueException( viewAttribute, s, e.getMessage() )
        	                         .appendMessage( "Format string = %s", editString )
        	                         .appendMessage( "See %s for help on Java Date formatting", JAVA_DATE_FORMATTING_URL )
        	                         .setCause( e );
        	}
        }

        private void setEditString( String editString )
        {
            this.editString = editString;
            formatter = DateTimeFormat.forPattern( editString );
        }
        
        @Override
        public void setAttribute(PortableFileReader reader)
        {
            if ( reader.getAttributeName().equals( "JavaEditString" ) )
                setEditString( reader.getAttributeValue() );
            else
                super.setAttribute( reader );
        }
    }
}