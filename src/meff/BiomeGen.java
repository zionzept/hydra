package meff;

import java.util.Random;

import meff.meff2D.CompositeF2D;
import meff.meff2D.F2D;
import meff.meff2D.Noise2D;
import meff.meff2D.SNoise2D;

public class BiomeGen implements F2D {

	private static final double TRANSITION = 0.3;
	private static final int T_SPLITS = 3;
	private static final int A_SPLITS = 3;
	
	private double tw;
	private double aw;
	
	private double tiw;
	private double aiw;
	
	private double tr;
	private double ar;
	
	CompositeF2D altitude;
	CompositeF2D temperature;
	
	F2D[][] biomes;
	
	public BiomeGen() {
		Random random;
		
		tw = (1 - TRANSITION * (T_SPLITS-1)) / T_SPLITS;
		aw = (1 - TRANSITION * (A_SPLITS-1)) / A_SPLITS;
		tiw = tw + TRANSITION;
		aiw = aw + TRANSITION;
		tr = tiw / TRANSITION;
		ar = aiw / TRANSITION;
		
		altitude = new CompositeF2D(0.5);
		random = new Random(3654);
		int c = 10;
		for (int i = 1; i < c+1; i++) {
			double a = 0.5*(random.nextDouble() + 1.0);
			double s = Math.pow(a*i*100, 2);
			System.out.println("S: " + s);
			altitude.add(new SNoise2D(s,s,a/c*1.4));//a/c));
		}
	//	altitude.add(new SNoise2D(23409, 54291, 0.2));
	//	altitude.add(new SNoise2D(54291, 23409, 0.2));
	//	altitude.add(new SNoise2D(542910, 234090, 0.4));
		
		temperature = new CompositeF2D(0.5);
		temperature.add(new SNoise2D(4000000, 4000000, 0.4));
		temperature.add(new SNoise2D(4600000, 4600000, 0.4));
		temperature.add(new SNoise2D(460, 460, 0.001));
		
		//temperature.addComponent(400432, 1, 1);
		//temperature.addComponent(1531182, 1, 1 + Math.PI/8);
		//temperature.setConstant(0.5);
		
		biomes = new F2D[A_SPLITS][T_SPLITS];

		Noise2D frozen_sea = new Noise2D();
		frozen_sea.setConstant(-10000);
		biomes[0][0] = frozen_sea;
		
		Noise2D sea = new Noise2D();
		sea.setConstant(-1000);
		biomes[0][1] = sea;
		
		Noise2D tropic_sea = new Noise2D();
		tropic_sea.setConstant(-100);
		tropic_sea.addComponent(300, 11, 0.7);
		biomes[0][2] = tropic_sea;
		
		Noise2D snowy = new Noise2D();
		snowy.setConstant(20);
		snowy.addComponent(80, 10, 0.5);
		biomes[1][0] = snowy;
		
		CompositeF2D plains = new CompositeF2D(10.0);
		plains.add(new SNoise2D(80, 80, 2));
		biomes[1][1] = plains;
		
		Noise2D desert = new Noise2D();
		desert.setConstant(10);
		desert.addComponent(80, 10, -0.3);
		biomes[1][2] = desert;
		
		Noise2D glacier = new Noise2D();
		random = new Random(87234);
		for (int i = 1; i <= 20; i++) {
			double size = 10 + Math.pow(1 + random.nextDouble() * i * 10, 2);
			double amp = 0.2 + 0.2*random.nextDouble();
			glacier.addComponent(size, size * amp, random.nextDouble());
		}
		glacier.setConstant(500);
		biomes[2][0] = glacier;
		
		CompositeF2D mountains = new CompositeF2D(8000);
		random = new Random(987123);
		for (int i = 1; i <= 30; i++) {
			double size = 10 + Math.pow(1 + random.nextDouble() * i * 10, 2);
			double amp = 0.04 + 0.04*random.nextDouble();
			mountains.add(new SNoise2D(size, size, amp*size));
		}
		biomes[2][1] = mountains;
		
		Noise2D desert_mountains = new Noise2D();
		random = new Random(84783);
		for (int i = 1; i <= 18; i++) {
			double size = 100 + Math.pow(1 + random.nextDouble() * i * 20, 2);
			double amp = 0.2 + 0.8*random.nextDouble();
			desert_mountains.addComponent(size, size * amp, random.nextDouble());
		}
		desert_mountains.setConstant(500);
		biomes[2][2] = desert_mountains;
		
		desert_mountains.addComponent(80000000, 10000000, -0.3);
	}
	
	@Override
	public double get(double x, double y) {
		return getData(x, y)[0];
	}
	
	public double[] getData(double x, double y) {
		double t = temperature.get(x, y);
		double a = altitude.get(x, y);
		
		int ti = Math.max(0, Math.min((int)(t / tiw), T_SPLITS - 1));
		int ai = Math.max(0, Math.min((int)(a / aiw), A_SPLITS - 1));
		double tf = 0;
		double af = 0;
		if (ti < T_SPLITS - 1) {
			tf = Math.max(0, (Math.max(0, Math.min((t / tiw), T_SPLITS - 1)) - ti) * tr - tr + 1);			
		}
		if (ai < A_SPLITS - 1) {
			af = Math.max(0, (Math.max(0, Math.min((a / aiw), T_SPLITS - 1)) - ai) * ar - ar + 1);			
		}
		//cubic softsteps
		tf = tf*tf*(3-2*tf);
		af = af*af*(3-2*af);
		
		// interpolating biomes across the different attributes
		double z00 = biomes[ai][ti].get(x, y);
		double z01 = 0;
		double z10 = 0;
		double z11 = 0;
		
		if (tf > 0) {
			z01 = biomes[ai][ti+1].get(x, y);
		}
		
		double z0 = (1-tf)*z00 + tf*z01;
		double z1 = 0;
		
		if (af > 0) {
			z10 = biomes[ai+1][ti].get(x, y);
			if (tf > 0) {
				z11 = biomes[ai+1][ti+1].get(x, y);
			}
			z1 = (1-tf)*z10 + tf * z11;
		}
		
		double z = (1 - af) * z0 + af * z1;
		z += Math.pow(a * 4, 2);
		z += 20;
		
		return new double[] {z, ti, ai, tf, af};
	}

}
