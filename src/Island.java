import java.util.concurrent.CopyOnWriteArrayList;

public class Island implements Runnable{
    private static final CopyOnWriteArrayList <Object>[][] ISLAND_LOCATIONS = new CopyOnWriteArrayList[100][20];

    public static CopyOnWriteArrayList<Object>[][] getIslandLocations() {
        return ISLAND_LOCATIONS;
    }
    public static void fillLocations() {
        for (int i = 0; i < ISLAND_LOCATIONS.length; i++) {
            for (int j = 0; j < ISLAND_LOCATIONS[i].length; j++) {
                ISLAND_LOCATIONS[i][j] = new CopyOnWriteArrayList<>();
                for (int k = 0; k < 5; k++) {
                    ISLAND_LOCATIONS[i][j].add(new Grass());
                }
            }
        }
    }

    public static void printIsland() {
        for (int i = 0; i < ISLAND_LOCATIONS.length; i++) {
            for (int j = 0; j < ISLAND_LOCATIONS[i].length; j++) {
                System.out.print(ISLAND_LOCATIONS[i][j]);
            }
            System.out.println(" ");
        }
    }

    @Override
    public void run() {
        Island.printIsland();
    }
}