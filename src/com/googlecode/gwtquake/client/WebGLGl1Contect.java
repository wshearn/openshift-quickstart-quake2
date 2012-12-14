/*
 * Copyright (C) 2010 Copyright 2010 Google Inc.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package com.googlecode.gwtquake.client;

import static com.google.gwt.webgl.client.WebGLRenderingContext.ARRAY_BUFFER;
import static com.google.gwt.webgl.client.WebGLRenderingContext.BYTE;
import static com.google.gwt.webgl.client.WebGLRenderingContext.COMPILE_STATUS;
import static com.google.gwt.webgl.client.WebGLRenderingContext.DYNAMIC_DRAW;
import static com.google.gwt.webgl.client.WebGLRenderingContext.ELEMENT_ARRAY_BUFFER;
import static com.google.gwt.webgl.client.WebGLRenderingContext.FLOAT;
import static com.google.gwt.webgl.client.WebGLRenderingContext.FRAGMENT_SHADER;
import static com.google.gwt.webgl.client.WebGLRenderingContext.INT;
import static com.google.gwt.webgl.client.WebGLRenderingContext.LINK_STATUS;
import static com.google.gwt.webgl.client.WebGLRenderingContext.NO_ERROR;
import static com.google.gwt.webgl.client.WebGLRenderingContext.SHORT;
import static com.google.gwt.webgl.client.WebGLRenderingContext.STATIC_DRAW;
import static com.google.gwt.webgl.client.WebGLRenderingContext.STREAM_DRAW;
import static com.google.gwt.webgl.client.WebGLRenderingContext.UNSIGNED_BYTE;
import static com.google.gwt.webgl.client.WebGLRenderingContext.UNSIGNED_SHORT;
import static com.google.gwt.webgl.client.WebGLRenderingContext.VERTEX_SHADER;


import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.html5.client.CanvasElement;
import com.googlecode.gwtgl.array.ArrayBufferView;
import com.googlecode.gwtgl.array.Float32Array;
import com.googlecode.gwtgl.array.Int16Array;
import com.googlecode.gwtgl.array.Int32Array;
import com.googlecode.gwtgl.array.Int8Array;
import com.googlecode.gwtgl.array.Uint16Array;
import com.googlecode.gwtgl.array.Uint8Array;
import com.google.gwt.user.client.Window;
import com.googlecode.gwtgl.binding.WebGLBuffer;
import com.googlecode.gwtgl.binding.WebGLProgram;
import com.google.gwt.webgl.client.WebGLRenderingContext;
import com.googlecode.gwtgl.binding.WebGLShader;
import com.googlecode.gwtgl.binding.WebGLTexture;
import com.googlecode.gwtgl.binding.WebGLUniformLocation;
import com.googlecode.gwtquake.shared.render.DisplayMode;
import com.googlecode.gwtquake.shared.render.Gl1Context;

/**
 * Partial mapping of GL1.x to WebGL.
 * 
 * @author Stefan Haustein
 */
@SuppressWarnings("unchecked")
public class WebGLGl1Contect extends Gl1Context {

  static final int SMALL_BUF_COUNT = 4;

  WebGLUniformLocation uMvpMatrix;
  WebGLUniformLocation uSampler0;
  WebGLUniformLocation uSampler1;
  WebGLUniformLocation uTexEnv0;
  WebGLUniformLocation uTexEnv1;
  WebGLUniformLocation uEnableTexture0;
  WebGLUniformLocation uEnableTexture1;

  JsArray<WebGLBuffer> staticBuffers = (JsArray<WebGLBuffer>) JavaScriptObject.createArray();

  class BufferData {
    ArrayBufferView toBind;
    WebGLBuffer buffer;
    int byteStride;
    int size;
    int type;
    int byteSize;
    boolean normalize;
  }

  private BufferData[] bufferData = new BufferData[SMALL_BUF_COUNT];

  public final WebGLRenderingContext gl;

  int logCount = 0;

  public void log(String msg) {
    if (logCount >= 1000) {
      return;
    }
    System.out.println((logCount++) + ": " + msg);
  };

  FloatBuffer colorBuffer;
  private CanvasElement canvas;

  JsArray<WebGLTexture> textures = (JsArray<WebGLTexture>) JsArray.createArray();
  JsArrayInteger textureFormats = (JsArrayInteger) JsArray.createArray();

  private int clientActiveTexture = 0;
  private int activeTexture = 0;
  private int[] boundTextureId = new int[2];
  private int[] texEnvMode = new int[2];
  private JsArrayInteger textureFormat = (JsArrayInteger) JavaScriptObject.createArray();
  private WebGLBuffer elementBuffer;

  public WebGLGl1Contect(CanvasElement canvas) {
    this.canvas = canvas;
    gl = canvas.getContextWebGL();

    if (gl == null) {
      throw new UnsupportedOperationException("WebGL N/A");
    }

    initShader();
    checkError("initShader");

    elementBuffer = gl.createBuffer();
    checkError("createBuffer f. elements");

    for (int i = 0; i < bufferData.length; i++) {
      BufferData bd = new BufferData();
      bd.buffer = gl.createBuffer();
      checkError("createBuffer" + i);
      bufferData[i] = bd;
    }
  }

  private WebGLShader loadShader(int shaderType, String shaderSource) {
    // Create the shader object
    WebGLShader shader = gl.createShader(shaderType);
    if (shader == null) {
      throw new RuntimeException();
    }
    // Load the shader source
    gl.shaderSource(shader, shaderSource);

    // Compile the shader
    gl.compileShader(shader);

    // Check the compile status
    boolean compiled = gl.getShaderParameterb(shader, COMPILE_STATUS);
    if (!compiled) {
      // Something went wrong during compilation; get the error
      throw new RuntimeException("Shader compile error: "
          + gl.getShaderInfoLog(shader));
    }
    return shader;
  }

  public static native String getUserAgent() /*-{
    return navigator.userAgent.toLowerCase();
  }-*/;

  private void initShader() {
    String vertexShaderSource = "attribute vec4 a_position;\n"
        + "attribute vec4 a_color;\n" + "attribute vec2 a_texCoord0; \n"
        + "attribute vec2 a_texCoord1; \n" + "uniform mat4 u_mvpMatrix; \n"
        + "varying vec4 v_color; \n" + "varying vec2 v_texCoord0; \n"
        + "varying vec2 v_texCoord1; \n" + "void main() {\n"
        + "  gl_Position = u_mvpMatrix * a_position;\n"
        + "  v_color = a_color;        \n" + "  v_texCoord0 = a_texCoord0;  \n"
        + "  v_texCoord1 = a_texCoord1;  \n" + "}\n";

    String fragmentShaderSource = "#ifdef GL_ES\n"
        + "precision mediump float;\n" + "#endif\n"
        + "uniform sampler2D s_texture0;  \n"
        + "uniform sampler2D s_texture1;  \n" + "uniform int s_texEnv0;  \n"
        + "uniform int s_texEnv1;  \n" + "uniform int u_enable_texture_0; \n"
        + "uniform int u_enable_texture_1; \n" + "varying vec4 v_color; \n"
        + "varying vec2 v_texCoord0;      \n" + "varying vec2 v_texCoord1;"
        + "vec4 finalColor;      \n" + "void main() {                 \n"
        + "finalColor = v_color;" + "  if (u_enable_texture_0 == 1) { \n"
        + "    vec4 texel = texture2D(s_texture0, v_texCoord0); \n"
        + "    if(s_texEnv0 == 1) { "
        + "      finalColor = finalColor * texel;"
        + "    } else if (s_texEnv0 == 2) {"
        + "      finalColor = vec4(texel.r, texel.g, texel.b, finalColor.a);"
        + "    } else {" + "      finalColor = texel;" + "    }" + "}"
        + " if (u_enable_texture_1 == 1) { \n"
        + "      vec4 texel = texture2D(s_texture1, v_texCoord1); \n"
        + "    if(s_texEnv1 == 1) { "
        + "      finalColor = finalColor * texel;"
        + "    } else if (s_texEnv1 == 2) {"
        + "      finalColor = vec4(texel.r, texel.g, texel.b, finalColor.a);"
        + "    } else {" + "      finalColor = texel;" + "    }" + "  } \n"
        +
        // simple alpha check
        "if (finalColor.a == 0.0) {\n" +
        "  discard;\n" +
        "}\n" +
        "float gamma = 1.5;\n" +
        "float igamma = 1.0 / gamma;\n" +
        "gl_FragColor = vec4(pow(finalColor.r, igamma), pow(finalColor.g, igamma), pow(finalColor.b, igamma), finalColor.a);" +
        "}\n";

    // create our shaders
    WebGLShader vertexShader = loadShader(VERTEX_SHADER, vertexShaderSource);
    WebGLShader fragmentShader = loadShader(FRAGMENT_SHADER,
        fragmentShaderSource);

    if (vertexShader == null || fragmentShader == null) {
      log("Shader error");
      throw new RuntimeException("shader error");
    }

    // Create the program object
    WebGLProgram programObject = gl.createProgram();
    if (programObject == null || gl.getError() != NO_ERROR) {
      log("Program errror");
      throw new RuntimeException("program error");
    }

    // Attach our two shaders to the program

    gl.attachShader(programObject, vertexShader);
    gl.attachShader(programObject, fragmentShader);

    // Bind "vPosition" to attribute 0
    gl.bindAttribLocation(programObject, Gl1Context.ARRAY_POSITION, "a_position");
    gl.bindAttribLocation(programObject, Gl1Context.ARRAY_COLOR, "a_color");
    gl.bindAttribLocation(programObject, Gl1Context.ARRAY_TEXCOORD_0,
        "a_texCoord0");
    gl.bindAttribLocation(programObject, Gl1Context.ARRAY_TEXCOORD_1,
        "a_texCoord1");

    // Link the program
    gl.linkProgram(programObject);

    // TODO(haustein) get position, color from the linker, too
    uMvpMatrix = gl.getUniformLocation(programObject, "u_mvpMatrix");
    uSampler0 = gl.getUniformLocation(programObject, "s_texture0");
    uSampler1 = gl.getUniformLocation(programObject, "s_texture1");
    uTexEnv0 = gl.getUniformLocation(programObject, "s_texEnv0");
    uTexEnv1 = gl.getUniformLocation(programObject, "s_texEnv1");

    uEnableTexture0 = gl.getUniformLocation(programObject, "u_enable_texture_0");
    uEnableTexture1 = gl.getUniformLocation(programObject, "u_enable_texture_1");

    // // Check the link status
    boolean linked = gl.getProgramParameterb(programObject, LINK_STATUS);
    if (!linked) {
      throw new RuntimeException("linker Error: "
          + gl.getProgramInfoLog(programObject));
    }

    gl.useProgram(programObject);

    gl.uniform1i(uSampler0, 0);
    gl.uniform1i(uSampler1, 1);
    gl.activeTexture(GL_TEXTURE0);
  }

  public String webGLFloatArrayToString(Float32Array fa) {
    StringBuilder sb = new StringBuilder();
    sb.append("len: " + fa.getLength());
    sb.append("data: ");
    for (int i = 0; i < Math.min(fa.getLength(), 10); i++) {
      sb.append(fa.get(i) + ",");
    }
    return sb.toString();
  }

  public String webGLIntArrayToString(Int32Array fa) {
    StringBuilder sb = new StringBuilder();
    sb.append("len: " + fa.getLength());
    sb.append("data: ");
    for (int i = 0; i < Math.min(fa.getLength(), 10); i++) {
      sb.append(fa.get(i) + ",");
    }
    return sb.toString();
  }

  public String webGLUnsignedShortArrayToString(Uint16Array fa) {
    StringBuilder sb = new StringBuilder();
    sb.append("len: " + fa.getLength());
    sb.append("data: ");
    for (int i = 0; i < Math.min(fa.getLength(), 10); i++) {
      sb.append(fa.get(i) + ",");
    }
    return sb.toString();
  }

  @Override
  public void glActiveTexture(int texture) {
    gl.activeTexture(texture);
    activeTexture = texture - GL_TEXTURE0;
    checkError("glActiveTexture");
  }

  @Override
  public void glAlphaFunc(int i, float j) {
    // TODO: Remove this. Alpha text/func are unsupported in ES.
  }

  @Override
  public void glClientActiveTexture(int texture) {
    clientActiveTexture = texture - GL_TEXTURE0;
  }

  @Override
  public void glColorPointer(int size, int stride, FloatBuffer colorArrayBuf) {
    glColorPointer(size, FLOAT, stride, colorArrayBuf);
  }

  @Override
  public void glColorPointer(int size, boolean unsigned, int stride,
      ByteBuffer colorAsByteBuffer) {
    glColorPointer(size, unsigned ? UNSIGNED_BYTE : BYTE, stride,
        colorAsByteBuffer);
  }

  private final void glColorPointer(int size, int type, int stride, Buffer buf) {
    glVertexAttribPointer(Gl1Context.ARRAY_COLOR, size, type, true, stride, buf);
    checkError("glColorPointer");
  }

  @Override
  public void glDeleteTextures(IntBuffer texnumBuffer) {
    for (int i = 0; i < texnumBuffer.remaining(); i++) {
      int tid = texnumBuffer.get(texnumBuffer.position() + i);
      gl.deleteTexture(textures.get(tid));
      textures.set(tid, null);
      checkError("glDeleteTexture");
    }
  }

  @Override
  public void glDepthFunc(int func) {
    gl.depthFunc(func);
    checkError("glDepthFunc");
  }

  @Override
  public void glDepthMask(boolean b) {
    gl.depthMask(b);
    checkError("glDepthMask");
  }

  @Override
  public void glDepthRange(float gldepthmin, float gldepthmax) {
    gl.depthRange(gldepthmin, gldepthmax);
    checkError("glDepthRange");
  }

  @Override
  public void glDrawBuffer(int buf) {
    // specify which color buffers are to be drawn into
  }

  @Override
  public void glDrawElements(int mode, ShortBuffer srcIndexBuf) {
    prepareDraw();

    gl.bindBuffer(ELEMENT_ARRAY_BUFFER, elementBuffer);
    checkError("bindBuffer(el)");
    gl.bufferData(ELEMENT_ARRAY_BUFFER, getTypedArray(srcIndexBuf,
        UNSIGNED_SHORT), DYNAMIC_DRAW);
    checkError("bufferData(el)");

    int count = srcIndexBuf.remaining();
    gl.drawElements(mode, count, UNSIGNED_SHORT, 0);
    checkError("drawElements");
  }

  @Override
  public void glFinish() {
    gl.finish();
  }

  @Override
  public String glGetString(int id) {
    // TODO: Where is getParameter()?
    // String s = gl.getParameter(id);
    //return s == null ? "" : s;
    return "glGetString not implemented";
  }

  @Override
  public void glPixelStorei(int i, int j) {
    gl.pixelStorei(i, j);
  }

  @Override
  public void glPointParameterf(int id, float value) {
    // TODO Auto-generated method stub
  }

  @Override
  public void glPointSize(float value) {
    // TODO Auto-generated method stub
  }

  @Override
  public void glPolygonMode(int i, int j) {
    // TODO Auto-generated method stub
  }

  @Override
  public void glReadPixels(int x, int y, int width, int height, int glBgr,
      int glUnsignedByte, ByteBuffer image) {
    // TODO Auto-generated method stub
  }

  @Override
  public void glTexCoordPointer(int size, int byteStride, FloatBuffer buf) {
    glVertexAttribPointer(Gl1Context.ARRAY_TEXCOORD_0 + clientActiveTexture,
        size, GL_FLOAT, false, byteStride, buf);
    checkError("texCoordPointer");
  }

  @Override
  public void glTexEnvi(int target, int pid, int value) {
    texEnvMode[activeTexture] = value;
  }

  @Override
  public void glTexImage2D(int target, int level, int internalformat,
      int width, int height, int border, int format, int type, ByteBuffer pixels) {

    textureFormat.set(boundTextureId[activeTexture], internalformat);
    ArrayBufferView array = getTypedArray(pixels, type);
    gl.texImage2D(target, level, internalformat, width, height, border,
        format, type, array);
    checkError("glTexImage2D");
  }

  @Override
  public void glTexImage2D(int target, int level, int internalformat,
      int width, int height, int border, int format, int type, IntBuffer pixels) {

    textureFormat.set(boundTextureId[activeTexture], internalformat);
    ArrayBufferView array = getTypedArray(pixels, type);
    gl.texImage2D(target, level, internalformat, width, height, border,
        format, type, array);
    checkError("glTexImage2D");
  }

  @Override
  public void glTexParameteri(int glTexture2d, int glTextureMinFilter,
      int glFilterMin) {
    gl.texParameteri(glTexture2d, glTextureMinFilter, glFilterMin);
    checkError("glTexParameteri");
  }

  @Override
  public void glTexSubImage2D(int target, int level, int xoffset, int yoffset,
      int width, int height, int format, int type, ByteBuffer pixels) {
    ArrayBufferView array = getTypedArray(pixels, type);
    gl.texSubImage2D(target, level, xoffset, yoffset, width, height, format,
        type, array);
    checkError("glTexSubImage2D");
  }

  @Override
  public void glTexSubImage2D(int target, int level, int xoffset, int yoffset,
      int width, int height, int format, int type, IntBuffer pixels) {
    ArrayBufferView array = getTypedArray(pixels, type);
    gl.texSubImage2D(target, level, xoffset, yoffset, width, height, format,
        type, array);
    checkError("glTexSubImage2D");
  }

  @Override
  public void glVertexPointer(int size, int byteStride, FloatBuffer buf) {
    glVertexAttribPointer(Gl1Context.ARRAY_POSITION, size, GL_FLOAT, false,
        byteStride, buf);
    checkError("glVertexPointer");

  }

  @Override
  public void shutdow() {
    // TODO Auto-generated method stub
  }

  @Override
  public void swapBuffers() {
    // TODO Auto-generated method stub
  }

  @Override
  public final void glBindTexture(int target, int textureId) {
    WebGLTexture texture = textures.get(textureId);
    if (texture == null) {
      texture = gl.createTexture();
      textures.set(textureId, texture);
    }

    // log ("binding texture " + texture + " id " + textureId +
    // " for activeTexture: " + (activeTexture-GL_TEXTURE0));
    gl.bindTexture(target, texture);
    checkError("glBindTexture");

    boundTextureId[activeTexture] = textureId;

    // glColor3f((float)Math.random(), (float)Math.random(),
    // (float)Math.random());
  }

  @Override
  public final void glBlendFunc(int a, int b) {
    gl.blendFunc(a, b);
    checkError("glBlendFunc");
  }

  @Override
  public final void glClear(int mask) {
    gl.clear(mask);
    checkError("glClear");
  }

  @Override
  public final void glColor4f(float red, float green, float blue, float alpha) {
    gl.vertexAttrib4f(Gl1Context.ARRAY_COLOR, red, green, blue, alpha);
    checkError("glColor4f");
  }

  public void glTexImage2d(int target, int level, int internalformat,
      int format, int type, ImageElement image) {
    // log("setting texImage2d; image: " + image.getSrc());
    gl.texImage2D(target, level, internalformat, format, type, image);
    checkError("texImage2D");
  }

  public void glTexImage2d(int target, int level, int internalformat,
      int format, int type, CanvasElement image) {
    // log("setting texImage2d; image: " + image.getSrc());
    gl.texImage2D(target, level, internalformat, format, type, image);
    checkError("texImage2D");
  }

  @Override
  public final void glEnable(int i) {
    // In ES, you don't enable/disable TEXTURE_2D. We use it this call to
    // enable one of the two active textures supported by the shader.
    if (i == GL_TEXTURE_2D) {
      switch (activeTexture) {
        case 0:
          gl.uniform1i(uEnableTexture0, 1);
          break;
        case 1:
          gl.uniform1i(uEnableTexture1, 1);
          break;
        default:
          throw new RuntimeException();
      }
      return;
    }

    gl.enable(i);
    checkError("glEnable");
  }

  @Override
  public final int glGetError() {
    return gl.getError();
  }

  @Override
  public final void glClearColor(float f, float g, float h, float i) {
    gl.clearColor(f, g, h, i);
    checkError("glClearColor");
  }

  @Override
  public void glDrawArrays(int mode, int first, int count) {
    prepareDraw();
    // log("drawArrays mode:" + mode + " first:" + first + " count:" +count);
    gl.drawArrays(mode, first, count);
    checkError("drawArrays");
  }

  public void checkError(String string) {
    // int err = gl.glGetError();
    // if (err != GL_NO_ERROR) {
    // log("GL_ERROR in " + string + "(): " + err);
    // //throw new RuntimeException("GL_ERROR in " + string + "(): " + err);
    // }
  }

  public void updatTCBuffer(FloatBuffer buf, int offset, int count) {
    BufferData bd = bufferData[Gl1Context.ARRAY_TEXCOORD_0];
    gl.bindBuffer(ARRAY_BUFFER, bd.buffer);

    int pos = buf.position();
    int limit = buf.limit();

    buf.position(pos + offset);
    buf.limit(pos + offset + count);

    ArrayBufferView data = getTypedArray(buf, GL_FLOAT);

    gl.bufferSubData(ARRAY_BUFFER, offset * 4, data);

    buf.position(pos);
    buf.limit(limit);
  }

  private void prepareDraw() {
    if (updateMvpMatrix()) {
      gl.uniformMatrix4fv(uMvpMatrix, false, Float32Array.create(mvpMatrix));
      checkError("prepareDraw");
    }

    gl.uniform1i(uTexEnv0, getTextureMode(0));
    gl.uniform1i(uTexEnv1, getTextureMode(1));

    // StringBuilder sizes = new StringBuilder();

    for (int i = 0; i < SMALL_BUF_COUNT; i++) {
      BufferData bd = bufferData[i];
      if (bd.toBind != null) {
        gl.bindBuffer(ARRAY_BUFFER, bd.buffer);
        checkError("bindBuffer" + i);

        // int len = bd.toBind.getByteLength();
        // if (len < bd.byteSize) {
        // gl.glBufferSubData(WebGL.GL_ARRAY_BUFFER, 0, bd.toBind);
        // } else {
        // bd.byteSize = len;
        gl.bufferData(ARRAY_BUFFER, bd.toBind, STREAM_DRAW);
        // }
        checkError("bufferData" + i);

        gl.vertexAttribPointer(i, bd.size, bd.type, bd.normalize,
            bd.byteStride, 0);
        checkError("vertexAttribPointer");

        bd.toBind = null;
      }
    }

    // log ("prepDraw: " + sizes);
  }

  private int getTextureMode(int i) {
    return texEnvMode[i] == GL_REPLACE ? 0
        : (textureFormats.get(boundTextureId[i]) == 3 ? 2 : 1);
  }

  @Override
  public final void glScissor(int i, int j, int width, int height) {
    gl.scissor(i, j, width, height);
    checkError("glScissor");
  }

  @Override
  public void glTexParameterf(int target, int pname, float param) {
    gl.texParameterf(target, pname, param);
    checkError("glTexParameterf");
  }

  @Override
  public final void glEnableClientState(int i) {
    switch (i) {
      case GL_COLOR_ARRAY:
        gl.enableVertexAttribArray(Gl1Context.ARRAY_COLOR);
        checkError("enableClientState colorArr");
        break;
      case GL_VERTEX_ARRAY:
        gl.enableVertexAttribArray(Gl1Context.ARRAY_POSITION);
        checkError("enableClientState vertexArrr");
        break;
      case GL_TEXTURE_COORD_ARRAY:
        switch (clientActiveTexture) {
          case 0:
            gl.enableVertexAttribArray(Gl1Context.ARRAY_TEXCOORD_0);
            checkError("enableClientState texCoord0");
            break;
          case 1:
            gl.enableVertexAttribArray(Gl1Context.ARRAY_TEXCOORD_1);
            checkError("enableClientState texCoord1");
            break;
          default:
            throw new RuntimeException();
        }
        break;
      default:
        log("unsupported / unrecogized client state " + i);
    }
  }

  @Override
  public final void glDisableClientState(int i) {
    switch (i) {
      case GL_COLOR_ARRAY:
        gl.disableVertexAttribArray(Gl1Context.ARRAY_COLOR);
        break;
      case GL_VERTEX_ARRAY:
        gl.disableVertexAttribArray(Gl1Context.ARRAY_POSITION);
        break;
      case GL_TEXTURE_COORD_ARRAY:
        switch (clientActiveTexture) {
          case 0:
            gl.disableVertexAttribArray(Gl1Context.ARRAY_TEXCOORD_0);
            break;
          case 1:
            gl.disableVertexAttribArray(Gl1Context.ARRAY_TEXCOORD_1);
            break;
          default:
            throw new RuntimeException();
        }
        break;
      default:
        log("unsupported / unrecogized client state");
    }
    checkError("DisableClientState");
  }

  @Override
  public final void glDisable(int i) {
    // In ES, you don't enable/disable TEXTURE_2D. We use it this call to
    // disable one of the two active textures supported by the shader.
    if (i == GL_TEXTURE_2D) {
      switch (activeTexture) {
        case 0:
          gl.uniform1i(uEnableTexture0, 0);
          break;
        case 1:
          gl.uniform1i(uEnableTexture1, 0);
          break;
        default:
          throw new RuntimeException();
      }
      return;
    }

    gl.disable(i);
    checkError("glDisable");
  }

  @Override
  public final void glCullFace(int c) {
    gl.cullFace(c);
    checkError("glCullFace");
  }

  @Override
  public final void glShadeModel(int s) {
  }

  @Override
  public final void glViewport(int x, int y, int w, int h) {
    super.glViewport(x, y, w, h);
    gl.viewport(x, y, w, h);
    checkError("glViewport");
  }

  public void glVertexAttribPointer(int arrayId, int size, int type,
      boolean normalize, int byteStride, Buffer nioBuffer) {
    BufferData bd = bufferData[arrayId];
    bd.byteStride = byteStride;
    bd.size = size;
    bd.normalize = normalize;
    bd.type = type;
    ArrayBufferView webGLArray = getTypedArray(nioBuffer, type);
    bd.toBind = webGLArray;
  }

  public void glVertexAttribPointer(int arrayId, int size, int type,
      boolean normalize, int byteStride, int offset, Buffer nioBuffer,
      int staticDrawId) {
    WebGLBuffer buffer = staticBuffers.get(staticDrawId);
    if (buffer == null) {
      buffer = gl.createBuffer();
      staticBuffers.set(staticDrawId, buffer);
      gl.bindBuffer(ARRAY_BUFFER, buffer);
      ArrayBufferView webGLArray = getTypedArray(nioBuffer, type);
      gl.bufferData(ARRAY_BUFFER, webGLArray, STATIC_DRAW);
      checkError("bufferData");
      log("static buffer created; id: " + staticDrawId + " remaining: "
          + nioBuffer.remaining());
    }
    gl.bindBuffer(ARRAY_BUFFER, buffer);
    gl.vertexAttribPointer(arrayId, size, type, normalize, byteStride, offset);
    bufferData[arrayId].toBind = null;
    checkError("vertexAttribPointer");
  }

  private ArrayBufferView getTypedArray(Buffer buffer, int type) {
    int elementSize;
    HasArrayBufferView arrayHolder;

    if (!(buffer instanceof HasArrayBufferView)) {
      if (type != GL_BYTE && type != GL_UNSIGNED_BYTE) {
        log("buffer byte order problem");
        throw new RuntimeException("Buffer byte order problem");
      }
      if (buffer instanceof IntBuffer) {
        elementSize = 4;
      } else {
        throw new RuntimeException("NYI");
      }
      arrayHolder = (HasArrayBufferView) ((ByteBufferWrapper) buffer).getByteBuffer();
    } else {
      arrayHolder = (HasArrayBufferView) buffer;
      elementSize = arrayHolder.getElementSize();
    }

    ArrayBufferView webGLArray = arrayHolder.getTypedArray();
    int remainingBytes = buffer.remaining() * elementSize;

    int byteOffset = webGLArray.getByteOffset() + buffer.position()
        * elementSize;

    switch (type) {
      case FLOAT:
        return Float32Array.create(webGLArray.getBuffer(), byteOffset,
            remainingBytes / 4);

      case UNSIGNED_BYTE:
        return Uint8Array.create(webGLArray.getBuffer(), byteOffset,
            remainingBytes);

      case UNSIGNED_SHORT:
        return Uint16Array.create(webGLArray.getBuffer(), byteOffset,
            remainingBytes / 2);

      case INT:
        return Int32Array.create(webGLArray.getBuffer(), byteOffset,
            remainingBytes / 4);

      case SHORT:
        return Int16Array.create(webGLArray.getBuffer(), byteOffset,
            remainingBytes / 2);

      case BYTE:
        return Int8Array.create(webGLArray.getBuffer(), byteOffset,
            remainingBytes);
    }

    throw new IllegalArgumentException();
  }

  public void glGenerateMipmap(int t) {
    gl.generateMipmap(t);
    checkError("genMipmap");
  }
}
