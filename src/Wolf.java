import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
public class Wolf extends Predator implements Runnable{
    private static final String GROUP_NAME = "\uD83D\uDC3A";
    private static final double WEIGHT = 50;
    private static final int GROUP = 30;
    private static final int MAX_SPEED = 3;
    private static final List<Wolf> WOLVES = new CopyOnWriteArrayList<>();
    {
        foodNorm = 8;
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
    public static CopyOnWriteArrayList<Wolf> getWolves(){
        return (CopyOnWriteArrayList<Wolf>) WOLVES;
    }
    static void fillWolvesList() {
        for (int i = 0; i < 100; i++) {
            Wolf wolf = new Wolf();
            WOLVES.add(wolf);
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
                    Wolf.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(random, this);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while (Wolf.semaphores[this.x][this.y].availablePermits() < 1);
        }
    }
    @Override
    void move() {
        synchronized (Island.getIslandLocations()) {
            int newWolfX = this.x;
            int newWolfY = this.y;

            Island.getIslandLocations()[this.x][this.y].remove(this);

            int delta = ThreadLocalRandom.current().nextInt(0, MAX_SPEED + 1);

            if ((newWolfX < 4) && (newWolfY < 4)) {
                newWolfY += delta;
            } else if ((newWolfX < 4) && (newWolfY > 16)) {
                newWolfX += delta;
            } else if ((newWolfX > 96) && (newWolfY > 16)) {
                newWolfY -= delta;
            } else if (((newWolfX > 96) && (newWolfY < 4))) {
                newWolfX -= delta;
            }
            if (Wolf.semaphores[newWolfX][newWolfY].availablePermits() > 0) {
                try {
                    Island.getIslandLocations()[this.x][this.y].remove(this);
                    Wolf.semaphores[this.x][this.y].release();
                    Wolf.semaphores[newWolfX][newWolfY].acquire();
                    this.x = newWolfX;
                    this.y = newWolfY;
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

            if (((!Island.getIslandLocations()[this.x][this.y].get(nearIndex1).equals(this) && (getPROBABILITY() <= 50) &&
                    (Wolf.semaphores[this.x][this.y].availablePermits() > 0) &&
                    Island.getIslandLocations()[this.x][this.y].get(nearIndex1) instanceof Wolf)) ||
                    ((!Island.getIslandLocations()[this.x][this.y].get(nearIndex2).equals(this) && (getPROBABILITY() <= 50) &&
                            (Wolf.semaphores[this.x][this.y].availablePermits() > 0) &&
                            Island.getIslandLocations()[this.x][this.y].get(nearIndex2) instanceof Wolf))) {
                try {
                    Wolf.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(new Wolf());
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

            if ((getPROBABILITY() <= 10) && (object1 instanceof Horse)) {
                meal += Horse.getWeight();
                ((Horse) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Horse) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Horse.getHorses().remove(object1);
                Horse.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 10) && (object2 instanceof Horse)) {
                meal += Horse.getWeight();
                ((Horse) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Horse) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Horse.getHorses().remove(object2);
                Horse.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 10) && (object1 instanceof Buffalo)) {
                meal += Buffalo.getWeight();
                ((Buffalo) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Buffalo) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Buffalo.getBuffaloes().remove(object1);
                Buffalo.getSemaphore()[this.x][this.y].release();
            } else if (((getPROBABILITY() >= 1) && (getPROBABILITY() <= 10)) && (object2 instanceof Buffalo)) {
                meal += Buffalo.getWeight();
                ((Buffalo) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Buffalo) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Buffalo.getBuffaloes().remove(object2);
                Buffalo.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 15) && (object1 instanceof Deer)) {
                meal += Deer.getWeight();
                ((Deer) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Deer) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Deer.getDeer().remove(object1);
                Deer.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 15) && (object2 instanceof Deer)) {
                meal += Deer.getWeight();
                ((Deer) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Deer) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Deer.getDeer().remove(object2);
                Deer.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 15) && (object1 instanceof Boar)) {
                meal += Boar.getWeight();
                ((Boar) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Boar) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Boar.getBoars().remove(object1);
                Boar.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 15) && (object2 instanceof Boar)) {
                meal += Boar.getWeight();
                ((Boar) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Boar) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Boar.getBoars().remove(object2);
                Boar.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 40) && (object1 instanceof Duck)) {
                meal += Duck.getWeight();
                ((Duck) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Duck) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Duck.getDucks().remove(object1);
                Duck.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 40) && (object2 instanceof Duck)) {
                meal += Duck.getWeight();
                ((Duck) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Duck) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Duck.getDucks().remove(object2);
                Duck.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 60) && (object1 instanceof Rabbit)) {
                meal += Rabbit.getWeight();
                ((Rabbit) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Rabbit) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Rabbit.getRabbits().remove(object1);
                Rabbit.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 60) && (object2 instanceof Rabbit)) {
                meal += Rabbit.getWeight();
                ((Rabbit) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Rabbit) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Rabbit.getRabbits().remove(object2);
                Rabbit.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 60) && (object1 instanceof Goat)) {
                meal += Goat.getWeight();
                ((Goat) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Goat) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Goat.getGoats().remove(object1);
                Goat.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 60) && (object2 instanceof Goat)) {
                meal += Goat.getWeight();
                ((Goat) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Goat) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Goat.getGoats().remove(object2);
                Goat.getSemaphore()[this.x][this.y].release();
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
            } else if ((getPROBABILITY() <= 80) && (object1 instanceof Mouse)) {
                meal += Mouse.getWeight();
                ((Mouse) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Mouse) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Mouse.getMouses().remove(object1);
                Mouse.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 80) && (object2 instanceof Mouse)) {
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
    public String toString() {return GROUP_NAME;}

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