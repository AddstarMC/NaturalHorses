package au.com.addstar.naturalhorses;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import au.com.addstar.naturalhorses.Metrics.Graph;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import org.bukkit.Location;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class NaturalHorses extends JavaPlugin {
	public static NaturalHorses instance;
	public PluginDescriptionFile pdfFile = null;
	public PluginManager pm = null;
	private static final Logger logger = Logger.getLogger("Minecraft");
	public ConfigManager cfg = new ConfigManager(this);
	public WorldGuardPlugin WG;
	public Map<String, RegionManager> RMS = new HashMap<String, RegionManager>();
	public long LastSpawn = 0;
	public static Random RandomGen = new Random();
	public int MetricHorsesSpawned = 0;
	public int MetricDonkeysSpawned = 0;

	// Pluing settings
	public static boolean DebugEnabled;
	public static boolean BroadcastLocation;
	public static List<String> HorseWorlds = new ArrayList<String>();
	public static List<String> HorseBiomes = new ArrayList<String>();
	public static int SpawnDelay;
	public static int ChunkRadius;
	public static double SpawnChance;
	public static double DonkeyChance;
	public static int MinHorses;
	public static int MaxHorses;

	@Override
	public void onEnable() {
		// Register necessary events
		pdfFile = this.getDescription();
		pm = this.getServer().getPluginManager();

		// Save the default config (if one doesn't exist)
		saveDefaultConfig();

		// Read (or initialise) plugin config file
		cfg.LoadConfig(getConfig());

		// Make sure we have at least 1 valid world
		if (HorseWorlds.size() == 0) {
			Log(pdfFile.getName() + " " + pdfFile.getVersion() + " has NOT been enabled!");
			this.setEnabled(false);
			return;
		}

		// Make sure we have at least 1 valid biome
		if (HorseBiomes.size() == 0) {
			Log(pdfFile.getName() + " " + pdfFile.getVersion() + " has NOT been enabled!");
			this.setEnabled(false);
			return;
		}

		WG = getWorldGuard();
		if (WG == null) {
			Log("WorldGuard not detected, integration disabled.");
		} else {
				for (String world : HorseWorlds) {
					try {
						RegionManager RM = WG.getRegionManager(getServer().getWorld(world));
						if (RM == null) {
							Warn("Unable to integrate WorldGuard for world \"" + world + "\"");
							continue;
						}
						Debug("Found RegionManager for \"" + world + "\"");
						RMS.put(world, RM);
					} catch(Exception e) {
						Warn("WorldGuard integration failed for \"" + world + "\"! Exception in getRegionManager: " + e.hashCode() + " - " + e.getLocalizedMessage());
						WG = null;
					}
				}
				
				if (RMS.size() == 0) {
					Warn("WorldGuard integration failed!");
					WG = null;
				} else {
					Log("WorldGuard integration successful.");
				}
		}

		SetupMetrics();
		
		pm.registerEvents(new ChunkListener(this), this);
		Log(pdfFile.getName() + " " + pdfFile.getVersion() + " has been enabled");
	}
	
	@Override
	public void onDisable() {
		// Nothing yet
	}

	public void Log(String data) {
		logger.info("[" + pdfFile.getName() + "] " + data);
	}

	public void Warn(String data) {
		logger.warning("[" + pdfFile.getName() + "] " + data);
	}
	
	public void Debug(String data) {
		if (DebugEnabled) {
			logger.info("[" + pdfFile.getName() + "] " + data);
		}
	}

	private WorldGuardPlugin getWorldGuard() {
	    Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
	 
	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null;
	    }
	 
	    return (WorldGuardPlugin) plugin;
	}
	
	public boolean CanSpawnMob(Location loc) {
		// WorldGuard integration is completely disabled?
		if (WG == null) { return true; }
		
		// World has a RegionManager?
		RegionManager RM = RMS.get(loc.getWorld().getName());
		if (RM == null) { return true; }
		
		// Get the applicable region in this location
		ApplicableRegionSet set = RM.getApplicableRegions(loc);
		if (set == null) { return true; }
		
		// Return the value of the "mob-spawning" flag for this region  
		return set.allows(DefaultFlag.MOB_SPAWNING);
	}
	
	private void SetupMetrics() {
		// Upload stats
		try {
		    Metrics metrics = new Metrics(this);
		    if (metrics.isOptOut()) { return; }
		    
			Graph animalGraph = metrics.createGraph("Animals Spawned");
			
		    animalGraph.addPlotter(new Metrics.Plotter("Horses") {
	            @Override
	            public int getValue() {
	            	Debug("METRICS: Current donkey counter: " + MetricHorsesSpawned);
                    return MetricHorsesSpawned; // Number of horses spawned
	            }
	            @Override
	            public void reset() {
	            	Debug("METRICS: Resetting horse counter..");
	            	MetricHorsesSpawned = 0;
	            }
		    });

		    animalGraph.addPlotter(new Metrics.Plotter("Donkeys") {
	            @Override
	            public int getValue() {
	            	Debug("METRICS: Current donkey counter: " + MetricDonkeysSpawned);
                    return MetricDonkeysSpawned; // Number of donkeys spawned
	            }
	            @Override
	            public void reset() {
	            	Debug("METRICS: Resetting donkey counter..");
	            	MetricDonkeysSpawned = 0;
	            }
		    });		
		    
		    metrics.start();
		} catch (IOException e) {
		    // Failed to submit the stats :-(
			Warn("Metrics sending failed!!");
			e.printStackTrace();
		}
	}
}