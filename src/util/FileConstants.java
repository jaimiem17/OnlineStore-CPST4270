import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Central place for file paths used by the command-line app.
 *
 * NOTE:
 *  - We store the canonical project data in ./data/
 *  - If legacy files (./Sellers.txt, ./Accounts.txt) exist but ./data/* do not, we copy them over.
 *  - We do NOT delete legacy files, so older code still works.
 */
public final class FileConstants {
    public static final String DATA_DIR = "data";
    public static final String SELLERS_FILE = DATA_DIR + File.separator + "Sellers.txt";
    public static final String ACCOUNTS_FILE = DATA_DIR + File.separator + "Accounts.txt";

    private FileConstants() {}

    public static void ensureDataDir() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * If the project still has legacy root-level files, copy them into ./data so the
     * program can consistently read/write using the same paths.
     */
    public static void bootstrapLegacyFilesIfNeeded() {
        ensureDataDir();
        copyIfMissing(new File("Sellers.txt"), new File(SELLERS_FILE));
        copyIfMissing(new File("Accounts.txt"), new File(ACCOUNTS_FILE));
    }

    private static void copyIfMissing(File src, File dst) {
        try {
            if (dst.exists() || !src.exists()) {
                return;
            }
            Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // Non-fatal. If copying fails, we'll fall back to whatever exists.
        }
    }
}
