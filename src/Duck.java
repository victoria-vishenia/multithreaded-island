import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

public class Duck extends Herbivorous implements Eatable, Runnable{
    private static final String GROUP_NAME="\uD83E\uDD86";
    private static final double WEIGHT=1;
    private static final int GROUP=200;
    private static final int MAX_SPEED=4;
    private static final List<Duck> DUCKS = new CopyOnWriteArrayList<>();
    {
        foodNorm=0.15;
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
    public static CopyOnWriteArrayList<Duck> getDucks(){
        return (CopyOnWriteArrayList<Duck>) DUCKS;
    }
    static void fillDucksList() {
        for (int i = 0; i < 100; i++) {
            Duck duck = new Duck();
            DUCKS.add(duck);
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
                    Duck.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(random, this);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while (Duck.semaphores[this.x][this.y].availablePermits() < 1);
        }
    }
    @Override
    void move() {
        synchronized (Island.getIslandLocations()) {
            int newDuckX = this.x;
            int newDuckY = this.y;

            Island.getIslandLocations()[this.x][this.y].remove(this);

            int delta = ThreadLocalRandom.current().nextInt(0, MAX_SPEED + 1);

            if (newDuckX < 90) {
                newDuckX += delta;
            } else if (newDuckX > 95) {
                newDuckX -= delta;

                if (newDuckY < 18) {
                    newDuckY += 1;
                }
                if (Duck.semaphores[newDuckX][newDuckY].availablePermits() > 0) {
                    try {
                        Island.getIslandLocations()[this.x][this.y].remove(this);
                        Duck.semaphores[this.x][this.y].release();
                        Duck.semaphores[newDuckX][newDuckY].acquire();
                        this.x = newDuckX;
                        this.y = newDuckY;
                        Island.getIslandLocations()[this.x][this.y].add(this);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
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
                    (Duck.semaphores[this.x][this.y].availablePermits() > 0) &&
                    Island.getIslandLocations()[this.x][this.y].get(nearIndex1) instanceof Duck)) ||
                    ((!Island.getIslandLocations()[this.x][this.y].get(nearIndex2).equals(this) &&
                            (Duck.semaphores[this.x][this.y].availablePermits() > 0) &&
                            Island.getIslandLocations()[this.x][this.y].get(nearIndex2) instanceof Duck))) {
                try {
                    Duck.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(new Duck());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    double eatGrass() {
        synchronized (Island.getIslandLocations()) {
            int thisIndex = Island.getIslandLocations()[this.x][this.y].indexOf(this);
            int arrayLength = Island.getIslandLocations()[this.x][this.y].toArray().length;

            //Calculation of the nearest indexes based on the possibility to go beyond the array boundaries
            int preIndex = Math.max((thisIndex - 1), 0);
            int postIndex = Math.min((thisIndex + 1), (arrayLength - 1));

            Object object1 = Island.getIslandLocations()[this.x][this.y].get(preIndex);
            Object object2 = Island.getIslandLocations()[this.x][this.y].get(postIndex);

            if ((getPROBABILITY() <= 90) && (object1 instanceof Caterpillar)) {
                meal += Caterpillar.getWeight();
                ((Caterpillar) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Caterpillar) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Caterpillar.getCaterpillars().remove(object1);
                Caterpillar.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 90) && (object2 instanceof Caterpillar)) {
                meal += Caterpillar.getWeight();
                ((Caterpillar) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Caterpillar) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Caterpillar.getCaterpillars().remove(object2);
                Caterpillar.getSemaphore()[this.x][this.y].release();
            } else if (object1 instanceof Grass) {
                meal += Grass.getWeight();
                ((Grass) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                Island.getIslandLocations()[this.x][this.y].remove(preIndex);
                Grass.getSemaphore()[this.x][this.y].release();
            } else if (object2 instanceof Grass) {
                meal += Grass.getWeight();
                ((Grass) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                Island.getIslandLocations()[this.x][this.y].remove(postIndex);
                Grass.getSemaphore()[this.x][this.y].release();
            } else meal += 0;

            return meal;
        }
    }

    @Override
    public String toString() {
        return Duck.GROUP_NAME;
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