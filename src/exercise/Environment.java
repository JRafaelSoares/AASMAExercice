package exercise;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        for (String agent: agents.keySet()) {
            agents.get(agent).decideAndAct();
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
