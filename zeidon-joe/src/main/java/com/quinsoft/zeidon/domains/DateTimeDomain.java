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

import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.quinsoft.zeidon.Application;
import com.quinsoft.zeidon.ObjectEngine;
import com.quinsoft.zeidon.Task;
import com.quinsoft.zeidon.objectdefinition.ViewAttribute;

/**
 * This domain handles any differences needed to deal with dates+time.
 * @author DG
 *
 */
public class DateTimeDomain extends DateDomain
{
    // orig protected DateTimeFormatter defaultDateTimeFormatter = DateTimeFormat.forPattern( ObjectEngine.INTERNAL_DATE_STRING_FORMAT + "HH:mm:ss");
    protected DateTimeFormatter defaultDateTimeFormatter = DateTimeFormat.forPattern( ObjectEngine.INTERNAL_DATE_STRING_FORMAT + "HHmmss");
    
    public DateTimeDomain(Application application, Map<String, Object> domainProperties, Task task )
    {
        super( application, domainProperties, task );
    }
    
    @Override
    public String convertToString(Task task, ViewAttribute viewAttribute, Object internalValue)
    {
        if ( internalValue == null )
            return StringDomain.checkNullString( viewAttribute.getDomain().getApplication(), null );

        return defaultDateTimeFormatter.print( (DateTime) internalValue );
    }
}
