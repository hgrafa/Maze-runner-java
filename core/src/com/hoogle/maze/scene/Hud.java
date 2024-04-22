package com.hoogle.maze.scene;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.hoogle.maze.MazeRunnerGame;
import com.hoogle.maze.screens.GameScreen;

import static com.hoogle.maze.models.Player.TOTAL_NUMBER_OF_HEARTS;

public class Hud {
    private Viewport viewportHUD;
    private GameScreen gameScreen;
    private TextureRegion bigKey;
    private MazeRunnerGame game;
    private OrthographicCamera camerHUD;
    public int numberOfHearts = TOTAL_NUMBER_OF_HEARTS;
    private Array<TextureRegion> lifeFrames = new Array<>();


    public Hud(MazeRunnerGame game, GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;
        camerHUD = new OrthographicCamera();
        camerHUD.setToOrtho(false, gameScreen.getViewport().getWorldWidth(), gameScreen.getViewport().getWorldHeight());
        viewportHUD = new FitViewport(gameScreen.getViewport().getWorldWidth(), gameScreen.getViewport().getWorldHeight(), gameScreen.getViewport().getCamera());

        //https://www.youtube.com/watch?v=7idwNW5a8Qs

    }

    public void render() {

        camerHUD.update();
        game.getSpriteBatch().setProjectionMatrix(camerHUD.combined);

        drawLifeFrames();
        drawKey();


    }

    private void drawLifeFrames() {
        if (game.getPlayer().isAlive()) {
            for (int i = 0; i < game.getPlayer().getLifeFrames().size; i++) {
                TextureRegion lifeFrame = game.getPlayer().getLifeFrames().get(i);
                int heartWidth = 24;
                int heartHeight = 24;
                int x = 4 + i * heartWidth;
                int y = 4;
                game.getSpriteBatch().draw(lifeFrame, x, y, heartWidth, heartHeight);
            }
        }
    }

    private void drawKey() {
        if(!game.getPlayer().hasKey()) {
            BitmapFont font = new BitmapFont(); // Create a BitmapFont instance
            Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);

            Label keyStatus = new Label("YOU HAVE NO KEY!", labelStyle);
            keyStatus.setPosition(120, 4);

            keyStatus.draw(game.getSpriteBatch(), 1); // Draw the label

        }

        else {
            bigKey = new TextureRegion(GameScreen.bigKey, 0, 0, 32, 32); // define the Textureregion for WALLS
            game.getSpriteBatch().draw(bigKey,120,4);
        }
    }

    public void deleteKey(){

    }

    public void clearHUD() {
        // Dispose of any resources (optional)
        // Reset HUD to initial state
        numberOfHearts = TOTAL_NUMBER_OF_HEARTS;
        lifeFrames.clear();



        // Assuming lifeFrames is an Array<TextureRegion>
        // Add code to reinitialize or reset other HUD elements if needed
    }


    /*public void reset() {
        // Reset any state-specific variables in your Hud
        numberOfHearts = TOTAL_NUMBER_OF_HEARTS;
        // Clear any existing life frames
        lifeFrames.clear();

        // Initialize the life frames again
        for (int i = 0; i < numberOfHearts; i++) {
            lifeFrames.add(fullHeart); // You need to have fullHeart initialized
        }
    }*/
public void resize() {

    camerHUD.setToOrtho(false);
}


}


