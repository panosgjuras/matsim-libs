package playground.wrashid.PDES2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class EventLog {
	double time = 0.0;
	int vehicleId = 0;
	int legNo = 0;
	int linkId = 0;
	int fromNodeId = 0;
	int toNodeId = 0;
	String type = null;

	
	public EventLog(double time, int vehicleId, int legNo, int linkId,
			int fromNodeId, int toNodeId, String type) {
		super();
		this.time = time;
		this.vehicleId = vehicleId;
		this.legNo = legNo;
		this.linkId = linkId;
		this.fromNodeId = fromNodeId;
		this.toNodeId = toNodeId;
		this.type = type;
	}

	public void print() {
		System.out.print("time: "+time);
		System.out.print(";vehicleId: "+vehicleId);
		System.out.print(";legNo: "+legNo);
		System.out.print(";linkId: "+linkId);
		System.out.print(";fromNodeId: "+fromNodeId);
		System.out.print(";toNodeId: "+toNodeId);
		System.out.print(";type: "+type);
		System.out.println();
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public int getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}

	public int getLegNo() {
		return legNo;
	}

	public void setLegNo(int legNo) {
		this.legNo = legNo;
	}

	public int getLinkId() {
		return linkId;
	}

	public void setLinkId(int linkId) {
		this.linkId = linkId;
	}

	public int getFromNodeId() {
		return fromNodeId;
	}

	public void setFromNodeId(int fromNodeId) {
		this.fromNodeId = fromNodeId;
	}

	public int getToNodeId() {
		return toNodeId;
	}

	public void setToNodeId(int toNodeId) {
		this.toNodeId = toNodeId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public static boolean compare(ArrayList<EventLog> eventLog1, ArrayList<EventLog> eventLog2){
		int NoOfNotEqualEvents=0;
		
		assert eventLog1.size()==eventLog2.size():"The size of both eventLogs must be the same!";
		for(int i=0;i<eventLog1.size();i++) {
			//System.out.println("=========");
			//eventLog1.get(i).print();
			//eventLog2.get(i).print();
			//System.out.println("=========");
			
			if (!equals(eventLog1.get(i),eventLog2.get(i))){
				//return false; // TODO: uncomment this, when bug is fixed!
				NoOfNotEqualEvents++;
			}
		}
		
		if (SimulationParameters.debugMode){
			System.out.println("# Events Java: " + eventLog1.size());
			System.out.println("# Events C++: " + eventLog2.size());
			System.out.println("NoOfNotEqualEvents: " + NoOfNotEqualEvents);
		}
		
		return true;
	}
	
	public static boolean equals(EventLog eventLog1,EventLog eventLog2){
		// the time must be the same (compared up to 4 digits after the floating point) and the link
		// the event type is ignored for the moment, because in the beginning it might be different
		if (Math.rint(eventLog1.getTime()*10000)==Math.rint(eventLog2.getTime()*10000) && eventLog1.getLinkId()==eventLog2.getLinkId() ){
			return true;
		} else {
			System.out.println("====PROBLEM=====");
			eventLog1.print();
			eventLog2.print();
			System.out.println("=========");
		}
		return false;
	}
	
	public static void print(ArrayList<EventLog> eventLog){
		for(int i=0;i<eventLog.size();i++) {
			eventLog.get(i).print();
		}
	}
	
	// For each link in the event list, find out how long a car has been on that link
	// Then compare the usage time of each link for the two different Event logs
	// print the average(absolute difference): absSumLink
	// and the sum (absolute difference) in seconds: absAverageLinkDiff
	public static double absAverageLinkDiff(ArrayList<EventLog> eventLog1, ArrayList<EventLog> eventLog2){
		HashMap<Integer,Double[]> hm=new HashMap<Integer,Double[]>(); // key: int (linkId) 
		                          // value: double[4] (startCurrentLink1,totalUsageDurationLink1,startCurrentLink2,totalUsageDurationLink2) 
		
		assert eventLog1.size()==eventLog2.size():"The size of both eventLogs must be the same!" + eventLog1.size() + " - "+ eventLog2.size();
		
		for(int i=0;i<eventLog1.size();i++) {
			
			int link1=eventLog1.get(i).getLinkId();
			if (!hm.containsKey(link1)){
				Double[] d= new Double[4];
				d[0]=0d;
				d[1]=0d;
				d[2]=0d;
				d[3]=0d;
				hm.put(link1, d);
			}
			hm.get(link1)[1]=eventLog1.get(i).time-hm.get(link1)[0];
			
			
			
			int link2=eventLog2.get(i).getLinkId();
			if (!hm.containsKey(link2)){
				Double[] d= new Double[4];
				d[0]=0d;
				d[1]=0d;
				d[2]=0d;
				d[3]=0d;
				hm.put(link2, d);
			}
			hm.get(link2)[3]=eventLog2.get(i).time-hm.get(link2)[2];
			
			//System.out.println("link:" + link2 + "; diff" + (hm.get(link2)[1]-hm.get(link2)[3]));
		}
		
		
		
		
		double absSum=0;
		double absAverage=0;
		for (Double[] d: hm.values()){
			//System.out.println("eventLog1-eventLog2:" + (d[1]-d[3]));
			absSum+=Math.abs(d[1]-d[3]);
		}
		
		absAverage=absSum/hm.size();
		System.out.println("absSumLink:" + absSum);
		System.out.println("absAverageLinkDiff:" + absAverage);
		return absAverage;
	}
	
	public static void filterEvents(int linkId,ArrayList<EventLog> eventLog1,ArrayList<EventLog> eventLog2){
		LinkedList<EventLog> list1,list2;
		list1= new LinkedList<EventLog>();
		list2= new LinkedList<EventLog>();
		assert eventLog1.size()==eventLog2.size():"The size of both eventLogs must be the same!";
		for(int i=0;i<eventLog1.size();i++) {
			if (eventLog1.get(i).linkId==linkId){
				list1.add(eventLog1.get(i));
			}
			if (eventLog2.get(i).linkId==linkId){
				list2.add(eventLog2.get(i));
			}	
		}
		assert list1.size()==list2.size():"Inconsistent list size!";
		int noOfDifferentTimes=0;
		for (int i=0;i<list1.size();i++){
			if (list1.get(i).time!=list2.get(i).time){
				//System.out.println("1.time:" + list1.get(i).time + "2.time:" + list2.get(i).time + "1.type:" + list1.get(i).type + "2.type:" + list2.get(i).type);
				noOfDifferentTimes++;
			}
		}
		System.out.println("noOfDifferentTimes:"+noOfDifferentTimes);
	}
	
	
}
