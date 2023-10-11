import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
public class Bear extends Predator implements Runnable {
    private static final String GROUP_NAME = "\uD83D\uDC3B";
    private static final double WEIGHT = 500;
    private static final int GROUP = 5;
    private static final int MAX_SPEED = 2;
    private static final List<Bear> BEARS = new CopyOnWriteArrayList<>();  //first group of Bears which come to Island
    {
        foodNorm = 2;
    }
    public static double getWeight() {
        return WEIGHT;
    }
    public static CopyOnWriteArrayList<Bear> getBears() {
        return (CopyOnWriteArrayList<Bear>) BEARS;
    }
    static void fillBearsList() {
        for (int i = 0; i < 100; i++) {
            Bear bear = new Bear();
            BEARS.add(bear);
        }
    }

    /*According to the rules of island's locations, one cell can't contain more
       animals of one type, then GROUP amount. Because of it we need Semaphore for controlling
       amount of each animal's group at every step of the movement of each animal.
        */
    private static final Semaphore[][] semaphores = new Semaphore[100][20];

    static {
        for (int i = 0; i < semaphores.length; i++) {
            for (int j = 0; j < semaphores[i].length; j++) {
                semaphores[i][j] = new Semaphore(GROUP, true);
            }
        }
    }

    public static Semaphore[][] getSemaphore() {
        return semaphores;
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
                    Bear.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(random, this);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            while (Bear.semaphores[this.x][this.y].availablePermits() < 1);
        }
    }


    void move() {
        synchronized (Island.getIslandLocations()) {
            int newBearX = this.x;
            int newBearY = this.y;

            int delta = ThreadLocalRandom.current().nextInt(1, Bear.MAX_SPEED + 1);

            if ((newBearX < 3) && (newBearY < 3)) {
                newBearY += delta;
            } else if ((newBearX >= 3) && (newBearX <= 97) && (newBearY >= 3) && (newBearY <= 17)) {
                newBearX += Math.abs(delta / 2);
                newBearY += Math.abs(delta - Math.abs(delta / 2));
            } else if ((newBearX > 97) && (newBearY < 3)) {
                newBearY += delta;
            } else if ((newBearX > 97) && (newBearY > 17)) {
                newBearY -= delta;
            } else if (((newBearX < 3) && (newBearY > 17))) {
                newBearY -= delta;
            }
            if (Bear.semaphores[newBearX][newBearY].availablePermits() > 0) {
                try {
                    Island.getIslandLocations()[this.x][this.y].remove(this);
                    Bear.semaphores[this.x][this.y].release();
                    Bear.semaphores[newBearX][newBearY].acquire();
                    this.x = newBearX;
                    this.y = newBearY;
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

        /*  We should count array length directly in this method, because our island is dynamic,
       and any time out of this method this length could be not relevant.
        */
            int arrayLength = Island.getIslandLocations()[this.x][this.y].size();
            int nearIndex = Math.min(thisIndex + 1, arrayLength - 1);

            // We could add baby of animals in this cell, only if group of these animals less than GROUP amount (Island's rules)
            if (!Island.getIslandLocations()[this.x][this.y].get(nearIndex).equals(this) && (getPROBABILITY() <= 50) &&
                    (Bear.semaphores[this.x][this.y].availablePermits() > 0) &&
                    Island.getIslandLocations()[this.x][this.y].get(nearIndex) instanceof Bear) {
                try {
                    Bear.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(new Bear());
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
            int arrayLength = Island.getIslandLocations()[this.x][this.y].size();

            int preIndex = Math.max((thisIndex - 1), 0);
            int postIndex = Math.min((thisIndex + 1), (arrayLength - 1));

            Object object1 = Island.getIslandLocations()[this.x][this.y].get(preIndex);
            Object object2 = Island.getIslandLocations()[this.x][this.y].get(postIndex);

            if ((getPROBABILITY() <= 10) && (object1 instanceof Duck)) {
                meal += Duck.getWeight();
                ((Duck) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Duck) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Duck.getDucks().remove(object1);
                Duck.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 10) && (object2 instanceof Duck)) {
                meal += Duck.getWeight();
                ((Duck) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Duck) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Duck.getDucks().remove(object2);
                Duck.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 20) && (object1 instanceof Buffalo)) {
                meal += Buffalo.getWeight();
                ((Buffalo) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Buffalo) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Buffalo.getBuffaloes().remove(object1);
                Buffalo.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 20) && (object2 instanceof Buffalo)) {
                meal += Buffalo.getWeight();
                ((Buffalo) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Buffalo) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Buffalo.getBuffaloes().remove(object2);
                Buffalo.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 40) && (object1 instanceof Horse)) {
                meal += Horse.getWeight();
                ((Horse) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Horse) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Horse.getHorses().remove(object1);
                Horse.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 40) && (object2 instanceof Horse)) {
                meal += Horse.getWeight();
                ((Horse) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Horse) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Horse.getHorses().remove(object2);
                Horse.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 50) && (object1 instanceof Boar)) {
                meal += Boar.getWeight();
                ((Boar) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Boar) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Boar.getBoars().remove(object1);
                Boar.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 50) && (object2 instanceof Boar)) {
                meal += Boar.getWeight();
                ((Boar) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Boar) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Boar.getBoars().remove(object2);
                Boar.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 70) && (object1 instanceof Sheep)) {
                meal += Sheep.getWeight();
                ((Sheep) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Sheep) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Sheep.getSheep().remove(object1);
                Sheep.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 70) && (object2 instanceof Sheep)) {
                meal += Sheep.getWeight();
                ((Sheep) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Sheep) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Sheep.getSheep().remove(object2);
                Sheep.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 70) && (object1 instanceof Goat)) {
                meal += Goat.getWeight();
                ((Goat) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Goat) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Goat.getGoats().remove(object1);
                Goat.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 70) && (object2 instanceof Goat)) {
                meal += Goat.getWeight();
                ((Goat) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Goat) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Goat.getGoats().remove(object2);
                Goat.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 80) && (object1 instanceof Deer)) {
                meal += Deer.getWeight();
                ((Deer) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Deer) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Deer.getDeer().remove(object1);
                Deer.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 80) && (object2 instanceof Deer)) {
                meal += Deer.getWeight();
                ((Deer) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Deer) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Deer.getDeer().remove(object2);
                Deer.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 80) && (object1 instanceof Rabbit)) {
                meal += Rabbit.getWeight();
                ((Rabbit) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Rabbit) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Rabbit.getRabbits().remove(object1);
                Rabbit.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 80) && (object2 instanceof Rabbit)) {
                meal += Rabbit.getWeight();
                ((Rabbit) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Rabbit) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Rabbit.getRabbits().remove(object2);
                Rabbit.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 80) && (object1 instanceof Snake)) {
                meal += Snake.getWeight();
                ((Snake) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Snake) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Snake.getSnakes().remove(object1);
                Snake.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 80) && (object2 instanceof Snake)) {
                meal += Snake.getWeight();
                ((Snake) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Snake) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Snake.getSnakes().remove(object2);
                Snake.getSemaphore()[this.x][this.y].release();
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

            return this.meal;
        }
    }

    @Override
    public String toString() {
        return Bear.GROUP_NAME;
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
