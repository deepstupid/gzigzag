/*   
Evaluator.java
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
 * Written by Antti-Juhani Kaijanaho.
 */


package org.gzigzag.clang.thales.*;

import org.gzigzag.*;

public class Evaluator {

    private Task task;

    public Evaluator() { this(null); }
    public Evaluator(Task task) { this.task = task; }

    public Environment getCurrentEnvironment() { 
        return task.getCurrentEnvironment();
    }

    public void setCurrentEnvironment(Environment e) {
        task.setCurrentEnvironment(e);
    }

    public void request(SyntaxForm sf) {
        task.push(sf.getCell());
        sf.registerReference();
    }

    public void finish_eval(SyntaxForm sf) {
        task.remove(sf.getCell());
        sf.unregisterReference();
    }
    
    private final void eval_iter() {
        ZZCell c = task.peek();
        SyntaxForm sf = SyntaxForm.rt_instantiate(c, this);
        sf.eval_iteration();
    }

    public void eval_iteration() { eval_iter(); }

    public void eval() { while (!task.empty()) eval_iter(); }

}
