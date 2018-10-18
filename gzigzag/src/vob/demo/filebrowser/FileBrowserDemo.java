/*   
FileBrowserDemo.java
 *    
 *    Copyright (c) 2001, Benja Fallenstein
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
 * Written by Benja Fallenstein
 */
package org.gzigzag.vob.demo;
import org.gzigzag.vob.*;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.*;

/** A demo showing some files from the filesystem as vobs.
 */

public class FileBrowserDemo {
    final JFileChooser fc = new JFileChooser();
    final javax.swing.plaf.basic.BasicFileChooserUI ui =
	    new javax.swing.plaf.basic.BasicFileChooserUI(fc);
    final FileView fw = ui.getFileView(fc);
    {
	ui.installUI(fc);
	if(fw == null)
	    throw new Error("Could not instantiate FileView");
    }

    public class DemoFileVob extends FileVob 
			     implements SimpleConnector.CenteredVob {
		
	Icon icon;
	String name;
	TextStyle style;

	/** Whether we layout in rows. */
	boolean rows;
	
	/** Width and height of the icon. */
	int iconw, iconh;
		
	/** Space between icon and text. */
	final int space = 5;
	
	public DemoFileVob(File f, TextStyle style, boolean rows) {
	    super(f);
	    icon = fw.getIcon(f);
	    iconw = icon.getIconWidth();
	    iconh = icon.getIconHeight();
	    name = f.getName();
	    this.style = style;
	    this.rows = rows;
	}
	
	public int getWidth() {
	    if(!rows)
		return iconw + space + style.getWidth(name, 1000);
	    else {
		return 80;
		//		int textw = style.getWidth(name, 1000);
		//if(textw > iconw) return textw;
		//else return iconw;
	    }
	}
	
	public int getHeight() {
	    if(!rows) {
		int texth = style.getHeight(name, 1000);
		if(texth > iconh) return texth;
		else return iconh;
	    } else
		return iconh + space + style.getHeight(name, 1000);
	}
	
	public void getCenter(Vob.Coords coords, Point writeInto) {
	    if(!rows) {
		writeInto.x = coords.x + iconw/2;
		writeInto.y = coords.y + iconh/2;
	    } else {
		writeInto.x = coords.x + getWidth() / 2;
		writeInto.y = coords.y + iconh/2;
	    }
	}
	
	public void render(Graphics g, 
			   int x, int y, int w, int h,
			   boolean boxDrawn,
			   RenderInfo info) {
	    Color oldfg = g.getColor();
	    if(!rows)
		icon.paintIcon(frame, g, x, y + (h/2) - (iconh/2));
	    else
		icon.paintIcon(frame, g, x + (w/2) - (iconw/2), y);
	    g.setColor(oldfg);
	    if(!rows)
		style.render(g, name, 1000, x + iconw + space, y,
			     w - iconw - space, h, boxDrawn, info);
	    else {
		int textw = style.getWidth(name, 1000);
		style.render(g, name, 1000, x + w/2 - textw/2, 
			     y + iconh + space,
			     w, h - iconh - space, boxDrawn, info);
	    }
	}
    }

    public class DemoFileVobFactory extends FileVobFactory {
	TextStyle style;
	{
	    ScalableFont font = new ScalableFont("sans-serif", Font.PLAIN, 10);
	    style = new RawTextStyle(font, Color.black);
	}

	boolean rows;
	public DemoFileVobFactory(boolean rows) { this.rows = rows; }
	
	public FileVob create(File f) {
	    return new DemoFileVob(f, style, rows);
	}
    }

    public class FileCursor extends MouseAdapter {
	public File dir;
	public FileCursor(File dir) { this.dir = dir; }
	
	public void mouseClicked(MouseEvent e) {
	    if(e.getClickCount() < 2) return;

	    VobPanel.Area a = (VobPanel.Area)e.getSource();
	    Point p = a.inScene(), q = e.getPoint();
	    p.x += q.x; p.y += q.y;
	    
	    Vob v = a.enclosing.scene.getVobAt(p.x, p.y);
	    if(v != null && v.key instanceof File) {
		File f = (File)v.key;
		// if(!f.isDirectory()) return; // XXX open the file somehow!
		this.dir = f;
		a.enclosing.rebuild();
		a.enclosing.repaint();
	    }
	}
    }

    public class FileArea extends VobPanel.Area {
	FileBrowserView view;
	FileCursor curs;
	FileVobFactory fact;
	FileSorter sorter;
	
	Action[] actions = new Action[] {
	    new AbstractAction("Directory up") {
		public void actionPerformed(ActionEvent e) {
		    if(curs.dir.getParent() != null) {
			curs.dir = new File(curs.dir.getParent());
			enclosing.rebuild();
			enclosing.repaint();
		    }
		}
	    },
	    null,
	    new AbstractAction("Sort by name") {
		public void actionPerformed(ActionEvent e) {
		    sorter = new FileSorter();
		    enclosing.rebuild();
		}
	    }, new AbstractAction("Sort by date") {
		public void actionPerformed(ActionEvent e) {
		    sorter = new FileSorter.DateSorter();
		    enclosing.rebuild();
		}
	    }, new AbstractAction("Sort by size") {
		public void actionPerformed(ActionEvent e) {
		    sorter = new FileSorter.SizeSorter();
		    enclosing.rebuild();
		}
	    },
	    null,
	    new AbstractAction("Row view") {
		public void actionPerformed(ActionEvent e) {
		    view = new ContentView(new GridView(false));
		    fact = new DemoFileVobFactory(true);
		    enclosing.rebuild();
		}
	    }, new AbstractAction("Column view") {
		public void actionPerformed(ActionEvent e) {
		    view = new ContentView(new GridView(true));
		    fact = new DemoFileVobFactory(false);
		    enclosing.rebuild();
		}
	    },
	};
	
	public FileArea(VobPanel enclosing, FileBrowserView view, 
			FileCursor curs, FileSorter sorter,
			FileVobFactory fact) {
	    super(enclosing);
	    addMouseListener(curs);
	    this.view = view; this.curs = curs; this.sorter = sorter;
	    this.fact = fact;
	}
	
	public void paint(Graphics g) {
	    // if we are in a toolbar, we should clean our background
	    Dimension size = getSize();
	    g.setColor(enclosing.getBackground());
	    g.fillRect(0, 0, size.width, size.height);
	}
	
	public void buildView(VobPlacer placer) {
	    view.build(placer, curs.dir, fact, sorter);
	}
    }

    /** A panel containing a toolbar and a FileArea */
    public class FilePanel extends JPanel {
	FileArea area; JToolBar toolbar;
	FileCursor curs;
	
	public FilePanel(VobPanel enclosing, String name, FileBrowserView view, 
			 File root, FileSorter sorter) {
	    setBackground(Color.white);
	    setLayout(new BorderLayout());
	
	    curs = new FileCursor(root);

	    area = new FileArea(enclosing, view, curs, sorter, new DemoFileVobFactory(true));
	    toolbar = new JToolBar();
	    toolbar.setFloatable(false);
	
	    toolbar.add(new JLabel("Directory: "));
	    toolbar.add(new FileArea(enclosing, new SingleFileView(),
				     curs, null, new DemoFileVobFactory(false)));
	
	    add(area, "Center");
	    add(toolbar, "North");
	
	    JMenu menu = new JMenu(name);

	    for(int i=0; i<area.actions.length; i++) {
		if(area.actions[i] != null)
		    menu.add(area.actions[i]);
		else
		    menu.addSeparator();
	    }
	    frame.getJMenuBar().add(menu);
	}
    }

    JFrame frame = new JFrame();
    {
	frame.setBackground(Color.white);
	frame.setJMenuBar(new JMenuBar());
    }

    FileBrowserDemo(String ldirectory, String rdirectory) {
	File lroot = new File(ldirectory),
	     rroot = new File(rdirectory);
	FileBrowserView lview = new ContentView(new GridView(false)),
			rview = new ContentView(new GridView(false));
	FileSorter lsorter = new FileSorter(),
		   rsorter = new FileSorter.DateSorter();

	JMenu filemenu = new JMenu("File");
	frame.getJMenuBar().add(filemenu);
	filemenu.add(new AbstractAction("Exit") {
	    public void actionPerformed(ActionEvent e) {
		System.exit(0);
	    }
	});

        final VobPanel vp = new VobPanel();

	JMenu viewmenu = new JMenu("View settings");
	frame.getJMenuBar().add(viewmenu);
	viewmenu.add(new AbstractAction("Show connections") {
		public void actionPerformed(ActionEvent e) {
		    vp.showConns = true;
		    vp.repaint();
		}
	    });
        viewmenu.add(new AbstractAction("Don't show connections") {
                public void actionPerformed(ActionEvent e) {
                    vp.showConns = false;
                    vp.repaint();
                }
            });
	viewmenu.addSeparator();
        viewmenu.add(new AbstractAction("Show animations") {
                public void actionPerformed(ActionEvent e) {
                    vp.showAnim = true;
                    vp.repaint();
                }
            });
        viewmenu.add(new AbstractAction("Don't show animations") {
                public void actionPerformed(ActionEvent e) {
                    vp.showAnim = false;
                    vp.repaint();
                }
            });


	Component left =  new FilePanel(vp, "Left pane", lview, lroot, lsorter),
	    right = new FilePanel(vp, "Right pane", rview, rroot, rsorter);

	vp.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
			       false, left, right));

	ImageVob.comp = frame;

	frame.getContentPane().add(vp);
	frame.setSize(600, 400);
        frame.setVisible(true);
    }

    static public void main(String argv[]) throws IOException {
	String ldirectory = "/", rdirectory = "/";
	if(argv.length > 0) { ldirectory = argv[0]; rdirectory = argv[0]; }
	if(argv.length > 1) ldirectory = argv[1];
	ldirectory = new File(ldirectory).getCanonicalPath();
	rdirectory = new File(rdirectory).getCanonicalPath();
	new FileBrowserDemo(ldirectory, rdirectory);
    }
}







