package simulation.interfaces;

import simulation.AgentFactory;
import simulation.BrainFactory;

public abstract class FactorySetup {
    public abstract void inject();

    public void registerAgent(String name, AgentConstructor constructor) {
        AgentFactory.registerAgent(name, constructor);
    }

    public void registerBrain(String name, BrainConstructor constructor) {
        BrainFactory.registerBrain(name, constructor);
    }
}
