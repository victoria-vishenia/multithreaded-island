import java.time.LocalTime;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args)  {
        Island.fillLocations();

        Bear.fillBearsList();
        for (Bear bear : Bear.getBears()) {
            bear.firstLocated();
        }
        Boar.fillBoarsList();
        for (Boar boar : Boar.getBoars()) {
            boar.firstLocated();
        }
        Buffalo.fillBuffaloesList();
        for (Buffalo buffalo : Buffalo.getBuffaloes()) {
            buffalo.firstLocated();
        }
        Caterpillar.fillCaterpillarsList();
        for (Caterpillar caterpillar : Caterpillar.getCaterpillars()) {
            caterpillar.firstLocated();
        }
        Deer.fillDeerList();
        for (Deer deer : Deer.getDeer()) {
            deer.firstLocated();
        }
        Duck.fillDucksList();
        for (Duck duck : Duck.getDucks()) {
            duck.firstLocated();
        }
        Eagle.fillEaglesList();
        for (Eagle eagle : Eagle.getEagles()) {
            eagle.firstLocated();
        }
        Fox.fillFoxesList();
        for (Fox fox : Fox.getFoxes()) {
            fox.firstLocated();
        }
        Goat.fillGoatsList();
        for (Goat goat : Goat.getGoats()) {
            goat.firstLocated();
        }
        Horse.fillHorsesList();
        for (Horse horse : Horse.getHorses()) {
            horse.firstLocated();
        }
        Mouse.fillMousesList();
        for (Mouse mouse : Mouse.getMouses()) {
            mouse.firstLocated();
        }
        Rabbit.fillRabbitsList();
        for (Rabbit rabbit : Rabbit.getRabbits()) {
            rabbit.firstLocated();
        }
        Sheep.getSheepList();
        for (Sheep sheep : Sheep.getSheep()) {
            sheep.firstLocated();
        }
        Snake.fillSnakesList();
        for (Snake snake : Snake.getSnakes()) {
            snake.firstLocated();
        }
        Wolf.fillWolvesList();
        for (Wolf wolf : Wolf.getWolves()) {
            wolf.firstLocated();
        }


        Runnable task1 = () -> {
            for (int i = 0; i < Island.getIslandLocations().length; i++) {
                for (int j = 0; j < Island.getIslandLocations()[i].length; j++) {
                    if (Island.getIslandLocations()[i][j].size() < 5) {
                        Island.getIslandLocations()[i][j].add(new Grass());
                    }
                }
            }
            for (Bear bear : Bear.getBears()) {
                bear.run();
                bear.meal+=bear.meal;
            }
            if (Boar.getBoars().size() > 0) {
                for (Boar boar : Boar.getBoars()) {
                    boar.run();
                }
            }
            if (Buffalo.getBuffaloes().size() > 0) {
                for (Buffalo buffalo : Buffalo.getBuffaloes()) {
                    buffalo.run();
                }
            }
            if (Caterpillar.getCaterpillars().size() > 0) {
                for (Caterpillar caterpillar : Caterpillar.getCaterpillars()) {
                    caterpillar.run();
                }
            }
            if (Deer.getDeer().size() > 0) {
                for (Deer deer : Deer.getDeer()) {
                    deer.run();
                }
            }
            if (Duck.getDucks().size() > 0) {
                for (Duck duck : Duck.getDucks()) {
                    duck.run();
                }
            }
            for (Eagle eagle : Eagle.getEagles()) {
                eagle.run();
            }
            if (Fox.getFoxes().size() > 0) {
                for (Fox fox : Fox.getFoxes()) {
                    fox.run();
                }
            }
            if (Goat.getGoats().size() > 0) {
                for (Goat goat : Goat.getGoats()) {
                    goat.run();
                }
            }
            if (Horse.getHorses().size() > 0) {
                for (Horse horse : Horse.getHorses()) {
                    horse.run();
                }
            }
            if (Mouse.getMouses().size() > 0) {
                for (Mouse mouse : Mouse.getMouses()) {
                    mouse.run();
                }
            }
            if (Rabbit.getRabbits().size() > 0) {
                for (Rabbit rabbit : Rabbit.getRabbits()) {
                    rabbit.run();
                }
            }
            if (Sheep.getSheep().size() > 0) {
                for (Sheep sheep : Sheep.getSheep()) {
                    sheep.run();
                }
            }
            if (Snake.getSnakes().size() > 0) {
                for (Snake snake : Snake.getSnakes()) {
                    snake.run();
                }
            }
            if (Wolf.getWolves().size() > 0) {
                for (Wolf wolf : Wolf.getWolves()) {
                    wolf.run();
                }
            }

            Island.printIsland();
            System.out.println(" -----------------------------------------------------------");
        };

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(16);

        LocalTime localTime = LocalTime.now();
        LocalTime localTime1 = localTime.plusMinutes(1);

        executorService.scheduleWithFixedDelay(task1, 10, 10, TimeUnit.SECONDS);

        while (!LocalTime.now().isAfter(localTime1)) {
            LocalTime.now();
        }

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}