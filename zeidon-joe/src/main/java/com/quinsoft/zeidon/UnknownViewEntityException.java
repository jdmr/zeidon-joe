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

package com.quinsoft.zeidon;

import com.quinsoft.zeidon.objectdefinition.ViewOd;

/**
 * @author DG
 *
 */
public class UnknownViewEntityException extends ZeidonException
{
    private static final long serialVersionUID = 1L;

    public UnknownViewEntityException( ViewOd viewOd, String viewEntityName )
    {
        super( "Entity name %s does not exist for View OD %s.", viewEntityName, viewOd );
    }
}
