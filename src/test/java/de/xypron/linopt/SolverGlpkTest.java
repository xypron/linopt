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

import junit.framework.TestCase;
import org.gnu.glpk.GlpkTerminal;
import org.gnu.glpk.GlpkTerminalListener;

/**
 *
 * @author Heinrich Schuchardt
 */
public class SolverGlpkTest extends TestCase implements GlpkTerminalListener {

    public SolverGlpkTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp()
            throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown()
            throws Exception {
        super.tearDown();
    }

    /**
     * Test of solve method, of class SolverGlpk.
     */
    public void testSolve() {
        Problem p = createTestProblem();
        System.out.println("testSolve - 1");
        double result;
        double expResult;

        Solver s = new SolverGlpk();

        expResult = p.objective().getValue();
        p.objective().setValue(0.);
        result = p.objective().getValue();
        assertFalse("objective set value", result == expResult);

        // block output
        GlpkTerminal.addListener(this);
        // solve
        assertTrue("no solution - 1", s.solve(p));
        GlpkTerminal.removeListener(this);

        result = p.objective().getValue();
        assertEquals("solution value - 1", expResult, result);

        // ------------
        
        p = createTestProblem2();
        System.out.println("testSolve - 2");

        s = new SolverGlpk();

        expResult = p.objective().getValue();
        p.objective().setValue(0.);
        result = p.objective().getValue();
        assertFalse("objective set value", result == expResult);

        // block output
        GlpkTerminal.addListener(this);
        // solve
        assertTrue("no solution - 2", s.solve(p));
        GlpkTerminal.removeListener(this);

        result = p.objective().getValue();
        assertEquals("solution value - 2", expResult, result);
    
    }

    @Override
    public boolean output(String str) {
        return false;
    }

    public Problem createTestProblem() {
        final String COLUMN_USE = "u";
        final String COLUMN_CUT = "x";
        final String OBJECTIVE = "waste";
        final String PROBLEM = "CuttingStock";
        final String ROW_DEMAND = "demand";
        final String ROW_STOCK = "stock";

        Problem p;

        // lengths of stock items
        long[] stock = {50, 50, 100, 100, 100, 200, 200};
        // sales products
        long[] product = {6, 10, 16};
        // demand per sales product
        long[] demand = {20, 10, 3};

        // define problem
        p = new Problem();
        p.setName(PROBLEM);

        // define columns
        for (int i = 0; i < stock.length; i++) {
            // u(i) : stock element i is used
            p.column(COLUMN_USE, i).type(Problem.ColumnType.BINARY);
            for (int j = 0; j < product.length; j++) {
                // x(i,j) : x pieces of product j are cut from stock i
                p.column(COLUMN_CUT, i, j).
                        type(Problem.ColumnType.INTEGER).
                        bounds(0., null);
            }
        }
        // define objective
        p.objective(OBJECTIVE, Problem.Direction.MINIMIZE);
        for (int i = 0; i < stock.length; i++) {
            // Minimize waste :
            //     stockLength(i) * u(i) - sum( productLength(j) * x(i,j) )
            p.objective().add(stock[i], COLUMN_USE, i);
            for (int j = 0; j < product.length; j++) {
                p.objective().add(-product[j], COLUMN_CUT, i, j);
            }
        }
        // define rows
        for (int i = 0; i < stock.length; i++) {
            // stockLength(i) * u(i) >= sum( productLength[j] * x(i,j)
            p.row(ROW_STOCK, i).bounds(0., null).
                    add(stock[i], COLUMN_USE, i);
            for (int j = 0; j < product.length; j++) {
                p.row(ROW_STOCK, i).add(-product[j], COLUMN_CUT, i, j);
            }
        }
        for (int j = 0; j < product.length; j++) {
            // demand(j) = sum( x(i,j) )
            p.row(ROW_DEMAND, j).bounds((double) demand[j], (double) demand[j]);
            for (int i = 0; i < stock.length; i++) {
                p.row(ROW_DEMAND, j).add(1., COLUMN_CUT, i, j);
            }
        }

        // expected result
        p.objective().setValue(32);
        return p;
    }

    public Problem createTestProblem2() {
        final String COLUMN = "C";
        final String OBJECTIVE = "obj";
        final String PROBLEM = "Trivial";
        final String ROW = "R";

        Problem p;

        p = new Problem().setName(PROBLEM);

        // define column
        p.column(COLUMN).type(Problem.ColumnType.FLOAT);
        // define objective
        p.objective(OBJECTIVE, Problem.Direction.MINIMIZE)
                .add(1., COLUMN);
        // define rows
        p.row(ROW).bounds(2., 3.).add(1., COLUMN);

        // expected result
        p.objective().setValue(2.);
        return p;
    }

}
