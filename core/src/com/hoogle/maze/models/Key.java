package com.hoogle.maze.models;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Key extends GameObject {
    public Key(int initialX, int initialY, int height, int width, TextureRegion textureRegion, Skin skin) {
        super(initialX, initialY, height, width, textureRegion, skin);
    }
}
