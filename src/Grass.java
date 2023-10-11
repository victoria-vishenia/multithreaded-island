import java.util.concurrent.Semaphore;

public class Grass implements Eatable{
    private final static String GROUP_NAME ="\uD83C\uDF3F";
    private final static double WEIGHT=1;
    private final static int GROUP=200;
    private static final Semaphore[][] semaphores  = new Semaphore [100][20];
    static {
        for (int i = 0; i < semaphores.length; i++) {
            for (int j = 0; j < semaphores[i].length; j++) {
                semaphores[i][j] = new Semaphore(GROUP, true);
            }
        }
    }
    public static double getWeight(){
        return WEIGHT;
    }
    public static Semaphore[][] getSemaphore(){
        return semaphores;
    }
    @Override
    public String toString() {
        return Grass.GROUP_NAME;
    }
}
