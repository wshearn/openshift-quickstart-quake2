package com.google.gwt.html5.client;

import com.google.gwt.core.client.JavaScriptObject;

public class CanvasGradient extends JavaScriptObject {

  protected CanvasGradient() {
  }

  public final native void addColorStop(float offset, String color) /*-{ this.addColorStop(offset, color); }-*/;
}
