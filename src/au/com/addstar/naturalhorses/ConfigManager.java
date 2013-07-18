package au.com.addstar.naturalhorses;
import java.util.List;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

	private NaturalHorses plugin;
	public ConfigManager(NaturalHorses instance) {
		plugin = instance;
	}
	
	public FileConfiguration Config() {
		return plugin.getConfig();
	}
	
	public void LoadConfig(FileConfiguration config) {
		config.options().copyDefaults(true);

		NaturalHorses.DebugEnabled = Config().getBoolean("debug", false);
		NaturalHorses.BroadcastLocation = Config().getBoolean("broadcast-location", false);
		NaturalHorses.SpawnDelay = Config().getInt("spawn-delay", 180);
		NaturalHorses.ChunkRadius = Config().getInt("chunk-check-radius", 3);
		NaturalHorses.SpawnChance = Config().getDouble("spawn-chance", 1);
		NaturalHorses.DonkeyChance = Config().getDouble("donkey-chance", 10);

		// Single world not supported any more
		if (Config().getString("horse-world") != null) {
			plugin.Warn("Your configuration is outdated! Please delete your config.yml and let the plugin re-create it.");
		}

		// Validate the list of "horse" worlds
		List<?> worlds = Config().getList("horse-worlds");
		for (int x = 0; x < worlds.size(); x++) {
			String name = (String) worlds.get(x);
			plugin.Debug("Checking world: " + name);
			World world = plugin.getServer().getWorld(name);
			if (world == null) {
				plugin.Warn("World \"" + name + "\" is not valid! It will be ignored.");
			} else {
				// We get the name again, to ensure the case is correct and we match it easily in the listener
				NaturalHorses.HorseWorlds.add(world.getName());
			}
		}
		plugin.Debug("Enabled worlds: " + NaturalHorses.HorseWorlds);
		
		// Validate the list of biomes where horses should spawn
		List<?> biomes = Config().getList("horse-biomes");
		for (int x = 0; x < biomes.size(); x++) {
			String name = ((String) biomes.get(x)).toUpperCase();
			plugin.Debug("Checking biome: " + name);
			try {
				// Attempt to enum the name
				Biome.valueOf(name);

				// We get the name again, to ensure the case is correct and we match it easily in the listener
				NaturalHorses.HorseBiomes.add(name);
			} catch (Exception e) {
				plugin.Warn("Biome \"" + name + "\" is not valid! It will be ignored.");
			}
		}
		plugin.Debug("Enabled biomes: " + NaturalHorses.HorseBiomes);
		
		// Validate min/max values
		NaturalHorses.MaxHorses = Config().getInt("max-horses", 7);
		NaturalHorses.MinHorses = Config().getInt("min-horses", 2);
		if ((NaturalHorses.MinHorses > NaturalHorses.MaxHorses) || (NaturalHorses.MinHorses < 0) || (NaturalHorses.MaxHorses < 0)) {
			NaturalHorses.MinHorses = 2;
			NaturalHorses.MaxHorses = 7;
			plugin.Warn("Min/Max horse values are invalid! Using default values instead.");
		}
	}
}
