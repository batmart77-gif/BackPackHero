package fr.uge.backpackhero.graphics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class responsible for loading and caching game images.
 * It provides a fallback placeholder if an image is missing to prevent the game from crashing.
 */
public class ImageLoader {
  private final Map<String, Image> images = new HashMap<>();

  /**
   * Retrieves an image by its name from the cache or loads it from resources.
   *
   * @param name the filename of the image (without extension).
   * @return the loaded Image, or a pink placeholder if the file is not found.
   */
  public Image getImage(String name) {
    Objects.requireNonNull(name);
    return images.computeIfAbsent(name, this::loadFromResources);
  }

  private Image loadFromResources(String name) {
    String[] paths = {"/resources/" + name + ".png", "/" + name + ".png", name + ".png"};
    for (String path : paths) {
      Image img = tryLoadPath(path);
      if (img != null) {
        return img;
      }
    }
    return createPlaceholder();
  }

  private Image tryLoadPath(String path) {
    InputStream stream = getClass().getResourceAsStream(path);
    if (stream == null) {
      return null;
    }
    try {
      return ImageIO.read(stream);
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Creates a default pink placeholder image for missing textures.
   * This ensures the graphical engine always has an object to draw.
   *
   * @return a 64x64 semi-transparent pink BufferedImage.
   */
  private Image createPlaceholder() {
    BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = img.createGraphics();
    g.setColor(new Color(255, 0, 255, 100));
    g.fillRect(0, 0, 64, 64);
    g.dispose();
    return img;
  }
}