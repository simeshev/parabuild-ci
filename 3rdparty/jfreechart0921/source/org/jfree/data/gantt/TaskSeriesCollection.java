/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 *
 * -------------------------
 * TaskSeriesCollection.java
 * -------------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: TaskSeriesCollection.java,v 1.1 2004/08/31 15:25:36 mungady Exp $
 *
 * Changes
 * -------
 * 06-Jun-2002 : Version 1 (DG);
 * 07-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 24-Oct-2002 : Amendments for changes in CategoryDataset interface and CategoryToolTipGenerator
 *               interface (DG);
 * 10-Jan-2003 : Renamed GanttSeriesCollection --> TaskSeriesCollection (DG);
 * 04-Sep-2003 : Fixed bug 800324 (DG);
 * 16-Sep-2003 : Implemented GanttCategoryDataset (DG);
 *
 */

package org.jfree.data.gantt;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.jfree.data.general.AbstractSeriesDataset;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.time.TimePeriod;
import org.jfree.util.ObjectUtils;
import org.jfree.util.PublicCloneable;

/**
 * A collection of {@link TaskSeries} objects.
 * <P>
 * This class provides one implementation of the {@link GanttCategoryDataset} interface.
 */
public class TaskSeriesCollection extends AbstractSeriesDataset
                                  implements GanttCategoryDataset,
                                             Cloneable, PublicCloneable, Serializable {

    /** Storage for aggregate task keys (the task description is used as the key). */
    private List keys;

    /** Storage for the series. */
    private List data;

    /**
     * Default constructor.
     */
    public TaskSeriesCollection() {
        this.keys = new java.util.ArrayList();
        this.data = new java.util.ArrayList();
    }

    /**
     * Returns the number of series in the collection.
     *
     * @return The series count.
     */
    public int getSeriesCount() {
        return getRowCount();
    }

    /**
     * Returns the name of a series.
     *
     * @param series  the series index (zero-based).
     *
     * @return The name of a series.
     */
    public String getSeriesName(int series) {
        TaskSeries ts = (TaskSeries) this.data.get(series);
        return ts.getName();
    }

    /**
     * Returns the number of rows (series) in the collection.
     *
     * @return The series count.
     */
    public int getRowCount() {
        return this.data.size();
    }

    /**
     * Returns the row keys.  In this case, each series is a key.
     *
     * @return The row keys.
     */
    public List getRowKeys() {
        return this.data;
    }

    /**
     * Returns the number of column in the dataset.
     *
     * @return The column count.
     */
    public int getColumnCount() {
        return this.keys.size();
    }

    /**
     * Returns a list of the column keys in the dataset.
     *
     * @return The category list.
     */
    public List getColumnKeys() {
        return this.keys;
    }

    /**
     * Returns a column key.
     *
     * @param index  the column index.
     *
     * @return The column key.
     */
    public Comparable getColumnKey(int index) {
        return (Comparable) this.keys.get(index);
    }

    /**
     * Returns the column index for a column key.
     *
     * @param columnKey  the columnKey.
     *
     * @return The column index.
     */
    public int getColumnIndex(Comparable columnKey) {
        return this.keys.indexOf(columnKey);
    }

    /**
     * Returns the row index for the given row key.
     *
     * @param rowKey  the row key.
     *
     * @return The index.
     */
    public int getRowIndex(Comparable rowKey) {
        int result = -1;
        int count = this.data.size();
        for (int i = 0; i < count; i++) {
            TaskSeries s = (TaskSeries) this.data.get(i);
            if (s.getName().equals(rowKey)) {
                result = i;
                break;
            }
        }
        return result;
    }

    /**
     * Returns the key for a row.
     *
     * @param index  the row index (zero-based).
     *
     * @return The key.
     */
    public Comparable getRowKey(int index) {
        TaskSeries series = (TaskSeries) this.data.get(index);
        return series.getName();
    }

    /**
     * Adds a series to the dataset and sends a {@link org.jfree.data.general.DatasetChangeEvent}
     * to all registered listeners.
     *
     * @param series  the series (<code>null</code> not permitted).
     */
    public void add(TaskSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Null 'series' argument.");
        }
        this.data.add(series);
        series.addChangeListener(this);

        // look for any keys that we don't already know about...
        Iterator iterator = series.getTasks().iterator();
        while (iterator.hasNext()) {
            Task task = (Task) iterator.next();
            String key = task.getDescription();
            int index = this.keys.indexOf(key);
            if (index < 0) {
                this.keys.add(key);
            }
        }
        fireDatasetChanged();
    }

    /**
     * Removes a series from the collection and sends a {@link org.jfree.data.general.DatasetChangeEvent}
     * to all registered listeners.
     *
     * @param series  the series.
     */
    public void remove(TaskSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Null 'series' argument.");
        }
        if (this.data.contains(series)) {
            series.removeChangeListener(this);
            this.data.remove(series);
            fireDatasetChanged();
        }
    }

    /**
     * Removes a series from the collection and sends a {@link org.jfree.data.general.DatasetChangeEvent}
     * to all registered listeners.
     *
     * @param series  the series (zero based index).
     */
    public void remove(int series) {
        if ((series < 0) || (series > getSeriesCount())) {
            throw new IllegalArgumentException(
                "TaskSeriesCollection.remove(): index outside valid range.");
        }

        // fetch the series, remove the change listener, then remove the series.
        TaskSeries ts = (TaskSeries) this.data.get(series);
        ts.removeChangeListener(this);
        this.data.remove(series);
        fireDatasetChanged();

    }

    /**
     * Removes all the series from the collection and sends a {@link org.jfree.data.general.DatasetChangeEvent}
     * to all registered listeners.
     */
    public void removeAll() {

        // deregister the collection as a change listener to each series in the collection.
        Iterator iterator = this.data.iterator();
        while (iterator.hasNext()) {
            final TaskSeries series = (TaskSeries) iterator.next();
            series.removeChangeListener(this);
        }

        // remove all the series from the collection and notify listeners.
        this.data.clear();
        fireDatasetChanged();

    }

    /**
     * Returns the value for an item.
     *
     * @param rowKey  the row key.
     * @param columnKey  the column key.
     *
     * @return The item value.
     */
    public Number getValue(Comparable rowKey, Comparable columnKey) {
        return getStartValue(rowKey, columnKey);
    }

    /**
     * Returns the value for a task.
     *
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     *
     * @return The start value.
     */
    public Number getValue(int row, int column) {
        return getStartValue(row, column);
    }

    /**
     * Returns the start value for a task.
     *
     * @param rowKey  the series.
     * @param columnKey  the category.
     *
     * @return The start value.
     */
    public Number getStartValue(Comparable rowKey, Comparable columnKey) {
        Number result = null;
        int row = getRowIndex(rowKey);
        TaskSeries series = (TaskSeries) this.data.get(row);
        Task task = series.get(columnKey.toString());
        if (task != null) {
            TimePeriod duration = task.getDuration();
            result = new Long(duration.getStart().getTime());
        }
        return result;
    }

    /**
     * Returns the start value for a task.
     *
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     *
     * @return The start value.
     */
    public Number getStartValue(int row, int column) {
        Comparable rowKey = getRowKey(row);
        Comparable columnKey = getColumnKey(column);
        return getStartValue(rowKey, columnKey);
    }

    /**
     * Returns the end value for a task.
     *
     * @param rowKey  the series.
     * @param columnKey  the category.
     *
     * @return The end value.
     */
    public Number getEndValue(Comparable rowKey, Comparable columnKey) {
        Number result = null;
        int row = getRowIndex(rowKey);
        TaskSeries series = (TaskSeries) this.data.get(row);
        Task task = series.get(columnKey.toString());
        if (task != null) {
            TimePeriod duration = task.getDuration();
            result = new Long(duration.getEnd().getTime());
        }
        return result;
    }

    /**
     * Returns the end value for a task.
     *
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     *
     * @return The end value.
     */
    public Number getEndValue(int row, int column) {
        Comparable rowKey = getRowKey(row);
        Comparable columnKey = getColumnKey(column);
        return getEndValue(rowKey, columnKey);
    }

    /**
     * Returns the percent complete for a given item.
     *
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     *
     * @return The percent complete (possibly <code>null</code>).
     */
    public Number getPercentComplete(int row, int column) {
        Comparable rowKey = getRowKey(row);
        Comparable columnKey = getColumnKey(column);
        return getPercentComplete(rowKey, columnKey);
    }

    /**
     * Returns the percent complete for a given item.
     *
     * @param rowKey  the row key.
     * @param columnKey  the column key.
     *
     * @return The percent complete.
     */
    public Number getPercentComplete(Comparable rowKey, Comparable columnKey) {
        Number result = null;
        int row = getRowIndex(rowKey);
        TaskSeries series = (TaskSeries) this.data.get(row);
        Task task = series.get(columnKey.toString());
        if (task != null) {
            result = task.getPercentComplete();
        }
        return result;
    }

    /**
     * Returns the number of sub-intervals for a given item.
     *
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     *
     * @return The sub-interval count.
     */
    public int getSubIntervalCount(int row, int column) {
        int result = 0;
        TaskSeries series = (TaskSeries) this.data.get(row);
        int tasks = series.getItemCount();
        if (column < tasks) {
            Task task = series.get(column);
            result = task.getSubtaskCount();
        }
        return result;
    }

    /**
     * Returns the number of sub-intervals for a given item.
     *
     * @param rowKey  the row key.
     * @param columnKey  the column key.
     *
     * @return The sub-interval count.
     */
    public int getSubIntervalCount(Comparable rowKey, Comparable columnKey) {
        int row = getRowIndex(rowKey);
        int column = getColumnIndex(columnKey);
        return getSubIntervalCount(row, column);
    }

    /**
     * Returns the start value of a sub-interval for a given item.
     *
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     * @param subinterval  the sub-interval index (zero-based).
     *
     * @return The start value (possibly <code>null</code>).
     */
    public Number getStartValue(int row, int column, int subinterval) {
        Comparable rowKey = getRowKey(row);
        Comparable columnKey = getColumnKey(column);
        return getStartValue(rowKey, columnKey, subinterval);
    }

    /**
     * Returns the start value of a sub-interval for a given item.
     *
     * @param rowKey  the row key.
     * @param columnKey  the column key.
     * @param subinterval  the subinterval.
     *
     * @return The start value (possibly <code>null</code>).
     */
    public Number getStartValue(Comparable rowKey, Comparable columnKey, int subinterval) {
        Number result = null;
        int row = getRowIndex(rowKey);
        TaskSeries series = (TaskSeries) this.data.get(row);
        Task task = series.get(columnKey.toString());
        if (task != null) {
            Task sub = task.getSubtask(subinterval);
            if (sub != null) {
                TimePeriod duration = sub.getDuration();
                result = new Long(duration.getStart().getTime());
            }
        }
        return result;
    }

    /**
     * Returns the end value of a sub-interval for a given item.
     *
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     * @param subinterval  the subinterval.
     *
     * @return The end value (possibly <code>null</code>).
     */
    public Number getEndValue(int row, int column, int subinterval) {
        Comparable rowKey = getRowKey(row);
        Comparable columnKey = getColumnKey(column);
        return getEndValue(rowKey, columnKey, subinterval);
    }

    /**
     * Returns the end value of a sub-interval for a given item.
     *
     * @param rowKey  the row key.
     * @param columnKey  the column key.
     * @param subinterval  the subinterval.
     *
     * @return The end value (possibly <code>null</code>).
     */
    public Number getEndValue(Comparable rowKey, Comparable columnKey, int subinterval) {
        Number result = null;
        int row = getRowIndex(rowKey);
        TaskSeries series = (TaskSeries) this.data.get(row);
        Task task = series.get(columnKey.toString());
        if (task != null) {
            Task sub = task.getSubtask(subinterval);
            if (sub != null) {
                TimePeriod duration = sub.getDuration();
                result = new Long(duration.getEnd().getTime());
            }
        }
        return result;
    }

    /**
     * Returns the percentage complete value of a sub-interval for a given item.
     *
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     * @param subinterval  the sub-interval.
     *
     * @return The percent complete value (possibly <code>null</code>).
     */
    public Number getPercentComplete(int row, int column, int subinterval) {
        Number result = null;
        TaskSeries series = (TaskSeries) this.data.get(row);
        int tasks = series.getItemCount();
        if (column < tasks) {
            Task task = series.get(column);
            Task subtask = task.getSubtask(subinterval);
            result = subtask.getPercentComplete();
        }
        return result;
    }

    /**
     * Returns the percentage complete value of a sub-interval for a given item.
     *
     * @param rowKey  the row key.
     * @param columnKey  the column key.
     * @param subinterval  the sub-interval.
     *
     * @return The precent complete value (possibly <code>null</code>).
     */
    public Number getPercentComplete(Comparable rowKey, Comparable columnKey, int subinterval) {
        int row = getRowIndex(rowKey);
        int column = getColumnIndex(columnKey);
        return getPercentComplete(row, column, subinterval);
    }

    /**
     * Called when a series belonging to the dataset changes.
     *
     * @param event  information about the change.
     */
    public void seriesChanged(SeriesChangeEvent event) {
        refreshKeys();
        fireDatasetChanged();
    }

    /**
     * Refreshes the keys.
     */
    private void refreshKeys() {

        this.keys.clear();
        for (int i = 0; i < getSeriesCount(); i++) {
            TaskSeries series = (TaskSeries) this.data.get(i);
            // look for any keys that we don't already know about...
            Iterator iterator = series.getTasks().iterator();
            while (iterator.hasNext()) {
                Task task = (Task) iterator.next();
                String key = task.getDescription();
                int index = this.keys.indexOf(key);
                if (index < 0) {
                    this.keys.add(key);
                }
            }
        }

    }
    
    /**
     * Tests this instance for equality with an arbitrary object.
     * 
     * @param obj  the object (<code>null</code> permitted).
     * 
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TaskSeriesCollection)) {
            return false;
        }
        TaskSeriesCollection that = (TaskSeriesCollection) obj;
        if (!ObjectUtils.equal(this.data, that.data)) {
            return false;
        }
        return true;
    }

}