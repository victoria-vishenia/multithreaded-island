public interface Eatable {
    // When somebody eat some animal, and it dies because of it
    default boolean eatenBySmb(boolean isEaten){
        return isEaten;
    }
}
