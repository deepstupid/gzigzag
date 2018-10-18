package org.gzigzag;

public class ZZScriptPerl {
	long ptr;
	public ZZScriptPerl() { init(); };
	public native void init();
	public native void call(String code, ZZCell view, ZZCell controlView, 
		ZZCell script, ZZCell cur, String dimX, String dimY);
}
