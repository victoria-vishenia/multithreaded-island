import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

class Horse extends Herbivorous implements Eatable, Runnable{
    private static final String GROUP_NAME = "\ud83d\udc0e";
    private static final double  WEIGHT=400;
    private static final int GROUP=20;
    private static final int MAX_SPEED=4;
    private static final List<Horse> HORSES = new CopyOnWriteArrayList<>();
    {
        foodNorm=60;
    }
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
    public static CopyOnWriteArrayList<Horse> getHorses(){
        return (CopyOnWriteArrayList<Horse>) HORSES;
    }
    static void fillHorsesList() {
        for (int i = 0; i < 100; i++) {
            Horse horse = new Horse();
            HORSES.add(horse);
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
                    Horse.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(random, this);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while (Horse.semaphores[this.x][this.y].availablePermits() < 1);
        }
    }
    @Override
    void move() {
        synchronized (Island.getIslandLocations()) {
            int newHorseX = this.x;
            int newHorseY = this.y;

            Island.getIslandLocations()[this.x][this.y].remove(this);

            int delta = ThreadLocalRandom.current().nextInt(0, MAX_SPEED + 1);

            if ((newHorseX < 5) && (newHorseY < 5)) {
                newHorseY += delta;
            } else if ((newHorseX < 5) && (newHorseY > 10)) {
                newHorseX += delta;
            } else if ((newHorseX > 95) && (newHorseY > 15)) {
                newHorseX -= delta;
            }
            if (Horse.semaphores[newHorseX][newHorseY].availablePermits() > 0) {
                try {
                    Island.getIslandLocations()[this.x][this.y].remove(this);
                    Horse.semaphores[this.x][this.y].release();
                    Horse.semaphores[newHorseX][newHorseY].acquire();
                    this.x = newHorseX;
                    this.y = newHorseY;
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

            if (((!Island.getIslandLocations()[this.x][this.y].get(nearIndex1).equals(this) && (getPROBABILITY() <= 60) &&
                    (Horse.semaphores[this.x][this.y].availablePermits() > 0) &&
                    Island.getIslandLocations()[this.x][this.y].get(nearIndex1) instanceof Horse)) ||
                    ((!Island.getIslandLocations()[this.x][this.y].get(nearIndex2).equals(this) && (getPROBABILITY() <= 70) &&
                            (Horse.semaphores[this.x][this.y].availablePermits() > 0) &&
                            Island.getIslandLocations()[this.x][this.y].get(nearIndex2) instanceof Horse))) {
                try {
                    Horse.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(new Horse());
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
