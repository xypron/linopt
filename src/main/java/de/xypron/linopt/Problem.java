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
 *
 * @author Heinrich Schuchardt
 */
public class Problem {

    private String name;
    private TreeMap<String, Column> columns =
            new TreeMap<String, Column>();
    private Objective objectiveFunction = null;
    private TreeMap<Row, TreeMap<Column, Double>> matrix =
            new TreeMap<Row, TreeMap<Column, Double>>();
    private TreeMap<String, Row> rows =
            new TreeMap<String, Row>();

    /**
     * Optimization direction
     */
    public enum Direction {

        MINIMIZE,
        MAXIMIZE
    }

    /**
     * Column type
     */
    public enum ColumnType {

        FLOAT,
        INTEGER,
        BINARY
    }

    /**
     * Get problem name
     * @return problem name
     */
    public String getName() {
        return name;
    }

    /**
     * Set problem name
     * @param name problem name
     * @return problem
     */
    public Problem setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get columns
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
     * Get matrix
     * @return matrix
     */
    public TreeMap<Row, TreeMap<Column, Double>> getMatrix() {
        return matrix;
    }

    /**
     * Get objective function
     * @return objective function
     */
    public Objective getObjective() {
        return objectiveFunction;
    }

    /**
     * Get rows (including objective)
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
     * Get column identified by name and indices
     * @param name column name
     * @param index indices
     * @return
     */
    public Column column(String name, Object... index) {
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
     * Create objective function
     * @param name name
     * @param direction optimization direction
     * @return
     */
    public Objective objective(String name, Direction direction) {
        if (objectiveFunction == null) {
            objectiveFunction = new Objective(name, direction);
        } else {
            throw new RuntimeException("Objective already defined.");
        }
        return objectiveFunction;
    }

    /**
     * Get row identified by name and indices
     * @param name
     * @param index
     * @return
     */
    public Row row(String name, Object... index) {
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
     * Create key for column or row
     * @param name
     * @param index
     * @return
     */
    private String key(String name, Object... index) {
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
     * Column
     */
    public class Column implements Comparable<Column> {

        private String key;
        private ColumnType type = null;
        private Double lowerBound = null;
        private Double upperBound = null;
        private int columnNumber;
        private double value;
        private double dual;

        private Column(String key) {
            this.key = key;
            initialize();
        }

        private void initialize() {
            if (columns.containsKey(key)) {
                throw new RuntimeException("Column " + key + " already exists.");
            }
            columns.put(key, this);
        }

        /**
         * Get column name
         * @return name
         */
        public String getKey() {
            return key;
        }

        /**
         * Get column type
         * @return type
         */
        public ColumnType getType() {
            return type;
        }

        /**
         * Get dual value
         * @return dual
         */
        public double getDual() {
            return dual;
        }

        /**
         * Set dual value
         * @param dual
         */
        public void setDual(double dual) {
            this.dual = dual;
        }

        /**
         * Get lower bound
         * @return lower bound
         */
        public Double getLowerBound() {
            return lowerBound;
        }

        /**
         * Set lower bound
         * @param lowerBound lower bound
         */
        public void setLowerBound(Double lowerBound) {
            this.lowerBound = lowerBound;
        }

        /**
         * Get column number
         * @return column number
         */
        public int getColumnNumber() {
            return columnNumber;
        }

        /**
         * Set column number
         * @param columnNumber column number
         */
        public void setColumnNumber(int columnNumber) {
            this.columnNumber = columnNumber;
        }

        /**
         * Get upper bound
         * @return upper bound
         */
        public Double getUpperBound() {
            return upperBound;
        }

        /**
         * Set upper bound
         * @param upperBound upper bound
         */
        public void setUpperBound(Double upperBound) {
            this.upperBound = upperBound;
        }

        /**
         * Get value
         * @return value
         */
        public double getValue() {
            return value;
        }

        /**
         * Set value
         * @param value value
         */
        public void setValue(double value) {
            this.value = value;
        }

        public Column bounds(Double lowerBound, Double upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
            return this;
        }

        /**
         * Set column type
         * @param type column type
         * @return column
         */
        public Column type(ColumnType type) {
            this.type = type;
            return this;
        }

        /**
         * Set coefficient
         * @param value coefficient
         * @param rowName row name
         * @param index row indices
         * @return
         */
        public Column add(double value, String rowName, Object... index) {
            return add(value, row(rowName, index));
        }

        /**
         * Set coefficient
         * @param value value
         * @param row row
         * @return
         */
        public Column add(double value, Row row) {
            matrix.get(row).put(this, value);
            return this;
        }

        @Override
        public String toString() {
            return key;
        }

        @Override
        public int compareTo(Column o) {
            return key.compareTo(o.key);
        }
    }

    public class Objective
            extends Row {

        private Direction direction;

        /**
         * Create objective
         * @param key name
         * @param direction optimization direction
         */
        Objective(String key, Direction direction) {
            super(key);
            this.direction = direction;
        }

        /**
         * Get optimization direction
         * @return optimization direction
         */
        public Direction getDirection() {
            return direction;
        }

        /**
         * Set optimization direction
         * @param direction optimization direction
         */
        public void setDirection(Direction direction) {
            this.direction = direction;
        }

    }

    /**
     * Row
     */
    public class Row implements Comparable<Row> {

        private String key;
        private Double lowerBound = null;
        private Double upperBound = null;
        private int rowNumber;
        private double value;
        private double dual;

        private Row(String key) {
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
         * Get row name
         * @return name
         */
        public String getKey() {
            return key;
        }

        /**
         * Get row dual value
         * @return dual
         */
        public double getDual() {
            return dual;
        }

        /**
         * Set row dual value
         * @param dual dual
         */
        public void setDual(double dual) {
            this.dual = dual;
        }

        /**
         * Get lower bound
         * @return lower bound
         */
        public Double getLowerBound() {
            return lowerBound;
        }

        /**
         * Set lower bound
         * @param lowerBound lower bound
         */
        public void setLowerBound(Double lowerBound) {
            this.lowerBound = lowerBound;
        }

        /**
         * Get row number
         * @return row number
         */
        public int getRowNumber() {
            return rowNumber;
        }

        /**
         * Set row number
         * @param rowNumber row number
         */
        public void setRowNumber(int rowNumber) {
            this.rowNumber = rowNumber;
        }

        /**
         * Get upper bound
         * @return upper bound
         */
        public Double getUpperBound() {
            return upperBound;
        }

        /**
         * Set upper bound
         * @param upperBound upper bound
         */
        public void setUpperBound(Double upperBound) {
            this.upperBound = upperBound;
        }

        /**
         * Get value
         * @return value
         */
        public double getValue() {
            return value;
        }

        /**
         * Set value
         * @param value value
         */
        public void setValue(double value) {
            this.value = value;
        }

        /**
         * Set coefficient
         * @param value coefficient
         * @param columnName column name
         * @param index column indices
         * @return this row
         */
        public Row add(double value, String columnName, Object... index) {
            return add(value, column(columnName, index));
        }

        /**
         * Set coefficient
         * @param value value
         * @param column column
         * @return this row
         */
        public Row add(double value, Column column) {
            matrix.get(this).put(column, value);
            return this;
        }

        /**
         * Set bounds
         * @param lowerBound lower bound
         * @param upperBound upper bound
         * @return this row
         */
        public Row bounds(Double lowerBound, Double upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
            return this;
        }

        @Override
        public String toString() {
            return key;
        }

        @Override
        public int compareTo(Row o) {
            return key.compareTo(o.key);
        }
    }
}
