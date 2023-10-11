import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

public class Snake extends Predator implements Eatable, Runnable{
    private static final String GROUP_NAME = "\uD83D\uDC0D";
    private static final double WEIGHT = 15;
    private static final int GROUP = 30;
    private static final int MAX_SPEED = 1;
    private static final List<Snake> SNAKES = new CopyOnWriteArrayList<>();
    {
        foodNorm = 3;
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
    public static CopyOnWriteArrayList<Snake> getSnakes(){
        return (CopyOnWriteArrayList<Snake>) SNAKES;
    }
    static void fillSnakesList() {
        for (int i = 0; i < 100; i++) {
            Snake snake = new Snake();
            SNAKES.add(snake);
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
                    Snake.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(random, this);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while (Snake.semaphores[this.x][this.y].availablePermits() < 1);
        }
    }
    void move() {
        synchronized (Island.getIslandLocations()) {
            int newSnakeX = this.x;
            int newSnakeY = this.y;

            Island.getIslandLocations()[this.x][this.y].remove(this);

            int delta = ThreadLocalRandom.current().nextInt(0, MAX_SPEED + 1);

            if ((newSnakeX < 2) && (newSnakeY < 2)) {
                newSnakeX += Math.abs(delta / 2);
                newSnakeY += Math.abs(delta - Math.abs(delta / 2));
            } else if ((newSnakeX > 98) && (newSnakeY > 18)) {
                newSnakeX -= Math.abs(delta / 2);
                newSnakeY -= Math.abs(delta - Math.abs(delta / 2));
            }
            if (Snake.semaphores[newSnakeX][newSnakeY].availablePermits() > 0) {
                try {
                    Island.getIslandLocations()[this.x][this.y].remove(this);
                    Snake.semaphores[this.x][this.y].release();
                    Snake.semaphores[newSnakeX][newSnakeY].acquire();
                    this.x = newSnakeX;
                    this.y = newSnakeY;
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
            int nearIndex1 = Math.min(thisIndex + 1, arrayLength - 1);
            int nearIndex2 = Math.min(thisIndex + 2, arrayLength - 1);
            int nearIndex3 = Math.min(thisIndex + 3, arrayLength - 1);

            if ((!Island.getIslandLocations()[this.x][this.y].get(nearIndex1).equals(this) &&
                    (Snake.semaphores[this.x][this.y].availablePermits() > 0) &&
                    Island.getIslandLocations()[this.x][this.y].get(nearIndex1) instanceof Snake) ||
                    (!Island.getIslandLocations()[this.x][this.y].get(nearIndex2).equals(this) &&
                            (Snake.semaphores[this.x][this.y].availablePermits() > 0) &&
                            Island.getIslandLocations()[this.x][this.y].get(nearIndex2) instanceof Snake) ||
                    (!Island.getIslandLocations()[this.x][this.y].get(nearIndex3).equals(this) &&
                            (Snake.semaphores[this.x][this.y].availablePermits() > 0) &&
                            Island.getIslandLocations()[this.x][this.y].get(nearIndex3) instanceof Snake)) {
                try {
                    Snake.semaphores[this.x][this.y].acquire();
                    Island.getIslandLocations()[this.x][this.y].add(new Snake());
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
            } else if ((getPROBABILITY() <= 15) && (object1 instanceof Fox)) {
                meal += Fox.getWeight();
                ((Fox) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Fox) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Fox.getFoxes().remove(object1);
                Fox.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 15) && (object2 instanceof Fox)) {
                meal += Fox.getWeight();
                ((Fox) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Fox) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Fox.getFoxes().remove(object2);
                Fox.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 20) && (object1 instanceof Rabbit)) {
                meal += Rabbit.getWeight();
                ((Rabbit) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Rabbit) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Rabbit.getRabbits().remove(object1);
                Rabbit.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 20) && (object2 instanceof Rabbit)) {
                meal += Rabbit.getWeight();
                ((Rabbit) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                ((Rabbit) Island.getIslandLocations()[this.x][this.y].get(postIndex)).die();
                Rabbit.getRabbits().remove(object2);
                Rabbit.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 40) && (object1 instanceof Mouse)) {
                meal += Mouse.getWeight();
                ((Mouse) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                ((Mouse) Island.getIslandLocations()[this.x][this.y].get(preIndex)).die();
                Mouse.getMouses().remove(object1);
                Mouse.getSemaphore()[this.x][this.y].release();
            } else if ((getPROBABILITY() <= 40) && (object2 instanceof Mouse)) {
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
