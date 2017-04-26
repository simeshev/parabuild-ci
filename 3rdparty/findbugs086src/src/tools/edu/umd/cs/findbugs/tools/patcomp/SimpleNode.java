/* Generated By:JJTree: Do not edit this line. SimpleNode.java */

package edu.umd.cs.findbugs.tools.patcomp;

public class SimpleNode implements Node {
  protected Node parent;
  protected Node[] children;
  protected int id;
  protected PatternCompiler parser;

  /* DHH: added these fields to keep track of tokens */
  private Token firstToken, lastToken;

  public SimpleNode(int i) {
    id = i;
  }

  public SimpleNode(PatternCompiler p, int i) {
    this(i);
    parser = p;
  }

  /** Get the id of this node. */
  public int getId() {
    return id;
  }

  /** Set first token. */
  public void setFirstToken(Token t) {
    if (t == null) throw new IllegalStateException();
    this.firstToken = t;
  }

  /** Get the first token. */
  public Token getFirstToken() {
    return firstToken;
  }

  /** Set last token. */
  public void setLastToken(Token t) {
    if (t == null) throw new IllegalStateException();
    this.lastToken = t;
  }

  /** Get the last token. */
  public Token getLastToken() {
    return lastToken;
  }

  /** Get number of tokens. */
  public int getNumTokens() {
    int count = 0;
    Token cur = firstToken;
    while (cur != lastToken) {
      ++count;
      cur = cur.next;
    }
    return count;
  }

  /**
   * Get <i>n</i>th token.
   * Returns null if there are fewer than <i>n+1</i> tokens.
   */
  public Token getToken(int n) {
    Token t = firstToken;
    int count = 0;
    while (t != null) {
      if (count == n)
        break;
      ++count;
      t = t.next;
    }
    return t;
  }

  public void jjtOpen() {
  }

  public void jjtClose() {
  }
  
  public void jjtSetParent(Node n) { parent = n; }
  public Node jjtGetParent() { return parent; }

  public void jjtAddChild(Node n, int i) {
    if (children == null) {
      children = new Node[i + 1];
    } else if (i >= children.length) {
      Node c[] = new Node[i + 1];
      System.arraycopy(children, 0, c, 0, children.length);
      children = c;
    }
    children[i] = n;
  }

  public Node jjtGetChild(int i) {
    return children[i];
  }

  public int jjtGetNumChildren() {
    return (children == null) ? 0 : children.length;
  }

  /* You can override these two methods in subclasses of SimpleNode to
     customize the way the node appears when the tree is dumped.  If
     your output uses more than one line you should override
     toString(String), otherwise overriding toString() is probably all
     you need to do. */

  public String toString() { return PatternCompilerTreeConstants.jjtNodeName[id]; }
  public String toString(String prefix) { return prefix + toString(); }

  /* Override this method if you want to customize how the node dumps
     out its children. */

  public void dump(String prefix) {
    System.out.println(toString(prefix));
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
	SimpleNode n = (SimpleNode)children[i];
	if (n != null) {
	  n.dump(prefix + " ");
	}
      }
    }
  }
}
