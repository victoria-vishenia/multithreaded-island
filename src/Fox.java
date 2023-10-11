import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

public class Fox extends Predator implements Eatable, Runnable {
    private static final String GROUP_NAME = "\uD83E\uDD8A";
    private static final double WEIGHT = 8;
    private static final int GROUP = 30;
    private static final int MAX_SPEED = 2;
    private static final List<Fox> FOXES = new CopyOnWriteArrayList<>();
    {
        foodNorm = 2;
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
    public static CopyOnWriteArrayList<Fox> getFoxes(){
        return (CopyOnWriteArrayList<Fox>) FOXES;
    }
    static void fillFoxesList() {
        for (int i = 0; i < 200; i++) {
            Fox fox = new Fox();
            FOXES.add(fox);
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
                    Fox.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(random, this);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while  (Fox.semaphores[this.x][this.y].availablePermits() < 1);
        }
    }
    @Override
    void move() {
        synchronized (Island.getIslandLocations()) {
            int newFoxX = this.x;
            int newFoxY = this.y;

            Island.getIslandLocations()[this.x][this.y].remove(this);

            int delta = ThreadLocalRandom.current().nextInt(0, MAX_SPEED + 1);

            if (newFoxX <= 90) {
                newFoxX += delta;
            } else if (newFoxX > 97) {
                newFoxX -= delta;
            }

            if (newFoxY < 18) {
                newFoxY += 1;
            }

            if (Fox.semaphores[newFoxX][newFoxY].availablePermits() > 0) {
                try {
                    Island.getIslandLocations()[this.x][this.y].remove(this);
                    Fox.semaphores[this.x][this.y].release();
                    Fox.semaphores[newFoxX][newFoxY].acquire();
                    this.x = newFoxX;
                    this.y = newFoxY;
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

            int nearIndex1 = Math.max(thisIndex - 1, 0);

            if ((!Island.getIslandLocations()[this.x][this.y].get(nearIndex1).equals(this) &&
                    (getPROBABILITY() <= 85) && (Fox.semaphores[this.x][this.y].availablePermits() > 0) &&
                    Island.getIslandLocations()[this.x][this.y].get(nearIndex1) instanceof Fox)) {
                try {
                    Fox.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(new Fox());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    double eatMeat() {
        synchronized (Island.getIslandLocations()) {
            int thisIndex = Island.getIslandLocations()[this.x][this.y].indexOf(this);
            int arrayLength = Island.getIslandLocations()[this.x][this.y].toArray().length;

            int preIndex = Math.max((thisIndex - 1), 0);
            int postIndex = Math.min((thisIndex + 1), (arrayLength - 1));

            Object object1 = Island.getIslandLocations()[this.x][this.y].get(preIndex);
            Object object2 = Island.getIslandLocations()[this.x][this.y].get(postIndex);

            if ((getPROBABILITY() <= 40) && (object1 instanceof Caterpillar)) {
                meal += Caterpillar.getWeight();
                ((Caterpillar) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Caterpillar) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Caterpillar.getCaterpillars().remove(object1);
                Caterpillar.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 40) && (object2 instanceof Caterpillar)) {
                meal += Caterpillar.getWeight();
                ((Caterpillar) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Caterpillar) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Caterpillar.getCaterpillars().remove(object2);
                Caterpillar.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 60) && (object1 instanceof Duck)) {
                meal += Duck.getWeight();
                ((Duck) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Duck) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Duck.getDucks().remove(object1);
                Duck.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 60) && (object2 instanceof Duck)) {
                meal += Duck.getWeight();
                ((Duck) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Duck) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Duck.getDucks().remove(object2);
                Duck.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 70) && (object1 instanceof Rabbit)) {
                meal += Rabbit.getWeight();
                ((Rabbit) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Rabbit) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Rabbit.getRabbits().remove(object1);
                Rabbit.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 70) && (object2 instanceof Rabbit)) {
                meal += Rabbit.getWeight();
                ((Rabbit) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Rabbit) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Rabbit.getRabbits().remove(object2);
                Rabbit.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 90) && (object1 instanceof Mouse)) {
                meal += Mouse.getWeight();
                ((Mouse) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Mouse) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Mouse.getMouses().remove(object1);
                Mouse.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 90) && (object2 instanceof Mouse)) {
                meal += Mouse.getWeight();
                ((Mouse) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Mouse) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Mouse.getMouses().remove(object2);
                Mouse.getSemaphore()[this.x][this.y].release();
            } else meal += 0;

            return meal;
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
            this.eatMeat();
        }
        getLOCK().unlock();
    }
}