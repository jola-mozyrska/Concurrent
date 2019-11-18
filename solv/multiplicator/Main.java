package multiplicator;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import petrinet.*;

public class Main {
    private static PetriNet<String> net;
    private static Set<Transition<String>> transitions = new HashSet<>();

    private static class petriRunnable implements Runnable {
        private String name;

        public petriRunnable(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            int counter = 0;
            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    Thread.currentThread.interrupt();
                    System.out.println("I am thread named " + this.name +
                            ", I fired " + counter + " transitions.");
                    Thread
                    return;
                }
                try {
                    Transition<String> t = net.fire(transitions);
                    if (t != null)
                        ++counter;
                } catch (InterruptedException e) {
                    Thread.currentThread.interrupt();
                }
            }
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int a = in.nextInt();
        int b = in.nextInt();

        Map<String, Integer> initial = new HashMap<>();
        initial.put("innerPlaceWithToken", 1);
        initial.put("outerPlaceWithA", a);
        initial.put("placeWithB", b);
        net = new PetriNet<>(initial, true);

        Map<String, Integer> input = new HashMap<>();
        input.put("innerPlaceWithToken", 1);
        input.put("outerPlaceEmpty", 1);
        Collection<String> inhibitor = new ArrayList<>();
        Map<String, Integer> output = new HashMap<>();
        output.put("outerPlaceWithA", 1);
        output.put("innerPlaceWithToken", 1);
        Transition<String> V =
                new Transition<String>(input, new ArrayList<>(), inhibitor, output);
        transitions.add(V);

        input.clear();
        output.clear();

        input.put("outerPlaceWithA", 1);
        input.put("innerPlaceEmpty", 1);

        output.put("innerPlaceEmpty", 1);
        output.put("outerPlaceEmpty", 1);
        output.put("A*B", 1);

        Transition<String> P =
                new Transition<String>(input, new ArrayList<>(), inhibitor, output);
        transitions.add(P);

        input.clear();
        output.clear();

        input.put("innerPlaceEmpty", 1);
        output.put("innerPlaceWithToken", 1);
        inhibitor.add("outerPlaceWithA");

        Transition<String> upperTransition =
                new Transition<String>(input, new ArrayList<>(), inhibitor, output);
        transitions.add(upperTransition);

        input.clear();
        inhibitor.clear();
        output.clear();

        input.put("innerPlaceWithToken", 1);
        input.put("placeWithB", 1);
        output.put("innerPlaceEmpty", 1);
        inhibitor.add("outerPlaceEmpty");

        Transition<String> lowerTransition =
                new Transition<String>(input, new ArrayList<>(), inhibitor, output);
        transitions.add(lowerTransition);

        input.clear();
        inhibitor.clear();
        output.clear();

        input.put("innerPlaceWithToken", 1);
        inhibitor.add("placeWithB");

        Transition<String> endingTransition =
                new Transition<String>(input, new ArrayList<>(), inhibitor, output);

        //  transitions created

        List<Thread> helpfulThreads = new ArrayList<>();
        for (int i = 0; i < 4; ++i) {
            Thread t = new Thread(new petriRunnable("A" + i));
            t.start();
            helpfulThreads.add(t);
        }
        HashSet<Transition<String>> endingSet = new HashSet<Transition<String>>();
        endingSet.add(endingTransition);

        try {
            net.fire(endingSet);
        } catch (InterruptedException e) {
            for (int i = 0; i < 4; ++i)
                helpfulThreads.get(i).interrupt();
            System.out.println("Ending fire interrupted.");
            return;
        }

        for (int i = 0; i < 4; ++i)
            helpfulThreads.get(i).interrupt();

        int result = net.getMarkingAt("A*B");
        System.out.println("Multiplication result: " + result);
    }
}
