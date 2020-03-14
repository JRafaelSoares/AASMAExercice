package exercise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class Utility {

    public String task;

    public Double expectedValue;

    public HashMap<Integer, Double> observedValues = new HashMap<>();

    public double memory;

    public boolean debug = true;


    public Utility(Double expectedValue, Double memory, String task){
        this.expectedValue = expectedValue;
        this.memory = memory;
        this.task = task;
    }

    public void addObservation(Double utility, int cycle){

        observedValues.put(cycle, utility);

        this.expectedValue = getValuesExpectedValue(this.observedValues);
    }

    public Double simulateRestart(int remainingCycles, int restart){

        Double expectedValue;
        if(restart >= remainingCycles) return 0.0;

        if(!observedValues.isEmpty()){
            expectedValue = getValuesExpectedValue(observedValues);
        }
        //If no observed value yet, we use the base expected value
        else{
            expectedValue = this.expectedValue;
        }

        return expectedValue*(remainingCycles-restart);


    }

    public Double getValuesExpectedValue(HashMap<Integer, Double> observedValues){
        double totalTime = 0;

        double value = 0;

        double memoryTime = 0;

        for (int cycleImplemented: observedValues.keySet()) {
            memoryTime = Math.pow(cycleImplemented, this.memory);
            value += (observedValues.get(cycleImplemented) * memoryTime);
            totalTime += memoryTime;
        }

        return value / totalTime;
    }

    public Double getExpectedValue(){
        return expectedValue;
    }

    public boolean wasNotExecuted() { return observedValues.isEmpty(); }

    public String getTask() { return this.task; }

}
