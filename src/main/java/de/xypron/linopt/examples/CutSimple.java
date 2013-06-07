/*
 *  Copyright (C) 2010-2012 Heinrich Schuchardt
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
/**
 * This program solves a small cutting stock problem. For solving usual size
 * problems column generation should be used.
 *
 * @author Heinrich Schuchardt
 */
package de.xypron.linopt.examples;

import de.xypron.linopt.Problem;
import de.xypron.linopt.Solver;
import de.xypron.linopt.SolverGlpk;
import org.gnu.glpk.GLPK;
import org.gnu.glpk.GlpkCallback;
import org.gnu.glpk.GlpkCallbackListener;
import org.gnu.glpk.glp_tree;

/**
 * A small cutting stock problem is solved.
 *
 * @author Heinrich Schuchardt
 */
public class CutSimple
        implements GlpkCallbackListener {

    /**
     * Column identifier for usage. u(i) : stock element i is used
     */
    static final String COLUMN_USE = "u";
    /**
     * Column identifier for cuts. x(i,j) : x pieces of product j are cut from
     * stock i
     */
    static final String COLUMN_CUT = "x";
    /**
     * Row identifier for objective.
     */
    static final String OBJECTIVE = "waste";
    /**
     * Problem identifier.
     */
    static final String PROBLEM = "CuttingStock";
    /**
     * Row identifier for demand.
     */
    static final String ROW_DEMAND = "demand";
    /**
     * Row identifier for stock length.
     */
    static final String ROW_STOCK = "stock";

    /**
     * Private constructor, never called.
     */
    private CutSimple() {
    }

    /**
     * Main method.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        new CutSimple().run(args);
    }

    /**
     * Run.
     *
     * @param args command line arguments
     */
    private void run(final String[] args) {
        Solver s = new SolverGlpk();

        for (String arg : args) {
            if (arg.equals("-f")) {
                GlpkCallback.addListener(this);
            } else if (arg.equals("-t")) {
                s.setTimeLimit(1.);
            } else if (arg.equals("-g")) {
                s.setMipGap(.3);
            } else {
                System.out.println("Usage: "
                        + this.getClass().toString()
                        + " [options]\n\n"
                        + "-f stop on first solution\n"
                        + "-t stop on time limit 1s\n"
                        + "-g set MIP gap .3\n\n"
                        + "Example:"
                        + "mvn exec:java -Dexec.args=-f");
                return;
            }
        }

        Problem p;
        // lengths of stock items
        long[] stock = {50, 50, 100, 100, 100, 200, 200};
        // sales products
        long[] product = {6, 10, 16, 25, 40, 63};
        // demand per sales product
        long[] demand = {20, 10, 3, 7, 2, 1};

        // define problem
        p = new Problem(PROBLEM);

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

        // write problem
        System.out.println(p.problemToString());

        // solve
        if (!s.solve(p)) {
            System.out.println("No solution found.");
        } else {
            // output solution
            System.out.println("Production plan");
            for (int i = 0; i < stock.length; i++) {
                boolean flag = false;
                for (int j = 0; j < product.length; j++) {
                    double v = p.column(COLUMN_CUT, i, j).getValue();
                    if (v > 0) {
                        if (flag) {
                            System.out.print(" + ");
                        } else {
                            System.out.print(stock[i] + " >= ");
                        }
                        System.out.print(v + " * " + product[j]);
                        flag = true;
                    }
                }
                if (flag) {
                    System.out.println();
                }
            }
            System.out.println("Waste = " + p.objective().getValue());
        }
    }

    @Override
    public void callback(glp_tree tree) {
        int reason = GLPK.glp_ios_reason(tree);
        if (reason == GLPK.GLP_IBINGO) {
            System.out.println("Better integer solution found.");
        }
    }
}
