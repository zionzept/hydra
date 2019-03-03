package scripts;


public class ScriptO extends Script {

	public ScriptO(Runnable script) {
		super(script);
	}
	
	@Override
	public boolean evaluate() {
		for (Condition c : conditions) {
			if (!c.evaluate()) {
				return false;
			}
		}
		script.run();
		return true;
	}
}
