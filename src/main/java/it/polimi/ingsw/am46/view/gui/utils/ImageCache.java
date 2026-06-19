package it.polimi.ingsw.am46.view.gui.utils;

import javafx.scene.image.Image;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.io.InputStream;

// * Centralized cache for JavaFX images.

// * All game images are loaded only once and
// * held in memory for the lifetime of the application.
//
// * Thread-safe: ConcurrentHashMap allows concurrent reads and writes
// * without explicit locks.

/**
 * The type Image cache.
 */
public class ImageCache {

    // Default card width — suitable for your assets
    private static final double CARD_WIDTH = 110;
    private static final Map<String, Image> cache = new ConcurrentHashMap<>();
    // * Returns the image to the indicated path.
    // * If it is not yet in cache it loads it, otherwise it returns
    // * the one already stored.
    // * Can be called from any thread
    // * @param resourcePath absolute path in the JAR, e.g. "/images/cards/card_1.png

    /**
     * Get image.
     *
     * @param resourcePath the resource path
     * @return the image
     */
    public static Image get(String resourcePath) {
        return cache.computeIfAbsent(resourcePath, p -> {
            try {
                InputStream is = ImageCache.class.getResourceAsStream(p);
                if (is == null) {
                    System.err.println("ERROR: Image not found at path: " + p);
                    return null;
                }
                // Load image with memory optimization
                return new Image(
                        is,
                        CARD_WIDTH, 0, // fixed width, proportional height
                        true, // preserve aspect ratio
                        true // smooth scaling
                );
            } catch (Exception e) {
                System.err.println("ERROR loading " + p + ": " + e.getMessage());
                return null;
            }
        });
    }

    /**
     * Loads the image at original size — use for backgrounds and large images.
     *
     * @param resourcePath the resource path
     * @return the image
     */
    public static Image getFull(String resourcePath) {
        return cache.computeIfAbsent("full_" + resourcePath, p -> {
            try {
                InputStream is = ImageCache.class.getResourceAsStream(resourcePath);
                if (is == null) {
                    System.err.println("ERROR: Image not found: " + resourcePath);
                    return null;
                }
                return new Image(is); // no resizing
            } catch (Exception e) {
                System.err.println("ERROR loading " + resourcePath + ": " + e.getMessage());
                return null;
            }
        });
    }

    // * Preloads a list of images in background.
    // * Called from GUIView.start() right after Stage creation,
    // * before the player reaches the game board.

    /**
     * Preload all.
     *
     * @param resourcePaths the resource paths
     */
// * @param resourcePaths list of paths to preload
    public static void preloadAll(List<String> resourcePaths) {
        if (resourcePaths == null) return;
        Thread t = new Thread(() -> {
            System.out.println("ImageCache: Preloading " + resourcePaths.size() + " images started...");
            resourcePaths.forEach(ImageCache::get);
            System.out.println("ImageCache: Preloading completed.");
        }, "image-preload-thread");
        // Set as Daemon so it doesn't block app shutdown if the thread is still active
        t.setDaemon(true);
        t.start();
    }

    //Clears the cache if needed (useful during testing or game reset).

    /**
     * Clear.
     */
    public static void clear() {
        cache.clear();
    }


}
