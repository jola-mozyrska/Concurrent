package alternator;

import java.util.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import petrinet.*;

public class Main {
    private static PetriNet<String> net;
    private static Map<String, Transition<String>> transitionsABC = new HashMap<>();

    private static class petriRunnable implements Runnable {
        private String name;

        public petriRunnable(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            while (true) {
                Transition<String> valueOfAcquire = transitionsABC.get(name + "acquire");
                HashSet<Transition<String>> hAcquire = new HashSet<Transition<String>>();
                hAcquire.add(valueOfAcquire);

                try {
                    net.fire(hAcquire);
                } catch (InterruptedException e) {
                    System.out.println("Exception while acquiring critical section.");
                    continue;
                }

                System.out.print(this.name);
                System.out.print(".");

                Transition<String> valueOfRelease = transitionsABC.get(name + "release");
                HashSet<Transition<String>> hRelease = new HashSet<Transition<String>>();
                hRelease.add(valueOfRelease);
                try {
                    net.fire(hRelease);
                } catch (InterruptedException e) {
                    System.out.println("Exception while releasing critical section.");
                }

            }
        }
    }

    public static void main(String[] args) {
        Map<String, Integer> currentMarking = new HashMap<>();
        currentMarking.put("Shared", 1);
        net = new PetriNet<>(currentMarking, true);

        for (int i = 1; i <= 3; ++i) {
            String inputNode = "Shared";
            String resetNode1 = "LB", resetNode2 = "LC";
            String inhibitNode = "LA";
            String outputNode1 = "LA", outputNode2 = "A";
            if (i == 2) {
                resetNode1 = "LA";
                resetNode2 = "LC";
                inhibitNode = "LB";
                outputNode1 = "LB";
                outputNode2 = "B";
            }
            if (i == 3) {
                resetNode1 = "LA";
                resetNode2 = "LB";
                inhibitNode = "LC";
                outputNode1 = "LC";
                outputNode2 = "C";
            }
            Map<String, Integer> input = new HashMap<>();
            input.put(inputNode, 1);
            Collection<String> reset = new ArrayList<>();
            reset.add(resetNode1);
            reset.add(resetNode2);
            Collection<String> inhibitor = new ArrayList<>();
            inhibitor.add(inhibitNode);
            Map<String, Integer> output = new HashMap<>();
            output.put(outputNode1, 1);
            output.put(outputNode2, 1);
            Transition<String> transition =
                    new Transition<String>(input, reset, inhibitor, output);
            transitionsABC.put(outputNode2 + "acquire", transition);
        }

        for (int i = 1; i <= 3; ++i) {
            String inputNode = "A";
            String outputNode = "Shared";
            if (i == 2)
                inputNode = "B";
            if (i == 3)
                inputNode = "C";

            Map<String, Integer> input = new HashMap<>();
            input.put(inputNode, 1);
            Map<String, Integer> output = new HashMap<>();
            output.put(outputNode, 1);
            Transition<String> transition =
                    new Transition<String>(input, new ArrayList<>(), new ArrayList<>(), output);
            transitionsABC.put(inputNode + "release", transition);
        }

        Set<Transition<String>> possibleTransitions = new HashSet<>();
        for (Map.Entry<String, Transition<String>> entry : transitionsABC.entrySet())
            possibleTransitions.add(entry.getValue());

        Set<Map<String, Integer>> reachableStates = net.reachable(possibleTransitions);
        System.out.println("Number of markings:");
        System.out.println(reachableStates.size());
        int counter = 0;

        Iterator<Map<String, Integer>> it = reachableStates.iterator();
        while (it.hasNext()) {
            for (Map.Entry<String, Integer> entry : it.next().entrySet()) {
                if (entry.getKey() == "A" && entry.getKey() == "B" && entry.getKey() == "C")
                    counter++;
                else if (entry.getKey() == "A" && entry.getKey() == "B")
                    counter++;
                else if (entry.getKey() == "A" && entry.getKey() == "C")
                    counter++;
                else if (entry.getKey() == "B" && entry.getKey() == "C")
                    counter++;
            }
        }

        if (counter != 0)
            System.out.println("It is not thread-safe!");

        Thread A = new Thread(new petriRunnable("A"));
        A.setName("A");
        Thread B = new Thread(new petriRunnable("B"));
        B.setName("B");
        Thread C = new Thread(new petriRunnable("C"));
        C.setName("C");
        A.start();
        B.start();
        C.start();
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            System.out.println("Interrupted while sleeping");
        } finally {
            A.interrupt();
            B.interrupt();
            C.interrupt();
        }
    }
}