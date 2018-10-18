Title: The Clasm primitives from the first Clasm primitive set
Rcs-Id: $Id: clasm-primitives.ly,v 1.5 2002/02/25 13:06:33 deetsay Exp $
Ly-Version: 0.0.2

<h1>The Clasm primitives from the first Clasm primitive set</h1>

<i>Benja Fallenstein</i>

<h2>Abstract</h2>

This document specifies and describes the implementation of the first set
of primitives for the Clasm ("Clang Assembler") programming language.
Read this if you want to know what primitives there are, currently,
or if you want to add a new one.

This is the first draft, and I'm still experimenting with
Literate Programming. Please do comment!

<h3>A warning from Tuomas</h3>

Tuomas warns:

<blockquote>
Note that these are really primitives used by actual clasm functions;
any changes to these should be done with *UTMOST* care, as it is
as dangerous as changing a system library like libc.
</blockquote>


<h2>Introduction</h2>

*Note*: A basic understanding of Clasm is a prerequisite for understanding
this document fully (or so I suppose).

A Clasm primitive is like a Clasm function, just that it's not written
in Clasm but (currently) in Java. This means that you can call the
primitive, passing it a list of zero or more parameters, and it will
return a single object.

<h3>The basic pattern</h3>

Let us look at the key lines of an example, a primitive that adds two ints.

-- The add primitive:
	public int exec(int a, int b) {
	    return a+b;
	}

As you can see, the |exec| function takes |int|s and returns an |int|.
Now, of course, clasm functions can operate on cells. However, you don't
have to care about that when writing a primitive: Clasm will employ
reflection to find out what types your primitive operates on, and it
will convert the values accordingly.

Here are the types a Clasm primitive is allowed to get or return:

<ul>
<li> |boolean|	</li>
<li> |int|	</li>
<li> |String|	</li>
<li> |Cell|	</li>
<li> |Expression|
(this is a special case; see section on "Control Structures," below)
</li>
</ul>


<h4>More examples</h4>

Now, as a different example, let's look at the arguably most primitive action 
in a hypergrid: stepping.

-- The step primitive:
	public Cell exec(Cell c, Cell dim, int dir) {
	    return c.s(dim, dir);
	}

Different types, different number of parameters, same overall pattern.
As you can see, dimensions are passed as |Cell| objects; as Clasm programs
are not normally expected to care about cell IDs, it is the 'right' way
to simply pass a cell to identify a dimension.

As a final example, let's look at the _disconnect_ operation. It is
special because it does not return a value.

-- The disconnect primitive:
	public void exec(Cell c, Cell dim, int dir) {
	    c.disconnect(dim, dir);
	}
	
As you can see, the return type of an |exec| function may be |void|;
in this case, Java reflection will automatically do the right thing,
which is returning a |null| pointer.

(At least currently that's defined as the right behavior. |;-)|)

<h3>The bureaucracy</h3>

Now, that's not all; there is some bureaucracy to wrap the |exec|
function up. Let us look at an example.

-- Clasm primitives:
	static public FunctionalPrimitive add = new FunctionalPrimitive() {
	    -- The add primitive.
	};

We are constructing an anonymous wrapper class containing the |exec| function.
There are only two things that vary here: the name of the primitive
(which obviously must be both unique and a Java identifier), and
the name of the superclass; it's |FunctionalPrimitive| in the example,
but it can also be |ImperativePrimitive|.

	static public ImperativePrimitive disconnect = new ImperativePrimitive() {
	    -- The disconnect primitive.
	};

The difference is that |ImperativePrimitive|s _do_ something, while
|FunctionalPrimitive|s just compute some value. A clearer name for
|FunctionalPrimitive| would be |ReferentiallyTransparentPrimitive|,
but that's a bit long. |:-)|

(Note to self: Would |PurePrimitive|, and maybe |ImpurePrimitive|, work?)

_step_ is a functional primitive, as it neither changes the space, nor
does any external I/O.

	static public FunctionalPrimitive step = new FunctionalPrimitive() {
	    -- The step primitive.
	};

Note one important point about |FunctionalPrimitive|s: They are guaranteed
not to do anything only when evaluating the parameters passed to them
does nothing. This is an important difference in the case of if/then/else,
for example (see the "Control Structures" section below): If all values
passed to if/then/else are referentially transparent, the primitive will
just select one of them and return it; if they are imperative actions,
executing if/then/else will also be imperative, because it will evaluate
two of its three parameters (the 'then' part iff the condition is true,
and the 'else' part iff the condition is false).

<h3>Putting it together</h3>

And this is it: the |exec| function wrapped up in the anonymous inner
class. Let's look at another example, logical negation, in the form
which we will also use in the remainder of this document.

	static public FunctionalPrimitive not = new FunctionalPrimitive() {
	    public boolean exec(boolean what) {
	        return !what;
	    }
	};

<h3>Putting a new primitive to use: make it cloneable in userspace</h3>

*XXX* This is probably the most important section of all, but I haven't
done this myself in a while, and am not sure how to explain; please,
can someone who's added a primitive recently (i.e., Tero) help me out?

Once you've added your primitive into this file and compiled, start
the client and jump to a cell with this ID:

0000000008000000E7C2E550C700043E3EC208FC03C2F68A854DA58908F1DB16B43BC4ACD60637-7:FF6F72672E677A69677A61672E696D706C2E636C61736D2E436C61736D5072696D697469766553657431-primitivename

The cell automatically contains this text:

org.gzigzag.impl.clasm.ClasmPrimitiveSet1.FF6F72672E677A69677A61672E696D706C2E636C61736D2E436C61736D5072696D697469766553657431-primitivename

Mark the cell. Create the primitive's masterclone with the primitive's
name in the system space where all the other primitives are, and
connect the marked cell to it, poswards on d.clasm-primitive-binding.

<h2>Gridwalking and structural modifications</h2>

We have already examined the _step_ and _disconnect_ operations, above.
There are four more operations missing: _connect_; _newcell_; _endcell_,
which returns the head- or tailcell on a rank; and _home_, which finds
the homecell.

_connect_ and _endcell_ are obvious.

	static public ImperativePrimitive connect = new ImperativePrimitive() {
	    public void exec(Cell c, Cell dim, Cell d) throws Exception {
	        c.connect(dim, d);
	    }
	};

	static public FunctionalPrimitive endcell = new FunctionalPrimitive() {
	    public Cell exec(Cell c, Cell dim, int dir) {
	        return c.h(dim, dir);
	    }
	};

With _home_ and _newcell_, there is a little twist, because the only parameter
they really need is a space context (for _home_, the space whose homecell
to get; for _newcell_, the slice in which to create the new cell).
Unfortunately, we do not have a space context available; when calling
one of those two primitives, we therefore have to pass them an arbitrary
cell, whose |.space| field will then be used.

	static public FunctionalPrimitive home = new FunctionalPrimitive() {
	    public Cell exec(Cell c) {
	        return c.space.getHomeCell();
	    }
	};

Note that the only version of _newcell_ available does not automatically
connect the new cell anywhere. You have to use a different Clasm primitive
to do that. This is because we want the set of Clasm primitives to be
minimal; I cannot see creating a new cell _and_ connecting it somewhere
as an atomic operation.

(Of course, often it's more convenient to have such an operation-- but
if you want to use it, simply write a function in Clasm itself that
does the trick!)

	static public ImperativePrimitive newcell = new ImperativePrimitive() {
	    public Cell exec(Cell c) {
	        return c.N();
	    }
	};

Note that some operations that have to be atomic (_insert_ etc.) are not
yet present here. (Anybody who'd like to add them?)



<h2>Logic and number crunching</h2>

The primitives for integer math are just the usual
stuff: addition (_add_), substraction (_sub_), multiplication (_mul_), 
division (_div_), and the modulo operator (_mod_). _add_ we have
already defined above.

	static public FunctionalPrimitive mul = new FunctionalPrimitive() {
	    public int exec(int a, int b) {
	        return a+b;
	    }
	};

	static public FunctionalPrimitive sub = new FunctionalPrimitive() {
	    public int exec(int a, int b) {
	        return a-b;
	    }
	};
	
	static public FunctionalPrimitive div = new FunctionalPrimitive() {
	    public int exec(int a, int b) {
	        return a/b;
	    }
	};

	static public FunctionalPrimitive mod = new FunctionalPrimitive() {
	    public int exec(int a, int b) {
	        return a%b;
	    }
	};

In boolean logic, we currently have only the _not_ primitive defined
above. (*XXX* Add _and_ and _or_ here.)



<h2>String operations</h2>

_concat_ concatenates two strings.

	static public FunctionalPrimitive concat = new FunctionalPrimitive() {
	    public String exec(String a, String b) {
	        return a+b;
	    }
	};

_substring_ works just like Java's |String.substring()| (see Java
documentation for details).

	static public FunctionalPrimitive substring = new FunctionalPrimitive() {
	    public String exec(String s, int firstIndex, int lastIndex) {
	        return s.substring(firstIndex, lastIndex);
	    }
	};

_length_ returns the length of a string, as expected.

	static public FunctionalPrimitive length = new FunctionalPrimitive() {
	    public int exec(String s) {
	        return s.length();
	    }
	};

<h2>Control structures</h2>

We currently have the control structures |__if| and |__while|, prefixed
by an underscore so as not to collide with the Java keywords of the
same name.

Now these two are a bit tricky, because they evaluate their parameters
in a special way. |__if| evaluates its second parameter (the 'then clause')
only if its first parameter is true, and its third parameter (the
'else clause') only if its first parameter is false.

Normally, values are passed to Clasm primitives using call-by-value.
In fact, this is true for all types except |Expression|. If the
Clasm fabric detects |Expression| as a parameter type, though, it passes
the parameter in unevaluated form (i.e., as an expression). You are
free to evaluate an expression |exp| zero, one or more times by calling
|exp.eval()|.

*Note*:
One type of value inside the clasm system is |Callable|-- this includes
Clasm functions, expressions, and primitives. Now, you may want to pass
a function to a primitive (which is a subclass of |Callable|). Then,
the parameter type in the primitive must be |Expression|; the primitive
is then passed the unevaluated expression, and has to evaluate it first
to get the |Function| object.

However, we do not need to do this, here; for our purposes, it suffices
to have control over how often the expressions are evaluated.

	static public FunctionalPrimitive _if = new FunctionalPrimitive() {
	    public Object exec(boolean cond, Expression _then,
	                       Expression _else) throws Exception {

We check |cond| (the condition) and use Java's |if| statement to decide
which one of the other two parameters we evaluate.

	        if(cond)
	            return _then.eval();
	        else
	            return _else.eval();
	    }
	};
	
In |__while|, we use the |isTrue()| function defined in |Callable|. This
function converts an |Object| (as returned from |Expression.eval()|) to
a boolean value, according to the normal rules inside the Clasm system.

Again, we simply use Java's |while| statement, using as the condition
and body the parameters we have been passed.
	
	static public ImperativePrimitive _while = new ImperativePrimitive() {
	    public void exec(Expression cond, Expression body) throws Exception {
	        while(isTrue(cond.eval()))
	            body.eval();
	    }
	};




<h2>Comparisons</h2>

The primitives in this category compare two objects-- cells, strings,
integers. Note that even you pass the same parameters to them, two of
these functions may return different results: two different cells can
contain the same string, making _celleq_ return |false| and
_streq_ return |true|.

_inteq_ and _streq_ are obvious:

	static public FunctionalPrimitive inteq = new FunctionalPrimitive() {
	    public boolean exec(int a, int b) {
	        return a == b;
	    }
	};

	static public FunctionalPrimitive streq = new FunctionalPrimitive() {
	    public boolean exec(String a, String b) {
	        return a.equals(b);
	    }
	};

With _celleq_, there's a trick, because it can also deal with |null|
pointers (so that we can step somewhere and then see whether the result
is |null|).

	static public FunctionalPrimitive celleq = new FunctionalPrimitive() {
	    public boolean exec(Cell a, Cell b) {

If the |a| is |null|, |a| and |b| are equal iff |b| is |null|, too.

	        if ( a == null )
	            return b == null;

If |a| is not |null|, we can use |a|'s |equals()| method to compare the
two cells.

		else
	            return a.equals(b);
	    }
	};


<h2>Cursors</h2>

In this category, we currently have only _setcursor_, which works just
like |Cursor.set()|:

	static public ImperativePrimitive setcursor = new ImperativePrimitive() {
	    public void exec(Cell cursor, Cell to) {
	        Cursor.set(cursor, to);
	    }
	};


<h2>Miscellaneous</h2>

Here are primitives that do not fit in any of the other categories.

The _console__println_ primitive takes a string, and prints it to the
Java console (stdout).

	static public ImperativePrimitive console_println = new ImperativePrimitive() {
	    public void exec(String str) {
	        System.out.println(str);
	    }
	};

The _getnull_ primitive takes no parameters, and returns a |null| pointer.

	static public FunctionalPrimitive getnull = new FunctionalPrimitive() {
	    public Object exec() {
	        return null;
	    }
	};

The _idstring_ primitive takes a cell, and returns the ID of that cell,
as a string.

	static public FunctionalPrimitive idstring = new FunctionalPrimitive() {
	    public String exec(Cell c) {
	        return c.id;
	    }
	};

The _system_ primitive executes programs on the host operating system!

	static public FunctionalPrimitive system = new FunctionalPrimitive() {

	    private ConfirmDialog cd = new ConfirmDialog();

	    public String exec(String cmd, String input) {
	        String s = "";
	        if (cd.confirm("A program wants to execute the following command\n"+
	            "on the host operating system:\n\n" + cmd +
	            "\n\nDo you want to allow it?")) {

	            try {
	                Runtime rt = Runtime.getRuntime();
	                Process pr = rt.exec(cmd);
	                OutputStream os = pr.getOutputStream();
	                BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
	                byte[] buf = input.getBytes();
	                os.write(buf, 0, buf.length);	// XXX this seems to be not working! :-(
	                os.flush();
	                os.close();
	                pr.waitFor();
	                char[] cbuf = new char[1024];
	                while (br.read(cbuf, 0, 1024) != -1) s += new String(cbuf);
	                br.close();
	            } catch (Exception e) { e.printStackTrace(); }
	        }
	        return s;
	    }
	};

<h2>Copyright and license statement</h2>

Copyright (c) 2001, Benja Fallenstein

You may use and distribute under the terms of either the GNU Lesser
General Public License, either version 2 of the license or,
at your choice, any later version. Alternatively, you may use and
distribute under the terms of the XPL.

See the LICENSE.lgpl and LICENSE.xpl files for the specific terms of
the licenses.

This software is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the README
file for more details.
