package simulation;

import java.util.HashMap;

import simulation.interfaces.BrainConstructor;

public class BrainFactory {
    int amount = 0;
    static HashMap<String, BrainConstructor> brains = new HashMap<>();

    public static void registerBrain(String name, BrainConstructor constructor) {
        brains.put(name, constructor);
    }

    public Brain createBrain(String brain) {
        Brain b = null;
        if (brains.containsKey(brain)) {
            b = brains.get(brain).createBrain();
        } else {
            System.out.println("WARNING: unknown brain type " + brain + "!");
        }
        return b;
    }
}
