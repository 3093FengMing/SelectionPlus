package me.fengming.selectionplus.config;

import com.google.gson.Gson;
import me.fengming.selectionplus.SelectionPlus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
    public Setting setting;
    public Lines lines;
    public Sides sides;
    public Blink blink;

    public static class Setting {
        public String mode;
        public int rainbowSpeed;
        public boolean displayConnected;
    }

    public static class Lines {
        public double thickness;
        public boolean rainbow;
        public int r;
        public int g;
        public int b;
        public int a;
    }

    public static class Sides {
        public boolean rainbow;
        public int r;
        public int g;
        public int b;
        public int a;
        public String picture;
    }

    public static class Blink {
        public boolean enable;
        public int speed;
        public int alpha;
    }

    public static Config readConfig(Path path) throws IOException {
        Gson gson = new Gson();
        try {
            String content = new String(Files.readAllBytes(path));
            return gson.fromJson(content, Config.class);
        } catch (NoSuchFileException e) {
            System.out.println("Config file not found, creating a new one");
            Config config = new Config();
            String content = gson.toJson(config);
            try {
                Files.write(path, content.getBytes());
            } catch (NoSuchFileException ex) {
                System.out.println("Directory not found, creating it");
                Files.createDirectories(path.getParent());
                Files.write(path, content.getBytes());
            }
            return config;
        }
    }
}
