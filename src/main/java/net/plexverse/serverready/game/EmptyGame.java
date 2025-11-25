package net.plexverse.serverready.game;

import com.mineplex.studio.sdk.modules.MineplexModuleManager;
import com.mineplex.studio.sdk.modules.game.*;
import com.mineplex.studio.sdk.modules.game.event.PostMineplexGameStateChangeEvent;
import com.mineplex.studio.sdk.modules.game.event.PreMineplexGameStateChangeEvent;
import com.mineplex.studio.sdk.modules.world.MineplexWorld;
import lombok.NonNull;
import org.bukkit.entity.Player;

public class EmptyGame implements SingleWorldMineplexGame {

    @Override
    public @NonNull MineplexWorld getGameWorld() {
        return null;
    }

    @Override
    public @NonNull String getName() {
        return "";
    }

    @Override
    public @NonNull MineplexGameMechanicFactory getGameMechanicFactory() {
        return MineplexModuleManager.getRegisteredModule(MineplexGameMechanicFactory.class);
    }

    @Override
    public @NonNull GameState getGameState() {
        return BuiltInGameState.PRE_START;
    }

    @Override
    public void setGameState(@NonNull final GameState gameState) {

    }

    @Override
    public @NonNull PlayerState getPlayerState(@NonNull final Player player) {
        return BuiltInPlayerState.ALIVE;
    }

    @Override
    public void setup() {
        (new PreMineplexGameStateChangeEvent(this, BuiltInGameState.PREPARING, BuiltInGameState.PRE_START)).callEvent();
        (new PostMineplexGameStateChangeEvent(this, BuiltInGameState.PREPARING, BuiltInGameState.PRE_START)).callEvent();
    }

    @Override
    public void teardown() {

    }
}
