Title: PlanVStream trees
Ly-version: 0.0.3

-- PlainVStreamDim inner classes:
	static public class Tree {
		Node nil = new Node(false);

		Node root = nil;

		class Node {
			float pos;
			Cell negside, posside;
			Node left = nil, right = nil, parent = nil;
			boolean red = true;

			public Node() {}

			public Node(boolean red) { this.red = red; }

If a node is red, then both its children are black.

			public boolean hasRedRedViolation() {
				return (this != nil && ((red && (left.red || right.red))
					|| left.hasRedRedViolation() || right.hasRedRedViolation()));
			}

Every simple path from a node to a descendant leaf contains the same number of black nodes.

			public int blackHeight() {
				if (this == nil) return 1;
				int leftBlackHeight = left.blackHeight();
				int rightBlackHeight = right.blackHeight();
				return ((leftBlackHeight == -1 || rightBlackHeight == -1
					|| leftBlackHeight != rightBlackHeight) ? -1 :
					leftBlackHeight + (red ? 0 : 1));
			}
		}

		private Node find(float pos) {
			-- Tree.find().
		}
		private Node minimum(Node x) {
			-- Tree.minimum().
		}
		private Node successor(Node x) {
			-- Tree.successor().
		}
		private void rotateLeft(Node x) {
			-- Tree.rotateLeft().
		}
		private void rotateRight(Node x) {
			-- Tree.rotateRight().
		}

		public void insert(Cell negside, Cell posside) {
			-- Tree.insert().
		}
		public void delete(Cell negside, Cell posside) {
			-- Tree.delete().
		}

		Cell next(Cell from) {
			-- Tree.next().
		}
		Cell prev(Cell from) {
			-- Tree.prev().
		}

		public void dumpTree() {
			-- Tree.dumpTree().
		}
		public void dumpTree(Node node, String indent) {
			-- Tree.dumpTree(Node, String).
		}
	}

-- PlainVStreamDim tests:
	public void testTreeStructure() {
		tree.insert(c[100], c[101]);

		assertTrue("tree.nil != tree.root", tree.nil != tree.root);

For some reason, there does not seem to be an |assertEquals(float, float)|.
So, we compare |String|s instead of |float|s (yuck).

		assertEquals("100.5", ""+tree.root.pos);
		assertEquals(tree.nil, tree.root.left);
		assertEquals(tree.nil, tree.root.right);

		tree.insert(c[50], c[51]);
		assertTrue("tree.nil != tree.root.left", tree.nil != tree.root.left);
		assertEquals("50.5", ""+tree.root.left.pos);
		assertEquals(tree.nil, tree.root.left.left);
		assertEquals(tree.nil, tree.root.left.right);
		assertEquals(tree.nil, tree.root.right);
	}

-- PlainVStreamDim tests:
	public void testTreeRedBlack() {
		for (int i=0; i<0x100; i++) {
			i ^= 0xf2;
			tree.insert(c[i], c[i+1]);
			if (tree.root.hasRedRedViolation()) fail("Tree has red-red-violation!");
			i ^= 0xf2;
		}
		for (int i=0; i<0x100; i++) {
			i ^= 0xd7;
			tree.delete(c[i], c[i+1]);
			if (tree.root.hasRedRedViolation()) fail("Tree has red-red-violation!");
			i ^= 0xd7;
		}
		for (int i=0xff; i>=0; i--) {
			i ^= 0x88;
			tree.insert(c[i], c[i+1]);
			if (tree.root.hasRedRedViolation()) fail("Tree has red-red-violation!");
			i ^= 0x88;
		}
		for (int i=0; i<0x100; i++) {
			tree.delete(c[i], c[i+1]);
			if (tree.root.hasRedRedViolation()) fail("Tree has red-red-violation!");
		}
	}

-- PlainVStreamDim tests:
	public void testTreeBalanced() {
		for (int i=0; i<0x100; i++) {
			i ^= 0x33;
			tree.insert(c[i], c[i+1]);
			//tree.dumpTree();
			if (tree.root.blackHeight() == -1) fail("Tree has black-height-violation: Tree is not balanced! "+i);
			i ^= 0x33;
		}
		for (int i=0; i<0x100; i++) {
			i ^= 0xf6;
			tree.delete(c[i], c[i+1]);
			if (tree.root.blackHeight() == -1) fail("Tree has black-height-violation: Tree is not balanced! "+i);
			i ^= 0xf6;
		}
		for (int i=0xff; i>=0; i--) {
			i ^= 0x77;
			tree.insert(c[i], c[i+1]);
			if (tree.root.blackHeight() == -1) fail("Tree has black-height-violation: Tree is not balanced!");
			i ^= 0x77;
		}
		for (int i=0; i<0x100; i++) {
			i ^= 0xbb;
			tree.delete(c[i], c[i+1]);
			if (tree.root.blackHeight() == -1) fail("Tree has black-height-violation: Tree is not balanced! "+i);
			i ^= 0xbb;
		}
	}

-- PlainVStreamDim tests:
	public void testTreeWorking() {

		tree.insert(c[100], c[101]);
		assertEquals(c[100], tree.next(c[99]));
		assertEquals(c[101], tree.prev(c[102]));

		tree.insert(c[50], c[51]);
		assertEquals(c[100], tree.next(c[99]));
		assertEquals(c[101], tree.prev(c[102]));
		assertEquals(c[50], tree.next(c[25]));
		assertEquals(c[51], tree.prev(c[75]));

		tree.delete(c[50], c[51]);
		assertEquals(c[100], tree.next(c[25]));
	}

-- PlainVStreamDim tests:
	public void testErrorChecking() {
		try {
			tree.next(c[150]);
			fail("No error when calling tree.next() with a "+
			     "cell that is followed by no cut");
		} catch(IllegalArgumentException _) {
		}
		try {
			tree.prev(c[99]);
			fail("No error when calling tree.prev() with a "+
			     "cell that is preceded by no cut");
		} catch(IllegalArgumentException _) {
		}

		tree.insert(c[100], c[101]);

		try {
			tree.next(c[150]);
			fail("No error when calling tree.next() with a "+
			     "cell that is followed by no cut");
		} catch(IllegalArgumentException _) {
		}
		try {
			tree.prev(c[99]);
			fail("No error when calling tree.prev() with a "+
			     "cell that is preceded by no cut");
		} catch(IllegalArgumentException _) {
		}

		try {
			tree.insert(c[50], c[59]);
			fail("No error when calling 'insert' with "+
			     "non-continuous cells in the argument");
		} catch(IllegalArgumentException _) {
		}
	}



-- PlainVStreamDim test setup:
	int old_format;
	PlainVStreamDim.Tree tree;
	Cell[] c;

	public void setUp() {
		old_format = Cell.cellFormat;
		Cell.cellFormat = Cell.ID_ONLY;

		c = new Cell[0x102];
		for(int i=0; i<0x102; i++)
			c[i] = new Cell(null, "c["+i+"]", null, null, i);

		tree = new PlainVStreamDim.Tree();
	}

	public void tearDown() {
		Cell.cellFormat = old_format;
	}

-- Tree.dumpTree(Node, String):
	System.out.println(indent + node.pos + (node.red ? " red" : " black"));

	if (node.left != nil)
		dumpTree(node.left, indent + "  ");
	else
		System.out.println(indent + "  nil black");

	if (node.right != nil)
		dumpTree(node.right, indent + "  ");
	else
		System.out.println(indent + "  nil black");

-- Tree.dumpTree():
	System.out.println("");
	dumpTree(root, "");



-- Tree.rotateLeft():
	Node y = x.right;		// Set y;
	x.right = y.left;		// Turn y's left subtree into x's right subtree.
	if (y.left != nil)
		y.left.parent = x;
	y.parent = x.parent;		// Link x's parent to y.
	if (x.parent == nil)
		root = y;
	else {
		if (x == x.parent.left)
			x.parent.left = y;
		else
			x.parent.right = y;
	}
	y.left = x;			// Put x on y's left.
	x.parent = y;

-- Tree.rotateRight():
	Node y = x.left;		// Set y;
	x.left = y.right;		// Turn y's right subtree into x's left subtree.
	if (y.right != nil)
		y.right.parent = x;
	y.parent = x.parent;		// Link x's parent to y.
	if (x.parent == nil)
		root = y;
	else {
		if (x == x.parent.right)
			x.parent.right = y;
		else
			x.parent.left = y;
	}
	y.right = x;			// Put x on y's right.
	x.parent = y;

-- Tree.insert():
	Node newNode = new Node();

	if (negside.inclusionIndex+1 != posside.inclusionIndex)
		throw new IllegalArgumentException(
			"Negside and posside are not continuous: " +
			negside + ", " + posside);

	newNode.negside = negside;
	newNode.posside = posside;
	newNode.pos = negside.inclusionIndex + 0.5f;

	Node y = nil;
	Node x = root;
	while (x != nil) {
		y = x;
		if (newNode.pos < x.pos)
			x = x.left;
		else
			x = x.right;
	}
	newNode.parent = y;
	if (y == nil)
		root = newNode;
	else {
		if (newNode.pos < y.pos)
			y.left = newNode;
		else
			y.right = newNode;
	}

	x = newNode;
	while (x != root  && x.parent.red) {
		if (x.parent == x.parent.parent.left) {
			y = x.parent.parent.right;
			if (y.red) {
				x.parent.red = false;
				y.red = false;
				x.parent.parent.red = true;
				x = x.parent.parent;
			}
			else {
				if (x == x.parent.right) {
					x = x.parent;
					rotateLeft(x);
				}
				x.parent.red = false;
				x.parent.parent.red = true;
				rotateRight(x.parent.parent);
			}
		}
		else {
			y = x.parent.parent.left;
			if (y.red) {
				x.parent.red = false;
				y.red = false;
				x.parent.parent.red = true;
				x = x.parent.parent;
			}
			else {
				if (x == x.parent.left) {
					x = x.parent;
					rotateRight(x);
				}
				x.parent.red = false;
				x.parent.parent.red = true;
				rotateLeft(x.parent.parent);
			}
		}
	}
	root.red = false;

-- Tree.minimum():
	while (x.left != nil)
		x = x.left;
	return x;

-- Tree.successor():
	if (x.right != nil)
		return minimum(x.right);
	Node y = x.parent;
	while (y != nil && x == y.right) {
		x = y;
		y = y.parent;
	}
	return y;

-- Tree.delete():
	Node x, y, z = find(negside.inclusionIndex + 0.5f);

	if (z.left == nil || z.right == nil)
		y = z;
	else
		y = successor(z);

	if (y.left != nil)
		x = y.left;
	else
		x = y.right;

	x.parent = y.parent;

	if (y.parent == nil)
		root = x;
	else {
		if (y == y.parent.left)
			y.parent.left = x;
		else
			y.parent.right = x;
	}

	if (y != z) {
		z.pos = y.pos;		// If y has other fields, copy them, too.
		z.posside = y.posside;
		z.negside = y.negside;
	}

	/*
	dumpTree();
	System.out.println("Spliced-out node y: " + y.pos);
	System.out.println("y's child x: " + x.pos);
	*/
	if (!y.red) {
		// delete-fixup

		Node w;
		while (x != root && !x.red) {
			if (x == x.parent.left) {
				w = x.parent.right;
				if (w.red) {
					w.red = false;
					x.parent.red = true;
					rotateLeft(x.parent);
					w = x.parent.right;
				}
				if (!w.left.red && !w.right.red) {
					w.red = true;
					x = x.parent;
				}
				else {
					if (!w.right.red) {
						w.left.red = false;
						w.red = true;
						rotateRight(w);
						w = x.parent.right;
					}
					w.red = x.parent.red;
					x.parent.red = false;
					w.right.red = false;
					rotateLeft(x.parent);
					x = root;
				}
			}
			else {
				w = x.parent.left;
				if (w.red) {
					w.red = false;
					x.parent.red = true;
					rotateRight(x.parent);
					w = x.parent.left;
				}
				if (!w.right.red && !w.left.red) {
					w.red = true;
					x = x.parent;
				}
				else {
					if (!w.left.red) {
						w.right.red = false;
						w.red = true;
						rotateLeft(w);
						w = x.parent.left;
					}
					w.red = x.parent.red;
					x.parent.red = false;
					w.left.red = false;
					rotateRight(x.parent);
					x = root;
				}
			}
		}
		x.red = false;
	}
	// return y;	??

-- Tree.find():
	Node current = root;

	while(pos != current.pos) {
		if(pos > current.pos)
			current = current.right;
		else
			current = current.left;
	}

	return current;

-- Tree.next():
	float pos = from.inclusionIndex;

	Node current = root;
	Node found = nil;

	while(current != nil) {
		if(pos < current.pos &&
		   (found == nil || current.pos < found.pos))
			found = current;

		if(pos > current.pos)
			current = current.right;
		else
			current = current.left;
	}

	if(found == nil)
		throw new IllegalArgumentException("No cut with an index "+
						   "larger than: "+pos);

	return found.negside;

-- Tree.prev():
	float pos = from.inclusionIndex;

	Node current = root;
	Node found = nil;

	while(current != nil) {
		if(pos > current.pos &&
		   (found == nil || current.pos > found.pos))
			found = current;

		if(pos > current.pos)
			current = current.right;
		else
			current = current.left;
	}

	if(found == nil)
		throw new IllegalArgumentException("No cut with an index "+
						   "larger than: "+pos);

	return found.posside;

-- file "test/TestPlainVStreamDim.java":
	package org.gzigzag.impl;
	import org.gzigzag.*;
	import junit.framework.*;

	public class TestPlainVStreamDim extends TestCase {
		public TestPlainVStreamDim(String s) { super(s); }

		-- PlainVStreamDim test setup.
		-- PlainVStreamDim tests.
	}
