package com.google.gwt.html5.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.ImageElement;

/*
 * TODO(jgw): Because of the screwy overloaded return types for strokeStyle and
 * fillStyle, we need to do something to ensure that the types are correct in
 * Java. The way it's currently written, you could call, e.g.,
 * getStrokeStyleColor() and get a CanvasGradient instead, which is a Bad Thing.
 */
public class CanvasRenderingContext2D extends JavaScriptObject {

  public static final String REPETITION_REPEAT = "repeat";
  public static final String REPETITION_REPEAT_X = "repeat-x";
  public static final String REPETITION_REPEAT_Y = "repeat-y";
  public static final String REPETITION_NO_REPEAT = "no-repeat";

  public static final String LINECAP_BUTT = "butt";
  public static final String LINECAP_ROUND = "round";
  public static final String LINECAP_SQUARE = "square";

  public static final String LINEJOIN_ROUND = "round";
  public static final String LINEJOIN_BEVEL = "bevel";
  public static final String LINEJOIN_MITER = "miter";

  /**
   * A atop B. Display the source image wherever both images are opaque. Display
   * the destination image wherever the destination image is opaque but the
   * source image is transparent. Display transparency elsewhere.
   */
  public static final String COMPOSITE_SOURCE_ATOP = "source-atop";

  /**
   * A in B. Display the source image wherever both the source image and
   * destination image are opaque. Display transparency elsewhere.
   */
  public static final String COMPOSITE_SOURCE_IN = "source-in";

  /**
   * A out B. Display the source image wherever the source image is opaque and
   * the destination image is transparent. Display transparency elsewhere.
   */
  public static final String COMPOSITE_SOURCE_OUT = "source-out";

  /**
   * A over B. Display the source image wherever the source image is opaque.
   * Display the destination image elsewhere.
   */
  public static final String COMPOSITE_SOURCE_OVER = "source-over";

  /**
   * B atop A. Same as source-atop but using the destination image instead of
   * the source image and vice versa.
   */
  public static final String COMPOSITE_DESTINATION_ATOP = "destination-atop";

  /**
   * B in A. Same as source-in but using the destination image instead of the
   * source image and vice versa.
   */
  public static final String COMPOSITE_DESTINATION_IN = "destination-in";

  /**
   * B out A. Same as source-out but using the destination image instead of the
   * source image and vice versa.
   */
  public static final String COMPOSITE_DESTINATION_OUT = "destination-out";

  /**
   * B over A. Same as source-over but using the destination image instead of
   * the source image and vice versa.
   */
  public static final String COMPOSITE_DESINATION_OVER = "destination-over";

  /**
   * A plus B. Display the sum of the source image and destination image, with
   * color values approaching 1 as a limit.
   */
  public static final String COMPOSITE_LIGHTER = "lighter";

  /**
   * A (B is ignored). Display the source image instead of the destination
   * image.
   */
  public static final String COMPOSITE_COPY = "copy";

  /**
   * A xor B. Exclusive OR of the source image and destination image.
   */
  public static final String COMPOSITE_XOR = "xor";

  protected CanvasRenderingContext2D() {
  }

  public final native void arc(float x, float y, float radius,
      float startAngle, float endAngle, boolean anticlockwise) /*-{
    this.arc(x, y, radius, startAngle, endAngle, anticlockwise);
  }-*/;

  public final native void arcTo(float x1, float y1, float x2, float y2,
      float radius) /*-{
    this.arcTo(x1, y1, x2, y2, radius);
  }-*/;

  public final native void beginPath() /*-{
    this.beginPath();
  }-*/;

  public final native void bezierCurveTo(float cp1x, float cp1y, float cp2x,
      float cp2y, float x, float y) /*-{
    this.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y);
  }-*/;

  public final native void clearRect(float x, float y, float w, float h) /*-{
    this.clearRect(x, y, w, h);
  }-*/;

  public final native void clip() /*-{
    this.clip();
  }-*/;

  public final native void closePath() /*-{
    this.closePath();
  }-*/;

  public final native ImageData createImageData(float sw, float sh) /*-{
    return this.createImageData(sw, sh);
  }-*/;

  public final native CanvasGradient createLinearGradient(float x0, float y0,
      float x1, float y1) /*-{
    return this.createLinearGradient(x0, y0, x1, y1);
  }-*/;

  public final native CanvasPattern createPattern(CanvasElement image,
      String repetition) /*-{
    return this.createPattern(image, repetition);
  }-*/;

  public final native CanvasPattern createPattern(ImageElement image,
      String repetition) /*-{
    return this.createPattern(image, repetition);
  }-*/;

  public final native CanvasGradient createRadialGradient(float x0, float y0,
      float r0, float x1, float y1, float r1) /*-{
    return this.createRadialGradient(x0, y0, r0, x1, y1, r1);
  }-*/;

  public final native void drawImage(CanvasElement image, float dx, float dy) /*-{
    this.drawImage(image, dx, dy);
  }-*/;

  public final native void drawImage(CanvasElement image, float dx, float dy,
      float dw, float dh) /*-{
    this.drawImage(image, dx, dy, dw, dh);
  }-*/;

  public final native void drawImage(CanvasElement image, float sx, float sy,
      float sw, float sh, float dx, float dy, float dw, float dh) /*-{
    this.drawImage(image, sx, sy, sw, sh, dx, dy, dw, dh);
  }-*/;

  public final native void drawImage(ImageElement image, float dx, float dy) /*-{
    this.drawImage(image, dx, dy);
  }-*/;

  public final native void drawImage(ImageElement image, float dx, float dy,
      float dw, float dh) /*-{
    this.drawImage(image, dx, dy, dw, dh);
  }-*/;

  public final native void drawImage(ImageElement image, float sx, float sy,
      float sw, float sh, float dx, float dy, float dw, float dh) /*-{
    this.drawImage(image, sx, sy, sw, sh, dx, dy, dw, dh);
  }-*/;

  public final native void fill() /*-{
    this.fill();
  }-*/;

  public final native void fillRect(float x, float y, float w, float h) /*-{
    this.fillRect(x, y, w, h);
  }-*/;

  /**
   * Draw text.
   */
  public final native void fillText(String text, float x, float y) /*-{
    this.fillText(text, x, y);
  }-*/;

  /**
   * Draw text squeezed into the given max width.
   */
  public final native void fillText(String text, float x, float y,
      float maxWidth) /*-{
    this.fillText(text, x, y, maxWidth);
  }-*/;

  public final native CanvasElement getCanvas() /*-{
    return this.canvas;
  }-*/;

  public final native String getFillStyleColor() /*-{
    return this.fillStyle;
  }-*/;

  public final native CanvasGradient getFillStyleGradient() /*-{
    return this.fillStyle;
  }-*/;

  public final native CanvasPattern getFillStylePattern() /*-{
    return this.fillStyle;
  }-*/;

  /**
   * Gets this context's font.
   */
  public final native String getFont() /*-{
    return this.font;
  }-*/;

  public final native float getGlobalAlpha() /*-{
    return this.globalAlpha;
  }-*/;

  public final native String getGlobalCompositeOperation() /*-{
    return this.globalCompositeOperation;
  }-*/;

  public final native ImageData getImageData(float sx, float sy, float sw,
      float sh) /*-{
    return this.createImageData(sx, sy, sw, sh);
  }-*/;

  public final native String getLineCap() /*-{
    return this.lineCap;
  }-*/;

  public final native String getLineJoin() /*-{
    return this.lineJoin;
  }-*/;

  public final native float getLineWidth() /*-{
    return this.lineWidth;
  }-*/;

  public final native float getMiterLimit() /*-{
    return this.miterLimit;
  }-*/;

  public final native float getShadowBlur() /*-{
    return this.shadowBlur;
  }-*/;

  public final native String getShadowColor() /*-{
    return this.shadowColor;
  }-*/;

  public final native float getShadowOffsetX() /*-{
    return this.shadowOffsetX;
  }-*/;

  public final native float getShadowOffsetY() /*-{
    return this.shadowOffsetY;
  }-*/;

  public final native String getStrokeStyleColor() /*-{
    return this.strokeStyle;
  }-*/;

  public final native CanvasGradient getStrokeStyleGradient() /*-{
    return this.strokeStyle;
  }-*/;

  public final native CanvasPattern getStrokeStylePattern() /*-{
    return this.strokeStyle;
  }-*/;

  /**
   * Gets the current text align.
   */
  public final native String getTextAlign() /*-{
    return this.textAlign;
  }-*/;

  /**
   * Gets the current text baseline.
   */
  public final native String getTextBaseline() /*-{
    return this.textBaseline;
  }-*/;

  public final native boolean isPointInPath(float x, float y) /*-{
    return this.isPointInPath(x, y);
  }-*/;

  public final native void lineTo(float x, float y) /*-{
    this.lineTo(x, y);
  }-*/;

  /**
   * Returns the metrics for the given text.
   */
  public final native TextMetrics measureText(String text) /*-{
    return this.measureText(text);
  }-*/;

  public final native void moveTo(float x, float y) /*-{
    this.moveTo(x, y);
  }-*/;

  public final native void putImageData(ImageData imagedata, float dx, float dy) /*-{
    this.putImageData(imagedata, dx, dy);
  }-*/;

  public final native void putImageData(ImageData imagedata, float dx,
      float dy, float dirtyX, float dirtyY, float dirtyWidth, float dirtyHeight) /*-{
    this.putImageData(imagedata, dx, dy, dirtyX, dirtyY, dirtyWidth, dirtyHeight);
  }-*/;

  public final native void quadraticCurveTo(float cpx, float cpy, float x,
      float y) /*-{
    this.quadraticCurveTo(cpx, cpy, x, y);
  }-*/;

  public final native void rect(float x, float y, float w, float h) /*-{
    this.rect(x, y, w, h);
  }-*/;

  public final native void restore() /*-{
    this.restore();
  }-*/;

  public final native void rotate(float angle) /*-{
    this.rotate(angle);
  }-*/;

  public final native void save() /*-{
    this.save();
  }-*/;

  public final native void scale(float x, float y) /*-{
    this.scale(x, y);
  }-*/;

  public final native void setFillStyleColor(String fillStyle) /*-{
    this.fillStyle = fillStyle;
  }-*/;

  public final native void setFillStyleGradient(CanvasGradient fillStyle) /*-{
    this.fillStyle = fillStyle;
  }-*/;

  public final native void setFillStylePattern(CanvasPattern fillStyle) /*-{
    this.fillStyle = fillStyle;
  }-*/;

  /**
   * Sets the font.
   */
  public final native void setFont(String f) /*-{
    this.font = f;
  }-*/;

  public final native void setGlobalAlpha(float alpha) /*-{
    this.globalAlpha = alpha;
  }-*/;

  public final native void setGlobalCompositeOperation(
      String globalCompositeOperation) /*-{
    this.globalCompositeOperation = globalCompositeOperation;
  }-*/;

  public final native void setLineCap(String lineCap) /*-{
    this.lineCap = lineCap;
  }-*/;

  public final native void setLineJoin(String lineJoin) /*-{
    this.lineJoin = lineJoin;
  }-*/;

  public final native void setLineWidth(float lineWidth) /*-{
    this.lineWidth = lineWidth;
  }-*/;

  public final native void setMiterLimit(float miterLimit) /*-{
    this.miterLimit = miterLimit;
  }-*/;

  public final native void setShadowBlur(float shadowBlur) /*-{
    this.shadowBlur = shadowBlur;
  }-*/;

  public final native void setShadowColor(String shadowColor) /*-{
    this.shadowColor = shadowColor;
  }-*/;

  public final native void setShadowOffsetX(float shadowOffsetX) /*-{
    this.shadowOffsetX = shadowOffsetX;
  }-*/;

  public final native void setShadowOffsetY(float shadowOffsetY) /*-{
    this.shadowOffsetY = shadowOffsetY;
  }-*/;

  public final native void setStrokeStyleColor(String strokeStyle) /*-{
    this.strokeStyle = strokeStyle;
  }-*/;

  public final native void setStrokeStyleGradient(CanvasGradient strokeStyle) /*-{
    this.strokeStyle = strokeStyle;
  }-*/;

  public final native void setStrokeStylePattern(CanvasPattern strokeStyle) /*-{
    this.strokeStyle = strokeStyle;
  }-*/;

  /**
   * Sets the text alignment.
   */
  public final native void setTextAlign(String align) /*-{
    this.textAlign = align
  }-*/;

  /**
   * Sets the text baseline.
   */
  public final native void setTextBaseline(String baseline) /*-{
    this.textBaseline = baseline
  }-*/;

  public final native void setTransform(float m11, float m12, float m21,
      float m22, float dx, float dy) /*-{
    this.setTransform(m11, m12, m21, m22, dx, dy);
  }-*/;

  public final native void stroke() /*-{
    this.stroke();
  }-*/;

  public final native void strokeRect(float x, float y, float w, float h) /*-{
    this.strokeRect(x, y, w, h);
  }-*/;

  /**
   * Draws the text outline.
   */
  public final native void strokeText(String text, float x, float y) /*-{
    this.strokeText(text, x, y);
  }-*/;

  /**
   * Draws the text outline, squeezing the text into the given max width by
   * compressing the font.
   */
  public final native void strokeText(String text, float x, float y,
      float maxWith) /*-{
    this.strokeText(text, x, y, maxWidth);
  }-*/;

  public final native void transform(float m11, float m12, float m21,
      float m22, float dx, float dy) /*-{
    this.transform(m11, m12, m21, m22, dx, dy);
  }-*/;

  public final native void translate(float x, float y) /*-{
    this.translate(x, y);
  }-*/;
}
