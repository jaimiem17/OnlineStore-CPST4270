# Happy Feet Marketplace - Installation & Run Guide

This guide explains how to build a runnable release (executable JAR) of the Happy Feet console marketplace and how end users can install and launch it. The project is plain Java and ships with everything it needs (including the H2 database driver) in the repository.

## Prerequisites
- Java 8+ (JDK for building, JRE acceptable for running a prebuilt JAR)
- Bash-compatible shell (commands are written for macOS/Linux; Windows users can run them in Git Bash or WSL)
- No external database install is required; the project uses the embedded `h2.jar` driver already in `lib/`

## Project Layout (relevant runtime assets)
- `src/` – Java sources (entry points: `src/main/Marketplace.java` and `src/main/Login.java`)
- `lib/h2.jar` – H2 JDBC driver packaged with the repo
- `marketplace.mv.db` – H2 file database that is created/used in the project root by default
- `Accounts.txt`, `Sellers.txt` – flat files used for account and seller data import

## Build a Release JAR
The application does not ship with a compiled binary. Use the following steps to create a runnable JAR that contains compiled classes and references the bundled H2 driver.

1. **Clean output folders (optional but recommended):**
   ```bash
   rm -rf out build
   ```
2. **Compile sources:**
   ```bash
   mkdir -p out
   javac -d out -cp "lib/h2.jar" $(find src -name "*.java")
   ```
3. **Create a manifest:**
   ```bash
   mkdir -p build
   cat > build/manifest.mf <<'MANIFEST'
   Main-Class: Marketplace
   Class-Path: ../lib/h2.jar

   MANIFEST
   ```
   - `Main-Class` points to the primary entry point. Switch to `Login` if you prefer the alternative flow.
4. **Package the release JAR:**
   ```bash
   jar cfm build/happy-feet-marketplace.jar build/manifest.mf -C out .
   ```

The resulting `build/happy-feet-marketplace.jar` is the distributable artifact to share with end users.

## Run the Application
You can run either the compiled JAR or the raw sources. The database and data files must reside in the same directory where you run the commands so the H2 file store and text files are found.

### Option A: Run the release JAR
```bash
java -cp "build/happy-feet-marketplace.jar:lib/h2.jar" Marketplace
```
- Add the jar and `lib/h2.jar` to the classpath so the H2 driver is available.
- On Windows, replace `:` with `;` in the classpath.

### Option B: Run directly from source
```bash
java -cp ".:lib/h2.jar" src/main/Marketplace
```
- Use `src/main/Login` instead if you want the alternate login-first workflow.

## Database Usage
- The application uses an embedded H2 database at `jdbc:h2:./marketplace` (username: `sa`, password: empty). The file store lives beside the runnable as `marketplace.mv.db` and is auto-created on first run.
- Database tables (users, rewards, products, orders, change log) are created automatically via `DatabaseManager.initializeTables()` when invoked by application logic.
- If you need to reset the database, stop the app and delete `marketplace.mv.db`; a fresh database will be generated on the next start.

## Data Files
- `Accounts.txt` and `Sellers.txt` are read from the working directory at runtime. Ensure they remain beside the executable/JAR if you want existing accounts and seller inventory to load.
- Legacy seller data is migrated to the newer multi-category format automatically. Warnings about skipped lines only affect malformed rows.

## Distributing to End Users
1. Provide the following files together in a folder: `build/happy-feet-marketplace.jar`, `lib/h2.jar`, `Accounts.txt`, `Sellers.txt`, and optionally any existing `marketplace.mv.db` you want to ship as seed data.
2. Instruct users to run `java -cp "build/happy-feet-marketplace.jar:lib/h2.jar" Marketplace` from that folder (or the Windows `;` variant).
3. If no `marketplace.mv.db` is supplied, the app will create a new one on first launch; existing text data will still be imported.

## Troubleshooting
- **`ClassNotFoundException: org.h2.Driver`** – Verify `lib/h2.jar` is present and included in the classpath.
- **`No such file or directory` for `Accounts.txt`/`Sellers.txt`** – Run the app from the repo root or copy those files next to the JAR.
- **Want to use a different database location?** Set the working directory to where you want the H2 files to live before running the app, or edit `DB_URL` in `src/util/DatabaseManager.java` and rebuild the JAR.
