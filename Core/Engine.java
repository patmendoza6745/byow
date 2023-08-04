package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.InputDemo.StringInputDevice;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.StdDraw;
import org.apache.commons.lang3.math.NumberUtils;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 100;
    public static final int HEIGHT = 50;
    public static final int DIMENSION_CANVAS_MENU = 800;
    public static final int MIN_ROOMS = 30;
    public static final int MAX_ROOMS = 40;
    public static final int MIN_LENGTH_WIDTH = 10;
    public static final int MAX_LENGTH_WIDTH = 12;
    private int radius = 6;
    private TETile[][] world = new TETile[WIDTH][HEIGHT];
    private TETile[][] maskingWorld = new TETile[WIDTH][HEIGHT];
    private TETile[][] mundo;
    private Square pov;
    private TETile outfitChange = Tileset.WARRIOR;
    private Avatar avatar;
    private TETile prevTile;

    private final double ZERO_POINT_NINE = 0.9;
    private final double ZERO_POINT_EIGHT = 0.8;
    private final double ZERO_POINT_TWO = 0.2;
    private final double ZERO_POINT_FOUR_SEVEN = 0.47;
    private final double ZERO_POINT_FOUR_FOUR = 0.44;
    private final double ZERO_POINT_FOUR_ONE = 0.41;
    private final double ZERO_POINT_FIVE = 0.5;

    private final int SIXTY = 60;
    private final int THIRTY = 30;
    private final int FIFTY_ONE = 51;
    private final int ONE_HUNDRED = 100;
    private final int TWENTY = 20;
    private final int ZERO = 0;





    //public int counter = 0;
    //public int coins = 0;

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        Long seed;
        StdDraw.setCanvasSize(DIMENSION_CANVAS_MENU, DIMENSION_CANVAS_MENU);
        this.drawMainFrame("CS61B: THE GAME");
        String decision = navigateMenu();
        if (decision.equals("n")) {
            seed = seed(decision);
            createWorld(seed);
        } else if (decision.equals("l")) {

            try (BufferedReader br = new BufferedReader(new FileReader("byow/Core/savedWorld.txt"))) {
                // Read in the saved state of the world.
                String savedSeed = br.readLine();
                int[] avatarPosition = new int[]{Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine())};
                String avatarImage = br.readLine();
                loadSavedGame(avatarPosition, savedSeed, avatarImage);
                // The following will entail the interactions of the user
                // with the game and the world with the new input.
                movement(getSeed(new StringInputDevice(savedSeed)));
            } catch (IOException e) {
                System.out.println("No game has been saved.");
            }
        }
        System.exit(0);
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     * <p>
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     * <p>
     * In other words, running both of these:
     * - interactWithInputString("n123sss:q")
     * - interactWithInputString("lww")
     * <p>
     * should yield the exact same world state as:
     * - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {

        ter.initialize(WIDTH, HEIGHT + 2);
        StringInputDevice userInput = new StringInputDevice(input.toLowerCase());
        char firstInput =
                userInput.getNextKey(); // could n, l, or q (new, load, quit) // Will use a switch statement for this.

        switch (firstInput) {
            case 'l' -> {  // 'l' -> search for saved world. If it DNE, quit game or else load game.
                try (BufferedReader br = new BufferedReader(new FileReader("byow/Core/savedWorld.txt"))) {
                    // Read in the saved state of the world.
                    String seed = br.readLine();
                    int[] avatarPosition = new int[]{Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine())};
                    String avatarImage = br.readLine();
                    loadSavedGame(avatarPosition, seed, avatarImage);
                    // The following will entail the interactions of the user
                    // with the game and the world with the new input.
                    interactWithUserString(userInput, seed);
                } catch (IOException e) {
                    System.out.println("No game has been saved");
                    return null;
                }
            }
            case 'q' -> { // 'q' -> quit game automatically
                return null;
            }
            default -> { // 'n' -> new world
                loadNewGame(userInput);
            }
        }

        ter.renderFrame(world);
        // Just so we can see how the world looks. What we care about is returning the world!
        return world;
    }

    private void createWorld(Long seed) {
        ter.initialize(WIDTH, HEIGHT + 2); // + 2
        initializeWorld();
        generateWorld(seed);
        ter.renderFrame(world);
        movement(seed);
    }

    private void generateWorld(Long seed) {
        generateWorldHelper(seed);
    }

    private void initializeWorld() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                world[x][y] = Tileset.GRASSPNG;
                maskingWorld[x][y] = Tileset.NOTHING;
            }
        }
    }

    private void interactWithUserString(StringInputDevice input, String seed) {
        while (input.possibleNextInput()) {
            char keystroke = input.getNextKey();
            if (keystroke == ':' && input.getNextKey() == 'q') {
                String[] avatarPosition = new String[]{String.valueOf(avatar.getPosX()),
                                            String.valueOf(avatar.getPosY())};
                saveGame(avatarPosition, seed, enumerateAvatarImage());
                return;
            }
            avatarMovement(Character.toString(keystroke));
            ter.renderFrame(world);
        }
    }

    private void saveGame(String[] avatarPosition, String seed, String avatarImage) {
        try {
            FileWriter savedGame = new FileWriter("byow/Core/savedWorld.txt");
            savedGame.write(seed);
            savedGame.write(System.lineSeparator());
            savedGame.write(avatarPosition[0]);
            savedGame.write(System.lineSeparator());
            savedGame.write(avatarPosition[1]);
            savedGame.write(System.lineSeparator());
            savedGame.write(avatarImage);
            savedGame.close();
        } catch (IOException e) {
            System.out.println("Error saving game.");
        }
    }

    private void loadSavedGame(int[] avatarPosition, String savedSeed, String avatarImage) {
        initializeWorld();
        ter.initialize(WIDTH, HEIGHT + 2);
        ter.renderFrame(world);
        mundo = world;
        ter.initialize(WIDTH, HEIGHT + 2);
        StringInputDevice seedInput = new StringInputDevice(savedSeed);
        Long seed = getSeed(seedInput);
        generateWorld(seed);
        world[avatar.getPosX()][avatar.getPosY()] = prevTile;
        avatar.changePosition(avatarPosition[0], avatarPosition[1]);
        avatar.changeImage(fetchAvatarImage(avatarImage));
        world[avatar.getPosX()][avatar.getPosY()] = avatar.getImage();
        ter.renderFrame(world);
    }

    private void loadNewGame(StringInputDevice userInput) {
        mundo = world;
        Long seed = getSeed(userInput);
        initializeWorld();
        generateWorld(seed);
        String seedString = 'n' + String.valueOf(seed) + 's';
        // The following will entail the interactions of the user with the game and world.
        interactWithUserString(userInput, seedString);
    }

    // HELPER FUNCTIONS

    private void generateWorldHelper(Long seed) {
        Random random = new Random(seed);
        int numRooms = RandomUtils.uniform(random, MIN_ROOMS, MAX_ROOMS);
        List<Room> rooms = createRooms(numRooms, random);
        List<Room> invalidRooms = new ArrayList<>();

        // Shuffle the rooms in our list
        RandomUtils.shuffle(random, rooms);

        for (Room room : rooms) {
            invalidRooms.add(drawRoom(room, random));

        }

        // Remove any invalid rooms from our list
        for (Room invalidRoom : invalidRooms) {
            if (invalidRoom != null) {
                rooms.remove(invalidRoom);
            }
        }

        for (int i = 0; i < rooms.size() - 1; i++) {
            createHallways(
                    rooms.get(i).getEntrance()[0],
                    rooms.get(i).getEntrance()[1],
                    rooms.get(i + 1).getEntrance()[0],
                    rooms.get(i + 1).getEntrance()[1]
            );
        }
        //Spawn coins
        /*for (int i = 0; i < rooms.size(); i++) {
            int[] coinSpawnPoint = rooms.get(RandomUtils.uniform(random, 0, rooms.size())).getEntrance();
            int coinSpawnPointX = coinSpawnPoint[0];
            int coinSpawnPointY = coinSpawnPoint[1];
            world[coinSpawnPointX][coinSpawnPointY] = Tileset.SAND;
            counter += 1;
        }*/
        // Instantiate and spawn in our avatar in a random room!
        int[] spawnPoint = rooms.get(RandomUtils.uniform(random, 0, rooms.size())).getEntrance();
        int spawnPointX = spawnPoint[0];
        int spawnPointY = spawnPoint[1];
        prevTile = Tileset.STONE;
        avatar = new Avatar(spawnPointX, spawnPointY, outfitChange);
        world[spawnPointX][spawnPointY] = avatar.getImage();
    }

    private List<Room> createRooms(int numRooms, Random random) {
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < numRooms; i++) {
            int length = RandomUtils.uniform(random, MIN_LENGTH_WIDTH, MAX_LENGTH_WIDTH);
            int width = RandomUtils.uniform(random, MIN_LENGTH_WIDTH, MAX_LENGTH_WIDTH);
            rooms.add(new Room(length, width));
        }
        return rooms;
    }

    private Room drawRoom(Room room, Random random) {
        // We wish to draw the room in a spot such that no other room exists there already.
        int startX = RandomUtils.uniform(random, WIDTH - room.width);
        int startY = RandomUtils.uniform(random, HEIGHT - room.length);
        int stopX = startX + room.width;
        int stopY = startY + room.length;

        room.addEntrance(startX, startY, stopX - 1, stopY - 1);

        for (int x = startX; x < stopX; x++) {
            for (int y = startY; y < stopY; y++) {

                if (world[x][y] == Tileset.STONE || world[x][y] == Tileset.STONEWALL) {
                    // We realize that we are overlapping, we need to reverse the work we have just done!
                    // Return the room that is invalid
                    reverseDrawRoom(startX, stopX, startY, stopY, x, y);
                    return room;
                }

                if ((x == startX || y == startY || y == stopY - 1 || x == stopX - 1)) {
                    world[x][y] = Tileset.STONEWALL;
                } else {
                    world[x][y] = Tileset.STONE;
                }
            }
        }
        return null;
    }

    private void createHallways(int startX, int startY, int stopX, int stopY) {
        // Four cases:
        // Case 1: our end point (x2, y2) is DOWN RIGHT from (x1, y1) [x1 < x2 and y1 > y2]
        // Case 2: our end point (x2, y2) is DOWN LEFT from (x1, y1) [x1 > x2 and y1 > y2]
        // Case 3: our end point (x2, y2) is UP RIGHT from (x1, y1) [x1 < x2 and y1 < y2]
        // Case 4: our end point (x2, y2) is UP LEFT from (x1, y1) [x1 > x2 and y1 < y2]
        // The other 4 cases are trivial

        world[startX][startY] = Tileset.STONE;
        world[stopX][stopY] = Tileset.STONE;

        if (startX < stopX && startY > stopY) {

            drawPathDown(startX, startY, stopY);
            drawPathRight(stopY, startX, stopX);

        } else if (startX > stopX && startY > stopY) {

            drawPathDown(startX, startY, stopY);
            drawPathLeft(stopY, startX, stopX);

        } else if (startX < stopX && startY < stopY) {

            drawPathUp(startX, startY, stopY);
            drawPathRight(stopY, startX, stopX);

        } else if (startX > stopX && startY < stopY) {

            drawPathUp(startX, startY, stopY);
            drawPathLeft(stopY, startX, stopX);

        } else if (startX == stopX && startY > stopY) {

            drawPathDown(startX, startY, stopY);

        } else if (startX == stopX && startY < stopY) {

            drawPathUp(startX, startY, stopY);

        } else if (startX > stopX) {

            drawPathLeft(stopY, startX, stopX);

        } else if (startX < stopX) {

            drawPathRight(stopY, startX, stopX);

        }
    }

    public Long getSeed(StringInputDevice input) {

        StringBuilder seedStringBuilder = new StringBuilder();
        while (input.possibleNextInput()) {
            char keystroke = input.getNextKey();
            // This means that we are done with building the seed.
            if (keystroke == 's') {
                break;
            } else if (keystroke != 'n') {
                seedStringBuilder.append(keystroke);
            }
        }

        return Long.parseLong(seedStringBuilder.toString());
    }


    // DRAWING HELPER FUNCTIONS

    private void drawPathUp(int x, int startY, int stopY) {
        for (int y = startY; y < stopY; y += 1) {
            drawPathHelper(x, y);
        }
    }

    private void drawPathDown(int x, int startY, int stopY) {
        for (int y = startY; y >= stopY; y -= 1) {
            drawPathHelper(x, y);
        }
    }

    private void drawPathRight(int y, int startX, int stopX) {
        for (int x = startX; x < stopX; x += 1) {
            drawPathHelper(x, y);
        }
    }

    private void drawPathLeft(int y, int startX, int stopX) {
        for (int x = startX; x >= stopX; x -= 1) {
            drawPathHelper(x, y);
        }
    }

    private void drawPathWall(int x, int y) {

        if (world[x + 1][y] != Tileset.DIRT && world[x + 1][y] != Tileset.STONE) {
            world[x + 1][y] = Tileset.STONEWALL;
        }

        if (world[x - 1][y] != Tileset.DIRT && world[x - 1][y] != Tileset.STONE) {
            world[x - 1][y] = Tileset.STONEWALL;
        }

        if (world[x][y - 1] != Tileset.DIRT && world[x][y - 1] != Tileset.STONE) {
            world[x][y - 1] = Tileset.STONEWALL;
        }

        if (world[x][y + 1] != Tileset.DIRT && world[x][y + 1] != Tileset.STONE) {
            world[x][y + 1] = Tileset.STONEWALL;
        }

        if (world[x + 1][y + 1] != Tileset.DIRT && world[x + 1][y + 1] != Tileset.STONE) {
            world[x + 1][y + 1] = Tileset.STONEWALL;
        }

        if (world[x - 1][y - 1] != Tileset.DIRT && world[x - 1][y - 1] != Tileset.STONE) {
            world[x - 1][y - 1] = Tileset.STONEWALL;
        }

        if (world[x - 1][y + 1] != Tileset.DIRT && world[x - 1][y + 1] != Tileset.STONE) {
            world[x - 1][y + 1] = Tileset.STONEWALL;
        }

        if (world[x + 1][y - 1] != Tileset.DIRT && world[x + 1][y - 1] != Tileset.STONE) {
            world[x + 1][y - 1] = Tileset.STONEWALL;
        }

    }

    private void drawPathHelper(int x, int y) {
        if (world[x][y] == Tileset.GRASSPNG || world[x][y] == Tileset.STONEWALL) {
            world[x][y] = Tileset.DIRT;
            drawPathWall(x, y);
        }
    }

    private void reverseDrawRoom(int startX, int stopX, int startY, int stopY, int x, int y) {
        for (int i = startX; i < stopX; i++) {
            for (int j = startY; j < stopY; j++) {
                if (i == x && j == y) {
                    return;
                }
                world[i][j] = Tileset.GRASSPNG;
            }
        }
    }

    //UI Controls
    private void movement(Long seed) {
        // while quit and save or quit isn't pressed move around the world
        //while in the loop pay attention to the key; dictating where you should go
        StringBuilder word = new StringBuilder();
        StringBuilder seedString = new StringBuilder();
        seedString.append('n');
        seedString.append(seed.toString());
        seedString.append('s');
        String move;
        mundo = world;
        while (!(word.toString().equals(":q"))) {
            while (StdDraw.hasNextKeyTyped()) {
                move = String.valueOf(StdDraw.nextKeyTyped()).toLowerCase();
                if (move.equals(":") || move.equals("q")) {
                    word.append(move);
                } else {
                    word = new StringBuilder();
                    // call avatar movement helper method
                    avatarMovement(move);
                }
            }
            whereTheMouseAt();
        }

        if (word.toString().equals(":q")) {
            String[] avatarPosition = new String[]{String.valueOf(avatar.getPosX()), String.valueOf(avatar.getPosY())};
            saveGame(avatarPosition, seedString.toString(), enumerateAvatarImage());
        }
    }

    private void avatarMovement(String direction) {
        int x = avatar.getPosX();
        int y = avatar.getPosY();
        mundo[x][y] = prevTile;
        pov = new Square(radius, avatar.getPosX(), avatar.getPosY());
        switch (direction) {
            case "w" -> {
                if (validateMove(x, y + 1)) {
                    y = y + 1;
                }
            }
            case "a" -> {
                if (validateMove(x - 1, y)) {
                    x = x - 1;
                }
            }
            case "s" -> {
                if (validateMove(x, y - 1)) {
                    y = y - 1;
                }
            }
            case "d" -> {
                if (validateMove(x + 1, y)) {
                    x = x + 1;
                }
            }
            case "l" -> {
                letThereBeLight(pov.leftX, pov.rightX, pov.leftY, pov.rightY);
                mundo = maskingWorld;
            }
            case "o" -> {
                darkness(pov.leftX, pov.rightX, pov.leftY, pov.rightY);
                mundo = world;
            }
            case "c" -> {
                //change outfit
                TETile changeOutFit = changeAvatar();
                avatar.changeImage(changeOutFit);
            }
            default -> System.out.println("Invalid keystroke");
        }
        avatar.changePosition(x, y);
        prevTile = world[x][y];
        /*if (mundo[x][y] != Tileset.SAND) {prevTile = world[x][y];}
        else{
            coins += 1;
            world[x][y] = Tileset.STONE;
        }*/
        if (Arrays.deepEquals(mundo, maskingWorld)) {
            darkness(pov.leftX, pov.rightX, pov.leftY, pov.rightY);
            pov = new Square(radius, avatar.getPosX(), avatar.getPosY());
            letThereBeLight(pov.leftX, pov.rightX, pov.leftY, pov.rightY);
        }
        mundo[x][y] = avatar.getImage();
    }

    private boolean validateMove(int x, int y) {
        return world[x][y] != Tileset.STONEWALL;
    }

    private TETile changeAvatar() {

        while (!StdDraw.hasNextKeyTyped()) {
            System.out.println("Waiting for user input...");
        }

        return fetchAvatarImage(String.valueOf(StdDraw.nextKeyTyped()));
    }

    private void letThereBeLight(int x1, int x2, int y1, int y2) {
        for (int i = x1; i < x2; i++) {
            for (int j = y1; j < y2; j++) {
                if (0 <= i && i < world.length && 0 <= j && j < world[0].length) {
                    maskingWorld[i][j] = world[i][j];
                }
            }
        }
    }

    private void darkness(int x1, int x2, int y1, int y2) {
        for (int i = x1; i < x2; i++) {
            for (int j = y1; j < y2; j++) {
                if (0 <= i && i < world.length && 0 <= j && j < world[0].length) {
                    maskingWorld[i][j] = Tileset.NOTHING;
                }
            }
        }
    }

    private Long seed(String decision) {
        String seedHolder = decision;
        while (!(seedHolder.contains("s"))) {
            while (StdDraw.hasNextKeyTyped()) {
                String number = String.valueOf(StdDraw.nextKeyTyped()).toLowerCase();
                if (NumberUtils.isCreatable(number) || number.contains("s")) {
                    seedHolder += number;
                    drawInputs(seedHolder);
                }
            }
        }
        return Long.parseLong(seedHolder.substring(1, seedHolder.length() - 1));
    }

    private String navigateMenu() {
        StringBuilder decision = new StringBuilder();
        String input;
        while (decision.length() == 0) {
            while (StdDraw.hasNextKeyTyped()) {
                input = String.valueOf(StdDraw.nextKeyTyped()).toLowerCase();
                if (input.equals("n") || input.equals("l") || input.equals("q")) {
                    decision.append(input);
                    drawInputs(input);
                    StdDraw.text(ZERO_POINT_FIVE, ZERO_POINT_NINE, "Enter SEED:");
                } else if (input.equals("c")) {
                    outfitChange = drawCharacterMenu();
                }
            }
        }
        return decision.toString();
    }

    private void drawInputs(String s) {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, SIXTY);
        StdDraw.setFont(fontBig);
        StdDraw.text(ZERO_POINT_FIVE, ZERO_POINT_EIGHT, s);
    }

    private TETile drawCharacterMenu() {
        TETile choice = null;
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, THIRTY);
        StdDraw.setFont(fontBig);
        StdDraw.text(ZERO_POINT_TWO, ZERO_POINT_FIVE, "Water (1)");
        StdDraw.text(ZERO_POINT_FIVE, ZERO_POINT_FIVE, "Flower (2)");
        StdDraw.text(ZERO_POINT_EIGHT, ZERO_POINT_FIVE, "Warrior (3)");
        while (choice == null) {
            choice = changeAvatar();
        }
        drawMainFrame("CS61B: THE GAME");
        return choice;
    }

    private void drawMainFrame(String s) {
        /* Take the input string S and display it at the center of the screen,
         * with the pen settings given below. */
        drawInputs(s);
        Font fontSmall = new Font("Monaco", Font.BOLD, TWENTY);
        StdDraw.setFont(fontSmall);
        StdDraw.text(ZERO_POINT_FIVE, ZERO_POINT_FIVE, "New Game (N)");
        StdDraw.text(ZERO_POINT_FIVE, ZERO_POINT_FOUR_SEVEN, "Load Game (L)");
        StdDraw.text(ZERO_POINT_FIVE, ZERO_POINT_FOUR_FOUR, "Quit (Q)");
        StdDraw.text(ZERO_POINT_FIVE, ZERO_POINT_FOUR_ONE, "Change Character (C)");
    }

    private String enumerateAvatarImage() {
        if (avatar.getImage().equals(Tileset.WATER)) {
            return "1";
        } else if (avatar.getImage().equals(Tileset.FLOWER)) {
            return "2";
        } else if (avatar.getImage().equals(Tileset.WARRIOR)) {
            return "3";
        }
        return null;
    }

    private TETile fetchAvatarImage(String avatarImage) {
        switch (avatarImage) {
            case "1" -> {
                return Tileset.WATER;
            }
            case "2" -> {
                return Tileset.FLOWER;
            }
            case "3" -> {
                return Tileset.WARRIOR;
            }
            default -> {
                return null;
            }
        }
    }

    private void whereTheMouseAt() {
        int x = (int) StdDraw.mouseX();
        int y = (int) StdDraw.mouseY();
        String tile;
        ter.renderFrame(mundo);
        if (x < mundo.length && y < mundo[0].length) {
            tile = mundo[x][y].description();
        } else {
            tile = "Nothing";
        }
        StdDraw.setPenColor(Color.WHITE);
        Font fontSmall = new Font("Monaco", Font.BOLD, TWENTY);
        StdDraw.setFont(fontSmall);
        StdDraw.textLeft(ZERO, FIFTY_ONE, "Tile: " + tile);
        //StdDraw.textLeft(0, 48, "Coin Collected: " + coins);
        //StdDraw.textLeft(30, 51, "Collect all the coins!");
        StdDraw.textRight(ONE_HUNDRED, FIFTY_ONE, "(a) right (w) up (s) down (d) left   (l) light (o) light off");
        StdDraw.show();
    }
}
