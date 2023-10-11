public abstract class Herbivorous extends Animal implements Eatable {

    /* Most of the Herbivorous have the same meal. All of them, who eat only grass, have absolutely
equal methods of eating grass. In classes of animals, who can eat caterpillar too, this method will be overridden.
     */

    double eatGrass() {
        synchronized (Island.getIslandLocations()) {
            int thisIndex = Island.getIslandLocations()[this.x][this.y].indexOf(this);
            int arrayLength = Island.getIslandLocations()[this.x][this.y].toArray().length;

            //Calculation of the nearest indexes based on the possibility to go beyond the array boundaries
            int preIndex = Math.max((thisIndex - 1), 0);
            int postIndex = Math.min((thisIndex + 1), (arrayLength - 1));

            boolean isGrass1 = Island.getIslandLocations()[this.x][this.y].get(preIndex) instanceof Grass;
            boolean isGrass2 = Island.getIslandLocations()[this.x][this.y].get(postIndex) instanceof Grass;

            if (isGrass1) {
                meal += Grass.getWeight();
                ((Grass) Island.getIslandLocations()[this.x][this.y].get(preIndex)).eatenBySmb(true);
                Island.getIslandLocations()[this.x][this.y].remove(preIndex);
                Grass.getSemaphore()[this.x][this.y].release();
            } else if (isGrass2) {
                meal += Grass.getWeight();
                ((Grass) Island.getIslandLocations()[this.x][this.y].get(postIndex)).eatenBySmb(true);
                Island.getIslandLocations()[this.x][this.y].remove(postIndex);
                Grass.getSemaphore()[this.x][this.y].release();
            } else meal += 0;
            return meal;
        }
    }
}