package playground.mzilske.pipeline;

import org.matsim.core.config.Config;
import org.matsim.core.router.costcalculators.TravelCostCalculatorFactory;
import org.matsim.core.router.costcalculators.TravelCostCalculatorFactoryImpl;

public class TravelCostCalculatorTaskManagerFactory extends TaskManagerFactory {

	@Override
	public TaskManager createTaskManagerImpl(TaskConfiguration taskConfiguration) {
		Config config = taskConfiguration.getConfig();
		TravelCostCalculatorFactory factory = new TravelCostCalculatorFactoryImpl();
		TravelCostCalculatorTask task = new TravelCostCalculatorTask(factory, config.planCalcScore());
		TravelCostCalculatorTaskManager travelCostCalculatorTaskManager = new TravelCostCalculatorTaskManager(task);
		return travelCostCalculatorTaskManager;
	}

}
