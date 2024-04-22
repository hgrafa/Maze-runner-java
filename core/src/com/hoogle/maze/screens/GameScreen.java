package com.hoogle.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.hoogle.maze.GameState;
import com.hoogle.maze.MapLoader;
import com.hoogle.maze.MazeRunnerGame;
import com.hoogle.maze.models.*;
import com.hoogle.maze.scene.Hud;

/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {
    // Load the tiles using Gdx.files
    public final static Texture tileSheet = new Texture(Gdx.files.internal("basictiles.png"));
    // Load the objects using Gdx.files
    public final static Texture objectSheet = new Texture(Gdx.files.internal("objects.png"));
    public final static Texture keySheet = new Texture(Gdx.files.internal("key_small1.png"));
    // Load the mobs using Gdx.files
    public final static Texture mobsSheet = new Texture(Gdx.files.internal("mobs.png"));
    public final static Texture bigKey = new Texture(Gdx.files.internal("key_big.png"));

    private boolean isMapLoaded = false;
    private boolean isWallLoaded = false;

    private final MazeRunnerGame game;
    private final OrthographicCamera camera;
    //private final OrthographicCamera cameraHUD;
    private Viewport viewport;
    private Hud hud;
    private final BitmapFont font;
    private float sinusInput = 0f;
    private Texture spriteSheet;
    private Player player;
    private MapLoader mapLoader;
    private Exit exit;
    int Cell_Size = 16;
    final float tolerance = 5.0f;
    final float toleranceWall = 2.0f;


    /**
     * Constructor for GameScreen. Sets up the camera and font.
     *
     * @param game The main game class, used to access global resources and methods.
     * @param player
     */
    public GameScreen(MazeRunnerGame game, Player player) {
        this.game = game;
        this.player = player;
        // Create and configure the camera for the game view
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        camera.zoom = 0.3f;

        viewport = new FitViewport(1000,600,camera);

        hud = new Hud(game, this);

        // Get the font from the game's skin
        font = game.getPlayer().getSkin().getFont("font");
        System.out.println("Height: " + Gdx.graphics.getHeight() + ", Width: " + Gdx.graphics.getWidth());

        mapLoader = new MapLoader(game, this);

    }

    // Screen interface methods with necessary functionality
    @Override
    public void render(float delta) {

        switch (game.getGameState()) {

            case RUNNING -> {
                // Check for escape key press to go back to the menu
                if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                    game.setGameState(GameState.PAUSED);
                }

                ScreenUtils.clear(0, 0, 0, 1); // Clear the screen

                camera.update(); // Update the camera
                //cameraHUD.update();
                // Move text in a circular path to have an example of a moving object
                sinusInput += delta;
                float textX = (float) (camera.position.x + Math.sin(sinusInput) * 100);
                float textY = (float) (camera.position.y + Math.cos(sinusInput) * 100);

                // Set up and begin drawing with the sprite batch
                game.getSpriteBatch().setProjectionMatrix(camera.combined);

                game.getSpriteBatch().begin(); // Important to call this before drawing anything

                camera.position.set(player.getInitialX() + (float) player.getWidth() / 2, player.getInitialY() + (float) player.getHeight() / 2, 0); // fix camera on player


                //calls the mapLoader only once to assure take objects are not duplicated
                if (!isMapLoaded) {
                    mapLoader.populateMap();
                    mapLoader.loadWalls();
                    isMapLoaded = true;
                    System.out.println("map loaded!");
                    game.getBackgroundMusic().setLooping(true);
                    game.getBackgroundMusic().play();
                }

                mapLoader.drawWalls();


                //draws GameObjects (enemies are only drawn if they are alive
                game.getGameObjects().forEach((gameObject -> {
                    if (gameObject instanceof Enemy enemy) {
                        if (enemy.isAlive()) {
                            enemy.handleMovement();
                            drawGameObject(enemy);
                        }
                    } else {
                        drawGameObject(gameObject);
                    }
                }));

                drawGameObject(game.getPlayer());
                hud.render();


                //Deals with player movement
                handlePlayerMovementTroughMap();
                //Check if enemies can be drawn again
                respawnEnemiesAfterCooldown();
                //Check if traps can be activated again
                reactivateTrapsAfterCooldown();

                for (int[] coordinates : mapLoader.getExitCoordinates()) {
                    int exitX = coordinates[0];
                    int exitY = coordinates[1];
                    float deltaX = Math.abs(player.getInitialX() - exitX);
                    float deltaY = Math.abs(player.getInitialY() - exitY);

                    if (deltaX <= tolerance && deltaY <= tolerance && player.hasKey()) {
                        game.setGameState(GameState.WON);

                    }
                }

                if (!player.isAlive()) {
                    game.setGameState(GameState.LOST);

                }
                game.getSpriteBatch().end(); // Important to call this after drawing everything
            }

            case LOST -> game.loseGame();

            case WON -> game.winGame();

            case PAUSED -> game.goToIngameScreen();

        }
    }


    /**
     * Draws gameObjects on the Game Screen in the given position and, if present, with given animation
     * @param gameObject
     */
    private void drawGameObject(GameObject gameObject) {
        if(gameObject.hasAnimation()) {
            game.getSpriteBatch().draw(
                    gameObject.getCurrentAnimation().getKeyFrame(sinusInput, true),
                    gameObject.getInitialX(),
                    gameObject.getInitialY(),
                    gameObject.getWidth(),
                    gameObject.getHeight()
            );
        } else {
            game.getSpriteBatch().draw(
                    gameObject.getTextureRegion(),
                    gameObject.getInitialX(),
                    gameObject.getInitialY(),
                    gameObject.getWidth(),
                    gameObject.getHeight()
            );
        }
    }

    /**
     * Defines if a collision happened by checking is objects are in the same frame
     * @param obj1 GameObject 1
     * @param obj2 GameObject 2
     * @return true or false
     */
    public boolean checkCollision(GameObject obj1, GameObject obj2) {
        double errorTolerance = 0.6;

        double xDifference = Math.abs(obj1.getCurrentFrameX() - obj2.getCurrentFrameX());
        double yDifference = Math.abs(obj1.getCurrentFrameY() - obj2.getCurrentFrameY());

        boolean collisionX = xDifference <= errorTolerance;
        boolean collisionY = yDifference <= errorTolerance;

        return collisionX && collisionY;
    }


    /**
     * Deals with possible player movements given collision with traps, mobs and/or key
     */
    public void handlePlayerMovementTroughMap() {
        handlePlayerMovementTroughEnemies();
        handlePlayerMovementTroughTraps();

        //handlePlayerWallCollision();
        if(game.hasAvailableKey()) handlePlayerSearchForAKey();
        else handlePlayerSearchForAnExit();

        game.getPlayer().handleMovement();
        //game.getPlayer().handleMovementWithWalls();
    }

    /**
     * Checks if player is by the exit and has key
     */
    public void handlePlayerSearchForAnExit(){
        boolean playerHasFoundExit = checkCollision(game.getPlayer(), game.getExit());
        boolean playerHasFoundExitWithKey = playerHasFoundExit && game.getPlayer().hasKey();

        if(playerHasFoundExitWithKey) {
            System.out.println("you won");
            game.getPlayer().handleColision(game.getExit());
        }
    }

    /**
     * Checks is player is in the same position as a key
     */
    public void handlePlayerSearchForAKey() {
        boolean playerHasFoundKey = checkCollision(game.getPlayer(), game.getKey());

        if(playerHasFoundKey) {
            System.out.println("key founded!");
            player.handleColision(game.getKey());
            game.collectKey();
            game.getKeySound().play();
        }
    }

    /**
     * Goes through the Array of enemies and checks if any of them collided with the player
     */
    public void handlePlayerMovementTroughEnemies(){
        Array<Enemy> enemiesWithCollision = new Array<>();

        for (Enemy enemy : game.getEnemies()) {
            if (checkCollision(game.getPlayer(), enemy) && enemy.isAlive())
                enemiesWithCollision.add(enemy);
        }

        boolean hasCollisionWithAnyEnemy = enemiesWithCollision.size > 0;

        if(hasCollisionWithAnyEnemy) {
            System.out.println("=> there are some enemy collision at x: " + game.getPlayer().getInitialX() + ", y: " + game.getPlayer().getInitialY() );
            enemiesWithCollision.forEach(System.out::println);

            enemiesWithCollision.forEach((enemy) -> {
                    game.getHitSound().play();
                    game.getPlayer().handleColision(enemy);
                    enemy.handleColision(game.getPlayer());
            });
        }
    }

    /**
     * Goes through the Array of traps and checks if any of them collided with the player
     */
    public void handlePlayerMovementTroughTraps() {
        Array<Trap> trapsWithCollision = new Array<>();

        for (Trap trap : game.getTraps()) {
            if (checkCollision(game.getPlayer(), trap) && trap.isActive())
                trapsWithCollision.add(trap);
        }

        boolean hasCollisionWithAnyTrap = trapsWithCollision.size > 0;

        if(hasCollisionWithAnyTrap) {
            System.out.println("=> there are some trap collision at x: " + game.getPlayer().getInitialX() + ", y: " + game.getPlayer().getInitialY() );

            trapsWithCollision.forEach((trap) -> {
                    game.getHitSound().play();
                    game.getPlayer().handleColision(trap);
                    trap.handleColision(game.getPlayer());
            });
        }
    }


    public void handlePlayerWallCollision() {
        for (int[] coordinates : mapLoader.getWallCoordinates()) {
            int wallX = coordinates[0];
            int wallY = coordinates[1];


            if (player.getInitialX() + player.getWidth() - toleranceWall >= wallX &&
                    wallX + Cell_Size - toleranceWall >= player.getInitialX() &&
                    player.getInitialY() + player.getHeight() - toleranceWall >= wallY &&
                    wallY + Cell_Size - toleranceWall >= player.getInitialY()) {
                return;
            }
        }
    }


    /**
     * Checks if enemies can be drawn again
     */
    private void respawnEnemiesAfterCooldown() {
        for(Enemy enemy : game.getEnemies()) {
            enemy.tryRespawn();
        }
    }

    /**
     * Checks if traps can be activated again
     */
    private void reactivateTrapsAfterCooldown() {
        for(Trap trap : game.getTraps()) {
            trap.tryReactivate();
        }
    }

    public void setMapLoaded(boolean mapLoaded) {
        isMapLoaded = mapLoaded;
    }

    public Viewport getViewport() {
        return viewport;
    }

    @Override
    public void resize(int width, int height) {

        Vector3 camerpos = camera.position.cpy();
        camera.setToOrtho(false);
        camera.position.set(camerpos);


    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }

}
