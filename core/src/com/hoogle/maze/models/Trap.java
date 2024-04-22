package com.hoogle.maze.models;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.TimeUtils;

public class Trap extends GameObject {
    public static final long DEATH_COOLDOWN_IN_MILLIS = 4000;
    private boolean active;
    private long inactiveTimeInMillis;

    //Constructor

    public Trap(int initialX, int initialY, int height, int width, TextureRegion textureRegion, Skin skin) {
        super(initialX, initialY, height, width, textureRegion, skin);
        active = true;
        loadCharacterAnimations();
    }

    //Methods

    /**
     * If the trap gets in contact with a player, it calls for the inactivate method
     * @param gameObject
     */
    @Override
    public void handleColision(GameObject gameObject) {
        if(gameObject instanceof Player player) {
            this.inactivate();
        }
    }

    /**
     * If time since the inactivation is greater than DEATH_COOLDOWN_IN_MILLIS, it activates the trap again
     */
    public void tryReactivate() {
        if(active)  return;

        if(TimeUtils.timeSinceMillis(this.inactiveTimeInMillis) < DEATH_COOLDOWN_IN_MILLIS)
            return;

        System.out.println("reactivating...");
        this.active = true;
        this.inactiveTimeInMillis = 0;
    }

    /**
     * Inactivates trap and starts counting the time since the inactivation
     */
    private void inactivate() {
        this.active = false;
        this.inactiveTimeInMillis = TimeUtils.millis();
        System.out.println("Inactivate Moment: " + inactiveTimeInMillis);
    }

    /**
     * Calls for the loadAnimationsFromFile from the upper class and loads the character animation from the objects.png file.
     */
    public void loadCharacterAnimations() {
        hasAnimation = true;
        int frameHeight = 16;
        int frameWidth = 16;

        int y = 3*frameHeight;
        int x = 4*frameWidth;

        currentAnimation = loadAnimationsFromFile("objects.png", 7, .1f, frameWidth, frameHeight, x, y);
    }

    //Getters & Setters

    public boolean isActive() {
        return active;
    }
}
