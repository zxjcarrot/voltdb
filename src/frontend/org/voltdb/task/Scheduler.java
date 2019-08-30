/* This file is part of VoltDB.
 * Copyright (C) 2019 VoltDB Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with VoltDB.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.voltdb.task;

import java.util.Collection;

/**
 * Interface used to create a custom scheduler.
 * <p>
 * A scheduler instance can have external parameters supplied by the DDL. If a scheduler needs to have procedure
 * parameters passed in it is done through a {@code public void initialize} method. Only valid column types are allowed
 * as initialize parameters with two exceptions. The first one being that the first argument may be an instance of
 * {@link TaskHelper}, but this is optional. The other exception is that the last parameter can be either
 * {@code String[]} or {@code Object[]}. If the last parameter is an array then it will be treated as a var args
 * parameter.
 * <p>
 * Example initialize methods:
 *
 * <pre>
 * public void initialize(int interval, String timeUnit)
 * public void initialize(TaskHelper helper, int interval, String timeUnit)
 * public void initialize(int interval, String timeUnit, String procedureName, Object... procedureParameters)
 * public void initialize(TaskHelper helper, int interval, String timeUnit, String procedureName, Object... procedureParameters)
 * </pre>
 * <p>
 * Optionally an implementation can implement a {@code validateParameters} method which will be invoked during the DDL
 * validation phase. All parameters must match exactly the type of parameters passed to the initialize method of the
 * Scheduler implementation. The return of {@code validateParameters} must be a {@link String} which is {@code null} if
 * no error is detected otherwise an appropriate error message should be returned.
 */
public interface Scheduler {

    /**
     * This method is invoked for the first action to be performed. All subsequent invocation will be of
     * {@link #getNextAction(ActionResult)}
     * <p>
     * If this method throws an exception or returns {@code null} the scheduler will halted and put into an error state.
     *
     * @return {@link Action} with the first action to be performed on behalf of this scheduler
     */
    Action getFirstAction();

    /**
     * Process the result of the previous action. Then return an {@link Action} which indicates the next action to be
     * performed.
     * <p>
     * If this method throws an exception or returns {@code null} the scheduler will halted and put into an error state.
     *
     * @param result of last action executed
     * @return {@link Action} with the next action to be performed on behalf of this scheduler
     */
    Action getNextAction(ActionResult result);

    /**
     * If this method is implemented then the scheduler will only be restarted when it or any classes marked as a
     * dependency are modified. However if this method is not implemented then the Scheduler instance will be restarted
     * any time any class is modified.
     *
     * @return {@link Collection} of {@code classesNames} which this instance depends upon.
     */
    default Collection<String> getDependencies() {
        return null;
    }

    /**
     * If this method returns {@code true} that means the type of procedure which this scheduler can run is restricted
     * based upon the scope type.
     * <ul>
     * <li>SYSTEM - No restrictions</li>
     * <li>HOSTS - Only NT procedures are allowed</li>
     * <li>PARTITIONS - Only partitioned procedures are allowed</li>
     * </ul>
     *
     * @return {@code true} if the type of procedure this scheduler can run should be restricted based upon scope
     */
    default boolean restrictProcedureByScope() {
        return false;
    }
}