package com.hoogle.maze.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.hoogle.maze.MazeRunnerGame;

public class Player extends GameObject {
    public static final int STEP = 1;
    public static final int TOTAL_NUMBER_OF_HEARTS = 4;
    private boolean[][] wallMap;
    private static Animation<TextureRegion> upAnimation;
    private static Animation<TextureRegion> downAnimation;
    private static Animation<TextureRegion> leftAnimation;
    private static Animation<TextureRegion> rightAnimation;
    private static TextureRegion fullHeart;
    private static TextureRegion emptyHeart;
    private boolean alive = true;
    private boolean hasKey = false;
    private boolean won = false;
    public int numberOfHearts = TOTAL_NUMBER_OF_HEARTS;
    private Array<TextureRegion> lifeFrames = new Array<>();
    private final MazeRunnerGame game;

    // Constructors
    public Player(MazeRunnerGame game, Skin skin, int height, int width, boolean[][] wallMap) {
        super(height, width, skin);
        loadStaticCharacterAnimations();
        loadStaticCharacterLifeVariants();
        this.game = game;
        this.wallMap = wallMap;
        hasAnimation = true;
        setCurrentAnimation(downAnimation);
        setFullLife();
    }

    public Player(MazeRunnerGame game, Skin skin, int initialX, int initialY, int height, int width, boolean[][] wallMap) {
        this(game, skin, height, width, wallMap);
        this.initialX = initialX;
        this.initialY = initialY;
    }

    // Methods

    /**
     * This method deals with main character movement and changes his animation
     */
    public void handleMovement() {
        setCurrentAnimation(downAnimation);

        int frameX = (int) getCurrentFrameX();
        int frameY = (int) getCurrentFrameY();

        System.out.println("current position: (" + frameX + ", " + frameY + ")");

        if (Gdx.input.isKeyPressed(Input.Keys.UP) && !wallMap[frameX][frameY + 1] ) {
            setCurrentAnimation(upAnimation);
            initialY += STEP;
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && frameX >= 0 && !wallMap[frameX][frameY]) {
            setCurrentAnimation(leftAnimation);
            initialX -= STEP;
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && !wallMap[frameX + 1][frameY]) {
            setCurrentAnimation(rightAnimation);
            initialX += STEP;
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) && !wallMap[frameX][frameY - 1]) {
            setCurrentAnimation(downAnimation);
            initialY -= STEP;
        }  else {
            System.out.println("COLLISION at" + initialX + initialY);
        }

    }

    private boolean isWallCollision(int nextX, int nextY) {
        for (int[] wallArray : game.getMapLoader().getWallCoordinates()) {
            int mapX = wallArray[0];
            int mapY = wallArray[1];

            if (mapX == nextX && mapY == nextY) {
                return true;
            }
        }
        return false;
    }

    /*public void handleMovementWithWalls() {

        setCurrentAnimation(downAnimation);

        int newX = initialX;
        int newY = initialY;

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {

            setCurrentAnimation(upAnimation);

            if (initialY + height < Gdx.graphics.getHeight()) {
                newY += STEP;
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            setCurrentAnimation(leftAnimation);
            if (initialX > 0) {
                newX -= STEP;
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            setCurrentAnimation(rightAnimation);

            if (initialX + 64 < Gdx.graphics.getWidth()) {
                newX += STEP;
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            setCurrentAnimation(downAnimation);

            if (initialY > 0) {
                newY -= STEP;
            }
        }

        // Check for wall collision before updating the player's position
        if (!isCollidingWithWall(newX, newY)) {
            // Update the player's position if there's no wall collision
            initialX = newX;
            initialY = newY;
        }
        //else System.out.println("no collision happening");
    }

    public boolean isCollidingWithWall(int newX, int newY) {
        int playerWidth = this.width;
        int playerHeight = this.height;
        int tolerance = 5; // Define the tolerance value

        for (int[] wallCoord : game.getMapLoader().getWallCoordinates()) {
            int wallX = wallCoord[0];
            int wallY = wallCoord[1];
            int wallWidth = Cell_Size;
            int wallHeight = Cell_Size;

            // Expand player bounds by tolerance
            int playerLeft = newX - tolerance;
            int playerRight = newX + playerWidth + tolerance;
            int playerTop = newY - tolerance;
            int playerBottom = newY + playerHeight + tolerance;

            // Check if expanded player bounds intersect with wall
            boolean overlapX = playerRight > wallX && playerLeft < wallX + wallWidth;
            boolean overlapY = playerBottom > wallY && playerTop < wallY + wallHeight;

            if (overlapX && overlapY) {
                Gdx.app.log("Collision", "Collision with wall at X: " + wallX + ", Y: " + wallY);
                return true; // Collision detected
            }
        }
        Gdx.app.log("Collision", "No collision at X: " + newX + ", Y: " + newY);
        return false; // No collision
    }*/



    /**
     * This method calls for different actions depending on the GameObject that collided with the player
     *
     * @param gameObject can be an enemy, a key, a trap or an exit
     */
    @Override
    public void handleColision(GameObject gameObject) {
        if (gameObject instanceof Enemy) {
            decrementHearts();
        }

        if (gameObject instanceof Trap) {
            decrementHearts();
        }

        if (gameObject instanceof Key) {
            hasKey = true;
        }

        if (gameObject instanceof Exit) {
            won = true;
        }
    }

    /**
     * Decreases the total number of hearts and changes the alive status to false if number of hearts gets to 0.
     * Changes the life frame to an empty heart according to how many hits were taken
     */
    public void decrementHearts() {
        if (!alive) {
            return;
        }
        numberOfHearts--;

        if (numberOfHearts == 0) {
            alive = false;
        }

        lifeFrames.set(numberOfHearts, emptyHeart);
    }


    /**
     * Loads the character life variants
     */
    public static void loadStaticCharacterLifeVariants() {
        Texture objects = new Texture(Gdx.files.internal("objects.png"));

        int frameWidth = 16;
        int frameHeight = 16;

        fullHeart = new TextureRegion(objects, 4 * frameWidth, 0, frameWidth, frameHeight);
        emptyHeart = new TextureRegion(objects, 8 * frameWidth, 0, frameWidth, frameHeight);
    }

    /**
     * Calls for the loadAnimationsFromFile from the upper class and loads the character animation from the character.png file.
     */
    private static void loadStaticCharacterAnimations() {
        downAnimation = loadAnimationsFromFile("character.png", 4, .1f, 16, 32, 0, 0);
        rightAnimation = loadAnimationsFromFile("character.png", 4, .1f, 16, 32, 0, 32);
        upAnimation = loadAnimationsFromFile("character.png", 4, .1f, 16, 32, 0, 64);
        leftAnimation = loadAnimationsFromFile("character.png", 4, .1f, 16, 32, 0, 96);
    }

    // Getters & Setters
    public void setFullLife() {
        lifeFrames.clear();
        numberOfHearts = 4;
        for (int currentHeart = 1; currentHeart <= TOTAL_NUMBER_OF_HEARTS; currentHeart++) {
            lifeFrames.add(fullHeart);
        }
    }

    public Array<TextureRegion> getLifeFrames() {
        return lifeFrames;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean hasKey() {
        return hasKey;
    }

    public boolean hasWon() {
        return won;
    }

    public void setHasKey(boolean hasKey) {
        this.hasKey = hasKey;
    }

}

