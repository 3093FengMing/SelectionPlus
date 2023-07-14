package me.fengming.selectionplus;

import me.fengming.selectionplus.config.Config;
import me.fengming.selectionplus.config.RainbowColor;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;


public class SelectionPlus implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("selectionplus");
    public static final Config config;
    public static final TickTimer timer = new TickTimer();
    public static RainbowColor rainbow = new RainbowColor();

    static {
        try {
            config = Config.readConfig(MinecraftClient.getInstance().runDirectory.toPath().resolve("config/selectionplus/config.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Loaded Selection Plus!");
        timer.start();
    }
}
