package Multiplicator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static PetriNet net;
    private static Map<String, petrinet.Transition<String>> transitions;

    public static void main(String[] args) {
        Map<String, Integer> initial = new HashMap<>();
        initial.put("Shared", 1);
        net = PetriNet(initial, true);

        transitions = new HashMap<>();
        Map<String, Integer> input = new HashMap<>();
        input.put("innerPlaceWithToken", 1);
        input.put("outerPlaceEmpty", 1);
        Collection<String> inhibitor = new ArrayList<>();
        Map<String, Integer> output = new HashMap<>();
        output.put("outerPlaceWithA", 1);
        output.put("innerPlaceWithToken", 1);
        petrinet.Transition<String> V =
                new petrinet.Transition<String>(input, new ArrayList<>(), inhibitor, output);
        transitions.put("V", V);

        input.clear();
        output.clear();

        input.put("outerPlaceWithA", 1);
        input.put("innerPlaceEmpty", 1);

        output.put("innerPlaceEmpty", 1);
        output.put("outerPlaceEmpty", 1);
        output.put("A*B", 1);

        petrinet.Transition<String> P =
                new petrinet.Transition<String>(input, new ArrayList<>(), inhibitor, output);
        transitions.put("P", P);

        input.clear();
        output.clear();





        input.clear();
        inhibitor.clear();
        output.clear();


    }
}
