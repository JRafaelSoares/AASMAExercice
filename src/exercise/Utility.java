package exercise;

public class Utility {

    public Float expectedValue;

    public Float totalValue = Float.NEGATIVE_INFINITY;

    public int timesExecuted = 0;

    public Utility(Float expectedValue){
        this.expectedValue = expectedValue;
    }

    public void addObservation(Float utility){
        if(this.totalValue == Float.NEGATIVE_INFINITY){
            this.totalValue = utility;
        }
        else{
            this.totalValue += utility;
        }

        this.timesExecuted++;

        this.expectedValue = this.totalValue/this.timesExecuted;

    }

    public Float getExpectedValue(){
        return expectedValue;
    }

    public int getTimesExecuted() { return timesExecuted; }
}
