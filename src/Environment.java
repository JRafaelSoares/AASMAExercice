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

    public boolean debugging = false;

    /******Homogeneous Variables******/

    public int cycle = 0;

    public double averageUtilities = 0.0;

    public int numAgents = 1;

    public int totalUtilitiesSeen = 0;

    public double concurrencyPenalty = 0;

    public ArrayList<String> chosenTasks;

    public HashMap<String, Double> seenTaskValues = new HashMap<>();

    public ArrayList<Utility> maxTasks = new ArrayList<>();

    /******* Concurrency Penalty ****/

    public ArrayList<Utility> currentTasks = new ArrayList<>();

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
            this.currentTasks = new ArrayList<>(agentNames.length);
            this.chosenTasks = new ArrayList<>(agentNames.length);

            for (String agent: agentNames) {
                if(debugging) System.out.println("Agent: " + agent);

                this.currentTasks.add(agents.get(agent).decide());
                this.chosenTasks.add(agents.get(agent).decide().getTask());
            }

            this.maxTasks = (ArrayList<Utility>) currentTasks.clone();

            for(String tasks: this.chosenTasks){
                if(Collections.frequency(this.chosenTasks, tasks) > 1){

                    recursiveSearch(0, agentNames);

                    break;
                }
            }

            if(debugging) System.out.println("[CONCURRENT] Max utility chosen: " + printUtility(this.maxTasks));
            for(int i = 0; i < agentNames.length; i++){
                agents.get(agentNames[i]).setProposedTask(this.maxTasks.get(i));
                agents.get(agentNames[i]).act();
            }

            for(int task = 0; task < this.chosenTasks.size(); task++){
                chosenTasks.set(task, this.maxTasks.get(task).getTask());
            }
        }

        this.cycle--;
        ;
    }

    public void recursiveSearch(int agent, String[] agentNames){
        for (Utility utility: agents.get(agentNames[agent]).getUtilityValues()) {

            currentTasks.set(agent, utility);


            if(agent < currentTasks.size()-1){
                recursiveSearch(agent+1, agentNames);
            }
            else {
                if(debugging) System.out.println(String.format("[CONCURRENT] Max utility: %s", printUtility(maxTasks)));
                if(debugging) System.out.println(String.format("[CONCURRENT] Trying utility: %s", printUtility(currentTasks)));
                isWorthChange(currentTasks);
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

    @SuppressWarnings("unchecked")
    public void isWorthChange(ArrayList<Utility> currentTasks){



        Double maxValue = calculateValue(this.maxTasks);
        Double currentValue = calculateValue(currentTasks);



        if(debugging) System.out.println(String.format(Locale.US, "[CONCURRENT] Current Utilities Value: %.2f", maxValue));
        if(debugging) System.out.println(String.format(Locale.US, "[CONCURRENT] Trying Utilities Value: %.2f", currentValue));


        if(maxValue > currentValue){
            return;
        }

        else if(maxValue.equals(currentValue)){
            if(lowestChoice(this.maxTasks, currentTasks)){
                replaceMaxTask();
            }
            return;
        }
        replaceMaxTask();

    }

    public void replaceMaxTask(){
        for(int i = 0; i < this.currentTasks.size(); i++){
            this.maxTasks.set(i, this.currentTasks.get(i));
        }
    }

    public double calculateValue(ArrayList<Utility> utilities){



        Double total = 0.0;

        ArrayList<String> tasks = new ArrayList<>(utilities.size());

        for(Utility utility: utilities) {
            tasks.add(utility.getTask());
        }


        for(Utility utility: utilities){

            if(Collections.frequency(tasks, utility.getTask()) == 1){
                total += utility.simulateRestart(this.cycle);
            }
            else{
                total += (utility.simulateRestart(this.cycle, this.concurrencyPenalty));
            }
        }



        return total;
    }

    public boolean lowestChoice(ArrayList<Utility> maxTasks, ArrayList<Utility> tryingTasks){


        if(debugging) System.out.println("[CONCURRENT][LOWEST CHOICE] Entered lowest choice");
        for(int agent = 0; agent < maxTasks.size()-1; agent++){
            if(debugging) System.out.println(String.format("[CONCURRENT][LOWEST CHOICE]  Agent %d MaxTask %s CurrentTask %s", agent, maxTasks.get(agent).getTask(), tryingTasks.get(agent).getTask()));

            if(tryingTasks.get(agent).getTask().compareTo(maxTasks.get(agent).getTask()) > 0) {
                if(debugging) System.out.println("[CONCURRENT][LOWEST CHOICE] Trying is higher index");
                return false;
            }

            else if(tryingTasks.get(agent).getTask().compareTo(maxTasks.get(agent).getTask()) < 0){
                if(debugging) System.out.println("[CONCURRENT][LOWEST CHOICE] Trying is lower index");
                return true;
            }

        }

        return false;
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
                case "cycle":
                    this.cycle = Integer.parseInt(initialization[1]);
                    agentOptions = agentOptions.concat(option.concat(" "));
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
            if(line.startsWith("TIK")) environment.decideAndAct();
            else environment.perceive(line);
        }
        System.out.println(environment.recharge());

        //Get memory usage
        /*
        Runtime runtime = Runtime.getRuntime();
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Used Memory in bytes: " + memory);
        System.out.println("Used Memory in kilobytes: " + (memory / (1024L)));

        System.out.println("Used Memory in megabytes: " + (memory / (1024L * 1024L)));
        */
        br.close();
    }
}
