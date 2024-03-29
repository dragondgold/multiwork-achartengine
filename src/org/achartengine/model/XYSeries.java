/**
 * Copyright (C) 2009 - 2013 SC 4ViewSoft SRL
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.achartengine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;

import org.achartengine.util.IndexXYMap;
import org.achartengine.util.MathHelper;
import org.achartengine.util.XYEntry;

/**
 * An XY series encapsulates values for XY charts like line, time, area,
 * scatter... charts.
 */
public class XYSeries implements Serializable {
  /** The series title. */
  private String mTitle;
  /** A map to contain values for X and Y axes and index for each bundle */
  private final IndexXYMap<Double, Double> mXY = new IndexXYMap<Double, Double>();
  /** The minimum value for the X axis. */
  private double mMinX = MathHelper.NULL_VALUE;
  /** The maximum value for the X axis. */
  private double mMaxX = -MathHelper.NULL_VALUE;
  /** The minimum value for the Y axis. */
  private double mMinY = MathHelper.NULL_VALUE;
  /** The maximum value for the Y axis. */
  private double mMaxY = -MathHelper.NULL_VALUE;
  /** The scale number for this series. */
  private final int mScaleNumber;
  /** A padding value that will be added when adding values with the same X. */
  private static final double PADDING = 0.000000000001;
  /** Contains the annotations. */
  private List<String> mAnnotations = new ArrayList<String>();
  /** A map contain a (x,y) value for each String annotation. */
  private final IndexXYMap<Double, Double> mStringXY = new IndexXYMap<Double, Double>();
  
  /** Rectangulos */
  private final ArrayList<Double[]> mRectangles = new ArrayList<Double[]>();
  /** Serie de rectángulos */
  private boolean isRectangle = false;

  /**
   * Builds a new XY series.
   * 
   * @param title the series title.
   */
  public XYSeries(String title) {
    this(title, 0);
  }

  /**
   * Mapa con las coordenadas XY
   * @return
   */
  public IndexXYMap<Double, Double> getXYMap (){
    return mXY;
  }
  
  /**
   * Builds a new XY series.
   * 
   * @param title the series title.
   * @param scaleNumber the series scale number
   */
  public XYSeries(String title, int scaleNumber) {
    mTitle = title;
    mScaleNumber = scaleNumber;
    initRange();
  }

  public int getScaleNumber() {
    return mScaleNumber;
  }

  /**
   * Initializes the range for both axes.
   */
  private void initRange() {
    mMinX = MathHelper.NULL_VALUE;
    mMaxX = -MathHelper.NULL_VALUE;
    mMinY = MathHelper.NULL_VALUE;
    mMaxY = -MathHelper.NULL_VALUE;
    int length = getItemCount();
    for (int k = 0; k < length; k++) {
      double x = getX(k);
      double y = getY(k);
      updateRange(x, y);
    }
  }

  /**
   * Updates the range on both axes.
   * 
   * @param x the new x value
   * @param y the new y value
   */
  private void updateRange(double x, double y) {
    mMinX = Math.min(mMinX, x);
    mMaxX = Math.max(mMaxX, x);
    mMinY = Math.min(mMinY, y);
    mMaxY = Math.max(mMaxY, y);
  }

  /**
   * Returns the series title.
   * 
   * @return the series title
   */
  public String getTitle() {
    return mTitle;
  }

  /**
   * Sets the series title.
   * 
   * @param title the series title
   */
  public void setTitle(String title) {
    mTitle = title;
  }

  /**
   * Adds a new value to the series.
   * 
   * @param x the value for the X axis
   * @param y the value for the Y axis
   */
  public synchronized void add(double x, double y) {
    while (mXY.get(x) != null) {
      // add a very small value to x such as data points sharing the same x will
      // still be added
      x += getPadding();
    }
    mXY.put(x, y);
    updateRange(x, y);
  }

  /**
   * Adds a new value to the series at the specified index.
   * 
   * @param index the index to be added the data to
   * @param x the value for the X axis
   * @param y the value for the Y axis
   */
  public synchronized void add(int index, double x, double y) {
    while (mXY.get(x) != null) {
      // add a very small value to x such as data points sharing the same x will
      // still be added
      x += getPadding();
    }
    mXY.put(index, x, y);
    updateRange(x, y);
  }
  
  protected double getPadding() {
    return PADDING;
  }

  /**
   * Removes an existing value from the series.
   * 
   * @param index the index in the series of the value to remove
   */
  public synchronized void remove(int index) {
    XYEntry<Double, Double> removedEntry = mXY.removeByIndex(index);
    double removedX = removedEntry.getKey();
    double removedY = removedEntry.getValue();
    if (removedX == mMinX || removedX == mMaxX || removedY == mMinY || removedY == mMaxY) {
      initRange();
    }
  }

  /**
   * Removes all the existing values from the series including annotations and rectangles
   */
  public synchronized void clear() {
    clearSeriesValues();
    clearAnnotations();
    clearRectangles();
    initRange();
  }
  
  /**
   * Elimina los valores XY de la serie
   */
  public synchronized void clearAnnotations (){
    mStringXY.clear();
    mAnnotations.clear();
  }
  
  /**
   * Elimina los valores XY de la serie
   */
  public synchronized void clearSeriesValues (){
    mXY.clear();
    initRange();
  }
  
  public synchronized void clearRectangles (){
    mRectangles.clear();
  }

  /**
   * Returns the X axis value at the specified index.
   * 
   * @param index the index
   * @return the X value
   */
  public synchronized double getX(int index) {
    return mXY.getXByIndex(index);
  }

  /**
   * Returns the Y axis value at the specified index.
   * 
   * @param index the index
   * @return the Y value
   */
  public synchronized double getY(int index) {
    return mXY.getYByIndex(index);
  }

  /**
   * Add an String at (x,y) coordinates
   * 
   * @param annotation String text
   * @param x
   * @param y
   */
  public void addAnnotation(String annotation, double x, double y) {
    mAnnotations.add(annotation);
    mStringXY.put(x, y);
  }
  
  /**
   * Reemplaza la anotación en el index por la nueva
   * @param index index de la anotación a reemplazar
   * @param annotation anotación nueva
   * @param x nueva coordenada x
   * @param y nueva coordenada y
   */
  public void replaceAnnotation(int index, String annotation, double x, double y){
    mAnnotations.set(index, annotation);
    mStringXY.replace(index, x, y);
  }

  /**
   * Remove an String at index
   * 
   * @param index
   */
  public void removeAnnotation(int index) {
    mAnnotations.remove(index);
    mStringXY.removeByIndex(index);
  }

  /**
   * Get X coordinate of the annotation at index
   * 
   * @param index the index in the annotations list
   * @return the corresponding annotation X value
   */
  public double getAnnotationX(int index) {
    return mStringXY.getXByIndex(index);
  }

  /**
   * Get Y coordinate of the annotation at index
   * 
   * @param index the index in the annotations list
   * @return the corresponding annotation Y value
   */
  public double getAnnotationY(int index) {
    return mStringXY.getYByIndex(index);
  }

  /**
   * Get the annotations count
   * 
   * @return the annotations count
   */
  public int getAnnotationCount() {
    return mAnnotations.size();
  }

  /**
   * Get the String at index
   * 
   * @param index
   * @return String
   */
  public String getAnnotationAt(int index) {
    return mAnnotations.get(index);
  }
  
  public void setIsRectangleSeries(boolean state){
    isRectangle = state;
  }
  
  public boolean isRectangleSeries(){
    return isRectangle;
  }
  
  /**
   * Agrega un rectangulo en la Serie
   * @param left coordenada x de la esquina superior izquierda
   * @param top coordenada y de la esquina superior izquierda
   * @param right coordenada x de la esquina inferior derecha
   * @param bottom coordenada y de la esquina inferior derecha
   */
  public void addRectangle (double left, double top, double right, double bottom){
    mRectangles.add(new Double[] {left,top,right,bottom});
  }
  
  /**
   * Reemplaza un rectangulo en la Serie
   * @param index index del rectangulo a reemplazar
   * @param left coordenada x de la esquina superior izquierda
   * @param top coordenada y de la esquina superior izquierda
   * @param right coordenada x de la esquina inferior derecha
   * @param bottom coordenada y de la esquina inferior derecha
   */
  public void replaceRectangle (int index, double left, double top, double right, double bottom){
    mRectangles.set(index, new Double[] {left,top,right,bottom});
  }
  
  /**
   * Obtiene el rectangulo en el index dado
   * @param index
   * @return Array Double[] siendo las coordenadas en el orden left, top, right, bottom
   */
  public Double[] getRectangle (int index){
    return mRectangles.get(index);
  }
  
  public int getRectangleCount(){
    return mRectangles.size();
  }

  /**
   * Returns submap of x and y values according to the given start and end
   * 
   * @param start start x value
   * @param stop stop x value
   * @param beforeAfterPoints if the points before and after the first and last
   *          visible ones must be displayed
   * @return a submap of x and y values
   */
  public synchronized SortedMap<Double, Double> getRange(double start, double stop,
      boolean beforeAfterPoints) {
    if (beforeAfterPoints) {
      // we need to add one point before the start and one point after the end
      // (if
      // there are any)
      // to ensure that line doesn't end before the end of the screen

      // this would be simply: start = mXY.lowerKey(start) but NavigableMap is
      // available since API 9
      SortedMap<Double, Double> headMap = mXY.headMap(start);
      if (!headMap.isEmpty()) {
        start = headMap.lastKey();
      }

      // this would be simply: end = mXY.higherKey(end) but NavigableMap is
      // available since API 9
      // so we have to do this hack in order to support older versions
      SortedMap<Double, Double> tailMap = mXY.tailMap(stop);
      if (!tailMap.isEmpty()) {
        Iterator<Double> tailIterator = tailMap.keySet().iterator();
        Double next = tailIterator.next();
        if (tailIterator.hasNext()) {
          stop = tailIterator.next();
        } else {
          stop += next;
        }
      }
    }
    return mXY.subMap(start, stop);
  }

  public int getIndexForKey(double key) {
    return mXY.getIndexForKey(key);
  }

  /**
   * Returns the series item count.
   * 
   * @return the series item count
   */
  public synchronized int getItemCount() {
    return mXY.size();
  }

  /**
   * Returns the minimum value on the X axis.
   * 
   * @return the X axis minimum value
   */
  public double getMinX() {
    return mMinX;
  }

  /**
   * Returns the minimum value on the Y axis.
   * 
   * @return the Y axis minimum value
   */
  public double getMinY() {
    return mMinY;
  }

  /**
   * Returns the maximum value on the X axis.
   * 
   * @return the X axis maximum value
   */
  public double getMaxX() {
    return mMaxX;
  }

  /**
   * Returns the maximum value on the Y axis.
   * 
   * @return the Y axis maximum value
   */
  public double getMaxY() {
    return mMaxY;
  }
}
