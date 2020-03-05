package exercise;

import java.util.HashMap;
import java.util.Map;

public class Utility {

    public Double expectedValue;

    public Double totalValue = Double.NEGATIVE_INFINITY;

    public int timesExecuted = 0;

    public HashMap<Integer, Double> observedValues = new HashMap<>();

    public double memory;

    public Utility(Double expectedValue, Double memory){
        this.expectedValue = expectedValue;
        this.memory = memory;
    }

    public void addObservation(Double utility, int cycle){

        observedValues.put(cycle, utility);

        double totalTime = 0;

        double value = 0;

        double memoryTime = 0;
        for (int cycleImplemented: observedValues.keySet()) {
            memoryTime = Math.pow(cycleImplemented, this.memory);
            value += (observedValues.get(cycleImplemented) * memoryTime);
            totalTime += memoryTime;
        }

        this.expectedValue = value / totalTime;

        this.timesExecuted++;
    }

    public Double getExpectedValue(){
        return expectedValue;
    }

    public int getTimesExecuted() { return timesExecuted; }
}
