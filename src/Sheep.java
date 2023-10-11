import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
public class Sheep extends Herbivorous implements Eatable, Runnable{
    private static final String GROUP_NAME = "\uD83D\uDC11";
    private static final double WEIGHT=70;
    private static final int GROUP=140;
    private static final int MAX_SPEED=3;
    private static final List<Sheep> SHEEP = new CopyOnWriteArrayList<>();
    {
        foodNorm=15;
    }
    private static final Semaphore[][] semaphores  = new Semaphore [100][20];
    static {
        for (int i = 0; i < semaphores.length; i++) {
            for (int j = 0; j < semaphores[i].length; j++) {
                semaphores[i][j] = new Semaphore(GROUP, true);
            }
        }
    }
    public static double getWeight(){return WEIGHT;}

    public static Semaphore[][] getSemaphore(){
        return semaphores;
    }
    public static CopyOnWriteArrayList<Sheep> getSheep(){
        return (CopyOnWriteArrayList<Sheep>) SHEEP;
    }
    static void getSheepList() {
        for (int i = 0; i < 100; i++) {
            Sheep sheep = new Sheep();
            SHEEP.add(sheep);
        }
    }

    public void firstLocated() {
        synchronized (Island.getIslandLocations()) {
            do {
                int random = ThreadLocalRandom.current().nextInt(0, 5);
                int randomX = ThreadLocalRandom.current().nextInt(0, 100);
                int randomY = ThreadLocalRandom.current().nextInt(0, 20);
                this.x = randomX;
                this.y = randomY;
                if ((Island.getIslandLocations()[this.x][this.y].size() < (random)) ||
                        !(Island.getIslandLocations()[this.x][this.y].get(random) instanceof Grass)) {
                    random = Island.getIslandLocations()[this.x][this.y].size();
                }
                try {
                    Sheep.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(random, this);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while  (Sheep.semaphores[this.x][this.y].availablePermits() < 1);
        }
    }
    @Override
    void move() {
        synchronized (Island.getIslandLocations()) {
            int newSheepX = this.x;
            int newSheepY = this.y;

            int delta = ThreadLocalRandom.current().nextInt(0, MAX_SPEED + 1);

            Island.getIslandLocations()[this.x][this.y].remove(this);

            if ((newSheepX < 4) && (newSheepY < 4)) {
                newSheepX += delta;
            } else if ((newSheepX > 55) && (newSheepY < 4)) {
                newSheepY += delta;
            } else if ((newSheepX > 55) && (newSheepY > 16)) {
                newSheepX -= delta;
            } else if (((newSheepX < 4) && (newSheepY > 16))) {
                newSheepX -= delta;
            }
            if (Sheep.semaphores[newSheepX][newSheepY].availablePermits() > 0) {
                try {
                    Island.getIslandLocations()[this.x][this.y].remove(this);
                    Sheep.semaphores[this.x][this.y].release();
                    Sheep.semaphores[newSheepX][newSheepY].acquire();
                    this.x = newSheepX;
                    this.y = newSheepY;
                    Island.getIslandLocations()[this.x][this.y].add(this);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    void reproduce() {
        synchronized (Island.getIslandLocations()) {
            int thisIndex = Island.getIslandLocations()[this.x][this.y].indexOf(this);

            int arrayLength = Island.getIslandLocations()[this.x][this.y].toArray().length;
            int nearIndex1 = Math.max(thisIndex - 1, 0);
            int nearIndex2 = Math.min(thisIndex + 1, arrayLength - 1);

            if (((!Island.getIslandLocations()[this.x][this.y].get(nearIndex1).equals(this) && (getPROBABILITY() <= 70) &&
                    (Sheep.semaphores[this.x][this.y].availablePermits() > 0) &&
                    Island.getIslandLocations()[this.x][this.y].get(nearIndex1) instanceof Sheep)) ||
                    ((!Island.getIslandLocations()[this.x][this.y].get(nearIndex2).equals(this) && (getPROBABILITY() <= 70) &&
                            (Sheep.semaphores[this.x][this.y].availablePermits() > 0) &&
                            Island.getIslandLocations()[this.x][this.y].get(nearIndex2) instanceof Sheep))) {
                try {
                    Sheep.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(new Sheep());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public String toString() {return GROUP_NAME;}

    @Override
    public void run() {
        getLOCK().lock();
        this.move();
        this.reproduce();
        if (this.meal < this.foodNorm) {
            this.eatGrass();
        }
        getLOCK().unlock();
    }
}
