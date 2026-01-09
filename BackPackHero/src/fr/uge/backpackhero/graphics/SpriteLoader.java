package fr.uge.backpackhero.graphics;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.IOException;
import java.util.HashMap;

public class SpriteLoader {
    private final HashMap<String, Image> cache = new HashMap<>();

    public Image get(String name) {
        return cache.computeIfAbsent(name, n -> {
            try {
                // On charge depuis /resources/ comme vu sur ta capture d'écran
                var path = "/resources/" + n + ".png";
                var stream = getClass().getResourceAsStream(path);
                if (stream == null) {
                    System.err.println("⚠️ Image manquante : " + path);
                    return null;
                }
                return ImageIO.read(stream);
            } catch (IOException e) {
                throw new RuntimeException("Erreur chargement : " + n, e);
            }
        });
    }
}