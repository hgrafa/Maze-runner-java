package com.hoogle.maze.models;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Wall extends GameObject {

    public Wall(int initialX, int initialY, int height, int width, TextureRegion textureRegion, Skin skin) {
        super(initialX, initialY, height, width, textureRegion, skin);
    }

    @Override
    public void setSkin(Skin skin) {

    }



    // x, y are the coordinates of your small image on the larger spritesheet
// width, height are the dimensions of the item, e.g. 16x16



// Now you can draw this region
/*              batch.begin();
                batch.draw(region, drawX, drawY); // drawX, drawY are screen coordinates where you want to draw the region
                batch.end();*/


    @Override
    public void handleColision(GameObject gameObject) {

    }

}
