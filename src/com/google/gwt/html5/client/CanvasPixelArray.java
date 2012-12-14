package com.google.gwt.html5.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Array-like object holding the actual image data for an ImageData object. For each pixel, 
 * this object contains a red, green, blue and alpha value between 0 and 255 (in this order).
 * Note that we use ints here to represent the data to avoid complexities stemming from
 * bytes being signed in Java.
 */
public class CanvasPixelArray extends JavaScriptObject {
  
  protected CanvasPixelArray() {
  }
  
  /**
   * Returns the data value at position i.
   */
  public final native int get(int i) /*-{
    return this[i];
  }-*/;
  
  /**
   * Sets the data value at position i to the given value. The value will be clamped to 
   * the range 0..255.
   */
  public final native void set(int i, int value) /*-{
    this[i] = value;
  }-*/;
}
