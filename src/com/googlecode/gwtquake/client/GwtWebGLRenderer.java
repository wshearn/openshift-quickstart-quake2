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


import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.html5.client.CanvasElement;
import com.google.gwt.html5.client.CanvasPixelArray;
import com.google.gwt.html5.client.CanvasRenderingContext2D;
import com.google.gwt.html5.client.ImageData;
import com.google.gwt.user.client.Timer;
import com.googlecode.gwtquake.shared.client.Renderer;
import com.googlecode.gwtquake.shared.common.Com;
import com.googlecode.gwtquake.shared.common.ResourceLoader;
import com.googlecode.gwtquake.shared.render.GlRenderer;
import com.googlecode.gwtquake.shared.render.GlState;
import com.googlecode.gwtquake.shared.render.Images;
import com.googlecode.gwtquake.shared.render.Image;
import com.googlecode.gwtquake.shared.sys.KBD;

import static com.google.gwt.webgl.client.WebGLRenderingContext.*;

public class GwtWebGLRenderer extends GlRenderer implements Renderer {
	
  KBD kbd = new GwtKBD();
  
  
  public KBD getKeyboardHandler() {
      return kbd;
  }
  
  
	static class VideoElement extends Element {
		protected VideoElement() {
		}
		
		native final JavaScriptObject getError() /*-{
		  return this.error;
	    }-*/;

		public final native void pause() /*-{
			this.pause();
		}-*/;

		public final native void play() /*-{
			this.play();
		}-*/;

		public final native double getDuration() /*-{
			return this.duration;
		}-*/;

		public final native double getCurrentTime() /*-{
			return this.currentTime;
		}-*/;

		public final native void setCurrentTime(double s) /*-{
			return this.currentTime = s;
		}-*/;
		
		public final boolean ended() {
//			System.out.println("video.error: " + getError() + 
//					" duration: " + getDuration() + 
//					" currentTime: " + getCurrentTime());
//			
			return getError() != null || !(Double.isNaN(getDuration()) || 
					getCurrentTime() < getDuration());
		}
	}
	
	static final int IMAGE_CHECK_TIME = 250;
	static final int MAX_IMAGE_REQUEST_COUNT = 12;
	static int HOLODECK_TEXTURE_SIZE = 128;
	static int MASK = 15;
	static int HIT = MASK/2;
	ByteBuffer holoDeckTexture = ByteBuffer.allocateDirect(HOLODECK_TEXTURE_SIZE * 
			HOLODECK_TEXTURE_SIZE * 4);

	WebGLGl1Contect webGL;
	CanvasElement canvas1;
	CanvasElement canvas2;
	ArrayList<Image> imageQueue = new ArrayList<Image>();
	int waitingForImages;
	VideoElement video;
	CanvasElement canvas;
	
	public GwtWebGLRenderer(CanvasElement canvas, Element video) {
	  super(canvas.getWidth(), canvas.getHeight());
		GlState.gl = this.webGL = new WebGLGl1Contect(canvas);
		this.canvas = canvas;
		this.video = (VideoElement) video;
		
		for (int y = 0; y < HOLODECK_TEXTURE_SIZE; y++) {
			for (int x = 0; x < HOLODECK_TEXTURE_SIZE; x++) {
				holoDeckTexture.put((byte) 0);
				holoDeckTexture.put((byte) (((x & MASK) == HIT) || ((y & MASK) == HIT) ? 255 : 0));
				holoDeckTexture.put((byte) 0);
				holoDeckTexture.put((byte) 0xff);
			}
		}
		holoDeckTexture.rewind();
		
		canvas1 = (CanvasElement) Document.get().createElement("canvas");
		canvas1.getStyle().setDisplay(Display.NONE);
		canvas1.setWidth(128);
		canvas1.setHeight(128);
		Document.get().getBody().appendChild(canvas1);
		
		canvas2 = (CanvasElement) Document.get().createElement("canvas");
		canvas2.setWidth(128);
		canvas2.setHeight(128);
		canvas2.getStyle().setDisplay(Display.NONE);
		Document.get().getBody().appendChild(canvas2);
		
		init();
	}

	@Override
	public void GL_ResampleTexture(int[] in, int inwidth, int inheight,
			int[] out, int outwidth, int outheight) {
		
		if (canvas1.getWidth() < inwidth) {
			canvas1.setWidth(inwidth);
		}
		if (canvas1.getHeight() < inheight) {
			canvas1.setHeight(inheight);
		}

		CanvasRenderingContext2D inCtx = canvas1.getContext2D();
		ImageData data = inCtx.createImageData(inwidth, inheight);
		CanvasPixelArray pixels = data.getData();
		
		int len = inwidth * inheight;
		int p = 0;
			
		for(int i = 0; i < len; i++) {
			int abgr = in[i];
			pixels.set(p, (abgr & 255));
			pixels.set(p + 1, (abgr >> 8) & 255);
			pixels.set(p + 2, (abgr >> 16) & 255);
			pixels.set(p + 3, (abgr >> 24) & 255);
			p += 4;
		}
		inCtx.putImageData(data, 0, 0);
		
		if (canvas2.getWidth() < outwidth) {
			canvas2.setWidth(outwidth);
		}
		if (canvas2.getHeight() < outheight) {
			canvas2.setHeight(outheight);
		}

		CanvasRenderingContext2D outCtx = canvas2.getContext2D();
		outCtx.drawImage(canvas1, 0, 0, inwidth, inheight, 0, 0, outwidth, outheight);
		
		data = outCtx.getImageData(0, 0, outwidth, outheight);
		pixels = data.getData();
		
		len = outwidth * outheight;
		p = 0;
			
		for(int i = 0; i < len; i++) {
			int r = pixels.get(p) & 255;
			int g = pixels.get(p + 1) & 255;
			int b = pixels.get(p + 2) & 255;
			int a = pixels.get(p + 3) & 255;
			p += 4;
			out[i] = (a << 24) | (b << 16) | (g << 8) | r;
		}
	}
	
	static native JsArrayInteger getImageSize(String name) /*-{
    return $wnd.__imageSizes[name];
  }-*/;
	
	public Image GL_LoadNewImage(final String name, int type) {
		final Image image = Images.GL_Find_free_image_t(name, type);

		int cut = name.lastIndexOf('.');
		String normalizedName = cut == -1 ? name : name.substring(0, cut);
		JsArrayInteger d = getImageSize(normalizedName);
		if (d == null) {
			GlState.gl.log("Size not found for " + name);
			image.width = 128;
			image.height = 128;
		} else {
			image.width = d.get(0);
			image.height = d.get(1);
		}
		
		if (type != com.googlecode.gwtquake.shared.common.QuakeImage.it_pic) {
			GlState.gl.glTexImage2D(TEXTURE_2D, 0, RGBA, HOLODECK_TEXTURE_SIZE, HOLODECK_TEXTURE_SIZE, 0, RGBA, 
			    UNSIGNED_BYTE, holoDeckTexture);
			GlState.gl.glTexParameterf(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR);
			GlState.gl.glTexParameterf(TEXTURE_2D, TEXTURE_MAG_FILTER, LINEAR);
		}

		imageQueue.add(image);
		if(imageQueue.size() == 1) {
		  new ImageLoader().schedule();
		}
		
		return image;
	}
	
	class ImageLoader extends Timer {

	  @Override
	  public void run() {
	    Document doc = Document.get();

	    while(!ResourceLoader.Pump() && waitingForImages < MAX_IMAGE_REQUEST_COUNT && imageQueue.size() > 0) {
	      final Image image = imageQueue.remove(0);

	      final ImageElement img = doc.createImageElement();
              String picUrl = convertPicName(image.name, image.type);
          /*    if (picUrl.endsWith("ggrat6_2.png")) {
                picUrl = convertPicName("textures/tron_poster.jpg", 0);
              }*/
              img.setSrc(picUrl);
	      img.getStyle().setDisplay(Display.NONE);
	      doc.getBody().appendChild(img);
	      
	      if (img.getPropertyBoolean("complete")) {
	        loaded(image, img);
	      } else {
	    	waitingForImages(+1);
	        com.google.gwt.user.client.ui.Image imgWidget = 
	        		com.google.gwt.user.client.ui.Image.wrap(img);
	        final ImageElement finalImg = img;
	        imgWidget.addLoadHandler(new LoadHandler() {
	          public void onLoad(LoadEvent event) {
	        	waitingForImages(-1);
	            loaded(image, finalImg);
	          }
	        });
	        imgWidget.addErrorHandler(new ErrorHandler() {
	          public void onError(ErrorEvent event) {
	           // String src = finalImg.getSrc();
	           // if (src.endsWith("&rt&rt&rt&rt")) {
	              GlState.gl.log("load errors for " + finalImg.getSrc() );
	              waitingForImages(-1);
	              image.complete = true;
	//            } else {
	 //           	finalImg.setSrc(finalImg.getSrc() + "&rt");
	  //          }
	          }
	        });	
	      } // else
	    } // while
	    if (imageQueue.size() > 0) {
		  schedule();
	    }
	  }
	  
	protected void waitingForImages(int i) {
		waitingForImages += i;
		if (waitingForImages > 0) {
		  Com.Printf("Waiting for " + waitingForImages + " images\n");
		}
	}

	public void schedule() {
		  schedule(IMAGE_CHECK_TIME);
	  }
	}
	
	public void loaded(Image image, ImageElement img) {
		setPicDataHighLevel(image, img);
	//	setPicDataLowLevel(image, img);
	}
		
	ByteBuffer bb = ByteBuffer.allocateDirect(128*128*4);
	
	public void setPicDataHighLevel(Image image, ImageElement img) {
		image.has_alpha = true;
		image.complete = true;
		image.height = img.getHeight();
		image.width = img.getWidth();
		
		boolean mipMap = image.type != com.googlecode.gwtquake.shared.common.QuakeImage.it_pic && 
			image.type != com.googlecode.gwtquake.shared.common.QuakeImage.it_sky;
		
		Images.GL_Bind(image.texnum);

		int p2w = 1 << ((int) Math.ceil(Math.log(image.width) / Math.log(2))); 
		int p2h = 1 << ((int) Math.ceil(Math.log(image.height) / Math.log(2))); 

		if (mipMap) {
			p2w = p2h = Math.max(p2w, p2h);
		}
		
		image.upload_width = p2w;
		image.upload_height = p2h;

		int level = 0;
		do {
			canvas1.setWidth(p2w);
			canvas1.setHeight(p2h);

			canvas1.getContext2D().clearRect(0, 0, p2w, p2h);
			canvas1.getContext2D().drawImage(img, 0, 0, p2w, p2h);

			webGL.glTexImage2d(TEXTURE_2D, level++, RGBA, RGBA, UNSIGNED_BYTE, canvas1);

			p2w = p2w / 2;
			p2h = p2h / 2;
		}
		while(mipMap && p2w > 0);
		
		GlState.gl.glTexParameterf(TEXTURE_2D, TEXTURE_MIN_FILTER, 
				mipMap ? LINEAR_MIPMAP_NEAREST : LINEAR);
		GlState.gl.glTexParameterf(TEXTURE_2D, TEXTURE_MAG_FILTER, LINEAR);
	}

  public void __setPicDataHighLevel(Image image, ImageElement img) {
    image.has_alpha = true;
    image.complete = true;
    image.height = img.getHeight();
    image.width = img.getWidth();
    image.upload_height = image.height;
    image.upload_width = image.width;
    Images.GL_Bind(image.texnum);
    webGL.glTexImage2d(TEXTURE_2D, 0, RGBA, RGBA, UNSIGNED_BYTE, img);
    GlState.gl.glTexParameterf(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR);
    GlState.gl.glTexParameterf(TEXTURE_2D, TEXTURE_MAG_FILTER, LINEAR);
  }

	public void setPicDataLowLevel(Image image, ImageElement img) {
		CanvasElement canvas = (CanvasElement) Document.get().createElement("canvas");
		int w = img.getWidth();
		int h = img.getHeight();
		canvas.setWidth(w);
		canvas.setHeight(h);
//		canvas.getStyle().setProperty("border", "solid 1px green");
		canvas.getStyle().setDisplay(Display.NONE);
		Document.get().getBody().appendChild(canvas);
		CanvasRenderingContext2D ctx = canvas.getContext2D();
		ctx.drawImage(img, 0, 0);
		ImageData data = ctx.getImageData(0, 0, w, h);
		CanvasPixelArray pixels = data.getData();
		
		int count = w * h * 4;
		byte[] pic = new byte[count];
		for (int i = 0; i < count; i += 4) {
			pic[i + 3] = (byte) pixels.get(i + 3); // alpha, then bgr
			pic[i + 2] = (byte) pixels.get(i + 2);
			pic[i + 1] = (byte) pixels.get(i + 1);
			pic[i] = (byte) pixels.get(i);
		}
		
		image.setData(pic, w, h, 32);
	}
	
	 protected void debugLightmap(IntBuffer lightmapBuffer, int w, int h, float scale) {
		 CanvasElement canvas = (CanvasElement) Document.get().createElement("canvas");
		 canvas.setWidth(w);
		 canvas.setHeight(h);
		 Document.get().getBody().appendChild(canvas);
		 ImageData id = canvas.getContext2D().createImageData(w, h);
		 CanvasPixelArray pd = id.getData();
		 for (int i = 0; i < w*h; i++) {
			 int abgr = lightmapBuffer.get(i);
			 pd.set(i*4, abgr & 255);
			 pd.set(i*4+1, abgr & 255);
			 pd.set(i*4+2, abgr & 255);
			 pd.set(i*4+3, abgr & 255);
		 }
		 canvas.getContext2D().putImageData(id, 0, 0);
	 }
	 
	private static String convertPicName(String name, int type) {
	  int dotIdx = name.indexOf('.');
	  assert dotIdx != -1;
	  return "baseq2/" + name.substring(0, dotIdx) + ".png";
	}
	
	public boolean updateVideo() {
		
		return !video.ended();
	}
	
	
	public void CinematicSetPalette(byte[] palette) {
		setVideoVisible(palette != null);
	}
	
	
	boolean videoVisible = false;
	
	private void setVideoVisible(boolean show) {
		if (videoVisible == show) {
			return;
		}
		System.out.println("setVideoVisible(" + show + ")");
		videoVisible = show;
		if (show) {
			canvas.getStyle().setProperty("display", "none");
			video.getStyle().setProperty("display", "");
			if (video.getAttribute("src") != null  && !video.ended()) {
				video.play();
			}
		} else {
			canvas.getStyle().setProperty("display", "");
			video.getStyle().setProperty("display", "none");
			if (video.getAttribute("src") != null && !video.ended()) {
				video.pause();
			}
		}
	}
	
	public boolean showVideo(String name) {
		if (name == null) {
			setVideoVisible(false);
			return true;
		}

//		String src = GWT.getModuleBaseURL();
//		int cut = src.indexOf("/", 8);
//		if (cut == -1) {
//			cut = src.length();
//		}
		String src = "baseq2/video/" + name + ".mp4";

		System.out.println("trying to play video: " + src);

		video.setAttribute("class", "video-stream");
		video.setAttribute("src", src);
		if (!Double.isNaN(video.getDuration())) {
			video.setCurrentTime(0);
		}
		
		setVideoVisible(true);
		return true;
	}

	
}
