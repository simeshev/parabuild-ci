
class NonShortCircuit {
  boolean b;
  boolean and( boolean x, boolean y) {
	return x & y;
	}
  void orIt( boolean x, boolean y) {
	x |= y;
	b |= x;
	}
  boolean ordered(int x, int y, int z) {
	if (x >= y | y >= z) System.out.println("Not ordered");
	return x < y & y < z;
	}
  boolean nonEmpty(Object o[]) {
	return o != null & o.length > 0;
	}

    public static final int BIT0 = 1;   // 1st bit

    protected int m_iType;

    public NonShortCircuit( boolean available ) {
        m_iType |= available ? BIT0 : 0;
    }
}
