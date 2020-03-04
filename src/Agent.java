import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class Agent {

	
	/**********************************/
	/******* A: AGENT BEHAVIOR ********/
	/**********************************/
	public int cycle;

	public int restart = 0;

	public double memoryFactor = 0.0;

	public String decision;

	public Map<String, Double> tasks;

	public Agent(String[] options) {
		for (String option: options) {
			initializationParse(option.split("="));
		}
	}
	
	public void perceive(String input) {
		String[] values = input.split(" ");

		//if(values[0].compareTo("A")){

		//}
	}
	
	public void decideAndAct() {}
	
	public String recharge() {
		return "output";
	}

	
	/******************************/
	/******* B: MAIN UTILS ********/
	/******************************/

    public static void main(String[] args) throws IOException { 
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line = br.readLine();
		Agent agent = new Agent(line.split(" "));
		while(!(line=br.readLine()).startsWith("end")) {
			if(line.startsWith("TIK")) agent.decideAndAct();
			else agent.perceive(line);
		}
		System.out.println(agent.recharge());
		br.close();
	}

	/******************************/
	/******* C: AUX FUNCTIONS******/
	/******************************/

	public void initializationParse(String[] initialization){
		switch (initialization[0]){
			case "cycle":
				this.cycle= Integer.parseInt(initialization[1]);
				break;
			case "decision":
				this.decision = initialization[1];
				break;
			case "restart":
				this.restart = Integer.parseInt(initialization[1]);
				break;
			case "memory-factor":
				this.memoryFactor = Double.parseDouble(initialization[1]);
				break;
		}
	}
}
