package com.google.gwt.html5.client;

import com.google.gwt.core.client.JavaScriptObject;

public class ImageData extends JavaScriptObject {

  protected ImageData() {
  }

  public final native int getWidth() /*-{ return this.width; }-*/;
  public final native int getHeight() /*-{ return this.height; }-*/;
  public final native CanvasPixelArray getData() /*-{ return this.data; }-*/;
}
