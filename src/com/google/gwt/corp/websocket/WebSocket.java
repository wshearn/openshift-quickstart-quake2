/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.corp.websocket;

import com.google.gwt.core.client.JavaScriptObject;

public class WebSocket extends JavaScriptObject {

  public interface Listener {
    void onClose(WebSocket socket, CloseEvent event);
    void onMessage(WebSocket socket, MessageEvent event);
    void onOpen(WebSocket socket, OpenEvent event);
  }

  public static final short CONNECTING = 0;
  public static final short OPEN = 1;
  public static final short CLOSED = 2;

  public static native WebSocket create(String url) /*-{
    return new WebSocket(url);
  }-*/;

  public static native WebSocket create(String url, String protocol) /*-{
    return new WebSocket(url, protocol);
  }-*/;

  protected WebSocket() {
  }

  public final native void close() /*-{
    this.close();
  }-*/;

  public final native int getBufferedAmount() /*-{
    return this.bufferedAmount;
  }-*/;

  public final native short getReadyState() /*-{
    return this.readyState;
  }-*/;

  public final native String getURL() /*-{
    return this.url;
  }-*/;

  public final native boolean send(String data) /*-{
    return this.send(data);
  }-*/;

  public final native void setListener(Listener listener) /*-{
    if (!listener) {
      this.onopen = null;
      this.onclose = null;
      this.onmessage = null;
      return;
    }

    var self = this;
    this.onopen = function(e) {
      listener.@com.google.gwt.corp.websocket.WebSocket.Listener::onOpen(Lcom/google/gwt/corp/websocket/WebSocket;Lcom/google/gwt/corp/websocket/OpenEvent;)(self, e);
    };
    this.onclose = function(e) {
      listener.@com.google.gwt.corp.websocket.WebSocket.Listener::onClose(Lcom/google/gwt/corp/websocket/WebSocket;Lcom/google/gwt/corp/websocket/CloseEvent;)(self, e);
    };
    this.onmessage = function(e) {
      listener.@com.google.gwt.corp.websocket.WebSocket.Listener::onMessage(Lcom/google/gwt/corp/websocket/WebSocket;Lcom/google/gwt/corp/websocket/MessageEvent;)(self, e);
    };
  }-*/;
}
