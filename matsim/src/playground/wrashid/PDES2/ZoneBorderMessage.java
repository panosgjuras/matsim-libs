package playground.wrashid.PDES2;

import org.matsim.network.Link;

public class ZoneBorderMessage extends SelfhandleMessage {

	String debugString="";
	
	@Override
	public void printMessageLogString() {
		// TODO Auto-generated method stub
		
	}

	public ZoneBorderMessage(){
		super();
	}

	@Override
	public void selfhandleMessage() {
		// send a null message to all roads ahead, only if the current road is
		// empty (has no cars on it)
		
		Road currentRoad = (Road) receivingUnit;
		
		if (currentRoad.isRoadEmpty()){
			return;
		}
		
		
		for (Link outLink:currentRoad.getLink().getToNode().getOutLinks().values()){
			assert(messageArrivalTime>=0);
			Road outRoad=Road.allRoads.get(outLink.getId().toString());
			
			ZoneBorderMessage nm=MessageFactory.getNullMessage();
			nm.sendingUnit=currentRoad;
			nm.receivingUnit=outRoad;
			nm.messageArrivalTime=messageArrivalTime+currentRoad.linkTravelTime-SimulationParameters.delta;
			nm.debugString="null message handler";
			
			if (nm.messageArrivalTime<0){
				System.out.println();
			}
			
			
			assert(nm.messageArrivalTime>=messageArrivalTime);
			outRoad.roadEntryHandler.registerNullMessage(nm);
		}
		
	}
	
	public static void initialNullMessage(Road receivingRoad, double simTime){
		ZoneBorderMessage nm=MessageFactory.getNullMessage();
		nm.sendingUnit=null;
		nm.receivingUnit=receivingRoad;
		nm.messageArrivalTime=simTime;
		assert(nm.messageArrivalTime>=0):"negative simTime...";
		
		//Do not schedule the message, but rather immediately invoke the selfhandler.
		// Reason: A race condition can occur: The initialNullMessage from A to B can be handled later
		// than a message from X to A to B. The main reason is the scheduling here.
		//receivingRoad.scheduler.schedule(nm);
		nm.selfhandleMessage();
	}

	@Override
	public void recycleMessage() {
		MessageFactory.disposeNullMessage(this);
	}
}
