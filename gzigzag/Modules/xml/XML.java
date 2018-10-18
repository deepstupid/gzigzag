/*   
XML.java
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
 * Written by Tuukka Hastrup
 * Very heavily modified by Tuomas Lukka
 */

/** An XML importer/exporter.
 * <p>
 * Export: <ul>
 * <li> with data view cursor, point to a cell containing the filename
 * <li> running the action <b>XML.EXPORT</b> will now export the whole space
 * </ul>
 *<p>
 * Import: <ul>
 * <li> with data view cursor, point to a cell containing the filename
 * <li> if there's a cell from that cell poswards on <d>d.1</b>, this cell
 * 	will be used as a root for the imported space, otherwise a new
 *	cell is created there
 * <li> running the action <b>XML.IMPORT</b> will now import the whole space.
 * XXX <string>Notice</string> that if there was a connection from home cell 
 * negwards on d.1 to be imported, it's not there because that's where 
 * the filename cell is!
 */
 
package org.gzigzag.module;

import org.gzigzag.*;
import java.io.*;
import java.net.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
//import org.openxml.x3p.*;
import org.apache.xml.serialize.*;
import java.util.*;

import java.awt.Point;

public class XML {
public static final String rcsid = "$Id: XML.java,v 1.11 2000/11/03 08:01:05 ajk Exp $";

	public static boolean dbg = true;
	static final void p(String s) { if(dbg) System.out.println(s); }
    	static final void pa(String s) { System.out.println(s); }
	
	static public XMLReader getXMLReader() {
	    return new org.apache.xerces.parsers.SAXParser();
	}
	static public Document getDocument() {
	    return new org.apache.xerces.dom.DOMImplementationImpl().
		    createDocument(null, "ZZSpace", null);
	}
	static public XMLSerializer getXMLSerializer(Writer writ) {
           return getXMLSerializer(writ, "UTF-8");
        }
        static public XMLSerializer getXMLSerializer(Writer writ, String enc) {
	    return new org.apache.xml.serialize.XMLSerializer(writ, 
			new org.apache.xml.serialize.OutputFormat("doc",
				    enc, true));
	}

	ZZSpace zz;

	static public ZZModule module = new ZZModule() {
	    public void action(String id, ZZCell code, 
	                       ZZCell target, 
			       ZZView v, ZZView cv,
			       String key, Point pt, 
			       ZZScene xi) {
	        if(id.equals("EXPORT")) {
		    synchronized(code.getSpace()) {
	                (new XML()).export(new File(target.getText()), code.getSpace());
		    }
		} else if(id.equals("IMPORT")) {
		    (new XML()).load(new File(target.getText()), target.getOrNewCell("d.1", 1)); 
		}
	    }
	};

	AttributesImpl ats(String[] a) {
	    AttributesImpl ai = new AttributesImpl();
	    for(int i=0; i<a.length-1; i+=2) {
		ai.addAttribute(null, a[i], a[i], null, a[i+1]);
	    }
	    return ai;
	}
	AttributesImpl ats(String a, String v) {
	    return ats(new String[] {a,v});
	}
	AttributesImpl ats(String a, String v, String a2, String v2) {
	    return ats(new String[] {a,v, a2, v2});
	}

	
    public void export(File out, ZZSpace space) { export(out, space, "UTF-8"); }

       public void export(File out, ZZSpace space, String encoding) {
		zz = space;
		

		try {
		    XMLSerializer xser = getXMLSerializer(new FileWriter(out), encoding);
		    xser.setOutputCharStream(new FileWriter(out));
		    org.xml.sax.ContentHandler ser = xser.asContentHandler();
		    ser.startDocument();
		    ZZCell home = zz.getHomeCell();
		    ser.startElement(null, "ZZSpace", "ZZSpace", 
			    ats("homeid", home.getID()));
	    
		    appendCells(ser);
//XXX Should be done		appendScrolls(); 
		    appendDims(ser);

		    ser.endElement(null, "ZZSpace", "ZZSpace");

		    ser.endDocument();


		} catch (Exception e) {
			ZZLogger.exc(e);
		}
	}
	
	public void appendCells(org.xml.sax.ContentHandler ser) 
		    throws SAXException {
	        for(Enumeration e = zz.cells();e.hasMoreElements();) {
		    ZZCell c = (ZZCell) e.nextElement();
		    String id = c.getID();
		    Span span = c.getSpan();
		    String content;

		    String dt;
		    if(span != null) {
		        dt="SPAN";
		        content = span.toString();
		    } else {
		        dt="TEXT";
		        content = c.getText();           // XXX clones!
		    }

		    ser.startElement(null, "CELL", "CELL", 
			ats("data", dt, "id", id));
		    ser.characters(content.toCharArray(), 0, content.length());
		    ser.endElement(null, "CELL", "CELL");
	        }
	}

	public void appendScrolls(Document doc, Element docElem) {
		StringScroll scr = zz.getStringScroll();
		Element n = doc.createElement("SCROLL");
		n.setAttribute("id", scr.getId());
		n.setAttribute("type", "STRING");
		n.appendChild(doc.createTextNode(scr.getString(0, (int)scr.curEnd())));
		docElem.appendChild(n);
	}

	public void appendDims(org.xml.sax.ContentHandler ser) 
		    throws SAXException {
	    String[] dims = zz.dims();
	    for(int n = 0;n < dims.length;n++) {
		if(!dims[n].equals("d.cellcreation") &&  // XXX non-persistents!
		   !dims[n].equals("d.masterdim")) {

		    ser.startElement(null, "DIMENSION", "DIMENSION",
			ats("name", dims[n]));
		    for(Enumeration e = zz.posconns(dims[n]);e.hasMoreElements();) {
		        ZZConnection c = (ZZConnection) e.nextElement();
			ser.startElement(null, "CONN", "CONN", 
			    ats("neg", c.c1.getID(), "pos", c.c2.getID()));
			ser.endElement(null, "CONN", "CONN");
		    }
		    ser.endElement(null, "DIMENSION", "DIMENSION");
		}
	    }
	}

	public void load(File in, final ZZCell nhome) {
	    zz = nhome.getSpace();
	    XMLReader p = getXMLReader();

	    p.setContentHandler(new DefaultHandler() {
		Hashtable id2cell = new Hashtable();
		String curdim;
		String curcontent;
		String ctype;
		ZZCell cell;
		ZZCell getCell(String id) {
		    ZZCell c = (ZZCell)id2cell.get(id);
		    if(c == null) 
			throw new ZZError("Can't find cell "+id+" in import");
		    return c;
		}
		public void startElement(String uri, String nsname,
			String name, Attributes attrs) {
		    if(name.equals("CELL")) {
			curcontent = "";
			ctype = attrs.getValue("data");
			if(attrs.getValue("id").equals("1")) {
			    cell = nhome;
			} else {
			    cell = nhome.N();
			}
			id2cell.put(attrs.getValue("id"), cell);
			return;
		    } else {
			curcontent = null;
		    }
		    if(name.equals("DIMENSION"))
			curdim = attrs.getValue("name");
		    else if(name.equals("CONN")) {
			getCell(attrs.getValue("neg")).
			    connect(curdim, 
				getCell(attrs.getValue("pos")));
		    }
		}
		public void endElement(String uri, String local, String name) {
		    if(!ctype.equals("TEXT"))
			throw new ZZError("No referential XML text yet");
		    if(curcontent != null)
			cell.setText(curcontent);
		}
		public void characters(char[] ch, int start, int len) {
		    if(curcontent == null) return;
		    curcontent = curcontent + new String(ch, start, len);
		}
	    });

	    try {
	    	p.parse(new InputSource(new FileInputStream(in)));
	    } catch (Exception e) {
		ZZLogger.exc(e);
		throw new ZZError("Error in XML import!");
	    };

/*
		    ZZCell c;
		    if(e.getAttribute("id").equals("1")) {
			p("Got home cell");
			c = nhome; // We're substituting cell #1
		    } else
		        c = nhome.N();
*/
	/*
		    if(type.equals("TEXT")) {
		    	c.setText(content);
		    } else if(type.equals("SPAN")) {
		        c.setSpan(Span.parse(content));
		    }
	*/
	/*
		 else if(tag.equals("SCROLL"))  {
		    Scroll scr = Scroll.obtain(zz, e.getAttribute("id"));
		    if(e.getAttribute("type").equals("STRING")) {
		    	((StringScroll)scr).append(((Text)e.getFirstChild()).getData());
		    }
		}
	*/
	}
}

