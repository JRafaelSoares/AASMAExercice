package exercise;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

public class Environment {

    public HashMap<String, Agent> agents = new HashMap<>();

    public String agentOptions = "";

    public String[] agentNames = new String[0];

    public String decision;

    public boolean debugging = true;

    /******Homogeneous Variables******/

    public double averageUtilities = 0.0;

    public int numAgents = 1;

    public int totalUtilitiesSeen = 0;

    public double concurrencyPenalty = 0;

    public Environment(String[] options){
        initializationParse(options);

        //removes extra space in the end
        agentOptions = agentOptions.substring(0, agentOptions.length()-1);
        for (String agent: agentNames) {
            agents.put(agent, new Agent(agentOptions.split(" "), debugging));
        }
        if(agents.isEmpty()){
            agents.put("A", new Agent(agentOptions.split(" "), debugging));
        }

        this.numAgents = agentNames.length;
    }

    /**********************************/
    /******* A: MAIN FUNCTIONS ********/
    /**********************************/

    public void decideAndAct(){

        if(concurrencyPenalty == 0){
            for (String agent: agents.keySet()) {
                agents.get(agent).decideAndAct();
            }
        }

        else {
            ArrayList<Utility> utilities = new ArrayList<>();
            ArrayList<String> tasks = new ArrayList<>();


            ArrayList<ArrayList<Utility>> allUtilities = new ArrayList<>();
            for (String agent: agents.keySet()) {
                ArrayList<Utility> maxUtils = agents.get(agent).decide();
                allUtilities.add(maxUtils);

                utilities.add(maxUtils.get(0));
                tasks.add(maxUtils.get(0).getTask());
            }

            ArrayList<String> tasksCopy = (ArrayList<String>) tasks.clone();

            while (true){
                //starting from the last agent
                for(int i = agentNames.length-1; i>= 0; i--){
                    //if a task happens more than once
                    if(debugging) System.out.println(String.format("[CONCURRENCY] Im agent: A%d", i));
                    if(debugging) System.out.println(String.format("[CONCURRENCY] Number of repeats: %d", Collections.frequency(tasks, tasks.get(i))));

                    int numAgentsSameTask = Collections.frequency(tasks, tasks.get(i));
                    if(numAgentsSameTask > 1){
                        if(debugging) System.out.println(String.format("[CONCURRENCY] Repeated Task: %s", tasks.get(i)));
                        //looking for other agent with same task
                        for(int j = 0; j < agentNames.length; j++){
                            //found an agent with same task which is not myself
                            if(tasks.get(j).equals(tasks.get(i)) && j!=i){
                                if(debugging) System.out.println(String.format("[CONCURRENCY] Agent with same task: A%d", j));
                                if(debugging) System.out.println(String.format(Locale.US, "[CONCURRENCY] A%d Value: %.2f, A%d Value: %.2f", i, utilities.get(i).getExpectedValue(), j, utilities.get(j).getExpectedValue()));

                                //if my expected value is less or equal to the other, need to see if I have another better value in store
                                if(utilities.get(i).getExpectedValue() <= utilities.get(j).getExpectedValue()){
                                    //Local maximum variables
                                    String maxTask = tasks.get(i);
                                    Double maxUtility = utilities.get(i).getExpectedValue();

                                    //Used to obtain utility later on
                                    int currentUtilityPosition = -1;
                                    int maxUtilityPosition = -1;
                                    for(Utility utility : allUtilities.get(i)){
                                        currentUtilityPosition++;
                                        //if it is a task that no one has taken
                                        //And if the value is bigger than my utility - discount for each other agent
                                        if(debugging) {
                                            if(!tasks.contains(utility.getTask())){
                                                System.out.println(String.format(Locale.US, "[CONCURRENCY] Max Task Utility: %.2f, Current Task Utility: %.2f", utilities.get(i).getExpectedValue()-(this.concurrencyPenalty*numAgentsSameTask), utility.getExpectedValue()));
                                            }
                                        }

                                        if(!tasks.contains(utility.getTask()) && isWorthChange(utilities.get(i).getExpectedValue(), utility.getExpectedValue(), numAgentsSameTask)){
                                            //if it isnt the first value higher
                                            if(!maxTask.equals(utilities.get(i).getTask())){
                                                if(utility.getExpectedValue() > maxUtility){
                                                    if(debugging) System.out.println(String.format("[CONCURRENCY] Found new higher task: %s", utility.getTask() ));
                                                    if(debugging) System.out.println(String.format(Locale.US, "[CONCURRENCY] New Task: %s, New Utility: %.2f", utility.getTask(), utility.getExpectedValue()));

                                                    maxTask = utility.getTask();
                                                    maxUtility = utility.getExpectedValue();
                                                    maxUtilityPosition = currentUtilityPosition;
                                                }
                                            }
                                            //if it is the first time finding a value, set local maximums
                                            else {
                                                if(debugging) System.out.println(String.format(Locale.US, "[CONCURRENCY] New Task: %s, New Utility: %.2f", utility.getTask(), utility.getExpectedValue()));

                                                maxTask = utility.getTask();
                                                maxUtility = utility.getExpectedValue();
                                                maxUtilityPosition = currentUtilityPosition;

                                            }

                                        }
                                    }
                                    //Set new maximums (which might not have changed)
                                    if(maxUtilityPosition != -1){
                                        if(debugging) System.out.println(String.format(Locale.US, "[CONCURRENCY] Final Task Chosen: %s", maxTask));

                                        tasks.set(i, maxTask);
                                        utilities.set(i, allUtilities.get(i).get(maxUtilityPosition));
                                    }
                                }
                            }
                        }
                    }
                }
                //If no changes were made, exit
                if(tasksCopy.equals(tasks)){
                    break;
                }

                tasksCopy = (ArrayList<String>)tasks.clone();
            }

            for(int i = 0; i < agentNames.length; i++){
                Agent agent = agents.get(agentNames[i]);
                agent.setProposedTask(tasks.get(i));
                agent.act();
            }
        }

    }

    public boolean isWorthChange(Double currentValue, Double tryingValue, int numberOfAgents){
        if(numberOfAgents > 2){
            return (numberOfAgents * (currentValue-this.concurrencyPenalty)) <= ( (numberOfAgents-1) * (currentValue-this.concurrencyPenalty) + tryingValue);
        }
        else{
            return (numberOfAgents * (currentValue-this.concurrencyPenalty) <= (currentValue + tryingValue));
        }
    }

    public void perceive(String line){
        String[] commandSplit = line.split(" ");

        if(commandSplit[0].matches("A.*")){
            switch (this.decision){
                case "heterogeneous-society":
                    this.agents.get(commandSplit[0]).perceive("A ".concat(commandSplit[1]));
                    break;
                case "homogeneous-society":
                    this.totalUtilitiesSeen++;
                    this.averageUtilities += Double.parseDouble(commandSplit[1].split("=")[1]);

                    if(this.totalUtilitiesSeen == this.numAgents){
                        this.averageUtilities = this.averageUtilities / this.numAgents;
                        for (String agent: agents.keySet()){
                            String input = "A u=" + this.averageUtilities;
                            if(debugging) System.out.println(String.format(Locale.US,"[ENVIRONMENT] Input: %s", input));
                            agents.get(agent).perceive("A u=" + this.averageUtilities);
                        }
                        this.averageUtilities = 0;
                        this.totalUtilitiesSeen = 0;
                    }
                    break;

                default:
                    this.agents.get("A").perceive(line);
                    break;
            }
        }
        else {
            for (String agent: agents.keySet()){
                agents.get(agent).perceive(line);
            }
        }
    }

    public String recharge(){
        Double totalValue = 0.0;
        if(agentNames.length == 0){
            return agents.get("A").recharge();
        }
        else{
            String output = "";

            for (String agentName: this.agentNames) {
                Agent agent = agents.get(agentName);
                output = output.concat(String.format("%s={%s},", agentName, agent.getTaskValues()));
                totalValue += agent.getTotal();
            }
            output = output.substring(0, output.length()-1);

            return String.format(Locale.US, "state={%s} gain=%.2f", output, totalValue);
        }

    }
    /**********************************/
    /******** B: AUX_FUNCTION *********/
    /**********************************/

    public void initializationParse(String[] options){

        for (String option: options) {
            String[] initialization = option.split("=");
            switch (initialization[0]){
                case "agents":
                    initialization[1] = initialization[1].substring(1, initialization[1].length()-1);
                    this.agentNames = initialization[1].split(",");
                    break;
                case "decision":
                    this.decision = initialization[1];
                    if(this.agentNames.length == 0){
                        agentOptions = agentOptions.concat(option.concat(" "));
                    }
                    else{
                        agentOptions = agentOptions.concat("decision=rationale ");
                    }
                    break;
                case "concurrency-penalty":
                    this.concurrencyPenalty = Double.parseDouble(initialization[1]);
                    break;
                default:
                    agentOptions = agentOptions.concat(option.concat(" "));
                    break;
            }
        }
    }

    /******************************/
    /******* C: MAIN UTILS ********/
    /******************************/

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = br.readLine();
        Environment environment = new Environment(line.split(" "));
        while(!(line=br.readLine()).startsWith("end")) {
            if(environment.debugging) System.out.println(line);
            if(line.startsWith("TIK")) environment.decideAndAct();
            else environment.perceive(line);
        }
        System.out.println(environment.recharge());
        br.close();
    }
}
