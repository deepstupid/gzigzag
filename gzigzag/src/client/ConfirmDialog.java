/*
ConfirmDialog.java
 *
 *	You may use and distribute under the terms of either the GNU Lesser
 *	General Public License, either version 2 of the license or,
 *	at your choice, any later version. Alternatively, you may use and
 *	distribute under the terms of the XPL.
 *
 *	See the LICENSE.lgpl and LICENSE.xpl files for the specific terms of
 *	the licenses.
 *
 *	This software is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the README
 *	file for more details.
 *
 */
/* Written by Tero Mäyränen */

package org.gzigzag.client;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import org.gzigzag.util.*;

/**
 *	The ConfirmDialog is a simple awt-dialog that displays a question and
 *	two possible choices, "Yes" and "No".  This is something of a breakaway
 *	from the ZigZag-paradigm into "PUI", so it is not recommended to use this
 *	anywhere.  The reason it is here is to make it possible to have another
 *	nasty feature, which is the "system" primitive in clasm.  This dialog
 *	is used to confirm all "system" actions with the user first.
 */

public class ConfirmDialog extends Dialog {
public static final String rcsid = "$Id: ConfirmDialog.java,v 1.1 2002/02/25 13:07:03 deetsay Exp $";

    private Button yes = new Button("Yes");
    private Button no = new Button("No");
    private boolean result = false;
    private Panel textPanel = new Panel();
    private ConfirmDialog dlg = this;

    /**
     *	Construct the dialog, with a textpanel and the 2 buttons.
     */

    public ConfirmDialog() {

	super(new Frame(), "GZigZag Confirm Dialog", true);
	textPanel.setLayout(new FlowLayout());
	this.add("Center", textPanel);
	Panel p = new Panel();
	p.setLayout(new FlowLayout());
	yes.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent ae) {
		result = true;
		dlg.hide();
	    }
	});
	p.add(yes);
	no.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent ae) {
		result = false;
		dlg.hide();
	    }
	});
	p.add(no);
	this.add("South", p);
	this.setSize(400, 200);
    }

    /**
     *	Show the question and the dialog window, and return true if the
     *	user replied "Yes", and false if it was "No".
     *
     *	@param question is what you want to confirm with the user.
     */

    public boolean confirm(String question) {
	result = false;
	textPanel.removeAll();

	Vector strings = new Vector();
	int i1 = 0;
	int i2 = question.indexOf("\n");
	while (i2 > 0) {
	    if (i1 == i2) strings.add(""); else strings.add(question.substring(i1, i2));
	    i1 = i2 + 1;
	    i2 = question.indexOf("\n", i1);
	}
	strings.add(question.substring(i1));

	for (Enumeration e = strings.elements(); e.hasMoreElements();) {
	    textPanel.add("North", new Label((String)e.nextElement()));
	}
	this.show();
	return result;
    }
}
