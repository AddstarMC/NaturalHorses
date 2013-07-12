package au.com.addstar.naturalhorses;
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

		NaturalHorses.DebugEnabled = Config().getBoolean("debug");
		NaturalHorses.HorseWorld = Config().getString("horse-world");
		NaturalHorses.BroadcastLocation = Config().getBoolean("broadcast-location");
		NaturalHorses.SpawnDelay = Config().getInt("spawn-delay");
		NaturalHorses.ChunkRadius = Config().getInt("chunk-check-radius");
		NaturalHorses.SpawnChance = Config().getDouble("spawn-chance");
		NaturalHorses.DonkeyChance = Config().getDouble("donkey-chance");
	}
}
