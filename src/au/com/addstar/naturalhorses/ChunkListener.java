package au.com.addstar.naturalhorses;

import java.util.Date;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
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
		if (!event.getWorld().getName().equals(plugin.HorseWorld)) { return; }
		
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
			if (plugin.RandomGen.nextDouble() <= plugin.SpawnChance) {
				
				// Surrounding chunks must also be empty (grid loop)
				int from = (0 - plugin.ChunkRadius);
				int to   = plugin.ChunkRadius;
				
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
						//if ((ents != null) && (ents.length > 0)) {
						//	plugin.Debug("Area not empty: " + (CX+x) + " / " + (CZ+z) + " (not spawning horses)");
						//	return;
						//}
					}
				}
				
				plugin.Debug("Chunk: " + world.getName() + ": " + CX + "/" + CZ + " = " + BX + " / " + BZ);

				// How many horses to spawn?
				int h = plugin.RandomGen.nextInt(5) + 2;
				Date date = new Date();
				long now = date.getTime();
				
				boolean EntitySpawned = false;
				for (int i = 0; i < h; i++) {
					// Spread horses out randomly around the selected chunk
					int RX = BX + plugin.RandomGen.nextInt(8);
					int RZ = BZ + plugin.RandomGen.nextInt(8);
					Location loc = new Location(world, RX, world.getHighestBlockYAt(RX, RZ)-1, RZ);
					// Only spawn on grass
					if (world.getBlockTypeIdAt(loc) == Material.GRASS.getId()) {
						Location entloc = new Location(world, RX, loc.getY()+2, RZ);

						// Check if WorldGuard allows us to spawn here
						if (plugin.CanSpawnMob(entloc)) {
							plugin.Debug("Last spawn " + (plugin.LastSpawn / 1000) + " / now " + (now / 1000));
							if ((plugin.LastSpawn > 0) && (now < (plugin.LastSpawn + (plugin.SpawnDelay * 1000)))) {
								plugin.Debug("Refusing to spawn anything.. too soon..");
								return;
							}

							// Horse or donkey?
							EntityType enttype = EntityType.COW;
							String entname = "Horsey";
							if (plugin.RandomGen.nextDouble() <= plugin.DonkeyChance) {
								enttype = EntityType.PIG;
								entname = "Donkey";
							}
							
							plugin.Debug("Spawn " + entname + " at: " + world.getName() + " / X:" + entloc.getBlockX() + " Y:" + entloc.getBlockY() + " Z:" + entloc.getBlockZ());

							// Spawn the "horse"
							LivingEntity ent = (LivingEntity) world.spawnEntity(entloc, enttype);
							ent.setCustomName(entname + " " + (i + 1));
							ent.setCustomNameVisible(true);
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
					if (plugin.DebugEnabled) {
						plugin.getServer().broadcastMessage(ChatColor.YELLOW + "[NaturalHorses] " + ChatColor.WHITE + "Horse herd spawned: X:" + BX + " Y:" + world.getHighestBlockAt(BX, BZ).getY() + " Z:" + BZ);
					}
				}
			}
		}
	}
}
