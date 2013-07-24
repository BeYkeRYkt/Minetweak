package org.minetweak.plugins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.minetweak.Minetweak;

import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class PluginManager {

    private ArrayList<File> files = new ArrayList<File>();
    public static URLClassLoader loader;
    public static HashMap<String, IPlugin> plugins = new HashMap<String, IPlugin>();
    public static ArrayList<String> enabledPlugins = new ArrayList<String>();
    private Gson gson = new GsonBuilder().create();

    /**
     * Creates an instance of PluginLoader and runs setupPlugins
     */
    public static void initialize() {
        PluginManager loader = new PluginManager();
        loader.setupPlugins();
        enableAll();
    }

    /**
     * Enable a plugin - must be loaded
     * @param pluginName the plugin name
     */
    public static void enable(String pluginName) {
        if (doesPluginExist(pluginName)) {
            IPlugin plugin = plugins.get(pluginName);
            plugin.onEnable();
            enabledPlugins.add(pluginName);
        }
    }

    /**
     * Disable a plugin - must be enabled
     * @param pluginName the plugin name
     */
    public static void disable(String pluginName) {
        IPlugin plugin = plugins.get(pluginName);
        if (isPluginEnabled(pluginName)) {
            plugin.purgeRegistrations();
            plugin.onDisable();
            enabledPlugins.remove(pluginName);
        }
    }

    /**
     * Loads plugins
     */
    public void setupPlugins() {
        createDir();
        getPluginFiles();
        ArrayList<String> classes = new ArrayList<String>();
        ArrayList<URL> urls = new ArrayList<URL>();
        HashMap<String, PluginInfo> pluginInformation = new HashMap<String, PluginInfo>();
        for (File f : files) {
            PluginInfo pluginInfo = getPluginInfo(f);
            if (pluginInfo == null || pluginInfo.getMainClass()==null) {
                Minetweak.getLogger().logInfo("Skipping Plugin JAR: " + f.getName() + ": Missing plugin information or main class");
                continue;
            }
            pluginInformation.put(pluginInfo.getMainClass(), pluginInfo);
            try {
                urls.add(f.toURI().toURL());
                classes.add(pluginInfo.getMainClass());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        loader = new URLClassLoader(urls.toArray(new URL[urls.size()]), this.getClass().getClassLoader());
        for (String c : classes) {
            try {
                Class pc = Class.forName(c, true, loader);
                IPlugin plugin = (IPlugin) pc.newInstance();
                plugin.setPluginInfo(pluginInformation.get(c));
                // Note that we override plugins even if they exist. This allows for alphabetical file-name plugin overriding
                plugins.put(plugin.getPluginInfo().getName(), plugin);
            } catch (Exception e) {
                throw new RuntimeException("Error loading plugin", e);
            }
        }
    }

    /**
     * Enable all Plugins
     */
    public static void enableAll() {
        new PluginLoadingThread().start();
    }

    /**
     * Gets plugin info
     * @param file jar file
     * @return plugin info
     */
    private PluginInfo getPluginInfo(File file) {
        try {
            JarFile jf = new JarFile(file);
            ZipEntry entry = jf.getEntry("plugin.json");
            ZipEntry bukkitYaml = jf.getEntry("plugin.yaml");
            ZipEntry langFolder = jf.getEntry("lang/");
            if (entry==null) {
                if (bukkitYaml!=null) {
                    Minetweak.info("Found Bukkit Plugin in " + file.getName() + ". Skipping....");
                }
                return null;
            }
            if (langFolder != null) registerLanguageFiles(jf);
            return gson.fromJson(new InputStreamReader(jf.getInputStream(entry)), PluginInfo.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a list of Plugin files
     * NOTE: No longer uses NIO
     */
    private void getPluginFiles() {
        File pluginDirectory = new File("./plugins/");
        if (!pluginDirectory.exists()) {
            if (!pluginDirectory.mkdirs()) {
                Minetweak.getLogger().logWarning("Unable to create plugin directory. Skipping Loading Plugins.");
                return;
            }
        }
        File[] fileList = pluginDirectory.listFiles();
        if (fileList==null) {
            return;
        }
        for (File file : fileList) {
            if (file.isDirectory()) {
                continue;
            }
            if (file.getName().endsWith(".jar")) {
                files.add(file);
            }
        }
    }

    /**
     * Disables all Plugins
     */
    public static void disableAll() {
        ArrayList<String> pluginsToDisable = (ArrayList<String>) enabledPlugins.clone();
        for (String pluginName : pluginsToDisable) {
            disable(pluginName);
        }
    }

    /**
     * Disable plugins, then reload from plugins directory
     */
    public static void reloadPlugins() {
        disableAll();
        for (String pluginName : plugins.keySet()) {
            plugins.remove(pluginName);
        }
        initialize();
    }

    /**
     * Creates the plugins directory
     */
    private void createDir() {
        File f = new File("plugins");
        if (!f.isDirectory()) {
            if (!f.mkdir()) {
                throw new RuntimeException("Unable to create plugins folder!");
            }
        }
    }

    /**
     * Detect if a plugin is enabled
     * @param pluginName name of plugin
     * @return if the plugin is enabled
     */
    public static boolean isPluginEnabled(String pluginName) {
        return enabledPlugins.contains(pluginName);
    }

    /**
     * Detect if a plugin is registered. This does not mean they are enabled
     * @param pluginName name of plugin
     * @return if the plugin is registered
     */
    public static boolean doesPluginExist(String pluginName) {
        return plugins.keySet().contains(pluginName);
    }

    public void registerLanguageFiles(JarFile file) {

    }
}