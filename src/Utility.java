import java.util.HashMap;

public class Utility {

    public String task;

    public Double expectedValue;

    public Double sumMemory = 0.0;

    public double memory;

    public int restart;

    /**************/
    /** Flexible **/
    /**************/

    public double lowestValue;

    public Utility(Double expectedValue, Double memory, String task, int restart){
        this.expectedValue = expectedValue;
        this.memory = memory;
        this.task = task;
        this.restart = restart;
        this.lowestValue = expectedValue;
    }

    public void addObservation(Double utility, int cycle){

        if(sumMemory == 0){
            expectedValue = utility;
            lowestValue = utility;
        }

        if(utility < lowestValue){
            lowestValue = utility;
        }

        sumMemory += Math.pow(cycle, memory);
        expectedValue += Math.pow(cycle, memory) * (utility-expectedValue) / sumMemory;
    }

    public Double simulateRestart(int remainingCycles){
        if(this.restart >= remainingCycles) return 0.0;
        return expectedValue*(remainingCycles-this.restart);
    }

    public Double simulateRestart(int remainingCycles, double councurrencyPenalty){
        if(this.restart >= remainingCycles) return 0.0;
        return (expectedValue-councurrencyPenalty)*(remainingCycles-this.restart);
    }

    public Double getExpectedValue(){
        return expectedValue;
    }

    public boolean wasNotExecuted() { return sumMemory == 0.0; }

    public String getTask() { return this.task; }

    public void setRestart(int restart) { this.restart = restart; }

    public void decrementRestart(){

        if(this.restart > 0){
            this.restart--;
        }
    }

    public boolean hasNegative(){
        return this.lowestValue < 0;
    }

    public double getLowestValue(){
        return this.lowestValue;
    }

}
