package com.hoogle.maze.models;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Entry extends GameObject {
    public Entry(int initialX, int initialY, int height, int width, TextureRegion textureRegion, Skin skin) {
        super(initialX, initialY, height, width, textureRegion, skin);
    }
}
