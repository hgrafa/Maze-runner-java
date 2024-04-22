package com.hoogle.maze.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;

import java.util.Objects;

public abstract class GameObject {

    private static final double CELL_SIZE = 16.0;
    protected int initialX;
    protected int initialY;
    protected int height;
    protected int width;
    private Skin skin;
    protected boolean hasAnimation;
    protected Animation<TextureRegion> currentAnimation;
    protected TextureRegion textureRegion;

    //Constructors
    public GameObject(int height, int width, Skin skin) {
        initialX = 0;
        initialY = 0;
        this.height = height;
        this.width = width;
        this.skin = skin;
        this.hasAnimation = false;
    }

    public GameObject(int initialX, int initialY, int height, int width, Skin skin) {
        this(height, width, skin);
        this.initialX = initialX;
        this.initialY = initialY;
    }

    public GameObject(int initialX, int initialY, int height, int width, TextureRegion textureRegion, Skin skin) {
        this(initialX, initialY, height, width, skin);
        this.textureRegion = textureRegion;
    }


    /**
     * Uses a for-loop to take frames from the asset file and them into an Animation
     * @param assetFileName file from which frames shall be taken
     * @param numberOfFrames How many frames shall be taken from assetFile
     * @param frameRate Animation frames
     * @param frameWidth
     * @param frameHeight
     * @param x Initial x-position in the PNG file
     * @param y Initial y-position in the PNG file
     * @return object's animation
     */
    protected static Animation<TextureRegion> loadAnimationsFromFile(String assetFileName, int numberOfFrames, float frameRate, int frameWidth, int frameHeight, int x, int y) {
        Texture walkSheet = new Texture(Gdx.files.internal(assetFileName));

        Array<TextureRegion> walkFrames = new Array<>(TextureRegion.class);

        for (int col = 0; col < numberOfFrames; col++) {
            walkFrames.add(new TextureRegion(walkSheet, x + col * frameWidth, y, frameWidth, frameHeight));
        }

        return new Animation<>(frameRate, walkFrames);
    }

    public void handleColision(GameObject gameObject){
    }

    //Getters & Setters

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Skin getSkin() {
        return skin;
    }

    public void setSkin(Skin skin) {
        this.skin = skin;
    }

    public boolean hasAnimation() {
        return hasAnimation;
    }

    public Animation<TextureRegion> getCurrentAnimation() {
        return currentAnimation;
    }
    public void setCurrentAnimation(Animation<TextureRegion> currentAnimation) {
        this.currentAnimation = currentAnimation;
    }
    public int getInitialX() {
        return initialX;
    }

    public double getCurrentFrameX() {
        return initialX / CELL_SIZE;
    }

    public void setInitialX(int initialX) {
        this.initialX = initialX;
    }

    public int getFinalX() {
        return initialX + width;
    }

    public int getInitialY() {
        return initialY;
    }
    public double getCurrentFrameY() {
        return initialY / CELL_SIZE;
    }

    public void setInitialY(int initialY) {
        this.initialY = initialY;
    }

    public int getFinalY() {
        return initialY + height;
    }

    public void setTextureRegion(TextureRegion textureRegion) {
        this.textureRegion = textureRegion;
    }
    public TextureRegion getTextureRegion() {
        return textureRegion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameObject that = (GameObject) o;
        return initialX == that.initialX && initialY == that.initialY && height == that.height && width == that.width && hasAnimation == that.hasAnimation && Objects.equals(skin, that.skin) && Objects.equals(currentAnimation, that.currentAnimation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(initialX, initialY, height, width, skin, hasAnimation, currentAnimation);
    }
}
