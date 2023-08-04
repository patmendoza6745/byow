package byow.Core;

import byow.TileEngine.TETile;

public class Avatar {

    private int posX;
    private int posY;
    private TETile image;

    public Avatar(int x, int y, TETile img) {
        posX = x;
        posY = y;
        image = img;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public TETile getImage() {
        return image;
    }

    public void changeImage(TETile img) { image = img; }

    public void changePosition(int x, int y) {
        posX = x;
        posY = y;
    }
}
