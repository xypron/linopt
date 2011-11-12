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

import java.util.Map.Entry;
import java.util.TreeMap;
import org.gnu.glpk.*;

/**
 * Wrapper for GLPK for Java.
 * @author Heinrich Schuchardt
 */
public class SolverGlpk implements Solver {

    @Override
    public final boolean solve(final Problem p) {
        try {
            return solveInternal(p);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    /**
     * Solve the linear problem.
     * @param p problem.
     * @return success = true
     */
    private boolean solveInternal(final Problem p) {
        boolean ret = false;
        int i, j;
        glp_prob lp;
        glp_iocp iocp;
        SWIGTYPE_p_int col;
        SWIGTYPE_p_int row;
        SWIGTYPE_p_double val;
        Problem.Objective obj;

        //  Create problem
        lp = GLPK.glp_create_prob();
        GLPK.glp_term_hook(null, null);
        GLPK.glp_set_prob_name(lp, p.getName());

        //  Create columns
        GLPK.glp_add_cols(lp, p.getColumns().size());
        i = 0;
        for (Problem.Column c : p.getColumns()) {
            Double lb;
            Double ub;
            c.setColumnNumber(++i);
            GLPK.glp_set_col_name(lp, i, c.getKey());
            switch (c.getType()) {
                case BINARY:
                    GLPK.glp_set_col_kind(lp, i, GLPKConstants.GLP_BV);
                    break;
                case FLOAT:
                    GLPK.glp_set_col_kind(lp, i, GLPKConstants.GLP_CV);
                    break;
                case INTEGER:
                    GLPK.glp_set_col_kind(lp, i, GLPKConstants.GLP_IV);
                    break;
                default:
                    throw new RuntimeException("Illegal column type");
            }
            lb = c.getLowerBound();
            ub = c.getUpperBound();
            if (lb != null && ub == null) {
                GLPK.glp_set_col_bnds(lp, i, GLPKConstants.GLP_LO, lb, 0);
            } else if (lb == null && ub != null) {
                GLPK.glp_set_col_bnds(lp, i, GLPKConstants.GLP_UP, 0, ub);
            } else if (lb != null && ub != null) {
                if (lb.equals(ub)) {
                    GLPK.glp_set_col_bnds(lp, i, GLPKConstants.GLP_FX, lb, ub);
                } else {
                    GLPK.glp_set_col_bnds(lp, i, GLPKConstants.GLP_DB, lb, ub);
                }
            }
        }

        // create objective
        obj = p.getObjective();
        if (obj != null) {
            GLPK.glp_set_obj_name(lp, obj.getKey());
            if (obj.getDirection() == Problem.Direction.MINIMIZE) {
                GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MIN);
            } else {
                GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MAX);
            }
        }

        // create rows
        if (obj != null) {
            GLPK.glp_add_rows(lp, p.getRows().size() - 1);
        } else {
            GLPK.glp_add_rows(lp, p.getRows().size());
        }
        j = 0;
        for (Problem.Row r : p.getRows()) {
            Double lb;
            Double ub;
            if (r == obj) {
                continue;
            }
            r.setRowNumber(++j);
            GLPK.glp_set_row_name(lp, j, r.getKey());
            lb = r.getLowerBound();
            ub = r.getUpperBound();
            if (lb != null && ub == null) {
                GLPK.glp_set_row_bnds(lp, j, GLPKConstants.GLP_LO, lb, 0);
            } else if (lb == null && ub != null) {
                GLPK.glp_set_row_bnds(lp, j, GLPKConstants.GLP_UP, 0, ub);
            } else if (lb != null && ub != null) {
                if (lb.equals(ub)) {
                    GLPK.glp_set_row_bnds(lp, j, GLPKConstants.GLP_FX, lb, ub);
                } else {
                    GLPK.glp_set_row_bnds(lp, j, GLPKConstants.GLP_DB, lb, ub);
                }
            }
        }

        // create matrix
        i = 1;
        for (Entry<Problem.Row, TreeMap<Problem.Column, Double>> e :
                p.getMatrix().
                entrySet()) {
            if (e.getKey() == obj) {
                continue;
            }
            for (Entry<Problem.Column, Double> f : e.getValue().entrySet()) {
                ++i;
            }
        }
        col = GLPK.new_intArray(i);
        row = GLPK.new_intArray(i);
        val = GLPK.new_doubleArray(i);
        i = 0;
        for (Entry<Problem.Row, TreeMap<Problem.Column, Double>> e :
                p.getMatrix().
                entrySet()) {
            Problem.Row r = e.getKey();

            if (r == obj) {
                for (Entry<Problem.Column, Double> c :
                        e.getValue().entrySet()) {
                    GLPK.glp_set_obj_coef(lp, c.getKey().getColumnNumber(),
                            c.getValue());
                }
            } else {
                j = r.getRowNumber();
                for (Entry<Problem.Column, Double> c :
                        e.getValue().entrySet()) {
                    GLPK.intArray_setitem(col, ++i,
                            c.getKey().getColumnNumber());
                    GLPK.intArray_setitem(row, i, j);
                    GLPK.doubleArray_setitem(val, i, c.getValue());
                }
            }
        }
        GLPK.glp_load_matrix(lp, i, row, col, val);

        //  Solve model
        iocp = new glp_iocp();
        GLPK.glp_init_iocp(iocp);
        iocp.setPresolve(GLPKConstants.GLP_ON);
        switch (GLPK.glp_intopt(lp, iocp)) {
            case 0:
                ret = true;
                break;
            default:
                ret = false;
                break;
        }

        obj.setValue(GLPK.glp_mip_obj_val(lp));
        for (Problem.Column c : p.getColumns()) {
            c.setValue(GLPK.glp_mip_col_val(lp, c.getColumnNumber()));
        }

        // free memory
        GLPK.glp_delete_prob(lp);

        return ret;
    }
}