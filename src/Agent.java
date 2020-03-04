import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Agent {

	
	/**********************************/
	/******* A: AGENT BEHAVIOR ********/
	/**********************************/

	//Agent caractheristics
	public int cycle;

	public int restart = 0;

	public double memoryFactor = 0.0;

	public String decision;

	//Task implementation
	public HashMap<String, Double> expectedValue = new HashMap<String, Double>();

	public HashMap<String, Double> realValue = new HashMap<String, Double>();

	public String taskChosen;

	public double total = 0;


	public Agent(String[] options) {
		for (String option: options) {
			initializationParse(option.split("="));
		}
	}
	
	public void perceive(String input) {
		String[] values = input.split(" ");

		if(values[0].equals("A")){
			//TODO - Case with multiple utilities for multiple tasks?
			double utilityValue = Double.parseDouble(values[1].split("=")[1]);

			//Updates utility value of task
			this.expectedValue.replace(taskChosen, utilityValue);
			this.realValue.replace(taskChosen, utilityValue);


			total+= utilityValue;
		}

		else{
			//Gets value from tasks of type TX u=Y
			this.expectedValue.put(values[0], Double.parseDouble(values[1].split("=")[1]));
			this.realValue.put(values[0], Double.NEGATIVE_INFINITY);

		}
	}
	
	public void decideAndAct() { maxUtil(); }
	
	public String recharge() { return getOutput(); }

	
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
	/**** C: UTILITY FUNCTIONS*****/
	/******************************/
	
	public void maxUtil(){
		double maxValue = Double.NEGATIVE_INFINITY;
		double currentvalue;

		for (String key: this.expectedValue.keySet()) {
			currentvalue = this.expectedValue.get(key);
			if (currentvalue > maxValue){
				maxValue = currentvalue;
				this.taskChosen = key;
			}

			//TODO - Check if compareTo is correct
			else if (currentvalue == maxValue && this.taskChosen.compareTo(key) < 0){
				this.taskChosen = key;
			}
		}
	}
	
	/******************************/
	/******* D: AUX FUNCTIONS******/
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

	public String getOutput(){
		String output = "state={";

		//TODO - Does a Set always keeps the same order?
		for (String key: this.realValue.keySet()) {
			double currentValue = this.realValue.get(key);
			if(currentValue != Double.NEGATIVE_INFINITY){
				output = output.concat(String.format("%s=%.2f,",key, currentValue));
			}

			else {
				output = output.concat(String.format("%s=NA,", key));
			}
		}
		//Removes last ","
		output = output.substring(0, output.length()-1);

		return output.concat(String.format("} gain=%.2f", this.total));

	}
}
