import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

public class Buffalo extends Herbivorous implements Eatable, Runnable{
    private  static final String GROUP_NAME = "\uD83D\uDC03";
    private static final double WEIGHT=700;
    private static final int GROUP=10;
    private static final int MAX_SPEED=3;
    private static final List<Buffalo> BUFFALOES = new CopyOnWriteArrayList<>();
    {
        foodNorm=100;
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

    public static CopyOnWriteArrayList<Buffalo> getBuffaloes(){
        return (CopyOnWriteArrayList<Buffalo>) BUFFALOES;
    }
    static void fillBuffaloesList() {
        for (int i = 0; i < 100; i++) {
            Buffalo buffalo = new Buffalo();
            BUFFALOES.add(buffalo);
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
                    Buffalo.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(random, this);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while (Buffalo.semaphores[this.x][this.y].availablePermits() < 1);
        }
    }
    @Override
    void move() {
        synchronized (Island.getIslandLocations()) {
            int newBuffaloX = this.x;
            int newBuffaloY = this.y;

            Island.getIslandLocations()[this.x][this.y].remove(this);

            int delta = ThreadLocalRandom.current().nextInt(0, MAX_SPEED + 1);

            if ((newBuffaloX < 4) && (newBuffaloY < 4)) {
                newBuffaloX += delta;
            } else if ((newBuffaloX >= 4) && (newBuffaloX <= 50) && (newBuffaloY >= 4) && (newBuffaloY <= 16)) {
                newBuffaloX += Math.abs(delta / 2);
                newBuffaloY += Math.abs(delta - Math.abs((delta / 2)));
            } else if ((newBuffaloX > 50) && (newBuffaloY < 4)) {
                newBuffaloY += delta;
            } else if ((newBuffaloX > 50) && (newBuffaloY > 16)) {
                newBuffaloY -= delta;
            } else if (((newBuffaloX < 4) && (newBuffaloY > 16))) {
                newBuffaloY -= delta;
            }
            if (Buffalo.semaphores[newBuffaloX][newBuffaloY].availablePermits() > 0) {
                try {
                    Island.getIslandLocations()[this.x][this.y].remove(this);
                    Buffalo.semaphores[this.x][this.y].release();
                    Buffalo.semaphores[newBuffaloX][newBuffaloY].acquire();
                    this.x = newBuffaloX;
                    this.y = newBuffaloY;
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
            int nearIndex = Math.min(thisIndex + 1, arrayLength - 1);

            if (!Island.getIslandLocations()[this.x][this.y].get(nearIndex).equals(this) &&
                    (Buffalo.semaphores[this.x][this.y].availablePermits() > 0 ) &&
                    Island.getIslandLocations()[this.x][this.y].get(nearIndex) instanceof Buffalo) {
                try {
                    Buffalo.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(new Buffalo());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public String toString() {
        return GROUP_NAME;
    }

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
