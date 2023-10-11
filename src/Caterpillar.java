import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

public class Caterpillar extends Herbivorous implements Eatable, Runnable{
    private static final String GROUP_NAME = "\uD83D\uDC1B";
    private static final double WEIGHT=0.01;
    private static final int GROUP=1000;
    private static final List<Caterpillar> CATERPILLARS = new CopyOnWriteArrayList<>();
    {
        this.foodNorm=0;
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

    public static Semaphore[][] getSemaphore(){return semaphores;}
    public static CopyOnWriteArrayList<Caterpillar> getCaterpillars(){
        return (CopyOnWriteArrayList<Caterpillar>) CATERPILLARS;
    }
    static void fillCaterpillarsList() {
        for (int i = 0; i < 100; i++) {
            Caterpillar caterpillar = new Caterpillar();
            CATERPILLARS.add(caterpillar);
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
                    Caterpillar.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(random, this);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while (Caterpillar.semaphores[this.x][this.y].availablePermits() < 1);
        }
    }
    @Override
    void move() {
        String stayHere = "Sorry, i don't want go anywhere";
    }

    @Override
    void reproduce() {
        synchronized (Island.getIslandLocations()) {
            int thisIndex = Island.getIslandLocations()[this.x][this.y].indexOf(this);

            int arrayLength = Island.getIslandLocations()[this.x][this.y].toArray().length;
            int nearIndex1 = Math.max(thisIndex - 1, 0);
            int nearIndex2 = Math.min(thisIndex + 1, arrayLength - 1);

            if (((!Island.getIslandLocations()[this.x][this.y].get(nearIndex1).equals(this) &&
                    (Caterpillar.semaphores[this.x][this.y].availablePermits() > 0) &&
                    Island.getIslandLocations()[this.x][this.y].get(nearIndex1) instanceof Caterpillar)) ||
                    ((!Island.getIslandLocations()[this.x][this.y].get(nearIndex2).equals(this) &&
                            (Caterpillar.semaphores[this.x][this.y].availablePermits() > 0) &&
                            Island.getIslandLocations()[this.x][this.y].get(nearIndex2) instanceof Caterpillar))) {
                try {
                    Caterpillar.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(new Caterpillar());
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
