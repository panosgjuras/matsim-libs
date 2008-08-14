package playground.wrashid.PDES2;

import java.util.ArrayList;

import org.matsim.events.BasicEvent;
import org.matsim.events.AgentArrivalEvent;
import org.matsim.events.AgentDepartureEvent;
import org.matsim.network.Link;
import org.matsim.population.Act;
import org.matsim.population.Leg;
import org.matsim.population.Plan;

public class StartingLegMessage extends EventMessage {

	public StartingLegMessage(Scheduler scheduler,Vehicle vehicle) {
		super(scheduler,vehicle);
		eventType=SimulationParameters.START_LEG;
	}

	@Override
	public void selfhandleMessage() {
		
		// attempt to enter street.
		
		System.out.println("starting leg message");
		
		if (vehicle.getCurrentLeg().getMode().equalsIgnoreCase("car")){
			Road road=Road.allRoads.get(vehicle.getCurrentLink().getId().toString());
			//road.enterRequest(vehicle);
			
			assert(road.roadEntryHandler.peekStaringLegMessages()==this);
			road.roadEntryHandler.removeStaringLegMessages(this);
			road.roadEntryHandler.registerEnterRequestMessage(road, vehicle, messageArrivalTime);
		} else {
			Plan plan = vehicle.getOwnerPerson().getSelectedPlan(); // that's the plan the
			ArrayList<Object> actsLegs = plan.getActsLegs();
			Link nextLink = ((Act) actsLegs.get(vehicle.getLegIndex() + 1)).getLink();
			Road road=Road.allRoads.get(nextLink.getId().toString());
			vehicle.scheduleEndLegMessage(MessageExecutor.getSimTime()+vehicle.getCurrentLeg().getTravTime(), road);
		}
	}
	
	public void logEvent() {
		BasicEvent event=null;
		
		if (eventType.equalsIgnoreCase(SimulationParameters.START_LEG)){
			event=new AgentDepartureEvent(this.getMessageArrivalTime(),vehicle.getOwnerPerson().getId().toString(),vehicle.getCurrentLink().getId().toString(),vehicle.getLegIndex()-1);
		}
		
		//SimulationParameters.events.processEvent(event);
		//SimulationParameters.processEvent(event);
		SimulationParameters.bufferEvent(event);
	}

	@Override
	public void recycleMessage() {
		MessageFactory.disposeStartingLegMessage(this);		
	}
	
}
