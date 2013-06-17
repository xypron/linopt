/*
 *  Copyright (C) 2010 Heinrich Schuchardt
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.xypron.linopt;

/**
 * Solver defines a unique interface for all linear programming packages.
 *
 * @author Heinrich Schuchardt
 */
public interface Solver {

    /**
     * Enables or disables presolver.
     * The preosolver is enabled by default. For using callback routines
     * it may have to be disabled.
     * @param enabled The presolver will be enabled if set to true.
     * @return true if successful
     */
    boolean setPresolve(boolean enabled);

    /**
     * Set relative mip gap. If the mip gap is smaller than the value provided
     * the solution process is stopped and the currently best solution is used.
     * The value has to be set before the solve method is called.
     *
     * @param gap relative mip gap
     * @return true if successful
     */
    boolean setMipGap(double gap);

    /**
     * Set time limit. If the solution process exceeds the the time limit, the
     * solution process is stopped and the currently best solution is used. The
     * value has to be set before the solve method is called.
     *
     * @param duration duration in seconds
     * @return true if successful
     */
    boolean setTimeLimit(double duration);

    /**
     * Solve problem.
     *
     * @param p problem
     * @return true if successful
     */
    boolean solve(Problem p);
}
