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

package com.quinsoft.zeidon.jmx;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;


/**
 * Interface for standard JMX operations with the Object Engine.
 *
 * @author dg
 *
 */
public interface JmxObjectEngineMonitorMBean
{
    /**
     * Returns a list of view names for each task.
     *
     * @return
     */
    Collection<Map<String, String>> getViewList();

    /**
     * @return A list of task IDs for all active tasks.
     */
    Collection<String> getTaskList();

    /**
     * Writes a list of all active views to the Zeidon log for the supplied task.
     *
     * @param taskId
     * @return Status message.
     */
    String logViews( String taskId );

    /**
     * Drops the view specified by taskId and viewName.
     *
     * @param taskId
     * @param viewName
     * @return
     */
    String dropViewByName( String taskId, String viewName );

    /**
     * @return runtime Properties.
     */
    Properties getRuntimeProperties();

    /**
     * Starts the object browser.
     *
     * @return
     */
    String startObjectBrowser();
}
