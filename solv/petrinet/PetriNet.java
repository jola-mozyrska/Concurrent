package petrinet;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.HashMap;

public class PetriNet<T> {
    private boolean fair;
    private ConcurrentMap<T, Integer> currentMarking;
    private Semaphore globalMutex = new Semaphore(1);

    //  structures helping with firing waiting not fired threads
    private LinkedList<Collection<petrinet.Transition<T>>> notFiredTransitions = new LinkedList<>();
    private LinkedList<Semaphore> notFiredSemaphores = new LinkedList<>();
    private LinkedList<Thread> notFiredThreads = new LinkedList<>();

    public PetriNet(Map<T, Integer> initial, boolean fair) {
        this.fair = fair;
        this.currentMarking = new ConcurrentHashMap<>(initial);
    }

    public int getMarkingAt(T placeName) {
        return currentMarking.get(placeName);
    }

    public Set<Map<T, Integer>> reachable(Collection<Transition<T>> transitions) {

        Map<T, Integer> currentState = new HashMap(currentMarking);

        Set<Map<T, Integer>> availableStates = new HashSet<>();
        Queue<Map<T, Integer>> queueOfStates = new LinkedList<>();
        queueOfStates.add(new HashMap(currentMarking));
        availableStates.add(new HashMap(currentMarking));

        Iterator value = availableStates.iterator();

        //  getting next marking after firing transitions
        while (!queueOfStates.isEmpty()) {
            Map<T, Integer> state = queueOfStates.poll();

            for (Transition<T> transition : transitions) {
                if (!transition.isEnabled(state))
                    continue;
                Map<T, Integer> nextState = transition.fireSingleTransition(state);
                if (!availableStates.contains(nextState)) {
                    queueOfStates.add(nextState);
                    availableStates.add(nextState);
                }
            }
        }

        value = availableStates.iterator();
        return availableStates;
    }

    public Transition<T> fire(Collection<Transition<T>> transitions) throws InterruptedException {
        Transition<T> successfulTransition = null;
        try {
            globalMutex.acquire();
            for (Transition<T> transition : transitions) {
                if (transition.isEnabled(currentMarking)) {
                    Map<T, Integer> temporaryMap = new HashMap<>(currentMarking);
                    currentMarking = new ConcurrentHashMap<>(transition.fireSingleTransition(temporaryMap));
                    successfulTransition = transition;
                    break;
                }
            }

            //  there was no available transition to fire
            if (successfulTransition == null) {
                notFiredTransitions.add(transitions);
                Semaphore abilityToFireMutex = new Semaphore(0);
                notFiredSemaphores.add(abilityToFireMutex);
                notFiredThreads.add(Thread.currentThread());
                try {
                    globalMutex.release();
                    abilityToFireMutex.acquire();
                    for (Transition<T> t : transitions) {
                        if (t.isEnabled(currentMarking)) {
                            Map<T, Integer> temporaryMap = new HashMap<>(currentMarking);
                            currentMarking = new ConcurrentHashMap<>(t.fireSingleTransition(temporaryMap));
                            successfulTransition = t;
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }

            int tsId = 0;
            for (Collection<Transition<T>> ts : notFiredTransitions) {
                if (notFiredThreads.get(tsId).isInterrupted()) {
                    notFiredThreads.get(tsId).interrupt();
                    notFiredTransitions.remove(tsId);
                    notFiredThreads.remove(tsId);
                    notFiredSemaphores.remove(tsId);
                } else {
                    for (Transition<T> t : ts) {
                        if (t.isEnabled(currentMarking)) {
                            notFiredTransitions.remove(tsId);
                            notFiredThreads.remove(tsId);
                            notFiredSemaphores.remove(tsId).release();
                            return successfulTransition;
                        }
                    }
                }
                ++tsId;
            }

            globalMutex.release();
        } catch (InterruptedException e) {
            globalMutex.release();
            Thread.currentThread().interrupt();
            return null;
        }

        return successfulTransition;
    }
}
