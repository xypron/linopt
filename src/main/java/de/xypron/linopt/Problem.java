/*
 *  Copyright (C) 2010-2012, Heinrich Schuchardt
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
import java.util.TreeSet;

/**
 * Linear programming problem. <p>Columns are created like this:
 * <pre>
 * Problem p = new Problem().
 *     setName("Cutting Stock");
 * // x(i,j) : x pieces of product j are cut from stock i
 * for (int i = 0; i < stock.length; i++) {
 *     for (int j = 0; j < product.length; j++) {
 *     p.column("x", i, j).
 *         type(Problem.ColumnType.INTEGER).
 *         bounds(0., null);
 *     }
 * }
 * </pre> <p>Rows can be created and populated like this:
 * <pre>
 * // demand(j) = sum( x(i,j) )
 * for (int j = 0; j < product.length; j++) {
 *     p.row("demand", j).bounds(demand[j], demand[j]);
 *     for (int i = 0; i < stock.length; i++) {
 *         p.row("demand", j).
 *             add(1., "x", i, j);
 *     }
 * }
 * </pre> <p>The keys for rows and columns are constructed from the name and the
 * indices by concatenation using parentheses and commas, e.g.
 * <pre>
 * name(index1,index2,index3)
 * </pre> Hence it is advisable not to use parentheses and commas in names and
 * indices to avoid duplicate keys.
 *
 * @author Heinrich Schuchardt
 */
public class Problem {

    /**
     * Problem name.
     */
    private String name = null;
    /**
     * Columns.
     */
    private TreeMap<String, Column> columns =
            new TreeMap<String, Column>();
    /**
     * Objective function.
     */
    private Objective objectiveFunction = null;
    /**
     * Matrix.
     */
    private TreeMap<Row, TreeMap<Column, Double>> matrix =
            new TreeMap<Row, TreeMap<Column, Double>>();
    /**
     * Rows.
     */
    private TreeMap<String, Row> rows =
            new TreeMap<String, Row>();

    /**
     * Optimization direction.
     */
    public enum Direction {

        /**
         * Minimize.
         */
        MINIMIZE,
        /**
         * Maximize.
         */
        MAXIMIZE
    }

    /**
     * Column type.
     */
    public enum ColumnType {

        /**
         * Float.
         */
        FLOAT,
        /**
         * Integer.
         */
        INTEGER,
        /**
         * Binary.
         */
        BINARY
    }

    /**
     * Creates problem.
     */
    public Problem() {
    }

    /**
     * Creates problem.
     *
     * @param name name
     */
    public Problem(final String name) {
        this();
        this.name = name;
    }

    /**
     * Gets problem name.
     *
     * @return problem name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets problem name.
     *
     * @param name problem name
     * @return problem
     */
    public Problem setName(final String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets columns.
     *
     * @return columns
     */
    public TreeSet<Column> getColumns() {
        TreeSet<Column> ret = new TreeSet<Column>();
        for (Entry<String, Column> e : columns.entrySet()) {
            ret.add(e.getValue());
        }
        return ret;
    }

    /**
     * Gets matrix.
     *
     * @return matrix
     */
    public TreeMap<Row, TreeMap<Column, Double>> getMatrix() {
        return matrix;
    }

    /**
     * Gets objective function. This method is deprecated. Use method
     * objective() instead.
     *
     * @return objective function
     */
    @Deprecated
    public Objective getObjective() {
        return objectiveFunction;
    }

    /**
     * Gets rows (including objective).
     *
     * @return rows
     */
    public TreeSet<Row> getRows() {
        TreeSet<Row> ret = new TreeSet<Row>();
        for (Entry<String, Row> e : rows.entrySet()) {
            ret.add(e.getValue());
        }
        return ret;
    }

    /**
     * Gets column identified by name and indices.
     *
     * @param name column name
     * @param index indices
     * @return column
     */
    public Column column(final String name, final Object... index) {
        Column ret;
        String key = key(name, index);
        if (columns.containsKey(key)) {
            ret = columns.get(key);
        } else {
            ret = new Column(key);
        }
        return ret;
    }

    /**
     * Gets objective function.
     *
     * @return objective
     */
    public Objective objective() {
        return objectiveFunction;
    }

    /**
     * Creates objective function.
     *
     * @param name name
     * @param direction optimization direction
     * @return objective
     */
    public Objective objective(final String name, final Direction direction) {
        if (objectiveFunction == null) {
            objectiveFunction = new Objective(name, direction);
        } else {
            throw new RuntimeException("Objective already defined.");
        }
        return objectiveFunction;
    }

    /**
     * Gets row identified by name and indices.
     *
     * @param name name
     * @param index index
     * @return row
     */
    public Row row(final String name, final Object... index) {
        Row ret;
        String key = key(name, index);
        if (rows.containsKey(key)) {
            ret = rows.get(key);
        } else {
            ret = new Row(key);
        }
        return ret;
    }

    /**
     * Creates key for column or row.
     *
     * @param name name
     * @param index index
     * @return key
     */
    private String key(final String name, final Object... index) {
        String sep = "(";
        String key = name;
        if (index.length > 0) {
            for (Object i : index) {
                key += sep;
                key += i;
                sep = ",";
            }
            key += ")";
        }
        return key;
    }

    /**
     * Returns problem as string.
     *
     * @return problem as string
     */
    public String problemToString() {
        String ret = "";
        ret += "problem " + name + ";\n\n";
        for (Entry<String, Column> entry : columns.entrySet()) {
            ret += entry.getValue().definitionToString() + "\n";
        }
        ret += "\n" + objectiveFunction.constraintToString() + "\n\n";

        for (Entry<String, Row> entry : rows.entrySet()) {
            Row row = entry.getValue();
            if (!row.equals(objectiveFunction)) {
                ret += row.constraintToString() + "\n";
            }
        }

        ret += "\nend;\n";

        return ret;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Column.
     */
    public class Column implements Comparable<Column> {

        /**
         * Key for column.
         */
        private String key;
        /**
         * Column type.
         */
        private ColumnType type = null;
        /**
         * Lower bound.
         */
        private Double lowerBound = null;
        /**
         * Upper bound.
         */
        private Double upperBound = null;
        /**
         * Column number.
         */
        private int columnNumber;
        /**
         * Column value.
         */
        private double value;
        /**
         * Column dual value.
         */
        private double dual;

        /**
         * Creates a column.
         * @param key identifier for column
         */
        private Column(final String key) {
            this.key = key;
            initialize();
        }

        /**
         * Initializes problem.
         */
        private void initialize() {
            if (columns.containsKey(key)) {
                throw new RuntimeException(
                        "Column " + key + " already exists.");
            }
            columns.put(key, this);
        }

        /**
         * Gets column name.
         *
         * @return name
         */
        public String getKey() {
            return key;
        }

        /**
         * Gets column type.
         *
         * @return type
         */
        public ColumnType getType() {
            return type;
        }

        /**
         * Gets dual value.
         *
         * @return dual value
         */
        public double getDual() {
            return dual;
        }

        /**
         * Sets dual value.
         *
         * @param dual dual value
         */
        public void setDual(final double dual) {
            this.dual = dual;
        }

        /**
         * Gets lower bound. Null signifies that the bound is not set.
         *
         * @return lower bound.
         */
        public Double getLowerBound() {
            return lowerBound;
        }

        /**
         * Sets lower bound. Null signifies that the bound is not set.
         *
         * @param lowerBound lower bound
         */
        public void setLowerBound(final Double lowerBound) {
            this.lowerBound = lowerBound;
        }

        /**
         * Gets column number.
         *
         * @return column number
         */
        public int getColumnNumber() {
            return columnNumber;
        }

        /**
         * Sets column number.
         *
         * @param columnNumber column number
         */
        public void setColumnNumber(final int columnNumber) {
            this.columnNumber = columnNumber;
        }

        /**
         * Gets upper bound. Null signifies that the bound is not set.
         *
         * @return upper bound
         */
        public Double getUpperBound() {
            return upperBound;
        }

        /**
         * Sets upper bound. Null signifies that the bound is not set.
         *
         * @param upperBound upper bound
         */
        public void setUpperBound(final Double upperBound) {
            this.upperBound = upperBound;
        }

        /**
         * Gets value.
         *
         * @return value
         */
        public double getValue() {
            return value;
        }

        /**
         * Sets value.
         *
         * @param value value
         */
        public void setValue(final double value) {
            this.value = value;
        }

        /**
         * Sets bounds of column. Null signifies that the bound is not set.
         *
         * @param lowerBound lower bound
         * @param upperBound upper bound
         * @return this column
         */
        public Column bounds(final Double lowerBound, final Double upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
            return this;
        }

        /**
         * Sets column type.
         *
         * @param type column type
         * @return column
         */
        public Column type(final ColumnType type) {
            this.type = type;
            return this;
        }

        /**
         * Sets coefficient.
         *
         * @param value coefficient
         * @param rowName row name
         * @param index row indices
         * @return column
         */
        public Column add(final double value, final String rowName,
                final Object... index) {
            return add(value, row(rowName, index));
        }

        /**
         * Sets coefficient.
         *
         * @param value value
         * @param row row
         * @return column
         */
        public Column add(final double value, final Row row) {
            matrix.get(row).put(this, value);
            return this;
        }

        /**
         * Returns definition as string.
         *
         * @return definition as string.
         */
        public String definitionToString() {
            String ret = "var " + key;

            // column type
            if (type.equals(ColumnType.BINARY)) {
                ret += ", binary";
            } else if (type.equals(ColumnType.INTEGER)) {
                ret += ", integer";
            }

            // bounds
            if (lowerBound != null) {
                ret += ", >= " + lowerBound;
            }
            if (upperBound != null) {
                ret += ", <= " + upperBound;
            }

            ret += ";";

            return ret;
        }

        @Override
        public String toString() {
            return key;
        }

        @Override
        public int compareTo(final Column o) {
            return key.compareTo(o.key);
        }
    }

    /**
     * Objective.
     */
    public class Objective
            extends Row {

        /**
         * Optimization direction.
         */
        private Direction direction;

        /**
         * Creates objective.
         *
         * @param key name
         * @param direction optimization direction
         */
        Objective(final String key, final Direction direction) {
            super(key);
            this.direction = direction;
        }

        /**
         * Gets optimization direction.
         *
         * @return optimization direction
         */
        public Direction getDirection() {
            return direction;
        }

        /**
         * Sets optimization direction.
         *
         * @param direction optimization direction
         */
        public void setDirection(final Direction direction) {
            this.direction = direction;
        }
    }

    /**
     * Row.
     */
    public class Row implements Comparable<Row> {

        /**
         * Key.
         */
        private String key;
        /**
         * Lower bound.
         */
        private Double lowerBound = null;
        /**
         * Upper bound.
         */
        private Double upperBound = null;
        /**
         * Row number.
         */
        private int rowNumber;
        /**
         * Value.
         */
        private double value;
        /**
         * Dual value.
         */
        private double dual;

        /**
         * Creates row.
         * @param key identifier of row
         */
        private Row(final String key) {
            this.key = key;
            initialize();
        }

        /**
         * Initializes row.
         */
        private void initialize() {
            if (rows.containsKey(key)) {
                throw new RuntimeException("Row " + key + " already exists.");
            }
            rows.put(key, this);
            matrix.put(this, new TreeMap<Column, Double>());
        }

        /**
         * Gets row name.
         *
         * @return name
         */
        public String getKey() {
            return key;
        }

        /**
         * Gets row dual value.
         *
         * @return dual
         */
        public double getDual() {
            return dual;
        }

        /**
         * Sets row dual value.
         *
         * @param dual dual
         */
        public void setDual(final double dual) {
            this.dual = dual;
        }

        /**
         * Gets lower bound. Null signifies that the bound is not set.
         *
         * @return lower bound
         */
        public Double getLowerBound() {
            return lowerBound;
        }

        /**
         * Sets lower bound. Null signifies that the bound is not set.
         *
         * @param lowerBound lower bound
         */
        public void setLowerBound(final Double lowerBound) {
            this.lowerBound = lowerBound;
        }

        /**
         * Gets row number.
         *
         * @return row number
         */
        public int getRowNumber() {
            return rowNumber;
        }

        /**
         * Sets row number.
         *
         * @param rowNumber row number
         */
        public void setRowNumber(final int rowNumber) {
            this.rowNumber = rowNumber;
        }

        /**
         * Gets upper bound. Null signifies that the bound is not set.
         *
         * @return upper bound
         */
        public Double getUpperBound() {
            return upperBound;
        }

        /**
         * Sets upper bound. Null signifies that the bound is not set.
         *
         * @param upperBound upper bound
         */
        public void setUpperBound(final Double upperBound) {
            this.upperBound = upperBound;
        }

        /**
         * Gets value.
         *
         * @return value
         */
        public double getValue() {
            return value;
        }

        /**
         * Sets value.
         *
         * @param value value
         */
        public void setValue(final double value) {
            this.value = value;
        }

        /**
         * Sets coefficient.
         *
         * @param value coefficient
         * @param columnName column name
         * @param index column indices
         * @return this row
         */
        public Row add(final double value, final String columnName,
                final Object... index) {
            return add(value, column(columnName, index));
        }

        /**
         * Sets coefficient.
         *
         * @param value value
         * @param column column
         * @return this row
         */
        public Row add(final double value, final Column column) {
            matrix.get(this).put(column, value);
            return this;
        }

        /**
         * Sets bounds. Null signifies that the bound is not set.
         *
         * @param lowerBound lower bound
         * @param upperBound upper bound
         * @return this row
         */
        public Row bounds(final Double lowerBound, final Double upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
            return this;
        }

        /**
         * Outputs constraint as string.
         *
         * @return constraint as string.
         */
        public String constraintToString() {
            String ret = "";
            String plus = " ";
            final String minus = " - ";

            if (this == objectiveFunction) {
                if (objectiveFunction.direction.equals(Direction.MINIMIZE)) {
                    ret += "minimize ";
                } else if (objectiveFunction.direction.
                        equals(Direction.MAXIMIZE)) {
                    ret += "minimize ";
                }
                ret += key + " :";
            } else {
                ret += "s.t. " + key + " :";
            }

            if (lowerBound != null) {
                ret += " " + lowerBound + " <=";
            }

            for (Entry<Column, Double> entry : matrix.get(this).entrySet()) {
                Column col = entry.getKey();
                Double val = entry.getValue();
                if (val >= 0) {
                    ret += plus + val;
                    plus = " + ";
                } else {
                    ret += minus + -val;
                }
                ret += " " + col.getKey();
            }

            if (upperBound != null) {
                ret += " <= " + upperBound;
            }

            ret += ";";

            return ret;
        }

        @Override
        public String toString() {
            return key;
        }

        @Override
        public int compareTo(final Row o) {
            return key.compareTo(o.key);
        }
    }
}
