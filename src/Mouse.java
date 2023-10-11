import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
public class Mouse extends Herbivorous implements Eatable, Runnable{
    private static final String GROUP_NAME = "\uD83D\uDC01";
    private static final double WEIGHT=0.05;
    private static final int GROUP = 500;
    private static final int MAX_SPEED = 1;
    private static final List<Mouse> MOUSES = new CopyOnWriteArrayList<>();
    {
        foodNorm=0.01;
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
    public static CopyOnWriteArrayList<Mouse> getMouses(){
        return (CopyOnWriteArrayList<Mouse>) MOUSES;
    }
    static void fillMousesList() {
        for (int i = 0; i < 100; i++) {
            Mouse mouse = new Mouse();
            MOUSES.add(mouse);
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
                    Mouse.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(random, this);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while (Mouse.semaphores[this.x][this.y].availablePermits() < 1);
        }
    }
    @Override
    void move() {
        synchronized (Island.getIslandLocations()) {
            int newMouseX = this.x;
            int newMouseY = this.y;

            Island.getIslandLocations()[this.x][this.y].remove(this);

            int delta = ThreadLocalRandom.current().nextInt(0, MAX_SPEED + 1);

            if ((newMouseX < 3) && (newMouseY < 3)) {
                newMouseX += Math.abs(delta / 2);
                newMouseY += Math.abs(delta - Math.abs(delta / 2));
            } else if ((newMouseX > 97) && (newMouseY > 17)) {
                newMouseY -= delta;
            } else if ((newMouseX > 96) && (newMouseY < 3)) {
                newMouseX -= delta;
            }
            if (Mouse.semaphores[newMouseX][newMouseY].availablePermits() > 0) {
                try {
                    Island.getIslandLocations()[this.x][this.y].remove(this);
                    Mouse.semaphores[this.x][this.y].release();
                    Mouse.semaphores[newMouseX][newMouseY].acquire();
                    this.x = newMouseX;
                    this.y = newMouseY;
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
            int nearIndex3 = Math.min(thisIndex + 1, arrayLength - 1);
            int nearIndex4 = Math.min(thisIndex + 2, arrayLength - 1);

            if ((!Island.getIslandLocations()[this.x][this.y].get(nearIndex1).equals(this) &&
                    (Mouse.semaphores[this.x][this.y].availablePermits() > 0) &&
                    Island.getIslandLocations()[this.x][this.y].get(nearIndex1) instanceof Mouse) ||
                    (!Island.getIslandLocations()[this.x][this.y].get(nearIndex2).equals(this) &&
                            (Mouse.semaphores[this.x][this.y].availablePermits() > 0) &&
                            Island.getIslandLocations()[this.x][this.y].get(nearIndex2) instanceof Mouse) ||
                    (!Island.getIslandLocations()[this.x][this.y].get(nearIndex3).equals(this) &&
                            (Mouse.semaphores[this.x][this.y].availablePermits() > 0) &&
                            Island.getIslandLocations()[this.x][this.y].get(nearIndex3) instanceof Mouse) ||
                    (!Island.getIslandLocations()[this.x][this.y].get(nearIndex4).equals(this) &&
                            (Mouse.semaphores[this.x][this.y].availablePermits() > 0) &&
                            Island.getIslandLocations()[this.x][this.y].get(nearIndex4) instanceof Mouse)) {
                try {
                    Mouse.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(new Mouse());
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
