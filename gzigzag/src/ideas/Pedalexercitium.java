/*   
PedalExercitium.java
 *    
 *    Copyright (c) 2001, Tuomas Lukka
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
 * Written by Tuomas Lukka
 */
package org.gzigzag.ideas;
import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import org.gzigzag.impl.*;

/** The art of analog controllers.
 * Test various ways of using a joystick (and wheel and pedals!)
 * to control things.
 * The name comes from Bach's BWV XXX, an organ piece for pedals solo.
 * <p>
 * This program <b>will</b> consume all available computer time...
 */

public class Pedalexercitium {
    public static boolean dbg = true;
    private static final void p(String s) { if(dbg) pa(s); }
    private static final void pa(String s) { System.err.println(s); }

    JoystickState s; // XXX Change name
    public abstract class Tema {
	public void reset() { }
	public void react() { }
	public abstract void draw(Graphics g, int w, int h);

	/** Return the name of this tema.
	 * The reason for this method is that we may want to parametrize
	 * some temas.
	 */
	public String name() {
	    String n = this.getClass().getName();
	    n = n.substring(n.lastIndexOf("$") + 5); // remove tema;
	    return n;
	}

	public abstract String description();
    }

    Tema[] temas = new Tema[] {
	new TemaValue(),
	new Tema2D1Rot(),
	new TemaIntegrate2D(),
	new Tema2DGridZoom()
    };
    Button[] buttons =new Button[temas.length];
    int currentTema = 0;

    /** Change the current tema to the given index.
     */
    void setTema(int t) {
	currentTema = t;
	for(int i=0; i<buttons.length; i++) {
	    buttons[i].setBackground(
		    i == t ?
			Color.cyan : drawnComponent.getBackground());
	    // XXX Work around bug in kaffe
	    buttons[i].repaint();
	}
	temas[currentTema].reset();
	if(temas[currentTema] instanceof JoystickListener)
	    joystick.setListener((JoystickListener)temas[currentTema]);
	textarea.setText(temas[currentTema].description());
    }

    ActionListener action = new ActionListener() {
	 public void actionPerformed(ActionEvent e) {
	     setTema(Integer.parseInt(e.getActionCommand()));
	 }
    };

    LinuxJoystick joystick;
    Frame frame = new Frame();
    Panel panel = new Panel();
    TextArea textarea = new TextArea();
    Component drawnComponent = new Component() {};

    public Pedalexercitium(String[] argv) {
	joystick = new LinuxJoystick(new File(argv[0]));
	s = joystick.getState();

	for(int i=0; i<temas.length; i++) {
	    buttons[i] = new Button(temas[i].name());
	    buttons[i].setActionCommand(""+i);
	    buttons[i].addActionListener(action);
	    panel.add(buttons[i]);
	}

	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	frame.setLayout(gridbag);

	frame.add(drawnComponent);
	c.gridx = 0; c.gridy = 0;
	c.weightx = 1; c.weighty = 1;
	c.fill=c.BOTH;
	gridbag.setConstraints(drawnComponent, c);

	frame.add(panel);
	c.gridx = 0; c.gridy = 1;
	c.weightx = c.weighty = 0;
	c.fill=c.HORIZONTAL;
	gridbag.setConstraints(panel, c);

	textarea.setEditable(false);
	textarea.setFont(new Font("SansSerif", Font.PLAIN, 12));
	textarea.setForeground(Color.black);
	frame.add(textarea);
	c.gridx = 0; c.gridy = 2;
	c.weighty = 0;
	c.fill=c.BOTH;
	gridbag.setConstraints(textarea, c);

	frame.setSize(600, 600);
	frame.show();

	setTema(0);

	runner.start();
	panel.setBackground(drawnComponent.getBackground().darker());
    }

    /** The thread which continuously updates the screen.
     */
    Thread runner = new Thread() {
	public void run() {
	    Image img = null;
	    while(true) {
		Dimension d = drawnComponent.getSize();
		int w = d.width, h = d.height;
		if(img == null || img.getWidth(null) != w ||
		    img.getHeight(null) != h)
			img = drawnComponent.createImage(w, h);

		Graphics g = img.getGraphics();
		g.setColor(drawnComponent.getBackground());
		g.fillRect(0, 0, w, h);
		temas[currentTema].draw(img.getGraphics(), 
			    w, h);;
		drawnComponent.getGraphics().drawImage(img, 0, 0, null);
		drawnComponent.getToolkit().sync();
		Thread.yield();
	    }
	}
    };

    int[] xs = new int[] {0, -10, 10};
    int[] ys = new int[] {0, -20, -20};

    void xmarker(Graphics g, int x, int y) {
	g.translate(x, y);
	g.setColor(Color.red);
	g.fillPolygon(xs, ys, xs.length);
	g.setColor(Color.black);
	g.drawPolygon(xs, ys, xs.length);
	g.translate(-x, -y);
    }

    void xaxis(Graphics g, int y, int w) {
	g.translate(0, y);
	g.setColor(Color.black);
	g.drawLine(0, 0, w, 0);
	g.drawLine(w/2, -10, w/2, +10);
	g.translate(0, -y);
    }

    void cross(Graphics g, int x, int y, double angle) {
	g.setColor(Color.black);
	int d = 20;
	int es = 3;
	double dx = -d*Math.sin(angle);
	double dy = d*Math.cos(angle);
	g.setColor(Color.red);
	g.fillOval(x-es, y-es, es*2, es*2);
	g.setColor(Color.black);
	g.drawOval(x-es, y-es, es*2, es*2);
	g.drawLine(x, y, (int)(x+dx), (int)(y+dy));
    }

    public class TemaValue extends Tema {
	public void draw(Graphics g, int w, int h) {
	    int naxes = s.axes.length;
	    for(int i=0; i<naxes; i++) {
		int y = h / (naxes + 4) * (i + 2);
		int x = (int)((s.axes[i] + 1) / 2 * w);
		xaxis(g, y, w);
		xmarker(g, x, y);
	    }
	}
	public String description() {
	    return 
"Shows the input values directly.";
	}
    }

    public class Tema2D1Rot extends Tema implements JoystickListener {
	float[] val = new float[3];  // 0..1, 0..1, 0..1 (0..360)
	public void button(long ms, int button, boolean value) { }
	public void axis(long ms, int axis, float value) {
	    if(axis < 3)
		val[axis] = (value + 1) / 2;
	}
	public void draw(Graphics g, int w, int h) {
	    cross(g, (int)(val[0] * w), (int)(val[1] * h), 
			val[2] * 2 * Math.PI);
	}
	public void reset() {
	    val[0] = val[1] = val[2] = 0.5f;
	}
	public String description() {
	    return 
"Shows the two first axes as the x and y coordinate and the third axis\n"+
"as the heading.";
	}
    }

    interface Integrator {
	void change(long ms, float input);
	void update(long ms);
	void reset();
	void reset(float value);
	float getValue();
	float getValue(float clampMin, float clampMax);
    }
    static abstract public class SimpleIntegrator implements Integrator {
	protected float value;
	protected float prevInput;
	protected long prevTime;
	public SimpleIntegrator() { reset(); }
	public void change(long ms, float input) {
	    prevInput = input;
	    prevTime = ms;
	}
	public void update(long ms) {
	    change(ms, prevInput);
	}
	public void reset() {
	    value = 0;
	    prevTime = System.currentTimeMillis();
	    prevInput = 0;
	}
	public void reset(float value) {
	    reset();
	    this.value = value;
	}
	public float getValue() {
	    return value;
	}
	public float getValue(float clampMin, float clampMax) {
	    if(value < clampMin)
		value = clampMin;
	    else if(value > clampMax)
		value = clampMax;
	    return value;
	}
    }
    /** Integrating an input from the joystick w.r.t.time.
     */
    static public class DirectIntegrator extends SimpleIntegrator {
	public void change(long ms, float input) {
	    double secs = (ms - prevTime) / 1000.0;
	    value += prevInput * secs;
	    super.change(ms, input);
	}
    }

    /** Integrating an input, but allowing only integer outputs.
     * This is not easy ;)
     */
    static public class IntegerIntegrator extends SimpleIntegrator {
	/** The fractional part of the current value.
	 */
	float accumulation;
	public void reset() {
	    accumulation = 0;
	    super.reset();
	}
	public void change(long ms, float input) {
	    double secs = (ms - prevTime) / 1000.0;
	    accumulation += prevInput * secs;
	    while(accumulation > 0.6) { accumulation --; value ++; }
	    while(accumulation < -0.6) { accumulation ++; value --; }
	    super.change(ms, input);
	}
    }

    public class TemaIntegrate2D extends Tema2D1Rot {
	Integrator[] ints;
	public TemaIntegrate2D() {
	    createIntegrators();
	}
	public void createIntegrators() {
	    ints = new Integrator[3];
	    for(int i=0; i<ints.length; i++)
		ints[i] = new DirectIntegrator();
	}
	public void button(long ms, int button, boolean value) { }
	public void axis(long ms, int axis, float value) {
	    if(axis >= ints.length) return;
	    ints[axis].change(ms, value);
	}
	/** Set the vals in Tema2D1Rot.
	 */
	public void setVals() {
	    long ms = System.currentTimeMillis();
	    for(int i=0; i<ints.length; i++) {
		ints[i].update(ms); 
		val[i] = ints[i].getValue(0, 1);
	    }
	}
	public void draw(Graphics g, int w, int h) {
	    setVals();
	    super.draw(g, w, h);
	}
	public void reset() {
	    super.reset();
	    for(int i=0; i<ints.length; i++) ints[i].reset();
	}
	public String description() {
	    return 
"Controls velocity and angular momentum using the three first axes.";
	}
    }

    public class Tema2DGridZoom extends TemaIntegrate2D {
	public void createIntegrators() {
	    ints = new Integrator[3];
	    for(int i=0; i<ints.length; i++)
		ints[i] = new IntegerIntegrator();
	}
	public void setVals() {
	    long ms = System.currentTimeMillis();
	    for(int i=0; i<ints.length; i++) {
		ints[i].update(ms); 
		val[i] = (ints[i].getValue(0, 6) / 6); 
	    }
	}

	public String description() {
	    return 
"A discrete grid, zoom with axis 3 XXX and walk along the grid with 0/1.\n"+
"The cursor stays at the center, this time. Getting closer to zz.\n"+
"The interesting problem here is that the stable state is simply\n"+
"the intersection, but on a small timescale there is other state,\n"+
"related to WHEN we came into this intersection and from what direction."
;
	}
    }

    static public void main(String[] argv) {
	Pedalexercitium p = new Pedalexercitium(argv);
    }
}
