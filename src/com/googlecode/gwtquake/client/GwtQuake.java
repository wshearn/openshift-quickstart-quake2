/*
Copyright (C) 2010 Copyright 2010 Google Inc.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package com.googlecode.gwtquake.client;


import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.html5.client.CanvasElement;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Navigator;
import com.googlecode.gwtquake.shared.client.Dimension;
import com.googlecode.gwtquake.shared.client.Renderer;
import com.googlecode.gwtquake.shared.client.Screen;
import com.googlecode.gwtquake.shared.client.WebSocketFactoryImpl;
import com.googlecode.gwtquake.shared.common.Compatibility;
import com.googlecode.gwtquake.shared.common.ConsoleVariables;
import com.googlecode.gwtquake.shared.common.Globals;
import com.googlecode.gwtquake.shared.common.QuakeCommon;
import com.googlecode.gwtquake.shared.common.ResourceLoader;
import com.googlecode.gwtquake.shared.sound.Sound;
import com.googlecode.gwtquake.shared.sys.NET;

public class GwtQuake implements EntryPoint {

	enum BrowserType {
		FIREFOX, CHROME, SAFARI, OTHER
	};
	
	private static final int INTER_FRAME_DELAY = 1;
	private static final int LOADING_DELAY = 500;

  static CanvasElement canvas;
  static Element video;
  private static BrowserType browserType;

	private static final java.lang.String NO_WEBGL_MESSAGE =
	  "<div style='padding:20px;font-family: sans-serif;'>" +
	  "<h2>WebGL Support Required</h2>" +
	  "<p>For a list of compatible browsers and installation instructions, please refer to" +
	  "<ul><li>"+
	  "<a href='http://code.google.com/p/quake2-gwt-port/wiki/CompatibleBrowsers' " + 
	  "style='color:#888'>http://code.google.com/p/quake2-gwt-port/wiki/CompatibleBrowsers</a>" + 
	  "</li></ul>"+
    "<p>For a detailed error log, please refer to the JS console.<p>" +
    "</div>";

  Timer timer;
  int w;
  int h;
  
  static BrowserType getBrowserType() {
	  if (browserType == null) {
			String userAgent = Navigator.getUserAgent();
			System.out.println("UA: " + userAgent);
			if (userAgent == null) {
				browserType = BrowserType.OTHER;
			} else if (userAgent.indexOf("Chrome/") != -1) {
				browserType = BrowserType.CHROME;
			} else if (userAgent.indexOf("Safari/") != -1) {
				browserType = BrowserType.SAFARI;
			} else if (userAgent.indexOf("Firefox/") != -1 || userAgent.indexOf("Minefield/") != -1) {
				browserType = BrowserType.FIREFOX;
			} else {
				browserType = BrowserType.OTHER;
			}

	  }
	  return browserType;
  }
  
  public void onModuleLoad() {
  // Initialize drivers.
	Document doc = Document.get();
	doc.setTitle("GWT Quake II");
	BodyElement body = doc.getBody();
	Style style = body.getStyle();
	style.setPadding(0, Unit.PX);
	style.setMargin(0, Unit.PX);
	style.setBorderWidth(0, Unit.PX);
	style.setProperty("height", "100%");
	style.setBackgroundColor("#000");
	style.setColor("#888");
	
//	Window.alert("UA: " + userAgent+ " type: " + browserType);
	
	boolean wireframe = (""+Window.Location.getHash()).indexOf("wireframe") != -1;
	
	canvas = (CanvasElement) doc.createElement("canvas");
	video = doc.createElement("video");
	
	w = Window.getClientWidth();
	h = Window.getClientHeight();
	canvas.setWidth(w);
	canvas.setHeight(h);
	style = canvas.getStyle();
	style.setProperty("height", "100%");
	style.setProperty("width", "100%");
	
	style = video.getStyle();
	style.setProperty("height", "100%");
	style.setProperty("width", "100%");
	style.setProperty("display", "none");
	
	body.appendChild(canvas);
	body.appendChild(video);
	
    try {
      Globals.autojoin.value =
          Window.Location.getHash().indexOf("autojoin") != -1 ? 1.0f : 0.0f;
      final Renderer renderer = wireframe 
      	? new GwtWireframeGLRenderer(canvas) : new GwtWebGLRenderer(canvas, video);
      Globals.re = renderer;

      ResourceLoader.impl = new GwtResourceLoaderImpl();
      Compatibility.impl = new CompatibilityImpl();
      
      Sound.impl = new GwtSound();
      NET.socketFactory = new WebSocketFactoryImpl();
//      Sys.impl = new Sys.SysImpl() {
//        public void exit(int status) {
//          Window.alert("Something's rotten in Denmark");
//          Window.Location.assign("gameover.html");
//        }
//      };

      // Flags.
      QuakeCommon.Init(new String[] { "GQuake" });


      // Enable stdout.
      Globals.nostdout = ConsoleVariables.Get("nostdout", "0", 0);

      Window.addResizeHandler(new ResizeHandler() {

        public void onResize(ResizeEvent event) {
          if (Window.getClientWidth() == w && 
              Window.getClientHeight() == h) {
            return;
          } 

          w = Window.getClientWidth();
          h = Window.getClientHeight();

          renderer.GLimp_SetMode(new Dimension(w, h), 0, false);
        }
      });
      
//      QuakeServer.main(new String[0], new DummySNetImpl(), false);
      
      timer = new Timer() {
        double startTime = Duration.currentTimeMillis();

        @Override
        public void run() {
          try {
            double curTime = Duration.currentTimeMillis();
            boolean pumping = ResourceLoader.Pump();
            if (pumping) {
            	Screen.UpdateScreen2();
            } else {
            	int dt = (int)(curTime - startTime);
            	GwtKBD.Frame(dt);
            	QuakeCommon.Frame(dt);
            }
            startTime = curTime;
            timer.schedule(ResourceLoader.Pump() ? LOADING_DELAY : INTER_FRAME_DELAY);
          } catch(Exception e) {
            Compatibility.printStackTrace(e);
          }
        }
      };
      timer.schedule(INTER_FRAME_DELAY);
    } catch (Exception e) {
    	Compatibility.printStackTrace(e);
    	DivElement div = doc.createDivElement();
    	div.setInnerHTML(NO_WEBGL_MESSAGE);
    	body.appendChild(div);
    }
  }
}
