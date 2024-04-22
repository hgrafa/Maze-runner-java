package com.hoogle.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.hoogle.maze.models.*;
import com.hoogle.maze.screens.GameScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapLoader {

    private final MazeRunnerGame game;
    private GameScreen gameScreen;
    private final int Cell_Size = 16;
    private final int Scale = 4;
    private int mapX;
    private int mapY;
    List<int[]> exitCoordinates = new ArrayList<>();
    List<int[]> wallCoordinates = new ArrayList<>();
    private int mapObjectType;
    private static TextureRegion textureRegionFor0;
    private static TextureRegion textureRegionFor1;
    private static TextureRegion textureRegionFor2;
    private static TextureRegion textureRegionFor3;
    private static TextureRegion textureRegionFor4;
    private static TextureRegion textureRegionFor5;
    private static final String SKIN_PATH = "craft/craftacular-ui.json";
    private static Skin sharedSkin;

    static {
        loadTextures();
        sharedSkin = new Skin(Gdx.files.internal(SKIN_PATH));
    }

    public MapLoader(MazeRunnerGame game, GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;
        loadTextures();
    }


    /**
     * Creates a new GameObject according to the matrix coordinates from the chosen map
     */
    public void populateMap() {

        int mapWidth = -1, mapHeight = -1;

        for (Map.Entry<int[], Integer> entry : game.getCoordinatesMap().entrySet()) {
            int[] coordinates = entry.getKey();
            mapX = coordinates[0] * Cell_Size;
            mapY = coordinates[1] * Cell_Size;

            mapWidth = Math.max(mapWidth, mapX / 16 + 1);
            mapHeight = Math.max(mapHeight, mapY / 16 + 1);
        }

        for (Map.Entry<int[], Integer> entry : game.getCoordinatesMap().entrySet()) {
            int[] coordinates = entry.getKey();
            mapX = coordinates[0] * Cell_Size;
            mapY = coordinates[1] * Cell_Size;
            mapObjectType = entry.getValue();


            final int currentFrameY = mapY / 16;
            final int currentFrameX = mapX / 16;

            GameObject objectDetected = switch (mapObjectType) {
                case 0 -> {
                    //adds variables to the double entry array hasWall
                    game.setWall(currentFrameX, currentFrameY);
                    yield null;
                }
                case 1 -> {
                    var entryDetected = new Entry(
                            mapX,
                            mapY,
                            textureRegionFor1.getRegionWidth(),
                            textureRegionFor1.getRegionHeight(),
                            textureRegionFor1,
                            sharedSkin);

                    //adds variables to the double entry array hasWall
                    game.setWall(currentFrameX, currentFrameY);
                    game.getPlayer().setInitialX(mapX);
                    game.getPlayer().setInitialY(mapY);
                    yield entryDetected;
                }
                case 2 -> {
                    var exit = new Exit(
                            mapX,
                            mapY,
                            textureRegionFor2.getRegionWidth(),
                            textureRegionFor2.getRegionHeight(),
                            textureRegionFor2,
                            sharedSkin);

                    exitCoordinates.add(new int[]{mapX, mapY});
                    //adds variables to the double entry array hasWall
                    game.setWall(currentFrameX, currentFrameY);
                    game.setExit(exit);
                    yield exit;
                }
                case 3 -> {
                    var trap = new Trap(
                            mapX,
                            mapY,
                            textureRegionFor3.getRegionWidth(),
                            textureRegionFor3.getRegionHeight(),
                            textureRegionFor3,
                            sharedSkin);

                    //adds variables to the double entry array hasWall
                    game.addTrap(trap);
                    yield trap;
                }
                case 4 -> {
                    var enemy = new Enemy(
                            mapX,
                            mapY,
                            textureRegionFor4.getRegionWidth(),
                            textureRegionFor4.getRegionHeight(),
                            textureRegionFor4,
                            sharedSkin);
                    //adds variables to the double entry array hasWall
                    game.addEnemy(enemy);
                    yield enemy;
                }
                case 5 -> {
                    var key = new Key(
                            mapX,
                            mapY,
                            textureRegionFor5.getRegionWidth(),
                            textureRegionFor5.getRegionHeight(),
                            textureRegionFor5,
                            sharedSkin);

                    game.setKey(key);
                    yield key;
                }
                default -> null;
            };

            // adds objects into the gameObjects Array
            if (objectDetected != null)
                game.addGameObject(objectDetected);

        }

        game.getEnemies().forEach(enemy -> {
            discoverHorizontalLimits(enemy);
            discoverVerticalLimits(enemy);
        });

    }
    public void loadWalls() {

        for (Map.Entry<int[], Integer> entry : game.getCoordinatesMap().entrySet()) {
            int[] coordinates = entry.getKey();
            int mapX = coordinates[0] * Cell_Size;
            int mapY = coordinates[1] * Cell_Size;
            int mapObjectType = entry.getValue();

            if (mapObjectType == 0) {
                wallCoordinates.add(new int[]{mapX, mapY});
                //System.out.println("Drawing wall at X: " + mapX + ", Y: " + mapY);
                // Skip creating an object for walls
            }

        }
    }

    public void drawWalls() {
        for (int[] wallCoord : wallCoordinates) {
            int wallX = wallCoord[0];
            int wallY = wallCoord[1];
            game.getSpriteBatch().draw(getTextureRegionFor0(), wallX, wallY);

            if(wallX == 0 && wallY == 0) {
                game.getSpriteBatch().draw(getTextureRegionFor4(), wallX, wallY);
            }
        }
    }

    /**
     * Discovers how far an enemy can go without hitting a wall in the x-axis
     * @param enemy
     */
    public void discoverVerticalLimits(Enemy enemy) {
        int currentFrameX = (int) enemy.getCurrentFrameX();

        while (!game.hasWallAt(currentFrameX, (int)enemy.getMinVertical() - 1)) {
            enemy.setMinVertical(enemy.getMinVertical() - 1);
        }

        while (!game.hasWallAt(currentFrameX, (int)enemy.getMaxVertical() + 1)) {
            enemy.setMaxVertical(enemy.getMaxVertical() + 1);
        }
    }

    /**
     * Discovers how far an enemy can go without hitting a wall in the y-axis
     * @param enemy
     */
    public void discoverHorizontalLimits(Enemy enemy) {
        int currentFrameY = (int) enemy.getCurrentFrameY();

        while (!game.hasWallAt((int)enemy.getMinHorizontal() - 1, currentFrameY)) {
            enemy.setMinHorizontal(enemy.getMinHorizontal() - 1);
        }

        while (!game.hasWallAt((int)enemy.getMaxHorizontal() + 1, currentFrameY)) {
            enemy.setMaxHorizontal(enemy.getMaxHorizontal() + 1);
        }
    }


//    public void clearMap() {
//        if (game.getCoordinatesMap().isEmpty()) {
//            System.out.println("Map is empty");
//        }
//        else {
//            game.getCoordinatesMap().clear();
//        }
//    }

    public void saveMap() {

    }

    public static void loadTextures() {
        textureRegionFor0 = new TextureRegion(GameScreen.tileSheet, 0, 0, 16, 16); // define the Textureregion for WALLS
        textureRegionFor1 = new TextureRegion(GameScreen.tileSheet, 32, 96, 16, 16); // define the Textureregion for ENTRY
        textureRegionFor2 = new TextureRegion(GameScreen.tileSheet, 0, 96, 16, 16); // define the Textureregion for EXIT
        textureRegionFor3 = new TextureRegion(GameScreen.objectSheet, 112, 48, 16, 16); // define the Textureregion for TRAP
        textureRegionFor4 = new TextureRegion(GameScreen.mobsSheet, 128, 64, 16, 16); // define the Textureregion for ENEMY
        textureRegionFor5 = new TextureRegion(GameScreen.keySheet, 0, 0, 16, 16); // define the Textureregion for KEY
    }

    //Getters & Setters

    public TextureRegion getTextureRegionFor0() {
        return textureRegionFor0;
    }

    public TextureRegion getTextureRegionFor1() {
        return textureRegionFor1;
    }

    public TextureRegion getTextureRegionFor2() {
        return textureRegionFor2;
    }

    public TextureRegion getTextureRegionFor3() {
        return textureRegionFor3;
    }

    public TextureRegion getTextureRegionFor4() {
        return textureRegionFor4;
    }

    public TextureRegion getTextureRegionFor5() {
        return textureRegionFor5;
    }

    public List<int[]> getExitCoordinates() {
        return exitCoordinates;
    }

    public List<int[]> getWallCoordinates() {
        return wallCoordinates;
    }
}

