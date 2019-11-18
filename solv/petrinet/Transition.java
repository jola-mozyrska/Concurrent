package petrinet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;

public class Transition<T> {
    Map<T, Integer> input, output;
    Collection<T> reset, inhibitor;

    public Transition(Map<T, Integer> input, Collection<T> reset,
                      Collection<T> inhibitor, Map<T, Integer> output) {
        this.input = new HashMap(input);
        this.output = new HashMap(output);
        this.reset = new HashSet<>(reset);
        this.inhibitor = new HashSet<>(inhibitor);
    }

    public boolean isEnabled(Map<T, Integer> stateOfTokens) {
        for (Map.Entry<T, Integer> entry : input.entrySet()) {
            T place = entry.getKey();
            int weight = entry.getValue();
            int tokens = 0;
            if (stateOfTokens.containsKey(place))
                tokens = stateOfTokens.get(place);
            if (tokens < weight)
                return false;
        }

        for (T place : inhibitor) {
            if (stateOfTokens.containsKey(place))
                return false;
        }

        return true;
    }

    public Map<T, Integer> fireSingleTransition(Map<T, Integer> originalStateOfTokens) {
        Map<T, Integer> newStateOfTokens = new HashMap(originalStateOfTokens);
        for (Map.Entry<T, Integer> entry : input.entrySet()) {
            T place = entry.getKey();
            int weight = entry.getValue();
            if (newStateOfTokens.containsKey(place)) {
                int tokens = newStateOfTokens.get(place);
                if (tokens - weight > 0)
                    newStateOfTokens.put(place, tokens - weight);
                else
                    newStateOfTokens.remove(place);
            }
        }

        for (T place : reset) {
            if (newStateOfTokens.containsKey(place))
                newStateOfTokens.remove(place);
        }

        for (Map.Entry<T, Integer> entry : output.entrySet()) {
            T place = entry.getKey();
            int weight = entry.getValue();
            if (newStateOfTokens.containsKey(place)) {
                int tokens = newStateOfTokens.get(place);
                if (tokens + weight > 0)
                    newStateOfTokens.put(place, tokens + weight);
                else
                    newStateOfTokens.remove(place);
            } else {
                newStateOfTokens.put(place, weight);
            }
        }
        return newStateOfTokens;
    }
}