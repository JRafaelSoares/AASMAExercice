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

    public ArrayList<String> chosenTasks;

    public HashMap<String, Double> seenTaskValues = new HashMap<>();

    public ArrayList<Utility> maxTasks = new ArrayList<>();

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
    @SuppressWarnings("unchecked")

    public void decideAndAct(){

        if(concurrencyPenalty == 0){
            for (String agent: agents.keySet()) {
                agents.get(agent).decideAndAct();
            }
        }

        else {
            ArrayList<Utility> utilities = new ArrayList<>();
            this.chosenTasks = new ArrayList<>();

            ArrayList<ArrayList<Utility>> allUtilities = new ArrayList<>();
            for (String agent: agents.keySet()) {
                if(debugging) System.out.println("Agent: " + agent);
                ArrayList<Utility> maxUtils = agents.get(agent).decide();
                allUtilities.add(maxUtils);

                utilities.add(maxUtils.get(0));
                this.chosenTasks.add(maxUtils.get(0).getTask());
            }

            this.maxTasks = (ArrayList<Utility>) utilities.clone();

            for(String tasks: this.chosenTasks){
                if(Collections.frequency(this.chosenTasks, tasks) > 1){
                    recursiveSearch(0, utilities, allUtilities);
                    break;
                }

            }

            if(debugging) System.out.println("[CONCURRENT] Max utility chosen: " + printUtility(this.maxTasks));
            for(int i = 0; i < agentNames.length; i++){
                Agent agent = agents.get(agentNames[i]);
                agent.setProposedTask(this.maxTasks.get(i).getTask());
                agent.act();
            }

            for(int task = 0; task < this.chosenTasks.size(); task++){
                chosenTasks.set(task, this.maxTasks.get(task).getTask());
            }
        }

    }

    public void recursiveSearch(int agent, ArrayList<Utility> currentTasks, ArrayList<ArrayList<Utility>> utilities){

        for (Utility utility: utilities.get(agent)) {
            currentTasks.set(agent, utility);

            if(agent < currentTasks.size()-1){
                recursiveSearch(agent+1, currentTasks, utilities);
            }
            else {
                if(debugging) System.out.println(String.format("[CONCURRENT] Max utility: %s", printUtility(maxTasks)));
                if(debugging) System.out.println(String.format("[CONCURRENT] Trying utility: %s", printUtility(currentTasks)));
                this.maxTasks = isWorthChange(currentTasks);
                if(debugging) System.out.println(String.format("[CONCURRENT] Chosen utility: %s\n", printUtility(maxTasks)));

            }
        }
    }

    public String printUtility(ArrayList<Utility> utilities){
        String print = "";
        for (Utility utility: utilities) {
            print += String.format(Locale.US, "%s :  %.2f, ", utility.getTask(), utility.getExpectedValue());
        }
        return print;

    }

    public ArrayList<Utility> isWorthChange(ArrayList<Utility> currentTasks){
        double currentUtilityValue = calculateValue(this.maxTasks);
        double tryingUtilityValue = calculateValue(currentTasks);

        if(debugging) System.out.println(String.format(Locale.US, "[CONCURRENT] Current Utilities Value: %.2f", currentUtilityValue));
        if(debugging) System.out.println(String.format(Locale.US, "[CONCURRENT] Trying Utilities Value: %.2f", tryingUtilityValue));

        if(currentUtilityValue > tryingUtilityValue){
            return this.maxTasks;
        }

        else if(currentUtilityValue == tryingUtilityValue){
            if(lowestChoice(this.maxTasks, currentTasks)){
                return (ArrayList<Utility>) currentTasks.clone();
            }
            return this.maxTasks;
        }

        return (ArrayList<Utility>) currentTasks.clone();
    }

    public double calculateValue(ArrayList<Utility> utilities){
        HashMap<String, Integer> tasks = new HashMap<>();
        Double total = 0.0;

        for(Utility utility: utilities){

            if(!tasks.containsKey(utility.getTask())){
                total += utility.getExpectedValue();
                tasks.put(utility.getTask(), 1);
            }
            else{
                total += (utility.getExpectedValue()-this.concurrencyPenalty);
                tasks.replace(utility.getTask(), tasks.get(utility.getTask())+1);
            }
        }

        for(String task : tasks.keySet()){
            if(tasks.get(task) > 1){
                total -= this.concurrencyPenalty;
            }
        }
        return total;
    }

    public boolean lowestChoice(ArrayList<Utility> maxTasks, ArrayList<Utility> tryingTasks){

        if(debugging) System.out.println("[CONCURRENT][LOWEST CHOICE] Entered lowest choice");
        for(int agent = 0; agent < maxTasks.size()-1; agent++){
            if(debugging) System.out.println(String.format("[CONCURRENT][LOWEST CHOICE]  Agent %d MaxTask %s CurrentTask %s", agent, maxTasks.get(agent).getTask(), tryingTasks.get(agent).getTask()));

            if(tryingTasks.get(agent).getTask().compareTo(maxTasks.get(agent).getTask()) < 0){
                if(debugging) System.out.println("[CONCURRENT][LOWEST CHOICE] Trying is lower");
                return true;
            }

        }

        return false;
    }

    public boolean isWorthChange(String currentTask, String tryingTask, ArrayList<Utility> utilities, ArrayList<String> tasks, Utility changeUtility, int agentIdentifier){
        int numCurrentTask = Collections.frequency(tasks, currentTask);
        int numTryingTask = Collections.frequency(tasks, tryingTask);
        if(debugging) System.out.println(String.format("[CONCURRENT][WORTH_CHANGE] numCurrentTask: %d, numTryingTask: %d", numCurrentTask, numTryingTask));
        int agent = 0;

        double totalCurrent = 0.0;
        double totalTry = 0.0;

        for (Utility utility: utilities) {
            if(agent == agentIdentifier){

                totalCurrent += utility.getExpectedValue()-this.concurrencyPenalty;
                totalTry += changeUtility.getExpectedValue()-this.concurrencyPenalty;
            }
            else{
                if(utility.getTask().equals(currentTask)){
                    totalCurrent += utility.getExpectedValue()-this.concurrencyPenalty;
                    totalTry += utility.getExpectedValue()-this.concurrencyPenalty;

                }
                else if(utility.getTask().equals(tryingTask)){

                    totalCurrent += utility.getExpectedValue()-this.concurrencyPenalty;
                    totalTry += utility.getExpectedValue()-this.concurrencyPenalty;
                }
            }
            agent++;
        }

        if(numCurrentTask == 2 && numTryingTask == 0){
            totalTry += 2*this.concurrencyPenalty;
        }

        else if(numCurrentTask > 2 && numTryingTask == 0){
            totalTry += this.concurrencyPenalty;
        }

        else if(numCurrentTask == 2 && numTryingTask > 0){
            totalTry += this.concurrencyPenalty;
        }

        if(numTryingTask == 1){
            totalCurrent += this.concurrencyPenalty;
        }

        if(debugging) System.out.println(String.format(Locale.US, "[CONCURRENT][WORTH_CHANGE] Total Current Value: %.2f, Total Trying Value: %.2f", totalCurrent, totalTry));

        if(totalCurrent == totalTry){
           return currentTask.compareTo(tryingTask) < 0;
        }
        return totalCurrent<=totalTry;
    }

    public void perceive(String line){
        String[] commandSplit = line.split(" ");

        if(commandSplit[0].matches("A.*")){
            switch (this.decision){
                case "heterogeneous-society":
                    this.agents.get(commandSplit[0]).perceive("A ".concat(commandSplit[1]));
                    break;
                case "homogeneous-society":
                    if(concurrencyPenalty != 0){
                        //Get task of this agent
                        Integer agentNumber = Integer.parseInt(commandSplit[0].split("A")[1]);
                        String task = this.chosenTasks.get(agentNumber-1);
                        Double valueSeen = Double.parseDouble(commandSplit[1].split("=")[1]);

                        //If first time seeing the task, add value to the dictionary
                        if(!seenTaskValues.containsKey(task)){
                            seenTaskValues.put(task, valueSeen);
                        }
                        else{
                            //Else, add the value;
                            seenTaskValues.replace(task, seenTaskValues.get(task) + valueSeen);
                        }

                        if(agentNumber == agentNames.length){
                            for(String currentTask: seenTaskValues.keySet()){
                                Double currentTaskValue = this.seenTaskValues.get(currentTask);
                                int occurrences = Collections.frequency(this.chosenTasks, currentTask);
                                Double valueObserver = currentTaskValue / occurrences;
                                String chosenTaskInput = "A u=" + valueObserver;
                                String observingTaskInput = String.format(Locale.US, "observation_%s u=%.2f", currentTask, valueObserver);

                                int currentAgent = 0;
                                //WARNING - Admiting it is the correct order of agents
                                for(String agent: agents.keySet()){
                                    if(debugging) System.out.println(String.format("[HOMOGENEOUS] Agent %s", agent));

                                    if(chosenTasks.get(currentAgent).equals(currentTask)){
                                        agents.get(agent).perceive(chosenTaskInput);
                                    }
                                    else{
                                        agents.get(agent).perceive(observingTaskInput);
                                    }
                                    currentAgent++;
                                }
                            }


                            seenTaskValues = new HashMap<>();
                        }
                    }

                    else{
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
