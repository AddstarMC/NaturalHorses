package au.com.addstar.naturalhorses;

import java.util.Date;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
//import org.bukkit.craftbukkit.v1_6_R1.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class ChunkListener implements Listener {
	private NaturalHorses plugin;
	public ChunkListener(NaturalHorses instance) {
		plugin = instance;
	}
	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		if (event.isNewChunk()) { return; }
		if (!event.getWorld().getName().equals(NaturalHorses.HorseWorld)) { return; }
		
		World world = event.getWorld();
		Chunk chunk = event.getChunk();
		int CX = chunk.getX();	// chunk X
		int CZ = chunk.getZ();	// chunk Z
		int BX = CX<<4;  		// corner block position of chunk
		int BZ = CZ<<4;  		// corner block position of chunk
		
		// Get chunk information
		ChunkSnapshot chunkdata = chunk.getChunkSnapshot(false, true, false);

		String bname = chunkdata.getBiome(0, 0).name();
		if (chunkdata.getBiome(0, 0).name() == "PLAINS" &&
				chunkdata.getBiome(0, 15).name() == "PLAINS" &&
				chunkdata.getBiome(15, 0).name() == "PLAINS" &&
				chunkdata.getBiome(15, 15).name() == "PLAINS") {
			bname = "FULL PLAINS";
		}

		// Only spawn horses in "full plains" (all four corners are plains)
		if (bname == "FULL PLAINS") {
			// % chance of spawning?
			if ((NaturalHorses.RandomGen.nextDouble() * 100) < NaturalHorses.SpawnChance) {
				
				// Surrounding chunks must also be empty (grid loop)
				int from = (0 - NaturalHorses.ChunkRadius);
				int to   = NaturalHorses.ChunkRadius;
				
				for (int x = from; x < to; x++) {
					for (int z = from; z < to; z++) {
						Entity[] ents = world.getChunkAt(CX+x, CZ+z).getEntities();
						for (Entity ent: ents) {
							// Look for other living entities
							if (ent.getType().isAlive()) {
								plugin.Debug("Area not empty: " + (CX+x) + " / " + (CZ+z) + " (not spawning horses)");
								return;
							}
						}
					}
				}
				
				plugin.Debug("Chunk: " + world.getName() + ": " + CX + "/" + CZ + " = " + BX + " / " + BZ);

				// How many horses to spawn?
				int h = NaturalHorses.RandomGen.nextInt(5) + 2;
				Date date = new Date();
				long now = date.getTime();
				
				boolean EntitySpawned = false;
				for (int i = 0; i < h; i++) {
					// Spread horses out randomly around the selected chunk
					int RX = BX + NaturalHorses.RandomGen.nextInt(8);
					int RZ = BZ + NaturalHorses.RandomGen.nextInt(8);
					Location loc = new Location(world, RX, world.getHighestBlockYAt(RX, RZ)-1, RZ);
					// Only spawn on grass
					if (world.getBlockTypeIdAt(loc) == Material.GRASS.getId()) {
						Location entloc = new Location(world, RX+1, loc.getY()+2, RZ+1);

						// Check if WorldGuard allows us to spawn here
						if (plugin.CanSpawnMob(entloc)) {
							if ((plugin.LastSpawn > 0) && (now < (plugin.LastSpawn + (NaturalHorses.SpawnDelay * 1000)))) {
								plugin.Debug("Too soon.. Refusing to spawn anything");
								return;
							}

							// Spawn the horse/donkey (YAY!! Horse API! :)
							Horse horse = (Horse) world.spawnEntity(entloc, EntityType.HORSE);

							// Donkey or horse?
							if ((NaturalHorses.RandomGen.nextDouble() * 100) < NaturalHorses.DonkeyChance) {
								horse.setVariant(Variant.DONKEY);
							} else {
								horse.setVariant(Variant.HORSE);
							}

							// Horse colours/markings
							if (horse.getVariant() == Variant.HORSE) {
								int color = NaturalHorses.RandomGen.nextInt(7);
								int style = NaturalHorses.RandomGen.nextInt(5);								
								horse.setStyle(Style.values()[style]);
								horse.setColor(Color.values()[color]);
							}

							// Set the horse MaxHealth: 15 - 30 half hearts 
							int mxh = 15 + NaturalHorses.RandomGen.nextInt(8) + NaturalHorses.RandomGen.nextInt(9);
							horse.setMaxHealth((double) mxh);
							
							// Prevent horses from despawning
							horse.setRemoveWhenFarAway(false);

							plugin.Debug(horse.getVariant() + " (" + horse.getColor() + " " + horse.getStyle() + ": " + horse.toString());
							EntitySpawned = true;
						} else {
							plugin.Debug("Entity spawning disabled here");
						}
					} else {
						plugin.Debug("Not grass at: " + world.getName() + " / X:" + loc.getBlockX() + " Y:" + loc.getBlockY() + " Z:" + loc.getBlockZ());
					}
				}

				// Tell everyone where the horse herd has been spawned (for debugging only)
				if (EntitySpawned) {
					plugin.LastSpawn = now; 

					String msg = ChatColor.YELLOW + "[NaturalHorses] " + ChatColor.WHITE + "Horses spawned (" + h + "): X:" + BX + " Y:" + world.getHighestBlockAt(BX, BZ).getY() + " Z:" + BZ;
					if (NaturalHorses.BroadcastLocation) {
						plugin.getServer().broadcastMessage(msg);
					}
					else if (NaturalHorses.DebugEnabled) {
						plugin.Debug(msg);
					}
					
				}
			}
		}
	}
}
