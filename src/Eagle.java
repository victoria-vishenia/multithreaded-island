import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

class Eagle extends Predator implements Runnable {
    private static final String GROUP_NAME = "\ud83e\udd85";
    private static final double WEIGHT = 6;
    private static final int GROUP = 20;
    private static final int MAX_SPEED = 3;
    private static final List<Eagle> EAGLES = new CopyOnWriteArrayList<>();
    {
        foodNorm = 1;
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
    public static CopyOnWriteArrayList<Eagle> getEagles(){
        return (CopyOnWriteArrayList<Eagle>) EAGLES;
    }
    static void fillEaglesList() {
        for (int i = 0; i < 100; i++) {
            Eagle eagle = new Eagle();
            EAGLES.add(eagle);
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
                    Eagle.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(random, this);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while  (Eagle.semaphores[this.x][this.y].availablePermits() < 1);
        }
    }
    @Override
    void move() {
        synchronized (Island.getIslandLocations()) {
            int newEagleX = this.x;
            int newEagleY = this.y;

            Island.getIslandLocations()[this.x][this.y].remove(this);

            int delta = ThreadLocalRandom.current().nextInt(0, MAX_SPEED + 1);

            newEagleY += delta;

            if (newEagleY > 16) {
                newEagleY -= delta;
            } else if (newEagleY < 5) {
                newEagleY += delta;
            }

            if (newEagleX < 95) {
                newEagleX += 1;
            }
            if (Eagle.semaphores[newEagleX][newEagleY].availablePermits() > 0) {
                try {
                    Island.getIslandLocations()[this.x][this.y].remove(this);
                    Eagle.semaphores[this.x][this.y].release();
                    Eagle.semaphores[newEagleX][newEagleY].acquire();
                    this.x = newEagleX;
                    this.y = newEagleY;
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
                    (Eagle.semaphores[this.x][this.y].availablePermits() > 0) &&
                    Island.getIslandLocations()[this.x][this.y].get(nearIndex1) instanceof Eagle)) ||
                    ((!Island.getIslandLocations()[this.x][this.y].get(nearIndex2).equals(this) &&
                            (Eagle.semaphores[this.x][this.y].availablePermits() > 0) &&
                            Island.getIslandLocations()[this.x][this.y].get(nearIndex2) instanceof Eagle))) {
                try {
                    Eagle.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(new Eagle());
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

            if ((getPROBABILITY() <= 10) && (object1 instanceof Fox)) {
                meal += Fox.getWeight();
                ((Fox) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Fox) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Fox.getFoxes().remove(object1);
                Fox.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 10) && (object2 instanceof Fox)) {
                meal += Fox.getWeight();
                ((Fox) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Fox) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Fox.getFoxes().remove(object2);
                Fox.getSemaphore()[this.x][this.y].release();
            } else if (((getPROBABILITY() <= 80)) && (object1 instanceof Duck)) {
                meal += Duck.getWeight();
                ((Duck) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Duck) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Duck.getDucks().remove(object1);
                Duck.getSemaphore()[this.x][this.y].release();
            } else if (((getPROBABILITY() <= 80)) && (object2 instanceof Duck)) {
                meal += Duck.getWeight();
                ((Duck) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Duck) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Duck.getDucks().remove(object2);
                Duck.getSemaphore()[this.x][this.y].release();
            } else if (((getPROBABILITY() <= 90)) && (object1 instanceof Rabbit)) {
                meal += Rabbit.getWeight();
                ((Rabbit) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Rabbit) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Rabbit.getRabbits().remove(object1);
                Rabbit.getSemaphore()[this.x][this.y].release();
            } else if (((getPROBABILITY() <= 90)) && (object2 instanceof Rabbit)) {
                meal += Rabbit.getWeight();
                ((Rabbit) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Rabbit) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Rabbit.getRabbits().remove(object2);
                Rabbit.getSemaphore()[this.x][this.y].release();
            } else if (((getPROBABILITY() <= 90)) && (object1 instanceof Mouse)) {
                meal += Mouse.getWeight();
                ((Mouse) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Mouse) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Mouse.getMouses().remove(object1);
                Mouse.getSemaphore()[this.x][this.y].release();
            } else if (((getPROBABILITY() <= 90)) && (object2 instanceof Mouse)) {
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