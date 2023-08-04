package byow.Core;

public class Square {
    public int leftX;
    public int rightX;
    public int leftY;
    public int rightY;

    public Square (int radius, int x, int y) {
        leftX = x - (radius / 2);
        rightX = x + (radius / 2);
        leftY = y - (radius / 2);
        rightY = y + (radius / 2);
    }
}
