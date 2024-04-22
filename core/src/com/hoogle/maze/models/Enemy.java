package com.hoogle.maze.models;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.TimeUtils;

public class Enemy extends GameObject {

    private static long idCounter = 0;
    private long id;
    public static final long DEATH_COOLDOWN_IN_MILLIS = 4000;
    private boolean alive;
    private long deathTimeInMillis;
    private Direction direction;
    private double maxHorizontal;
    private double minHorizontal;
    private double maxVertical;
    private double minVertical;

    //Constructor
    public Enemy(int initialX, int initialY, int height, int width, TextureRegion textureRegion, Skin skin) {
        super(initialX, initialY, height, width, textureRegion, skin);
        id = idCounter++;
        alive = true;
        direction = Direction.START_TO_END;
        maxHorizontal = getCurrentFrameX();
        minHorizontal = getCurrentFrameX();
        maxVertical = getCurrentFrameY();
        minVertical = getCurrentFrameY();
        loadCharacterAnimations();
    }

    //Methods

    /**
     * Calls for the kill method if enemy gets in contact with a player
     * @param gameObject
     */
    @Override
    public void handleColision(GameObject gameObject) {
        if (gameObject instanceof Player) {
            this.kill();
        }
    }

    /**
     * Checks if enemy is set to move vertically or horizontally and makes it move accordingly.
     * If enemy is approaching its max value for x or y (where the walls are), its direction is changed
     */
    public void handleMovement() {
        var frameStep = 1;
        boolean turnAround = false;

        if(direction.equals(Direction.START_TO_END) && getAxis().equals(Axis.HORIZONTAL)) {
            initialX += frameStep;
            turnAround = (getCurrentFrameX() >= maxHorizontal);
        } else if (direction.equals(Direction.START_TO_END) && getAxis().equals(Axis.VERTICAL)) {
            initialY += frameStep;
            turnAround = (getCurrentFrameY() >= maxVertical);
        } else if (direction.equals(Direction.END_TO_START) && getAxis().equals(Axis.HORIZONTAL)) {
            initialX -= frameStep;
            turnAround = (getCurrentFrameX() < minHorizontal);
        } else if (direction.equals(Direction.END_TO_START) && getAxis().equals(Axis.VERTICAL)) {
            initialY -= frameStep;
            turnAround = (getCurrentFrameY() < minVertical);
        }

        if(turnAround)
            direction = direction.getInverted();

    }

    /**
     * Calls for the loadAnimationsFromFile from the upper class and loads the character animation from the mobs.png file.
     */
    private void loadCharacterAnimations() {
        hasAnimation = true;
        int frameWidth = 16;
        int frameHeight = 16;

        int y = 5 * frameHeight;
        int x = 6 * frameWidth;

        currentAnimation = loadAnimationsFromFile("mobs.png", 3, .1f, frameWidth, frameHeight, x, y);
    }

    /**
     * If enemy is not alive, checks if his death happened more than DEATH_COOLDOWN_IN_MILLIS, if yes, it sets alive to true again
     */
    public void tryRespawn() {
        if (alive) return;

        if (TimeUtils.timeSinceMillis(this.deathTimeInMillis) < DEATH_COOLDOWN_IN_MILLIS)
            return;

        System.out.println("respawing...");
        this.alive = true;
        this.deathTimeInMillis = 0;
    }

    /**
     * Sets alive to false and starts counting the time of death
     */
    public void kill() {
        this.alive = false;
        this.deathTimeInMillis = TimeUtils.millis();
        System.out.println("Death Moment: " + deathTimeInMillis);
    }

    //Getters & Setters

    public boolean isAlive() {
        return alive;
    }
    public long getId() {
        return id;
    }

    public double getMaxHorizontal() {
        return maxHorizontal;
    }

    public void setMaxHorizontal(double maxHorizontal) {
        this.maxHorizontal = maxHorizontal;
    }

    public double getMinHorizontal() {
        return minHorizontal;
    }

    public void setMinHorizontal(double minHorizontal) {
        this.minHorizontal = minHorizontal;
    }

    public double getMaxVertical() {
        return maxVertical;
    }

    public void setMaxVertical(double maxVertical) {
        this.maxVertical = maxVertical;
    }

    public double getMinVertical() {
        return minVertical;
    }

    public void setMinVertical(double minVertical) {
        this.minVertical = minVertical;
    }

    public double getDistVertical() {
        return maxVertical - minVertical + 1;
    }

    public double getDistHorizontal() {
        return maxHorizontal - minHorizontal + 1;
    }

    private Axis getAxis() {
        return getDistHorizontal() > getDistVertical() ? Axis.HORIZONTAL : Axis.VERTICAL;
    }

    @Override
    public String toString() {
        return "Enemy{" +
                "deathTimeInMillis=" + deathTimeInMillis +
                ", initialX=" + initialX +
                ", initialY=" + initialY +
                ", height=" + height +
                ", width=" + width +
                '}';
    }

    private enum Axis {
        HORIZONTAL,
        VERTICAL

    }

    private enum Direction {
        START_TO_END,
        END_TO_START;

        public Direction getInverted() {
            if (this == START_TO_END) {
                return END_TO_START;
            } else {
                return START_TO_END;
            }
        }
    }
}
