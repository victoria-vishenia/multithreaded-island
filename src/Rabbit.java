import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
class Rabbit extends Herbivorous implements Eatable, Runnable{
    private static final String GROUP_NAME = "\uD83D\uDC07";
    private static final double WEIGHT=2;
    private static final int GROUP=150;
    private static final int MAX_SPEED=2;
    private static final List<Rabbit> RABBITS = new CopyOnWriteArrayList<>();
    {
        foodNorm = 0.45;
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
    public static CopyOnWriteArrayList<Rabbit> getRabbits(){
        return (CopyOnWriteArrayList<Rabbit>) RABBITS;
    }
    static void fillRabbitsList() {
        for (int i = 0; i < 100; i++) {
            Rabbit rabbit = new Rabbit();
            RABBITS.add(rabbit);
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
                    Rabbit.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(random, this);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while (Rabbit.semaphores[this.x][this.y].availablePermits() < 1);
        }
    }
    @Override
    void move() {
        synchronized (Island.getIslandLocations()) {
            int newRabbitX = this.x;
            int newRabbitY = this.y;

            Island.getIslandLocations()[this.x][this.y].remove(this);

            int delta = ThreadLocalRandom.current().nextInt(0, MAX_SPEED + 1);

            if ((newRabbitX < 4) && (newRabbitY < 4)) {
                newRabbitX += delta;
            } else if ((newRabbitX > 96) && (newRabbitY < 4)) {
                newRabbitY += delta;
            } else if ((newRabbitX > 96) && (newRabbitY > 16)) {
                newRabbitX -= Math.abs(delta / 2);
                newRabbitY -= Math.abs(delta - Math.abs((delta / 2)));
            }
            if (Rabbit.semaphores[newRabbitX][newRabbitY].availablePermits() > 0) {
                try {
                    Island.getIslandLocations()[this.x][this.y].remove(this);
                    Rabbit.semaphores[this.x][this.y].release();
                    Rabbit.semaphores[newRabbitX][newRabbitY].acquire();
                    this.x = newRabbitX;
                    this.y = newRabbitY;
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

// a lot of opportunities for mouses for reproducing
            int arrayLength = Island.getIslandLocations()[this.x][this.y].toArray().length;
            int nearIndex1 = Math.max(thisIndex - 1, 0);
            int nearIndex2 = Math.max(thisIndex - 2, 0);
            int nearIndex3 = Math.max(thisIndex - 3, 0);
            int nearIndex4 = Math.min(thisIndex + 1, arrayLength - 1);
            int nearIndex5 = Math.min(thisIndex + 2, arrayLength - 1);
            int nearIndex6 = Math.min(thisIndex + 3, arrayLength - 1);

            if ((!Island.getIslandLocations()[this.x][this.y].get(nearIndex1).equals(this) &&
                    (Rabbit.semaphores[this.x][this.y].availablePermits() > 0) &&
                    Island.getIslandLocations()[this.x][this.y].get(nearIndex1) instanceof Rabbit) ||
                    (!Island.getIslandLocations()[this.x][this.y].get(nearIndex2).equals(this) &&
                            (Rabbit.semaphores[this.x][this.y].availablePermits() > 0) &&
                            Island.getIslandLocations()[this.x][this.y].get(nearIndex2) instanceof Rabbit) ||
                    (!Island.getIslandLocations()[this.x][this.y].get(nearIndex3).equals(this) &&
                            (Rabbit.semaphores[this.x][this.y].availablePermits() > 0) &&
                            Island.getIslandLocations()[this.x][this.y].get(nearIndex3) instanceof Rabbit) ||
                    (!Island.getIslandLocations()[this.x][this.y].get(nearIndex4).equals(this) &&
                            (Rabbit.semaphores[this.x][this.y].availablePermits() > 0) &&
                            Island.getIslandLocations()[this.x][this.y].get(nearIndex4) instanceof Rabbit) ||
                    (!Island.getIslandLocations()[this.x][this.y].get(nearIndex5).equals(this) &&
                            (Rabbit.semaphores[this.x][this.y].availablePermits() > 0) &&
                            Island.getIslandLocations()[this.x][this.y].get(nearIndex5) instanceof Rabbit) ||
                    (!Island.getIslandLocations()[this.x][this.y].get(nearIndex6).equals(this) &&
                            (Rabbit.semaphores[this.x][this.y].availablePermits() > 0) &&
                            Island.getIslandLocations()[this.x][this.y].get(nearIndex6) instanceof Rabbit)) {
                try {
                    Rabbit.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(new Rabbit());
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
