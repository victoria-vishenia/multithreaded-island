import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

abstract class Animal implements Eatable{
    double foodNorm;
    double meal;
    // Probability of eating somebody and reproducing
    int x ;
    int y;
    private final int PROBABILITY = ThreadLocalRandom.current().nextInt(1, 101);
    public int getPROBABILITY(){
        return PROBABILITY;
    }
    private final ReentrantLock LOCK = new ReentrantLock();
    public ReentrantLock getLOCK(){
        return LOCK;
    }
    abstract void firstLocated();
    abstract void move();
    abstract void reproduce();
    void die() {
        if (this.eatenBySmb(true)) {
            Island.getIslandLocations()[this.x][this.y].remove(this);
        }
    }
}