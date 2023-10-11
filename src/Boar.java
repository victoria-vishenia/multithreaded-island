import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

class Boar extends Herbivorous implements Eatable, Runnable{
    private static final String GROUP_NAME = "\uD83D\uDC17";
    private static final double WEIGHT = 400;
    private static final int MAX_SPEED = 2;
    private static final int GROUP = 5;
    private static final List<Boar> BOARS = new CopyOnWriteArrayList<>();
    {
        foodNorm = 50;
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
    public static CopyOnWriteArrayList<Boar> getBoars(){
        return (CopyOnWriteArrayList<Boar>) BOARS;
    }
    static void fillBoarsList() {
        for (int i = 0; i < 100; i++) {
            Boar boar = new Boar();
            BOARS.add(boar);
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
                    Boar.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(random, this);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while  (Boar.semaphores[this.x][this.y].availablePermits() < 1);
        }
    }

    @Override
    void move() {
        synchronized (Island.getIslandLocations()) {
            int newBoarX = this.x;
            int newBoarY = this.y;

            Island.getIslandLocations()[this.x][this.y].remove(this);

            int delta = ThreadLocalRandom.current().nextInt(0, MAX_SPEED + 1);

            if ((newBoarX < 3) && (newBoarY < 3)) {
                newBoarX += delta;
            } else if ((newBoarX >= 3) && (newBoarX <= 97) && (newBoarY >= 3) && (newBoarY <= 17)) {
                newBoarX += Math.abs(delta / 2);
                newBoarX += Math.abs(delta - Math.abs(delta / 2));
            } else if ((newBoarX > 97) && (newBoarY < 3)) {
                newBoarY += delta;
            } else if ((newBoarX > 97) && (newBoarY > 17)) {
                newBoarY -= delta;
            } else if (((newBoarX < 3) && (newBoarY > 17))) {
                newBoarY -= delta;
            }
            if (Boar.semaphores[newBoarX][newBoarY].availablePermits() > 0) {
                try {
                    Island.getIslandLocations()[this.x][this.y].remove(this);
                    Boar.semaphores[this.x][this.y].release();
                    Boar.semaphores[newBoarX][newBoarY].acquire();
                    this.x = newBoarX;
                    this.y = newBoarY;
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

            int nearIndex = Math.max(thisIndex - 1, 0);

            if ((!Island.getIslandLocations()[this.x][this.y].get(nearIndex).equals(this) && (getPROBABILITY() <= 80) &&
                    (Boar.semaphores[this.x][this.y].availablePermits() > 0) &&
                    Island.getIslandLocations()[this.x][this.y].get(nearIndex) instanceof Boar)) {
                try {
                    Boar.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(new Boar());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    double eatGrass() {
// override this method because some herbivorous eat not only grass
        synchronized (Island.getIslandLocations()) {
            int thisIndex = Island.getIslandLocations()[this.x][this.y].indexOf(this);
            int arrayLength = Island.getIslandLocations()[this.x][this.y].toArray().length;

        /*Calculation of the nearest indexes based on the possibility to go beyond the array boundaries
        Idea suggests me to use method Math.max(). But I prefer ternary operator in this case for visibility
        */
            int preIndex = Math.max((thisIndex - 1), 0);
            int postIndex = Math.min((thisIndex + 1), (arrayLength - 1));

            Object object1 = Island.getIslandLocations()[this.x][this.y].get(preIndex);
            Object object2 = Island.getIslandLocations()[this.x][this.y].get(postIndex);

            if ((getPROBABILITY() <= 50) && (object1 instanceof Mouse)) {
                meal += Mouse.getWeight();
                ((Mouse) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Mouse) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Mouse.getMouses().remove(object1);
                Mouse.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 50) && (object2 instanceof Mouse)) {
                meal += Mouse.getWeight();
                ((Mouse) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Mouse) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Mouse.getMouses().remove(object2);
                Mouse.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 90) && (object1 instanceof Caterpillar)) {
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
    public String toString() {return GROUP_NAME;}

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
