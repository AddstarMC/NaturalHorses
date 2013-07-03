package au.com.addstar.naturalhorses;

import net.minecraft.server.v1_6_R1.World;

public class MyHorse extends net.minecraft.server.v1_6_R1.EntityHorse {
	public MyHorse(World world) {
		super(world);

		int horsetype = 0; // Horse
		if (NaturalHorses.RandomGen.nextInt(100) < NaturalHorses.DonkeyChance) {
			horsetype = 1; // donkey
		}
		this.p(horsetype);
		
		// Horse colours/markings
		if (horsetype == 0) {
			int variant = NaturalHorses.RandomGen.nextInt(7);
			int markings = NaturalHorses.RandomGen.nextInt(5);
			this.q((markings * 256) + (variant));
		}
	}
}
