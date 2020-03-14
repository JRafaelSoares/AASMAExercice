package exercise;

import java.util.Comparator;
import java.util.Locale;

public class RestartComparator implements Comparator<Utility> {

    public int cycle;
    public int restart;

    public int currentRestart;

    public String currentTask;

    public RestartComparator(int cycle, int restart, int currentRestart, String currentTask){
        this.cycle=cycle;
        this.restart=restart;
        this.currentRestart=currentRestart;
        this.currentTask=currentTask;
    }

    public int compare(Utility o1, Utility o2){

        double o1Value;
        double o2Value;

        if(o1.getTask().equals(this.currentTask)){
            o1Value = o1.simulateRestart(this.cycle, this.currentRestart);
        }
        else {
            o1Value = o1.simulateRestart(this.cycle, this.restart);
        }

        if(o2.getTask().equals(this.currentTask)){
            o2Value = o2.simulateRestart(this.cycle, this.currentRestart);
        }
        else {
            o2Value = o2.simulateRestart(this.cycle, this.restart);
        }
        if(o1Value > o2Value){
            return -1;
        }
        else if(o1Value == o2Value){
            if (o1.getTask().compareTo(o2.getTask()) > 0){
                return 1;
            }
            else return -1;
        }
        else return 1;
    }
}
