package exercise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class Utility {

    public Double expectedValue;

    public HashMap<Integer, Double> observedValues = new HashMap<>();

    public double memory;

    public boolean debug = true;

    public Utility(Double expectedValue, Double memory){
        this.expectedValue = expectedValue;
        this.memory = memory;
    }

    public void addObservation(Double utility, int cycle){

        observedValues.put(cycle, utility);

        ArrayList<Double> values = getValuesExpectedValue(this.observedValues);

        this.expectedValue = values.get(0) / values.get(1);
    }

    public Double simulateRestart(int currentCycle, int remainingCycles, int restart){

        if(restart >= remainingCycles) return 0.0;

        //Get current observed values
        ArrayList<Double> values = getValuesExpectedValue(observedValues);
        Double currentSumValues = values.get(0);
        Double currentTotalTime = values.get(1);
        Double currentExpectedValue;

        if(currentTotalTime != 0){
            currentExpectedValue = currentSumValues/currentTotalTime;
        }
        //If no observed value yet, we use the base expected value
        else{
            currentExpectedValue = this.expectedValue;
        }

        int cycle = currentCycle + restart;

        Double totalValue = 0.0;
        //Calculates iterations of future results
        for(int i = remainingCycles-restart; i > 0; i--){
            totalValue += currentExpectedValue* Math.pow(cycle, this.memory);
            cycle++;
        }

        return totalValue;


    }

    public ArrayList<Double> getValuesExpectedValue(HashMap<Integer, Double> observedValues){
        if(observedValues.isEmpty()) ;

        double totalTime = 0;

        double value = 0;

        double memoryTime = 0;

        for (int cycleImplemented: observedValues.keySet()) {
            memoryTime = Math.pow(cycleImplemented, this.memory);
            value += (observedValues.get(cycleImplemented) * memoryTime);
            totalTime += memoryTime;
        }

        ArrayList<Double> values = new ArrayList<>();

        values.add(value);
        values.add(totalTime);

        return values;
    }

    public Double getExpectedValue(){
        return expectedValue;
    }

    public boolean wasNotExecuted() { return observedValues.isEmpty(); }
}
