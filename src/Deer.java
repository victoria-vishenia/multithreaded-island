import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

class Deer extends Herbivorous implements Eatable, Runnable{
    private static final String GROUP_NAME = "\uD83E\uDD8C";
    private static final double WEIGHT=300;
    private static final int GROUP=20;
    private static final int MAX_SPEED=4;
    private static final List<Deer> DEER = new CopyOnWriteArrayList<>();
    {
        foodNorm=50;
    }
    private static final Semaphore[][] semaphores  = new Semaphore [100][20];
    static {
        for (int i = 0; i < Deer.semaphores.length; i++) {
            for (int j = 0; j < semaphores[i].length; j++) {
                semaphores[i][j] = new Semaphore(GROUP, true);
            }
        }
    }
    public static double getWeight(){return WEIGHT;}

    public static Semaphore[][] getSemaphore(){
        return semaphores;
    }
    public static CopyOnWriteArrayList<Deer> getDeer(){
        return (CopyOnWriteArrayList<Deer>) DEER;
    }
    static void fillDeerList() {
        for (int i = 0; i < 200; i++) {
            Deer deer = new Deer();
            Deer.DEER.add(deer);
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
                    Deer.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(random, this);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while  (Deer.semaphores[this.x][this.y].availablePermits() < 1);
        }
    }
    @Override
    void move() {
        synchronized (Island.getIslandLocations()) {
            int newDeerX = this.x;
            int newDeerY = this.y;

            Island.getIslandLocations()[this.x][this.y].remove(this);

            int delta = ThreadLocalRandom.current().nextInt(0, MAX_SPEED + 1);

            if ((newDeerX < 5) && (newDeerY < 5)) {
                newDeerY += delta;
            } else if ((newDeerX >= 5) && (newDeerX <= 95) && (newDeerY >= 5) && (newDeerY <= 15)) {
                newDeerX += delta;
            } else if ((newDeerX < 5) && (newDeerY > 15)) {
                newDeerY -= delta;
            }
            if (Deer.semaphores[newDeerX][newDeerY].availablePermits() > 0) {
                try {
                    Island.getIslandLocations()[this.x][this.y].remove(this);
                    Deer.semaphores[this.x][this.y].release();
                    Deer.semaphores[newDeerX][newDeerY].acquire();
                    this.x = newDeerX;
                    this.y = newDeerY;
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

            if (((!Island.getIslandLocations()[this.x][this.y].get(nearIndex1).equals(this) &&
                    (Deer.semaphores[this.x][this.y].availablePermits() > 0) &&
                    Island.getIslandLocations()[this.x][this.y].get(nearIndex1) instanceof Deer)) ||
                    ((!Island.getIslandLocations()[this.x][this.y].get(nearIndex2).equals(this) &&
                            (Deer.semaphores[this.x][this.y].availablePermits() > 0) &&
                            Island.getIslandLocations()[this.x][this.y].get(nearIndex2) instanceof Deer))) {
                try {
                    Deer.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(new Deer());
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
