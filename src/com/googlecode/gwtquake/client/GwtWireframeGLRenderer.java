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


import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.html5.client.CanvasElement;
import com.google.gwt.html5.client.CanvasRenderingContext2D;
import com.googlecode.gwtquake.shared.client.Renderer;
import com.googlecode.gwtquake.shared.render.GlRenderer;
import com.googlecode.gwtquake.shared.render.GlState;
import com.googlecode.gwtquake.shared.render.Images;
import com.googlecode.gwtquake.shared.render.Image;
import com.googlecode.gwtquake.shared.sys.KBD;

public class GwtWireframeGLRenderer extends GlRenderer implements Renderer {
	KBD kbd = new GwtKBD();
	private CanvasRenderingContext2D ctx;

	public GwtWireframeGLRenderer(final CanvasElement canvas) {
	  super(canvas.getWidth(), canvas.getHeight());
		GlState.gl = new WireframeGl1Context(canvas);
		init();
	}

	public KBD getKeyboardHandler() {
		return kbd;
	}

	@Override
	public void DrawChar_(int x, int y, int num) {
		ctx.setGlobalAlpha(1);
		num &= 255;
		
		if ( (num&127) == 32 ) return; // space

		if (y <= -8) return; // totally off screen

		switch(num) {
		case 11: num = '_'; break;
		case 13: num = '>'; break;
		default:
			if (num < 32) {
				num = '+';
			}
		}
		ctx.fillText("" + (char) num, x, y + 10); 
	}
	
	public void DrawStretchPic (int x, int y, int w, int h, String pic) {
		super.DrawStretchPic(x, y, w, h, pic);
		ctx.setGlobalAlpha(1);
		ctx.fillText(pic.substring(pic.lastIndexOf('_') + 1), x, y + 10);
	}

	public void DrawPic (int x, int y, String pic) {
		super.DrawPic(x, y, pic);
		ctx.setGlobalAlpha(1);
		ctx.fillText(pic.substring(pic.lastIndexOf('_') + 1), x, y + 10);
	}

	
	@Override
	public void GL_ResampleTexture(int[] in, int inwidth, int inheight,
			int[] out, int outwidth, int outheight) {
		// TODO(haustein) Auto-generated method stub
		// Should be simple with canvas and is not needed for wireframe
	}
  
  static native JsArrayInteger getImageSize(String name) /*-{
    return $wnd.__imageSizes[name];
  }-*/;
  
  public Image GL_LoadNewImage(final String name, int type) {
		final Image image = Images.GL_Find_free_image_t(name, type);

		JsArrayInteger d = getImageSize(name);
		if (d == null) {
			GlState.gl.log("Size not found for " + name);
			image.width = 128;
			image.height = 128;
		} else {
			image.width = d.get(0);
			image.height = d.get(1);
		}
		
		return image;
	}
  



}
