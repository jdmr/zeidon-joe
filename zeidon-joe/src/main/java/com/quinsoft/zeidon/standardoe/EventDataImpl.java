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

package com.quinsoft.zeidon.standardoe;

import com.quinsoft.zeidon.EntityInstance;
import com.quinsoft.zeidon.EventData;
import com.quinsoft.zeidon.Task;
import com.quinsoft.zeidon.View;
import com.quinsoft.zeidon.objectdefinition.ViewAttribute;
import com.quinsoft.zeidon.objectdefinition.ViewEntity;

/**
 * @author DG
 *
 */
public class EventDataImpl implements EventData
{
    private final Task task;
    
    private View           view;
    private ViewEntity     viewEntity;
    private ViewAttribute  viewAttribute;
    private EntityInstance entityInstance;
    
    /**
     * @param task
     */
    public EventDataImpl(Task task)
    {
        this.task = task;
    }

    public EventDataImpl(View view)
    {
        this.task = view.getTask();
        this.view = view;
    }

    public EventDataImpl setEntityInstance( EntityInstance ei )
    {
        entityInstance = ei;
        return this;
    }
    
    public EventDataImpl setView( View v )
    {
        view = v;
        return this;
    }
    
    public EventDataImpl setViewEntity( ViewEntity ve )
    {
        viewEntity = ve;
        return this;
    }
    
    public EventDataImpl setViewAttribute( ViewAttribute va )
    {
        viewAttribute = va;
        return this;
    }
    
    /* (non-Javadoc)
     * @see com.quinsoft.zeidon.EventData#getTask()
     */
    @Override
    public Task getTask()
    {
        return task;
    }

    /* (non-Javadoc)
     * @see com.quinsoft.zeidon.EventData#getView()
     */
    @Override
    public View getView()
    {
        return view;
    }

    /* (non-Javadoc)
     * @see com.quinsoft.zeidon.EventData#getViewEntity()
     */
    @Override
    public ViewEntity getViewEntity()
    {
        return viewEntity;
    }

    /* (non-Javadoc)
     * @see com.quinsoft.zeidon.EventData#getViewAttribute()
     */
    @Override
    public ViewAttribute getViewAttribute()
    {
        return viewAttribute;
    }

    /* (non-Javadoc)
     * @see com.quinsoft.zeidon.EventData#getEntityInstance()
     */
    @Override
    public EntityInstance getEntityInstance()
    {
        return entityInstance;
    }
}
