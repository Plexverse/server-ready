package net.plexverse.serverready;

import net.plexverse.serverready.game.EmptyGame;
import lombok.Getter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main plugin class for ServerReady.
 * Handles plugin loading coordination and game state initialization.
 */
@Getter
public class ServerReady extends JavaPlugin implements Listener {

    private static final int REQUIRED_STABLE_TICKS = 20; // 1 second of stable state

    @Override
    public void onEnable() {
        // Wait for all other plugins to be enabled or failed
        new PluginLoadingCoordinator().runTaskTimer(this, 1L, 1L);
    }

    /**
     * Sets up the game state by initializing the EmptyGame.
     */
    private static void setState() {
        new EmptyGame().setup();
    }

    /**
     * Coordinates the loading of all plugins and manages the transition to game state.
     * Monitors plugin states and ensures all plugins have finished loading before proceeding.
     */
    private class PluginLoadingCoordinator extends BukkitRunnable {
        private final Map<String, Boolean> pluginStates = new HashMap<>();
        private final List<String> warnedPlugins = new ArrayList<>();
        private int stableTicks = 0;

        @Override
        public void run() {
            final PluginStateSnapshot snapshot = this.createPluginStateSnapshot();

            if (this.hasPluginStatesChanged(snapshot.getCurrentStates())) {
                this.updatePluginStates(snapshot.getCurrentStates());
            } else {
                this.stableTicks++;
            }

            this.warnAboutFailedPlugins(snapshot.getFailedPlugins());

            if (this.shouldProceedToGameState(snapshot.isAllPluginsFinished())) {
                this.proceedToGameState();
            }
        }

        /**
         * Creates a snapshot of the current plugin states.
         *
         * @return PluginStateSnapshot containing current plugin information
         */
        private PluginStateSnapshot createPluginStateSnapshot() {
            final PluginManager pluginManager = ServerReady.this.getServer().getPluginManager();
            final Map<String, Boolean> currentStates = new HashMap<>();
            final List<String> failedPlugins = new ArrayList<>();
            boolean allPluginsFinished = true;

            for (final Plugin plugin : pluginManager.getPlugins()) {
                if (!plugin.equals(ServerReady.this)) {
                    final boolean isEnabled = plugin.isEnabled();
                    currentStates.put(plugin.getName(), isEnabled);

                    if (!isEnabled) {
                        allPluginsFinished = false;
                        failedPlugins.add(plugin.getName());
                    }
                }
            }

            return new PluginStateSnapshot(currentStates, failedPlugins, allPluginsFinished);
        }

        /**
         * Checks if plugin states have changed since the last check.
         *
         * @param currentStates Current plugin states
         * @return true if states have changed, false otherwise
         */
        private boolean hasPluginStatesChanged(final Map<String, Boolean> currentStates) {
            return !currentStates.equals(this.pluginStates);
        }

        /**
         * Updates the stored plugin states and resets the stability counter.
         *
         * @param currentStates New plugin states to store
         */
        private void updatePluginStates(final Map<String, Boolean> currentStates) {
            this.pluginStates.clear();
            this.pluginStates.putAll(currentStates);
            this.stableTicks = 0;
        }

        /**
         * Warns about failed plugins (only once per plugin).
         *
         * @param failedPlugins List of failed plugin names
         */
        private void warnAboutFailedPlugins(final List<String> failedPlugins) {
            for (final String failedPlugin : failedPlugins) {
                if (!this.warnedPlugins.contains(failedPlugin)) {
                    ServerReady.this.getLogger().warning(
                            "Plugin '" + failedPlugin + "' failed to load but continuing with game state setup."
                    );
                    this.warnedPlugins.add(failedPlugin);
                }
            }
        }

        /**
         * Determines if the system should proceed to game state initialization.
         *
         * @param allPluginsFinished Whether all plugins have finished loading
         * @return true if you should proceed, false otherwise
         */
        private boolean shouldProceedToGameState(final boolean allPluginsFinished) {
            return this.stableTicks >= ServerReady.REQUIRED_STABLE_TICKS && allPluginsFinished;
        }

        /**
         * Proceeds to game state initialization and logs the status.
         */
        private void proceedToGameState() {
            if (!this.warnedPlugins.isEmpty()) {
                ServerReady.this.getLogger().info(
                        "All plugins have finished loading. Setting up game state with " +
                                this.warnedPlugins.size() + " failed plugin(s)."
                );
            } else {
                ServerReady.this.getLogger().info(
                        "All plugins have finished loading successfully. Setting up game state."
                );
            }
            ServerReady.setState();
            this.cancel();
        }
    }

    /**
     * Snapshot of plugin states at a given moment.
     * Contains information about current plugin states, failed plugins, and overall completion status.
     */
    private static class PluginStateSnapshot {
        private final Map<String, Boolean> currentStates;
        private final List<String> failedPlugins;
        private final boolean allPluginsFinished;

        /**
         * Creates a new plugin state snapshot.
         *
         * @param currentStates      Current plugin states
         * @param failedPlugins      List of failed plugin names
         * @param allPluginsFinished Whether all plugins have finished loading
         */
        public PluginStateSnapshot(final Map<String, Boolean> currentStates,
                                   final List<String> failedPlugins,
                                   final boolean allPluginsFinished) {
            this.currentStates = currentStates;
            this.failedPlugins = failedPlugins;
            this.allPluginsFinished = allPluginsFinished;
        }

        public Map<String, Boolean> getCurrentStates() {
            return this.currentStates;
        }

        public List<String> getFailedPlugins() {
            return this.failedPlugins;
        }

        public boolean isAllPluginsFinished() {
            return this.allPluginsFinished;
        }
    }
}