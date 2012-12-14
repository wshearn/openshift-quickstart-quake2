package com.googlecode.gwtquake.client;

import com.googlecode.gwtgl.array.ArrayBufferView;

public interface HasArrayBufferView {

	public ArrayBufferView getTypedArray();
	public int getElementSize();
}
