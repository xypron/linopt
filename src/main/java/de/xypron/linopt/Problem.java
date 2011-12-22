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
import java.util.TreeSet;

/**
 * Linear programming problem.
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
     * @param name name
     */
    public Problem(final String name) {
        this();
        this.name = name;
    }

    /**
     * Gets problem name.
     * @return problem name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets problem name.
     * @param name problem name
     * @return problem
     */
    public Problem setName(final String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets columns.
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
     * @return matrix
     */
    public TreeMap<Row, TreeMap<Column, Double>> getMatrix() {
        return matrix;
    }

    /**
     * Gets objective function.
     * @return objective function
     */
    @Deprecated
    public Objective getObjective() {
        return objectiveFunction;
    }

    /**
     * Gets rows (including objective).
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
     * @return objective
     */
    public Objective objective() {
        return objectiveFunction;
    }

    /**
     * Create objective function.
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
     * Create key for column or row.
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
         * @return name
         */
        public String getKey() {
            return key;
        }

        /**
         * Gets column type.
         * @return type
         */
        public ColumnType getType() {
            return type;
        }

        /**
         * Gets dual value.
         * @return dual value
         */
        public double getDual() {
            return dual;
        }

        /**
         * Sets dual value.
         * @param dual dual value
         */
        public void setDual(final double dual) {
            this.dual = dual;
        }

        /**
         * Gets lower bound.
         * Null signifies that the bound is not set.
         * @return lower bound.
         */
        public Double getLowerBound() {
            return lowerBound;
        }

        /**
         * Sets lower bound.
         * Null signifies that the bound is not set.
         * @param lowerBound lower bound
         */
        public void setLowerBound(final Double lowerBound) {
            this.lowerBound = lowerBound;
        }

        /**
         * Gets column number.
         * @return column number
         */
        public int getColumnNumber() {
            return columnNumber;
        }

        /**
         * Sets column number.
         * @param columnNumber column number
         */
        public void setColumnNumber(final int columnNumber) {
            this.columnNumber = columnNumber;
        }

        /**
         * Gets upper bound.
         * Null signifies that the bound is not set.
         * @return upper bound
         */
        public Double getUpperBound() {
            return upperBound;
        }

        /**
         * Sets upper bound.
         * Null signifies that the bound is not set.
         * @param upperBound upper bound
         */
        public void setUpperBound(final Double upperBound) {
            this.upperBound = upperBound;
        }

        /**
         * Gets value.
         * @return value
         */
        public double getValue() {
            return value;
        }

        /**
         * Sets value.
         * @param value value
         */
        public void setValue(final double value) {
            this.value = value;
        }

        /**
         * Sets bounds of column.
         * Null signifies that the bound is not set.
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
         * @param type column type
         * @return column
         */
        public Column type(ColumnType type) {
            this.type = type;
            return this;
        }

        /**
         * Sets coefficient.
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
         * @param value value
         * @param row row
         * @return
         */
        public Column add(final double value, final Row row) {
            matrix.get(row).put(this, value);
            return this;
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

        private Direction direction;

        /**
         * Creates objective.
         * @param key name
         * @param direction optimization direction
         */
        Objective(final String key, final Direction direction) {
            super(key);
            this.direction = direction;
        }

        /**
         * Gets optimization direction.
         * @return optimization direction
         */
        public Direction getDirection() {
            return direction;
        }

        /**
         * Sets optimization direction.
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

        private Row(final String key) {
            this.key = key;
            initialize();
        }

        private void initialize() {
            if (rows.containsKey(key)) {
                throw new RuntimeException("Row " + key + " already exists.");
            }
            rows.put(key, this);
            matrix.put(this, new TreeMap<Column, Double>());
        }

        /**
         * Gets row name.
         * @return name
         */
        public String getKey() {
            return key;
        }

        /**
         * Gets row dual value.
         * @return dual
         */
        public double getDual() {
            return dual;
        }

        /**
         * Sets row dual value.
         * @param dual dual
         */
        public void setDual(final double dual) {
            this.dual = dual;
        }

        /**
         * Gets lower bound.
         * Null signifies that the bound is not set.
         * @return lower bound
         */
        public Double getLowerBound() {
            return lowerBound;
        }

        /**
         * Sets lower bound.
         * Null signifies that the bound is not set.
         * @param lowerBound lower bound
         */
        public void setLowerBound(final Double lowerBound) {
            this.lowerBound = lowerBound;
        }

        /**
         * Gets row number.
         * @return row number
         */
        public int getRowNumber() {
            return rowNumber;
        }

        /**
         * Sets row number.
         * @param rowNumber row number
         */
        public void setRowNumber(final int rowNumber) {
            this.rowNumber = rowNumber;
        }

        /**
         * Gets upper bound.
         * Null signifies that the bound is not set.
         * @return upper bound
         */
        public Double getUpperBound() {
            return upperBound;
        }

        /**
         * Sets upper bound.
         * Null signifies that the bound is not set.
         * @param upperBound upper bound
         */
        public void setUpperBound(final Double upperBound) {
            this.upperBound = upperBound;
        }

        /**
         * Gets value.
         * @return value
         */
        public double getValue() {
            return value;
        }

        /**
         * Sets value.
         * @param value value
         */
        public void setValue(final double value) {
            this.value = value;
        }

        /**
         * Sets coefficient.
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
         * @param value value
         * @param column column
         * @return this row
         */
        public Row add(final double value, final Column column) {
            matrix.get(this).put(column, value);
            return this;
        }

        /**
         * Sets bounds.
         * Null signifies that the bound is not set.
         * @param lowerBound lower bound
         * @param upperBound upper bound
         * @return this row
         */
        public Row bounds(final Double lowerBound, final Double upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
            return this;
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
