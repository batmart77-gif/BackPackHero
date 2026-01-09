package fr.uge.backpackhero.graphics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class ImageManager {
    private final Map<String, Image> images = new HashMap<>();

    public Image getImage(String name) {
        return images.computeIfAbsent(name, n -> {
            // On essaie tous les chemins possibles pour Eclipse
            String[] paths = {"/resources/" + n + ".png", "/" + n + ".png", n + ".png"};
            for (String path : paths) {
                var stream = getClass().getResourceAsStream(path);
                if (stream != null) {
                    try { return ImageIO.read(stream); } catch (IOException ignored) {}
                }
            }
            System.out.println("Essai de chargement de : " + n);
            return createPlaceholder(); // NE RETOURNE JAMAIS NULL
        });
    }

    private Image createPlaceholder() {
        var img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        var g = img.createGraphics();
        g.setColor(new Color(255, 0, 255, 100)); // Rose semi-transparent
        g.fillRect(0, 0, 64, 64);
        g.dispose();
        return img;
    }
}