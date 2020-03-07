package exercise;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class Environment {

    public HashMap<String, Agent> agents = new HashMap<>();

    public String agentOptions = "";

    public String[] agentNames = new String[0];

    public String decision;

    public boolean debugging = false;

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
        for (String agent: agents.keySet()){
            agents.get(agent).perceive(line);
        }
    }

    public String recharge(){
        if(agentNames.length == 0){
            return agents.get("A").recharge();
        }

        return "AAAAAAAAAAAAA";
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
            //DEBUGGING
            if(environment.debugging) System.out.println(line);
            if(line.startsWith("TIK")) environment.decideAndAct();
            else environment.perceive(line);
        }
        System.out.println(environment.recharge());
        br.close();
    }
}
