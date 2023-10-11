import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

class Goat extends Herbivorous implements Eatable, Runnable{
    private static final String GROUP_NAME = "\uD83D\uDC10";
    private static final double WEIGHT = 60.0;
    private static final int GROUP = 140;
    private static final int MAX_SPEED = 3;
    private static final List<Goat> GOATS = new CopyOnWriteArrayList<>();
    {
        foodNorm =10.0;
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
    public static CopyOnWriteArrayList<Goat> getGoats(){
        return (CopyOnWriteArrayList<Goat>) GOATS;
    }
    static void fillGoatsList() {
        for (int i = 0; i < 100; i++) {
            Goat goat = new Goat();
            GOATS.add(goat);
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
                    Goat.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(random, this);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while (Goat.semaphores[this.x][this.y].availablePermits() < 1);
        }
    }
    @Override
    void move() {
        synchronized (Island.getIslandLocations()) {
            int newGoatX = this.x;
            int newGoatY = this.y;

            Island.getIslandLocations()[this.x][this.y].remove(this);

            int delta = ThreadLocalRandom.current().nextInt(0, MAX_SPEED + 1);

            if ((newGoatX < 4) && (newGoatY < 4)) {
                newGoatY += delta;
            } else if ((newGoatX > 96) && (newGoatY < 4)) {
                newGoatY += delta;
            } else if ((newGoatX > 96) && (newGoatY > 16)) {
                newGoatX -= Math.abs(delta / 2);
                newGoatY -= Math.abs(delta - Math.abs(delta / 2));
            } else if (((newGoatX < 4) && (newGoatY > 16))) {
                newGoatX += Math.abs(delta);
                newGoatY -= (delta - newGoatX);
            }
            if (Goat.semaphores[newGoatX][newGoatY].availablePermits() > 0) {
                try {
                    Island.getIslandLocations()[this.x][this.y].remove(this);
                    Goat.semaphores[this.x][this.y].release();
                    Goat.semaphores[newGoatX][newGoatY].acquire();
                    this.x = newGoatX;
                    this.y = newGoatY;
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

            int nearIndex2 = Math.min(thisIndex + 1, arrayLength - 1);

            if ((!Island.getIslandLocations()[this.x][this.y].get(nearIndex2).equals(this) &&
                    (Goat.semaphores[this.x][this.y].availablePermits() > 0) &&
                    Island.getIslandLocations()[this.x][this.y].get(nearIndex2) instanceof Goat)) {
                try {
                    Goat.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(new Goat());
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
