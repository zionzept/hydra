package aaa;


public class TTESSSTT {

	public static void main(String[] args) {
		System.out.println(sqrt(1000000.));
		System.out.println(sqrt(-0.));
		System.out.println(abs(5));
		System.out.println(1023l<<52);
		System.out.println("derp");
	}
	
	public static float sqrt(float x) {
		System.out.println("f");
		return Float.intBitsToFloat((Float.floatToIntBits(x) & 0x7FFFFFFF) + 0x3F800000 >>> 1);
	}
	
	public static double sqrt(double x) {
		return Double.longBitsToDouble((Double.doubleToLongBits(x) & 0x7FFFFFFFFFFFFFFFL) + 0x3FF0000000000000L >>> 1);
	}
	
	public static double abs(double x) {
		return Double.doubleToLongBits(x) & 0x7fffffffffffffffl;
	}
}
