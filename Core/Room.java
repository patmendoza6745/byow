package byow.Core;

public class Room {

    public final int width;
    public final int length;
    private int[] entrance;

    public Room(int l, int w) {
        length = l;
        width = w;
        entrance = new int[2];
    }

    public void addEntrance(int x1, int y1, int x2, int y2) {
        entrance[0] = (x1 + x2) / 2;
        entrance[1] = (y1 + y2) / 2;
    }

    public int[] getEntrance() {
        return entrance;
    }

}
