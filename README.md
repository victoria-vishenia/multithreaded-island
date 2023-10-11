# Multithreaded island (Dynamic island)

**Description**

After launch, the application displays the state of the island at a specific point in time at a specified frequency:

![Island console view](https://github.com/victoria-vishenia/multithreaded-island/blob/master/MultiThreadedIsland.jpg)

This application is a model of an island with changeable parameters, consisting of an array of locations. The locations are filled with vegetation and animals. Animals can:
eat plants and/or other animals (if there is suitable food in their places),
move (to neighboring locations),
reproduce (if there is a pair in their location),
starve or be eaten.

The inhabitants of the island are represented by a hierarchy of classes:
Predators: Wolf, Boa constrictor, Fox, Bear, Eagle.
Herbivores: Horse, Deer, Rabbit, Mouse, Goat, Sheep, Boar, Buffalo, Duck, Caterpillar.
Plants.

Each animal has a certain probability with which it eats "food" if they are on the same square.

Each animal has characteristics: 
Weight of one animal, kg.
Maximum number of animals of this species on one cell.
Speed ​​of movement, no more than cells per turn.
How many kilograms of food does an animal need to be completely satisfied.

Each animal has methods: to eat, to reproduce, to choose the direction of movement.


