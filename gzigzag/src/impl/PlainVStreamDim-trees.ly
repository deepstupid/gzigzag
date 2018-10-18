Title: A tree structure for the PlainVStreamDim cache
Rcs-Id: $Id: PlainVStreamDim-trees.ly,v 1.1 2002/03/18 01:51:54 bfallenstein Exp $

For the PlainVStreamDim cache, we need a tree of intervals...

-- PlainVStreamDim inner classes:
	protected final class Interval {
		Cell negend, posend;

		Interval neg, pos;

		int cut = -1;

		-- class Interval.
	}


-- class Interval:
	protected Interval(Cell negend, Cell posend) {
		this.negend = negend;
		this.posend = posend;

		this.neg = null;
		this.pos = null;
		
		cut = -1;
	}

	protected Interval(Interval i, Interval j) {
		Interval neg, pos;

		if(i.posend.inclusionIndex < j.posend.inclusionIndex) {
			neg = i;
			pos = j;
		} else {
			neg = j;
			pos = i;
		}

		if(neg.posend.inclusionIndex + 1 != pos.negend.inclusionIndex)
			throw new IllegalArgumentException("cannot join "+
							   "intervals");

		this.neg = neg;
		this.pos = pos;

		this.negend = neg.negend;
		this.posend = pos.posend;

		cut = pos.negend.inclusionIndex;
	}

	public int side(Cell c) {
		if(c.inclusionIndex >= cut)
			return 1;
		else
			return -1;
	}
	
	public Cell getRangeEnd(Cell c, int dir) {
		if(cut >= 0) {
			if(c.inclusionIndex >= cut)
				return pos.getRangeEnd(c, dir);
			else
				return neg.getRangeEnd(c, dir);
		} else {
			if(dir > 0)
				return posend;
			else
				return negend;
		}
	}

	public void cut(Cell negside, Cell posside) {
		if(cut < 0) {
			neg = new Interval(negend, negside);
			pos = new Interval(posside, posend);
			cut = posside.inclusionIndex;
		} else {
			if(posside.inclusionIndex > cut)
				pos.cut(negside, posside);
			else if(posside.inclusionIndex == cut)
				// same as this!
				//return;
				throw new Error("oops");
			else
				neg.cut(negside, posside);
		}
	}

	public void join(int at) {
		if(cut < 0) {
			((Interval)cache.get(negend.inclusionObject)).dump("");
			throw new ZZError("Cannot join: interval wasn't cut: "+
					  at+" @ "+negend+" "+posend+
	"; conn: "+s(negend.spacepart.getCell(negend.inclusionObject, at),-1)+" / "+cn.get(negend.spacepart.getCell(negend.inclusionObject, at))+" / "+cn.get(negend)+"; cut: "+cut);
		}
		else if(at < cut)
			neg.join(at);
		else if(at > cut)
			pos.join(at);
		else {
			// at == cut: join *this* interval

			// XXX this kludges HARD!

			if(neg.cut < 0 && pos.cut < 0) {
				//System.out.println("unfoo");
				neg = null;
				pos = null;
				cut = -1;
			} else if(neg.cut < 0) {
				//System.out.println("foon");
				int c = cut;
				cut = pos.cut;
				neg = new Interval(neg, pos.neg);
				neg.join(c);
				pos = pos.pos;
			} else if(pos.cut < 0) {
				//dump("");
				//System.out.println("foop");
				int c = cut;
				cut = neg.cut;
				pos = new Interval(neg.pos, pos);
				pos.join(c);
				neg = neg.neg;
				//dump("");
			} else {
				//System.out.println("foo");

				Interval i = new Interval(neg.pos, pos.neg);
				i.join(cut);
				
				cut = pos.cut;
				pos = pos.pos;
				neg.pos = i;
				neg.posend = i.posend;
			}
		}
	}

	public void join(Cell negside, Cell posside) {
		if(negside.inclusionIndex + 1 != posside.inclusionIndex)
			throw new IllegalArgumentException("Not adjacent: "+
							   negside+", "+
							   posside);

		join(posside.inclusionIndex);
	}

	public boolean containsIndex(int i) {
		return (i >= negend.inclusionIndex)
		    && (i <= posend.inclusionIndex);
	}

	public void dump(String indent) {
		System.out.println(indent+"Interval("+negend+","+posend+")");
		if(cut < 0)
			System.out.println(indent+"No cut.");
		else {
			System.out.println(indent+"Neg:");
			neg.dump(indent+"  ");
			System.out.println(indent+"Pos:");
			pos.dump(indent+"  ");
			System.out.println(indent+"Cut: "+cut);
		}
	}

*XXX* This must be documented, of course.

Also, should do rebalancing...

	
