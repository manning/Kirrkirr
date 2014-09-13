package Kirrkirr.util;

import java.util.*;

/** KRandom.java - Java 1.1's Random class does not have nextInt(int n),
 * which is useful for us (e.g., in the Network Visualization Panel), so
 * we subclass off Random and create / override the nextInt(int n) method,
 * copying the algorithm that is in the Java 1.2 and higher docs.
 *
 * - cw 2002
 */

public class KRandom extends Random {

    public int nextInt(int n) {
	if (n<=0)
	    throw new IllegalArgumentException("n must be positive");

	if ((n & -n) == n)  // i.e., n is a power of 2
	    return (int)((n * (long)next(31)) >> 31);

	int bits, val;
	do {
	    bits = next(31);
	    val = bits % n;
	} while(bits - val + (n-1) < 0);
	return val;
    }

}

