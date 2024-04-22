package com.hoogle.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.hoogle.maze.models.*;
import com.hoogle.maze.scene.Hud;
import com.hoogle.maze.screens.*;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import games.spooky.gdx.nativefilechooser.NativeFileChooserIntent;

import java.util.HashMap;
import java.util.Map;

/**
 * The MazeRunnerGame class represents the core of the Maze Runner game.
 * It manages the screens and global resources like SpriteBatch and Skin.
 */
public class MazeRunnerGame extends Game {
    // Screens
    private MenuScreen menuScreen;
    private GameScreen gameScreen;
    private InGameMenuScreen inGameMenuScreen;
    private LoseScreen loseScreen;
    private WinScreen winScreen;

    private Player player;
    private Array<Enemy> enemies;
    private Array<Trap> traps;
    private Key key;
    private Exit exit;
    private Array<GameObject> gameObjects;
    private Map<int[], Integer> coordinatesMap = new HashMap<int[], Integer>();
    private MapLoader mapLoader;
    private GameState gameState;
    private NativeFileChooser fileChooser;
    // Sprite Batch for rendering
    private SpriteBatch spriteBatch;
    // private final int scale = 4;
    private Hud hud;
    private boolean isMapLoaded;
    private boolean[][] hasWall = new boolean[10000][10000];
    private Music backgroundMusic;
    private Music keySound;
    private Music hitSound;
    private Music gameOver;
    private Music victory;
    private Music menuMusic;

    /**
     * Constructor for MazeRunnerGame.
     *
     * @param fileChooser The file chooser for the game, typically used in desktop environment.
     */
    public MazeRunnerGame(NativeFileChooser fileChooser) {
        super();
        this.fileChooser = fileChooser;
        this.enemies = new Array<>();
        this.traps = new Array<>();
        this.gameObjects = new Array<>();
        this.gameState = GameState.PAUSED;
    }

    /**
     * Called when the game is created. Initializes the SpriteBatch and Skin.
     */
    @Override
    public void create() {
        spriteBatch = new SpriteBatch(); // Create SpriteBatch
        player = new Player(
                this,
                new Skin(Gdx.files.internal("craft/craftacular-ui.json")),
                16,
                12,
                hasWall
        );

        // Play some background music
        // Background sound
        loadMusic();
        //handleScreens();
        mapLoader = new MapLoader(this, gameScreen);
        goToMenu(); // Navigate to the menu screen

    }

    /**
     * Switches to the menu screen.
     */
    public void goToMenu() {
        this.setScreen(new MenuScreen(this)); // Set the current screen to MenuScreen
        setGameState(GameState.PAUSED);
        if (gameScreen != null) {
            gameScreen.dispose(); // Dispose the game screen if it exists
            gameScreen = null;
        }
        menuMusic.setLooping(true);
        menuMusic.play();

    }

    /**
     * Switches to the game screen.
     */
    public void goToGame() {

        if (gameScreen == null) {
                gameScreen = new GameScreen(this, player); // Only create a new GameScreen if it doesn't exist
        }

        this.setScreen(gameScreen);
        setGameState(GameState.RUNNING);
        menuMusic.stop();

            // Clean up other screens if necessary
        if (menuScreen != null) {
            menuScreen.dispose(); // Dispose the menu screen if it exists
            menuScreen = null;
        }
        if (winScreen != null) {
            winScreen.dispose(); // Dispose the menu screen if it exists
            winScreen = null;
        }
        if (loseScreen != null) {
            loseScreen.dispose();
            loseScreen = null;
        }
    }


    public void returnToGame() {
        if (gameScreen != null) {
            this.setScreen(gameScreen); // Set the existing GameScreen
            setGameState(GameState.RUNNING);
        }
        // Dispose the InGameMenuScreen if necessary
        if (inGameMenuScreen != null) {
            inGameMenuScreen.dispose();
            inGameMenuScreen = null;
        }
    }

    public void goToIngameScreen() {

        this.setScreen(new InGameMenuScreen(this)); // Set the current screen to GameScreen


    }

    public void handleScreens() {
        switch (getGameState()) {
            case PAUSED -> goToMenu();
            case RUNNING -> goToGame();
            case WON -> winGame();
            case LOST -> loseGame();
        }
    }

    public void winGame() {
        this.setScreen(new WinScreen(this));
        // setGameState(GameState.RUNNING);
        getGameObjects().clear();
        getCoordinatesMap().clear();
        getEnemies().clear();
        getTraps().clear();
        player.setFullLife();
        player.setHasKey(false);
        player.setAlive(true);
        victory.play();
        backgroundMusic.stop();
        if (gameScreen != null) {
            gameScreen.dispose(); // Dispose the menu screen if it exists
            gameScreen = null;
        }
    }

    public void loseGame() {

        getGameObjects().clear();
        getCoordinatesMap().clear();
        getEnemies().clear();
        getTraps().clear();
        setKey(null);
        player.setFullLife();
        player.setAlive(true);
        setMapLoaded(false);
        backgroundMusic.stop();
        gameOver.play();
        // setGameState(GameState.RUNNING);
        this.setScreen(new LoseScreen(this));
        if (gameScreen != null) {
            gameScreen.dispose(); // Dispose the menu screen if it exists
            gameScreen = null;
        }

    }

    /**
     * Switches to the file chooser.
     */

    public void showFileChooser() {

        var fileChooserConfig = new NativeFileChooserConfiguration();
        fileChooserConfig.title = "Pick a maze file"; // Title of the window that will be opened
        fileChooserConfig.intent = NativeFileChooserIntent.OPEN; // We want to open a file
        fileChooserConfig.nameFilter = (file, name) -> name.endsWith("properties"); // Only accept .properties files
        fileChooserConfig.directory = Gdx.files.absolute(System.getProperty("user.home")); // Open at the user's home directory

        fileChooser.chooseFile(fileChooserConfig, new NativeFileChooserCallback() {
            @Override
            public void onFileChosen(FileHandle fileHandle) {
                // Do something with fileHandle
                // Do something with fileHandle
                if (gameScreen != null) {
                    gameScreen.dispose();
                    gameScreen = null;
                }

                getGameObjects().clear();
                getCoordinatesMap().clear();
                getEnemies().clear();
                getTraps().clear();
                setKey(null);
                player.setHasKey(false);
                player.setFullLife();
                player.setAlive(true);


                String fileContent = fileHandle.readString();
                // System.out.println("File Content: " + fileContent);

                String[] splitContents = fileContent.split("\n");

                for (String splitContent : splitContents) {

                    splitContent = splitContent.trim(); // Trim leading and trailing whitespace
                    //System.out.println("Split Content: " + splitContent);
                    String[] pairs = splitContent.split("=");
                    String[] coordinates = pairs[0].split(",");

                    try {
                        int x = Integer.parseInt(coordinates[0]);
                        int y = Integer.parseInt(coordinates[1]);
                        int objectType = Integer.parseInt(pairs[1]);
                        int[] coordinatesXY = new int[]{x, y};

                        //System.out.println("Parsed Data: x=" + x + ", y=" + y + ", objectType=" + objectType);
                        coordinatesMap.put(coordinatesXY, objectType);
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing data: " + e.getMessage());
                    }
                    System.out.println("Filechooser working");
                }

                goToGame();

            }

            @Override
            public void onCancellation() {
                // User closed the window, don't need to do anything
            }

            @Override
            public void onError(Exception exception) {
                System.err.println("Error picking maze file: " + exception.getMessage());
            }
        });

    }

    private void loadMusic() {
          backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("background.mp3"));
          keySound = Gdx.audio.newMusic(Gdx.files.internal("keySound.wav"));
          hitSound = Gdx.audio.newMusic(Gdx.files.internal("man-scream-121085.mp3"));
          gameOver = Gdx.audio.newMusic(Gdx.files.internal("gameOver.wav"));
          victory = Gdx.audio.newMusic(Gdx.files.internal("victory.mp3"));
          menuMusic = Gdx.audio.newMusic(Gdx.files.internal("menu music.mp3"));
    }

    /**
     * Cleans up resources when the game is disposed.
     */
    @Override
    public void dispose() {
        getScreen().hide(); // Hide the current screen
        getScreen().dispose(); // Dispose the current screen
        spriteBatch.dispose(); // Dispose the spriteBatch
        player.getSkin().dispose(); // Dispose the skin
    }

    // Getter methods
    public Key getKey() {
        return key;
    }

    public boolean hasAvailableKey() {
        return key != null;
    }

    public void collectKey() {
        this.gameObjects.removeValue(key, false);
        this.key = null;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public Skin getSkin() {
        return player.getSkin();
    }

    public Player getPlayer() {
        return player;
    }

    public Array<Enemy> getEnemies() {
        return enemies;
    }

    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    public void removeEnemy(Enemy enemy) {
        enemies.removeValue(enemy, false);
    }

    public Array<Trap> getTraps() {
        return traps;
    }

    public void addTrap(Trap trap) {
        traps.add(trap);
    }

    public void removeTrap(Trap trap) {
        traps.removeValue(trap, false);
    }

    public Array<GameObject> getGameObjects() {
        return gameObjects;
    }

    public void addGameObject(GameObject gameObject) {
        gameObjects.add(gameObject);
    }

    public void removeGameObject(GameObject gameObject) {
        gameObjects.removeValue(gameObject, false);
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    public boolean hasWallAt(int x, int y) {
        return hasWall[x][y];
    }

    public void setWall(int x, int y) {
        hasWall[x][y] = true;
    }

    // GETTER SETTER FOR STATE ENUMs
    public void setMapLoaded(boolean mapLoaded) {
        isMapLoaded = mapLoaded;
    }

    public boolean isMapLoaded() {
        return isMapLoaded;
    }

    public Hud getHud() {
        return hud;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public Map<int[], Integer> getCoordinatesMap() {
        return coordinatesMap;
    }

    public MapLoader getMapLoader() {
        return mapLoader;
    }
    public Exit getExit() {
        return exit;
    }

    public void setExit(Exit exit) {
        this.exit = exit;
    }

    public Music getBackgroundMusic() {
        return backgroundMusic;
    }

    public Music getKeySound() {
        return keySound;
    }

    public Music getHitSound() {
        return hitSound;
    }

    public Music getGameOver() {
        return gameOver;
    }

    public Music getVictory() {
        return victory;
    }

    public Music getMenuMusic() {
        return menuMusic;
    }
}
