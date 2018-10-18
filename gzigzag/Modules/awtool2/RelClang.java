

public class RelClang {

    static String rel_args = "d.rel_args";

    static private Hashtable AllPrimitives = new Hashtable();

    static {
	AllPrimitives.put("sum", (Object)new Sum());
    }

    private class BalanceTrigger implements ZZObs {
	private ZZCell c;
	private String cText;
	public void chg() {
	    if(!c.t().equals(cText)) return;
	    cText = c.t();
	    uniqueKey = getUniqueKey(c);
	    balanceEquations(c, uniqueKey);
	}
	public void init(ZZCell c) {
	    this.c = c;
	    cText = c.t();
	}
    }

    static public void balanceRelations(ZZCell rootvar, String changeKey) {
	ZZCell relroot, c;
	ZZCell[] modified;
	int solvable, n=0;
	touch(rootvar, changeKey);
	for(c=rootvar.s("d.clone"); c!=null; c=c.s("d.clone")) {
	    relroot = c.h(rel_args);
	    notBalanced[0].addElement(relroot);
	    n ++;
	}
	int level = 0;
	while(n > 0) {
	    while(notBalanced[level].size() == 0) {
		level++;
		if(level > 4) level = 0;
	    }
	    solvable = -1; 
	    // balance function changes 'solvable'
	    to_be_balanced = balance(relroot, solvable);
	    solvable = solvable > 4 ? 4 : solvable;
	    if(solvable == -1) throw ZZError("Couldn't solve!!");
	    if(solvable == 0) {
		// root relation is in balance
		n--;
		for(i=0; i<to_be_balanced.lenght; i++) {
		    tbb = to_be_balanced[i];
		    notBalanced[4].addElement(tbb);
		    n++;
		}
	    }
	    else {
		notBalanced[solvable].addElement(relroot);
	    }
	}
    }
    private ZZCell[] balance(ZZCell rel, int retval) {
	String op;
	opName = rel.t();
	rp = (RelPrimitive)AllPrimitives.get((Object)opName);
	Data data = new Data(rel.s(rel_args));
	return rp.solve(data);
    } 

    private void touch(ZZCell c, String uniqueKey) {
        c.getOrNewCell(rel_fingerprint).setText(uniqueKey);
    }

    private boolean isTouched(ZZCell c, String uniqueKey) {
        if(c.getOrNewCell(rel_fingerprint).t().equals(uniqueKey)) 
            return true;
        return false;
    }



    public class Sum(Data dt) {
        for(int i=dt.len-1; i>=0; i--) {
            if(isTouched(dt.c())) touched++;
            else notTouched = i;
        }
        if(touched==dt.len-1) {
            
        }
    }  

				   
} 
