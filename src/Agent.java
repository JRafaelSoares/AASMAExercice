import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Dictionary;

public class Agent {

	
	/**********************************/
	/******* A: AGENT BEHAVIOR ********/
	/**********************************/
	public int cycle;

	public Dictionary<String, Integer> Tasks;

	public Agent(String[] options) {}
	
	public void perceive(String input) {}
	
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
}
