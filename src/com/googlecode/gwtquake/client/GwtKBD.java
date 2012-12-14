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
package com.googlecode.gwtquake.client;


import com.google.gwt.core.client.Duration;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.googlecode.gwtquake.shared.client.Key;
import com.googlecode.gwtquake.shared.client.Keys;
import com.googlecode.gwtquake.shared.common.Globals;
import com.googlecode.gwtquake.shared.sys.IN;
import com.googlecode.gwtquake.shared.sys.KBD;
import com.googlecode.gwtquake.shared.sys.Timer;

public class GwtKBD extends KBD {

  /** 
   * Show a brighter cursor for the outer quarters of the screen,
   * to serve as a reminder to recenter.
   */
  private static final boolean RECENTER_REMINDER = true;
  
  /** 
   * Mouse position sets a rotation speed instead.
   */
  private static final boolean AUTOROTATE = false;
  
  /** 
   * Use the left mouse key to drag the view, instead of capturing 
   * automatically.
   */
  private static final boolean LEFT_MOUSE_DRAG = false;
  
  private static final double SCALE = 10.0;
  private static final double AUTOROTATE_SCALE = SCALE;
  private static final int MAX_CLICK_TIME = 333;

  private static double normalX;
  private static int lastCmx;
  private static int lastCmy;
  private static boolean captureMove;
  
  public static void Frame(int ms) {
	  if (AUTOROTATE && captureMove) {
		  mx += (int) ms * normalX * AUTOROTATE_SCALE;
	  }
  }

public double mouseDownTime;

public double mouseUpTime;		

 

  private boolean hasMeta(NativeEvent nevt) {
    return nevt.getAltKey() || nevt.getMetaKey() || nevt.getCtrlKey();
  }

  private class Handler implements NativePreviewHandler {

	public void onPreviewNativeEvent(NativePreviewEvent event) {
      NativeEvent nevt = event.getNativeEvent();
      if ("keydown".equals(nevt.getType())) {
        Do_Key_Event(XLateKey(nevt.getKeyCode()), true);
        if (!hasMeta(nevt)) {
          nevt.preventDefault();
        }
      } else if ("keyup".equals(nevt.getType())) {
        Do_Key_Event(XLateKey(nevt.getKeyCode()), false);
        if (!hasMeta(nevt)) {
          nevt.preventDefault();
        }
      } else if (!IN.mouse_active) {
    	  stopCapturingMouse();
      } else {
        if ("mousemove".equals(nevt.getType())) {
          int cmx = nevt.getClientX();
          int cmy = nevt.getClientY();
   
          double cx = Globals.viddef.width / 2;
          normalX = (cmx - cx) / cx;
          
          if (captureMove) {
        	  if (RECENTER_REMINDER) {
        		  if (Math.abs(normalX) > 0.5) {
        			  GwtQuake.canvas.getStyle().setCursor(Cursor.MOVE);
        		  } else {
        			  GwtQuake.canvas.getStyle().setCursor(Cursor.CROSSHAIR);
        		  }
        	  }
          
        	  if (!AUTOROTATE) {
        		  mx += (cmx - lastCmx) * SCALE;
        		  my += (cmy - lastCmy) * SCALE;
        	 
        		  lastCmx = cmx;
        		  lastCmy = cmy;
        	  }
          } 
          nevt.preventDefault();
        } else if("mousedown".equals(nevt.getType())) {
          boolean ignoreClick = false;
          if (nevt.getButton() == NativeEvent.BUTTON_RIGHT) {
            stopCapturingMouse();
          } else if (nevt.getButton() == NativeEvent.BUTTON_LEFT) {
        	mouseDownTime = Duration.currentTimeMillis();
        	ignoreClick = startCapturingMouse(nevt);
          }
          int button = translateMouseButton(nevt);
          if (!ignoreClick && (!LEFT_MOUSE_DRAG || mouseDownTime - mouseUpTime < MAX_CLICK_TIME)) {
        	  Do_Key_Event(button, true);
          }
        } else if("mouseup".equals(nevt.getType())) {
          int button = translateMouseButton(nevt);
          if (LEFT_MOUSE_DRAG && nevt.getButton() == NativeEvent.BUTTON_LEFT) {
        	  stopCapturingMouse();
        	  if (Duration.currentTimeMillis() - mouseDownTime < MAX_CLICK_TIME) {
        		  mouseUpTime = Duration.currentTimeMillis();
        		  Do_Key_Event(button, true);
        	  }
          } else {
        	  startCapturingMouse(nevt);
          } 
          Do_Key_Event(button, false);
        } else if("mousewheel".equals(nevt.getType())) {
          Do_Key_Event(nevt.getMouseWheelVelocityY() < 0 ? Keys.K_MWHEELUP : Keys.K_MWHEELDOWN, true);
          Do_Key_Event(nevt.getMouseWheelVelocityY() < 0 ? Keys.K_MWHEELUP : Keys.K_MWHEELDOWN, false);
    		  nevt.preventDefault();
    		  nevt.stopPropagation();
        } else if("contextmenu".equals(nevt.getType())) {
        	// try to stop that pesky menu on right button, for some reason, intercepting it on mousedown/up doesn't work
        	nevt.preventDefault();
        	nevt.stopPropagation();
        } 
      } 
    }
  }

  private void stopCapturingMouse() {
	  captureMove = false;
      GwtQuake.canvas.getStyle().setCursor(Cursor.DEFAULT);      
  }
  private boolean startCapturingMouse(NativeEvent nevt) {
	if (captureMove) {
		return false;
	}
  	captureMove = true;
  	GwtQuake.canvas.getStyle().setProperty("cursor", "none");  
  	lastCmx = nevt.getClientX();
  	lastCmy = nevt.getClientY();
  	return true;
  }
  
  private static int translateMouseButton(NativeEvent evt) {
    switch(evt.getButton()) {
      case NativeEvent.BUTTON_LEFT:
        return Keys.K_MOUSE1;
      case NativeEvent.BUTTON_RIGHT:
        return Keys.K_MOUSE2;
      default:
        return Keys.K_MOUSE3;
    }
  }

  @Override
  public void Close() {
  }

  @Override
  public void Do_Key_Event(int key, boolean down) {
    Key.Event(key, down, Timer.Milliseconds());
  }

  @Override
  public void Init() {
    Event.addNativePreviewHandler(new Handler());
  }

  @Override
  public void Update() {
  }

  @Override
  public void installGrabs() {
	  
  }

  @Override
  public void uninstallGrabs() {
	 
  }

  private int XLateKey(int key) {
    switch(key) {
      case KeyCodes.KEY_PAGEUP: key = Keys.K_PGUP; break;
      case KeyCodes.KEY_PAGEDOWN: key = Keys.K_PGDN; break;
      case KeyCodes.KEY_HOME: key = Keys.K_HOME; break;
      case KeyCodes.KEY_END: key = Keys.K_END; break;
      case KeyCodes.KEY_LEFT: key = Keys.K_LEFTARROW; break;
      case KeyCodes.KEY_RIGHT: key = Keys.K_RIGHTARROW; break;
      case KeyCodes.KEY_DOWN: key = Keys.K_DOWNARROW; break;
      case KeyCodes.KEY_UP: key = Keys.K_UPARROW; break; 
      case KeyCodes.KEY_ESCAPE: key = Keys.K_ESCAPE; break; 
      case KeyCodes.KEY_ENTER: key = Keys.K_ENTER; break; 
      case KeyCodes.KEY_TAB: key = Keys.K_TAB; break; 
      case KeyCodes.KEY_BACKSPACE: key = Keys.K_BACKSPACE; break; 
      case KeyCodes.KEY_DELETE: key = Keys.K_DEL; break; 
      case KeyCodes.KEY_SHIFT: key = Keys.K_SHIFT; break; 
      case KeyCodes.KEY_CTRL: key = Keys.K_CTRL; break; 
      
      // Safari on MAC (TODO(jgw): other browsers may need tweaking):
      case 112: key = Keys.K_F1;break;
      case 113: key = Keys.K_F2;break;
      case 114: key = Keys.K_F3;break;
      case 115: key = Keys.K_F4;break;
      case 116: key = Keys.K_F5;break;
      case 117: key = Keys.K_F6;break;
      case 118: key = Keys.K_F7;break;
      case 119: key = Keys.K_F8;break;
      case 120: key = Keys.K_F9;break;
      case 121: key = Keys.K_F10;break;
      case 122: key = Keys.K_F11;break;
      case 123: key = Keys.K_F12;break;
      
      case 186: key = ';'; break;
      case 187: key = '='; break;
      case 188: key = ','; break;
      case 189: key = '-'; break;
      case 190: key = '.'; break;
      case 191: key = '/'; break;
      case 192: key = '`'; break;
      case 222: key = '\''; break;
      case 219: key = '['; break;
      case 220: key = '\\'; break;
      case 221: key = ']'; break;
      
      default:
        if (key < '0' || key > '9') {
          if (key >= 'A' &&  key <= 'Z') {
            key = Character.toLowerCase((char) key);
          }
        }
      
// TODO(jgw): We probably need keycodes for these.
//      case KeyCodes.KEY_PAUSE: key = Key.K_PAUSE; break; 
//      case KeyCodes.KEY_MENU: key = Key.K_ALT; break;
//      case KeyCodes.KEY_INSERT: key = Key.K_INS; break;
    }

    return key;
  } 
}
