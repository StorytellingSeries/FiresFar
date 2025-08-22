package com.qurenie.relics_thirteenflames.util;

import net.neoforged.bus.api.Event;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Predicate;

import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

@Deprecated
public class Scheduler {
    
    private final Queue<Action> executor = new ConcurrentLinkedDeque<>();
    
    public <T extends Event> Scheduler(Class<T> eventType) {
        EVENT_BUS.addListener(eventType, (event) -> executorRun());
    }
    
    public <T extends Event> Scheduler(Class<T> eventType, Predicate<T> condition) {
        EVENT_BUS.addListener(eventType, (event) -> {
            if (condition.test(event))
                executorRun();
        });
    }
    
    public void schedule(int timer, Runnable action) {
        executor.add(new DelayedAction(timer, (InstantAction) action::run));
    }
    
    private void executorRun() {
        executor.removeIf(Action::execute);
    }
    
    private interface InstantAction extends Action, Runnable {
        
        void run();
        
        @Override
        default boolean execute() {
            run();
            return true;
        }
        
    }
    
    private interface Action {
        
        boolean execute();
        
    }
    
    private static class DelayedAction implements Action {
        
        int timer;
        Action action;
        
        public DelayedAction(int timer, Action action) {
            this.timer = timer;
            this.action = action;
        }
        
        @Override
        public boolean execute() {
            if (timer <= 0)
                return action.execute();
            
            timer--;
            return false;
        }
        
    }
    
}
