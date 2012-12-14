/*
Copyright (C) 1997-2001 Id Software, Inc.

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
/* Modifications
   Copyright 2003-2004 Bytonic Software
   Copyright 2010 Google Inc.
*/
package com.googlecode.gwtquake.client;


import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import com.google.gwt.html5.client.CanvasElement;
import com.google.gwt.html5.client.CanvasRenderingContext2D;
import com.googlecode.gwtquake.shared.render.DisplayMode;
import com.googlecode.gwtquake.shared.render.Gl1Context;
import com.googlecode.gwtquake.shared.util.CanvasHelper;

/**
 * Crude attempt at showing some of the output on a regular 2D canvas for
 * debugging. 
 * 
 * @author Stefan Haustein
 */
public class WireframeGl1Context extends Gl1Context {

	FloatBuffer colorBuffer;

	private int vertexPointerSize;
	private int vertexPointerType;
	private int vertexPointerStride;
	private Buffer vertexPointerData;

	private int colorPointerSize;
	private int colorPointerType;
	private int colorPointerStride;
	private Buffer colorPointerBuffer;

	private String uniformColor;
	private boolean colorArrayEnabled;

	private CanvasRenderingContext2D ctx;
	private int vertexPointerPosition;
	private boolean debugHighlight;
	
	private final CanvasElement canvas;
	
	public WireframeGl1Context(CanvasElement canvas) {
		this.canvas = canvas;
		swapBuffers();
	}
	
	@Override
	public final void glBindTexture(int t, int i) {
	}

	@Override
	public final void glBlendFunc(int a, int b) {
	}

	@Override
	public final void glClear(int mask) {
		ctx.clearRect(0, 0, 3000, 3000);
	}

	@Override
	public final void glColor4f(float red, float green, float blue, float alpha) {
		uniformColor = CanvasHelper.getCssColor(red, green, blue, alpha);
	}


	@Override
	public void glDrawElements(int mode, ShortBuffer indices) {
		debugDraw(mode, indices.position(),
				indices.limit(), indices);
	}

	@Override
	public final void glEnable(int i) {
	}

	@Override
	public final int glGetError() {
		return GL_NO_ERROR;
	}

	@Override
	public final void glClearColor(float f, float g, float h, float i) {
	}

	private void debugDraw(int mode, int first, int count, Buffer indices) {
		
		updateMvpMatrix();
		
		float[] v = new float[4];
		float[] result = new float[4];

		float x0 = 0;
		float x1 = 0;
		float x2 = 0;
		float y0 = 0;
		float y1 = 0;
		float y2 = 0;

		float z0 = 0;
		float z1 = 0;
		float z2 = 0;
		
//		System.out.println("Viewport x: " + viewportX + " y: " + viewportY
//				+ " w:" + viewportW + " h:" + viewportH);

//		if (AUTO_REWIND) {
//			vertexPointerData.rewind();
//		}

		if (!(vertexPointerData instanceof FloatBuffer)) {
			throw new RuntimeException("float coordinates only!");
		}
		
		FloatBuffer vertexBuf = (FloatBuffer) vertexPointerData;
		int stride = vertexPointerStride == 0 ? vertexPointerSize : vertexPointerStride / 4;
			
		if (vertexPointerSize != 3 || vertexPointerStride % 4 != 0) {
			System.out.println("VertexPointerSize: " + vertexPointerSize);
		}

		int[] view = { viewportX, viewportY, viewportW, viewportH };
		float[] win = new float[3];

//		System.out.println("VertexPointerType: " + GLDebugWrapper.c(vertexPointerType));

		int p = vertexPointerPosition;
		
		boolean v2 = false;
		boolean v1 = false;
		boolean v0 = false;
		
		if(debugHighlight) {
			System.out.println("going to draw "+ count + " points");
		}
		
		for (int j = 0; j < count; j++) {
			int i;
			if (indices == null) {
				i = first + j;
			} else if (indices instanceof ShortBuffer) {
				i = ((ShortBuffer) indices).get(j);
			} else if (indices instanceof IntBuffer) {
				i = ((IntBuffer) indices).get(j);
			} else {
				throw new RuntimeException("Unsupported: " + indices.getClass());
			}

			// TODO(Haustein) use MVP matrix here...
//			GLU.gluProject(vertexBuf.get(p + i * stride), vertexBuf.get(p + i * stride + 1), 
//					vertexBuf.get(p + i * stride + 2), modelViewMatrix, 0, projectionMatrix, 
//					0, view, 0, win, 0);

			if (mode != GL_TRIANGLE_FAN || j == 2) {
				x0 = x1;
				y0 = y1;
				z0 = z1;
				v0 = v1;
			}
			x1 = x2;
			y1 = y2;
			z1 = z2;
			v1 = v2;

			v2 = project(vertexBuf.get(p + i * stride), vertexBuf.get(p + i * stride + 1), 
					vertexBuf.get(p + i * stride + 2), view, win);
			
			if(!v2 && debugHighlight) {
				System.out.println("projection returned false");
			}
			
			x2 = win[0] ;
			y2 = viewportH - win[1] ;
			z2 = win[2];
			v2 &= z2 < 1;

//			v2 &= x2 > viewportX && x2 < viewportX + viewportW;
//			v2 &= y2 > viewportY && y2 < viewportY + viewportH;
			
			int k = i * 4;

			boolean fill = false;
			boolean draw = false;
			switch (mode) {
			case GL_TRIANGLE_FAN:
			case GL_TRIANGLE_STRIP:
				fill = j >= 2;
				break;
			case GL_TRIANGLES:
				fill = (j % 3) == 2;
				break;
			case GL_LINES:
				draw = (j & 1) == 1;
				break;
			default:
				throw new IllegalArgumentException();
			}

			if (fill && v0 && v1 && v2) {
				float z = (z1+z0+z2)/3;
				float alpha = (1 - z);
				if (alpha < 0) {
					alpha = 0;
				} else if (alpha > 1) {
					alpha = 1;
				}
				
				float am1 = alpha-1;
				
				alpha = 1-am1*am1*am1*am1;
//				System.out.println("z: "+z + " sqrt "+ Math.sqrt(z) + " sqr "+ z*z + " e" + Math.exp(z));
				ctx.setGlobalAlpha(debugHighlight ? 0.5f : alpha);
				
				if (debugHighlight) {
					System.out.println("x0: " + x0 + " y0: "+ y0 + 
							"x1: " + x1 + " y1: " + y1 + " x2: " + x2 + " y2: " + y2);
				}
				
				ctx.beginPath();
				ctx.moveTo(x0, y0);
				ctx.lineTo(x1, y1);
				ctx.lineTo(x2, y2);
				ctx.lineTo(x0, y0);
				ctx.stroke();
			} else if (draw && v1 && v2) {
				ctx.beginPath();
				ctx.moveTo(x1, y1);
				ctx.lineTo(x2, y2);
				ctx.stroke();
			}
		}
	}

	@Override
	public void glDrawArrays(int mode, int first, int count) {
		// mv-matrix
//		
		debugDraw(mode, first, count, null);
	}

//	private void prepareDraw() {
//		Matrix
//				.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, modelViewMatrix,
//						0);
//	}

	public final void glHint(int h, int i) {
	}

	public final void glReadPixels(int i, int j, int width, int height,
			int format, int type, IntBuffer buffer) {
	}

	@Override
	public final void swapBuffers() {
		ctx = canvas.getContext2D();
		ctx.setFont("8px Courier");
		ctx.setStrokeStyleColor("#00ff00");
		ctx.setFillStyleColor("#00ff00");
	}


	public final void glEnableClientState(int i) {
		switch (i) {
		case GL_COLOR_ARRAY:
			colorArrayEnabled = true;
			break;
		case GL_VERTEX_ARRAY:
			break;
		case GL_TEXTURE_COORD_ARRAY:
			break;
		default:
			System.out.println("unsupported / unrecogized client state");
		}
	}

	public final void glDisableClientState(int i) {
		switch (i) {
		case GL_COLOR_ARRAY:
			colorArrayEnabled = false;
			break;
		case GL_VERTEX_ARRAY:
			break;
		case GL_TEXTURE_COORD_ARRAY:
			break;
		default:
			System.out.println("unsupported / unrecogized client state");
		}
	}



	@Override
	public void glColorPointer(int size, int stride, FloatBuffer buf) {
		colorPointerSize = size;
		colorPointerType = GL_FLOAT;
		colorPointerStride = stride;
		colorPointerBuffer = buf;
	}

	@Override
	public void glColorPointer(int size, boolean b, int stride, ByteBuffer buf) {
		colorPointerSize = size;
		colorPointerType = GL_UNSIGNED_BYTE;
		colorPointerStride = stride;
		colorPointerBuffer = buf;
	}

	@Override
	public void glVertexPointer(int size, int byteStride, FloatBuffer buf) {
		vertexPointerSize = size;
		vertexPointerType = GL_FLOAT;
		vertexPointerStride = byteStride;
		vertexPointerData = buf;
		vertexPointerPosition = buf.position();
	}


	public final void glScissor(int i, int j, int width, int height) {
	}

	public final void glTexEnvx(int env, int mode, int replace) {
	}

	@Override
	public void glTexParameterf(int target, int pname, float param) {
	}

	public final void glDeleteTextures(int i, int[] textureId, int j) {
	}

	public final void glGenTextures(int n, int[] result, int offset) {
	}
	
	public final void glTexCoordPointer(int size, int type, int stride,
			Buffer buf) {
	}

	public final void glDisable(int i) {
	}

	public final void glCullFace(int c) {
	}

	public final void glFrontFace(int f) {
	}

	public final void glShadeModel(int s) {
	}


	@Override
	public void glAlphaFunc(int i, float j) {
	}

	
	@Override
	public void glDeleteTextures(IntBuffer texnumBuffer) {
	}

	@Override
	public void glDepthFunc(int func) {
	}

	@Override
	public void glDepthMask(boolean b) {
	}

	@Override
	public void glDepthRange(float gldepthmin, float gldepthmax) {
	}

	@Override
	public void glDrawBuffer(int buf) {
		// selects front or back buffer (???)
	}


	@Override
	public void glFinish() {
		
	}

	@Override
	public String glGetString(int id) {
		switch (id) {
		case GL_VENDOR: return "Google";
		case GL_RENDERER: return "Wireframe Debug OpenGL Engine";
		case GL_VERSION: return "2.0 Wireframe-1.0.0";
		case GL_EXTENSIONS: return "GL_ARB_transpose_matrix GL_ARB_vertex_program GL_ARB_vertex_blend GL_ARB_window_pos GL_ARB_shader_objects GL_ARB_vertex_shader GL_ARB_shading_language_100 GL_EXT_multi_draw_arrays GL_EXT_clip_volume_hint GL_EXT_rescale_normal GL_EXT_draw_range_elements GL_EXT_fog_coord GL_EXT_gpu_program_parameters GL_EXT_geometry_shader4 GL_EXT_transform_feedback GL_APPLE_client_storage GL_APPLE_specular_vector GL_APPLE_transform_hint GL_APPLE_packed_pixels GL_APPLE_fence GL_APPLE_vertex_array_object GL_APPLE_vertex_program_evaluators GL_APPLE_element_array GL_APPLE_flush_render GL_APPLE_aux_depth_stencil GL_NV_texgen_reflection GL_NV_light_max_exponent GL_IBM_rasterpos_clip GL_SGIS_generate_mipmap GL_ARB_imaging GL_ARB_point_parameters GL_ARB_texture_env_crossbar GL_ARB_texture_border_clamp GL_ARB_multitexture GL_ARB_texture_env_add GL_ARB_texture_cube_map GL_ARB_texture_env_dot3 GL_ARB_multisample GL_ARB_texture_env_combine GL_ARB_texture_compression GL_ARB_texture_mirrored_repeat GL_ARB_shadow GL_ARB_depth_texture GL_ARB_fragment_program GL_ARB_fragment_program_shadow GL_ARB_fragment_shader GL_ARB_occlusion_query GL_ARB_point_sprite GL_ARB_texture_non_power_of_two GL_ARB_vertex_buffer_object GL_ARB_pixel_buffer_object GL_ARB_draw_buffers GL_ARB_shader_texture_lod GL_EXT_compiled_vertex_array GL_EXT_framebuffer_object GL_EXT_framebuffer_blit GL_EXT_framebuffer_multisample GL_EXT_texture_rectangle GL_ARB_texture_rectangle GL_EXT_texture_env_add GL_EXT_blend_color GL_EXT_blend_minmax GL_EXT_blend_subtract GL_EXT_texture_lod_bias GL_EXT_abgr GL_EXT_bgra GL_EXT_stencil_wrap GL_EXT_texture_filter_anisotropic GL_EXT_secondary_color GL_EXT_blend_func_separate GL_EXT_shadow_funcs GL_EXT_stencil_two_side GL_EXT_depth_bounds_test GL_EXT_texture_compression_s3tc GL_EXT_texture_compression_dxt1 GL_EXT_texture_sRGB GL_EXT_blend_equation_separate GL_EXT_texture_mirror_clamp GL_EXT_packed_depth_stencil GL_EXT_bindable_uniform GL_EXT_texture_integer GL_EXT_gpu_shader4 GL_EXT_draw_buffers2 GL_APPLE_flush_buffer_range GL_APPLE_ycbcr_422 GL_APPLE_vertex_array_range GL_APPLE_texture_range GL_APPLE_float_pixels GL_ATI_texture_float GL_ARB_texture_float GL_ARB_half_float_pixel GL_APPLE_pixel_buffer GL_APPLE_object_purgeable GL_NV_point_sprite GL_NV_register_combiners GL_NV_register_combiners2 GL_NV_blend_square GL_NV_texture_shader GL_NV_texture_shader2 GL_NV_texture_shader3 GL_NV_fog_distance GL_NV_depth_clamp GL_NV_multisample_filter_hint GL_NV_fragment_program_option GL_NV_fragment_program2 GL_NV_vertex_program2_option GL_NV_vertex_program3 GL_ATI_texture_mirror_once GL_ATI_texture_env_combine3 GL_ATI_separate_stencil GL_SGIS_texture_edge_clamp GL_SGIS_texture_lod ";
		default: return "Unknown Sting ID";
		}
	}


	@Override
	public void glPixelStorei(int i, int j) {
	}

	@Override
	public void glPointSize(float value) {
	}

	@Override
	public void glPolygonMode(int i, int j) {
	}

	@Override
	public void glReadPixels(int x, int y, int width, int height, int glBgr,
			int glUnsignedByte, ByteBuffer image) {
	}

	@Override
	public void glTexCoordPointer(int size, int byteStride, FloatBuffer buf) {
	}

	@Override
	public void glTexEnvi(int glTextureEnv, int glTextureEnvMode, int mode) {
	}

	@Override
	public void glTexImage2D(int target, int level, int internalformat,
			int width, int height, int border, int format, int type,
			ByteBuffer pixels) {
	}

	@Override
	public void glTexImage2D(int target, int level, int internalformat,
			int width, int height, int border, int format, int type,
			IntBuffer pixels) {
	}

	@Override
	public void glTexParameteri(int glTexture2d, int glTextureMinFilter,
			int glFilterMin) {
	}

	@Override
	public void glTexSubImage2D(int target, int level, int xoffset,
			int yoffset, int width, int height, int format, int type,
			ByteBuffer pixels) {
	}

	@Override
	public void glTexSubImage2D(int target, int level, int xoffset,
			int yoffset, int width, int height, int format, int type,
			IntBuffer pixels) {
	}


	@Override
	public void shutdow() {
		// ignored
	}

	@Override
	public void glActiveTexture(int texture) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glClientActiveTexture(int texture) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glPointParameterf(int id, float value) {
		// TODO Auto-generated method stub
		
	}
	
	public CanvasRenderingContext2D get2dContext() {
		return ctx;
	}

	@Override
	public void debugHighlight(boolean on) {
		System.out.println("debugHL: " + on);
		debugHighlight = on;
		ctx.setStrokeStyleColor(on ? "#ff8888" : "#0000ff");
	}
}
