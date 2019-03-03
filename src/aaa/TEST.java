package aaa;

import java.io.FileNotFoundException;

public class TEST {
	
	public static void main(String[] args) throws FileNotFoundException {	
		pre();
		double min = 0;
		double max = 1000000;
		double[] d = new double[10000000];
		for (int i = 0; i < d.length; i++) {
			d[i] = min + Math.random() * (max - min);
		}
		long t;
		long test_time_1;
		double[] a1 = new double[d.length];
		double[] a2 = new double[d.length];
		double[] o = new double[d.length];
		t = System.currentTimeMillis();
		for (int i = 0; i < d.length; i++) {
			a1[i] = TEST.sin(d[i]);
		}
		test_time_1 = System.currentTimeMillis() - t;
		long test_time_2;
		t = System.currentTimeMillis();
		for (int i = 0; i < d.length; i++) {
			a2[i] = Math.sin(d[i]);
		}
		test_time_2 = System.currentTimeMillis() - t;
		long overhead;
		t = System.currentTimeMillis();
		for (int i = 0; i < d.length; i++) {
			o[i] = d[i];
		}
		overhead = System.currentTimeMillis() - t;
		test_time_1 -= overhead;
		test_time_2 -= overhead;
		System.out.println(1d * test_time_1 / 1000 + "\t" + 1d * test_time_2 / 1000);
		System.out.println(1d * test_time_1 / test_time_2);
		
		double q1 = 0;
		double q2 = 0;
		double asd = 0;
		double error = 0;
		for (double a : d) {
			double s1 = sin(a);
			double s2 = Math.sin(a);
			double e = (s1 - s2)/s1;
			if (Math.abs(e)>Math.abs(error)) {
				error = e;
				asd = a;
				q1 = s1;
				q2 = s2;
			}
		}
		System.out.println(asd + ": " + error);
		System.out.println(q1 + "\t" + q2);
	}
	
	static final int lookup_size = 10000;
	static final double lookup_size_i = 1d / lookup_size;
	static double[] sin_lookup;
	
	private static boolean pred;
	public static void pre() {
		if (pred) {
			return;
		}
		sin_lookup = new double[lookup_size+1];
		for (int i = 0; i <= lookup_size; i++) {
			sin_lookup[i] = Math.sin(i*lookup_size_i*PI_half);
		}
		pred = true;
	}
	
	static final double PI = Math.PI;
	static final double PI_i = 1 / PI;
	static final double PI_half = 0.5 * PI;
	static final double PI_i_twice = 2 * PI_i;
	static final double PI_i_half = 0.5 * PI_i;
	static final double PI_twice = 2 * PI;
	
	
	public static double sin(double a) {
		if (a > PI_twice)
			a -= ((int)(a*PI_i_half))*PI_twice;
		else if (a < 0)
			a -= ((int)(a*PI_i_half)-1)*PI_twice;
		double inv = 1;
		if (a > PI) {
			a -= PI;
			inv = -1;
		}
		if (a > PI_half)
			a = PI - a;
		double ix = a*PI_i_twice*lookup_size;
		int i = (int) ix;
		double f = ix - i;
		return inv*((1-f)*sin_lookup[i]+f*sin_lookup[i+1]);
	}
}
