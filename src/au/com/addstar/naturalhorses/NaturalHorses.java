package au.com.addstar.naturalhorses;
import java.util.Random;
import java.util.logging.Logger;
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
	public boolean DebugEnabled = true;
	public Random RandomGen = new Random();
	public WorldGuardPlugin WG;
	public RegionManager RM;
	public long LastSpawn = 0;

	// Pluing settings
	public String HorseWorld = "survival";
	public int SpawnDelay = 30;
	public int ChunkRadius = 2;
	public double SpawnChance = 0.01;
	public double DonkeyChance = 0.05;
	
	@Override
	public void onEnable() {
		// Register necessary events
		pdfFile = this.getDescription();
		pm = this.getServer().getPluginManager();
		pm.registerEvents(new ChunkListener(this), this);
		WG = getWorldGuard();
		if (WG == null) {
			instance.setEnabled(false);
			Log(" WorldGuard plugin NOT found!");
		} else {
			RM = WG.getRegionManager(getServer().getWorld(HorseWorld));
			Log("WorldGuard integration successful.");
			
		}
		
		Log(pdfFile.getName() + " " + pdfFile.getVersion() + " has been enabled");
	}
	
	@Override
	public void onDisable() {
		// Nothing yet
	}

	public void Log(String data) {
		logger.info(pdfFile.getName() + " " + data);
	}

	public void Warn(String data) {
		logger.warning(pdfFile.getName() + " " + data);
	}
	
	public void Debug(String data) {
		if (DebugEnabled) {
			logger.info(pdfFile.getName() + " " + data);
		}
	}

	private WorldGuardPlugin getWorldGuard() {
	    Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
	 
	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null; // Maybe you want throw an exception instead
	    }
	 
	    return (WorldGuardPlugin) plugin;
	}
	
	public boolean CanSpawnMob(Location loc) {
		ApplicableRegionSet set = RM.getApplicableRegions(loc);
		if (set == null) { return true; }
		return set.allows(DefaultFlag.MOB_SPAWNING);
	}
}

