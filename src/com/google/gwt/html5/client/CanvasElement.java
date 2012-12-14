package com.google.gwt.html5.client;

import com.google.gwt.dom.client.Element;
import com.googlecode.gwtgl.binding.WebGLContextAttributes;
import com.google.gwt.webgl.client.WebGLRenderingContext;

public class CanvasElement extends Element {

  protected CanvasElement() {
  }

  public final native CanvasRenderingContext2D getContext2D() /*-{
    return this.getContext("2d");
  }-*/;

  public final WebGLRenderingContext getContextWebGL() {
    return getContextWebGL(WebGLContextAttributes.create());
  }

  /**
   * Returns a WebGL context for the given canvas element. Returns null if no 3d
   * context is available.
   */
  public final native WebGLRenderingContext getContextWebGL(WebGLContextAttributes attributes) /*-{
    var names = ["experimental-webgl", "webgl", "moz-webgl", "webkit-webgl", "webkit-3d"];
    for (var i = 0; i < names.length; i++) {
      try {
        var ctx = this.getContext(names[i], attributes);
        if (ctx != null) {
          // Hook for the semi-standard WebGLDebugUtils script.
          if ($wnd.WebGLDebugUtils) {
            return $wnd.WebGLDebugUtils.makeDebugContext(ctx);
          }
          return ctx;
        }
      } catch(e) {
      }
    }
    return null;
  }-*/;

  public final native int getHeight() /*-{
    return this.height;
  }-*/;

  public final native int getWidth() /*-{
    return this.width;
  }-*/;

  public final native boolean isSupported() /*-{
    return typeof this.getContext != "undefined";
  }-*/;

  public final native void setHeight(int height) /*-{
    this.height = height;
  }-*/;

  public final native void setWidth(int width) /*-{
    this.width = width;
  }-*/;

  public final native String toDataURL() /*-{
    return this.toDataURL();
  }-*/;

  public final native String toDataURL(String mimeType) /*-{
    return this.toDataURL(mimeType);
  }-*/;
}
