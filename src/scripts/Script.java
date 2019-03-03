package scripts;

import java.util.LinkedList;

public class Script {
	
	protected Runnable script;
	protected LinkedList<Condition> conditions;
	
	public Script(Runnable script) {
		this.script = script;
		conditions = new LinkedList<Condition>();
	}
	
	public void addCondition(Condition condition) {
		conditions.add(condition);
	}
	
	public boolean evaluate() {
		for (Condition c : conditions) {
			if (!c.evaluate()) {
				return false;
			}
		}
		script.run();
		return false;
	}
}
