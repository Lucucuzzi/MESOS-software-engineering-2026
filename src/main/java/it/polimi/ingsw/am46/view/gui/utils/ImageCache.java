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

    // Larghezza di default per le carte — adatta ai tuoi asset
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
                // Caricamento dell'immagine con ottimizzazione per la memoria
                return new Image(
                        is,
                        CARD_WIDTH, 0, // width fissa, height proporzionale
                        true, // preserva aspect ratio
                        true // smooth scaling
                );
            } catch (Exception e) {
                System.err.println("ERRORE durante il caricamento di " + p + ": " + e.getMessage());
                return null;
            }
        });
    }

    /**
     * Carica l'immagine a dimensione originale — usare per sfondi e immagini grandi.
     *
     * @param resourcePath the resource path
     * @return the full
     */
    public static Image getFull(String resourcePath) {
        return cache.computeIfAbsent("full_" + resourcePath, p -> {
            try {
                InputStream is = ImageCache.class.getResourceAsStream(resourcePath);
                if (is == null) {
                    System.err.println("ERRORE: Immagine non trovata: " + resourcePath);
                    return null;
                }
                return new Image(is); // nessun ridimensionamento
            } catch (Exception e) {
                System.err.println("ERRORE caricamento " + resourcePath + ": " + e.getMessage());
                return null;
            }
        });
    }

    // * Precarica una lista di immagini in background.
    // * Chiamato da GUIView.start() subito dopo la creazione dello Stage,
    // * prima che il giocatore arrivi al tabellone.

    /**
     * Preload all.
     *
     * @param resourcePaths the resource paths
     */
// * @param resourcePaths lista di path da precaricare
    public static void preloadAll(List<String> resourcePaths) {
        if (resourcePaths == null) return;
        Thread t = new Thread(() -> {
            System.out.println("ImageCache: Preloading di " + resourcePaths.size() + " immagini iniziato...");
            resourcePaths.forEach(ImageCache::get);
            System.out.println("ImageCache: Preloading completato.");
        }, "image-preload-thread");
        // Impostato come Daemon così non blocca la chiusura dell'app se il thread è ancora attivo
        t.setDaemon(true);
        t.start();
    }

    //Pulisce la cache se necessario (utile in fase di test o reset gioco).

    /**
     * Clear.
     */
    public static void clear() {
        cache.clear();
    }


}
