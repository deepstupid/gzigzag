/*   
Sound.java
 *    
 *    Copyright (c) 2000, Ted Nelson, Tuomas Lukka and Vesa Parkkinen
 *
 *    You may use and distribute under the terms of either the GNU Lesser
 *    General Public License, either version 2 of the license or,
 *    at your choice, any later version. Alternatively, you may use and
 *    distribute under the terms of the XPL.
 *
 *    See the LICENSE.lgpl and LICENSE.xpl files for the specific terms of 
 *    the licenses.
 *
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the README
 *    file for more details.
 *
 */
/*
 * Written by Vesa Parkkinen
 */
package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.*;

/**
 * Should be broken to smaller pieces.
 * @author Vesa Parkkinen <veparkki@iki.fi>
 *
 */
public class Sound {
    public static final String rcsid = "$Id: Sound.java,v 1.2 2001/03/27 20:43:25 tjl Exp $";
    
    /* debugging */
    public static boolean dbg = true;
    static final void p(String s)  { if(dbg) ZZLogger.log(s); }
    static final void pa(String s) { ZZLogger.log(s);         }
    
    /**
     * Static module used in gzigzag module loading.
     */
    static public ZZModule module = new ZZModule() {
	    public void action( String id, ZZCell code, 
				ZZCell target,
				ZZView v, ZZView cv, 
				String key, Point pt, 
				ZZScene xi) {
		
		p("Sound.module.action() " + id);
		if( id.equals("PLAY") ) {
		    p("PLAYing...");
		    instance.play(target);
		} else if( id.equals("RECORD")){
		    p("RECORDing...");
		    // what ehis should mean ?
		    instance.record(target);
		} else if ( id.equals("STOP")) {
		    p("STOPing...");
		    instance.stop();
		} else if ( id.equals("CUT")){ 
		    p("CUTing...");
		    instance.cut();
 		} else if ( id.equals("TAG")) {
		    p("TAGing...");
		    instance.tag(target);
		} else if ( id.equals("INIT")) {
		    p("INITing...");
		    instance.init(target);
		}
	    }
	    public ZOb newZOb(String id) {
		p("Sound.module.newZOb(" + id + ")");
		return null;
	    }
	};

    static String stream = "SNDSTR"; 
    static String file = "/tmp/soundscroll";

    static String d          = "d.sound";
    static String dtag       = "d.handle";
    static String dspan      = "d.sound-span";
    static String dsessions  = "d.sound-sessions";
    
    static long   duration = 2000000;
    static String mod = "Sound";
    
    static String[] ops = { //"INIT", 
	"PLAY",			    
	"RECORD",
	"STOP",
	"CUT",
	"TAG"
    };
    
    Recorder recorder = new SimpleRecorder(); 
    
    public Span getSpan(){
	return null;
    }
    
    public ZZCell getSpanAsCell(){
	return null;
    }
    
    //************************************************************************
    //*  private variables
    //************************************************************************
    
    private static Sound instance;
    
    static {
	instance = new Sound();
    }
    
    // current offset from the start of the session
    private long offset = 0; 

    // offset of the end of the last span
    private long lastOffset = 0;
    private long sessionStart = 0;
    
    // are we playing at the moment 
    private boolean playing = false;
    
    // are we recording at the moment 
    private boolean recording = false;

    private Player player = null;
    
    private ZZCell last = null;
    private ZZCell spanCell = null;
    private ZZCell sessionCell = null;
    
    // THESE SHOULD COME FROM STRUCTURE
    
    private SoundScroll soundScroll = null;
    // the session this stream belongs to
    private ZZCell session = null;
    // the input device associated with this stream.
    private ZZCell inDevice = null;
    // the input device associated with this stream.
    private ZZCell outDevice = null;

    private ZZCell offsetCell = null;
    
    public Sound(){
	//t.start();
	pa("CONSTRUCTOR!!!");
    }

    ZZCell scrollCell = null;
    
    private void init(ZZCell c){
	// TODO:
	// create a new window
	// with soundvie
	ZZDefaultSpace.addDimensionToDefaultDimlist(c, d);	
	ZZDefaultSpace.addDimensionToDefaultDimlist(c, dspan);	
	ZZDefaultSpace.addDimensionToDefaultDimlist(c, dsessions);	
	ZZDefaultSpace.addDimensionToDefaultDimlist(c, "d.handle");		
	//soundScroll = new SoundScroll(stream, file, 1000);
	//Scroll.register(stream, soundScroll);
	for(int i = 0; i < ops.length; i++){
	    c = c.N("d.2", 1);
	    c.setText(mod+"."+ops[i]);
	}
	addScroll(c);
	scrollCell = ZZDefaultSpace.findScrollCell(c.getHomeCell(), stream);
	soundScroll = (SoundScroll)Scroll.obtain( c.getSpace(), 
						  stream);	
	if( spanCell != null ) 
	    spanCell = scrollCell.N(dspan,1);
	
	p("SOUNDSCROLL = " + soundScroll);
    }
    
    private void addScroll(ZZCell c){
	if( c==null ) return;
	ZZCell home = c.getHomeCell();
	ZZCell d = home.findText("d.2", -1, "Scrolls");
	if( d == null ) {
	    d = home.N("d.2",-1); 
	    d.setText("Scrolls"); 
	}
	ZZCell x = d.findText("d.1", 1, stream);
	if( x != null ) return ;
	d = d.N("d.1",1);
	d.setText(stream);
	d = d.N("d.2",1);
	d.setText(file);
	d = d.N("d.2",1);
	d.setText("Sound");
	d = d.N("d.2",1);
	d.setText("1000000");
	d = d.N("d.2",1);
	d.setText("offset");
	d = d.N("d.1",1);
	d.setText("0");
	offsetCell = d;
    }
    
    
    //************************************************************************
    //*  public methods
    //************************************************************************
    /**
     * 
     */
    public void record(ZZCell session){
	if( playing || recording ) return;
	p("not recording or playing");
	// create a new scroll
	if( soundScroll == null ) 
	    soundScroll = (SoundScroll)Scroll.obtain( session.getSpace(), 
						      stream);	
 	//file = soundScroll.getFile(file);
	
	p("SOUNDSCROLL: " + soundScroll);
	
	if( scrollCell == null ) {
	    scrollCell = ZZDefaultSpace.findScrollCell(session.getHomeCell(), 
						       stream);
	}
	if( offsetCell == null) {
	    pa("SCRCELL = " + scrollCell);
	    offsetCell = scrollCell.findText("d.2", 1, "offset");
	    offsetCell = offsetCell.s("d.1",1);
	}
	if( spanCell ==null ){
	    spanCell = scrollCell.N(dspan,1);
	}
	if( sessionCell == null ){
	    sessionCell = scrollCell.h(dsessions, 1);
	    sessionCell = sessionCell.N(dsessions,1);
	}
	last = sessionCell;	    
	
	final long scOffset = Long.parseLong(offsetCell.getText());
	offset = scOffset;
	p("are we recording yet... " + recording);
	p("initializing");
	// XXX chech the offset 
	recorder.init("/tmp/soundscroll", scOffset);
	p("initalized...");
	Thread t = new Thread() {
		public void run() {
		    p("S REC Thread");
		    recorder.start();
		    offset = recorder.getCurrentOffset();
		    p("offset = " + offset);
		    offsetCell.setText(""+ offset);
		    Span s = Span.create(
					 Address.streamOffs(stream, 0),
					 Address.streamOffs(stream, offset)
					 );
		    spanCell.setSpan(s);
		    sessionCell.setSpan(s = Span.create(
				   Address.streamOffs(stream, scOffset),
				   Address.streamOffs(stream, offset)
				   ));					      
		    last.setSpan(s);
		    lastOffset = offset;
		}
	    };
	t.start();
	last = last.N(d,1);
	recording = true;
    }
    
    public void play(){
	play(0);
    }
    
    public void play(long off){
	if( ! (playing || recording )) ; // start to play
    }
    
    public void play(ZZCell from ){
	p("play " + from);
	
	Span s = from.getSpan();
	if( s == null ) {
	    pa("Not a span cell!!!");
	    return;
	}
	if( soundScroll == null ) 
	    soundScroll = (SoundScroll)s.getStart().getScroll(from.getSpace());
	// get these from factory???
	player = new SimplePlayer();
	File f = soundScroll.getFile();
	p("FILE = " + f);
	p("FILE = " + f.toString());
	player.init(f.toString());
	p("initialized...\n");
	final ZZCell fc = from;
	Thread t = new Thread() {
		public void run() {
		    p("PlayerThread");
		    player.play(fc.getSpan().getStart().getOffs(),
				fc.getSpan().getEnd().getOffs(), false);
		}
	    };
	t.start();
	playing = true;
	
    }
    
    public void play(ZZCell from, Dimension d, int dir ){
	
    }
    
    /**
     * Create a new span cell from last cut to this cut and connect
     * new cell to the stream cells.
     */
    public void cut(){
	offset = recorder.getCurrentOffset();
	p("offset = " + offset);
	Span s = Span.create(
			     Address.streamOffs(stream, lastOffset),
			     Address.streamOffs(stream, offset)
			     );
	p("SPAN = " +s );
	p("span.getText(): " + s.getString(last));
	
	last.setSpan(s);
	lastOffset = offset;
	last = last.N(d,1);
    }
    
    public void tag(ZZCell t){
	p( "target: " + t);
	last.insert(dtag,1, t);
    }
    
    
    public void pause(){
	if( playing ){
	    player.stop();
	    playing = false;
	    return ;
	}
	
	if( recording ){
	    player.stop();
	    recording = false;
	    return ;
	}
    }
    
    public void stop() { 
	if( playing ){
	    player.stop();
	    p("player stopped\n");
	    playing = false;
	    return ;
	}
	
	if( recording ){
	    p("stopping recorder...");
	    recorder.stop();
	    recording = false;
	    p("recorder stopped\n");
	    return ;
	}
    }
    
}
