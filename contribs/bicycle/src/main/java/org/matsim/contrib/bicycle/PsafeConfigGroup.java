package org.matsim.contrib.bicycle;

import org.matsim.core.config.ReflectiveConfigGroup;
import java.util.Map;

public final class PsafeConfigGroup extends ReflectiveConfigGroup{

	public static final String GROUP_NAME = "Psafe";
	
	private static final String INPUT_PERCEIVED_SAFETY_CAR = "marginalUtilityOfPerceivedSafety_car";
	private static final String INPUT_PERCEIVED_SAFETY_EBIKE = "marginalUtilityOfPerceivedSafety_ebike";
	private static final String INPUT_PERCEIVED_SAFETY_ESCOOT = "marginalUtilityOfPerceivedSafety_escoot";
	private static final String INPUT_PERCEIVED_SAFETY_WALK = "marginalUtilityOfPerceivedSafety_walk";
	
	private double marginalUtilityOfPerceivedSafety_car;
    private double marginalUtilityOfPerceivedSafety_ebike;
    private double marginalUtilityOfPerceivedSafety_escoot;
    private double marginalUtilityOfPerceivedSafety_walk;
    
	public PsafeConfigGroup(){
		super(GROUP_NAME);
		}
	
	@StringSetter(INPUT_PERCEIVED_SAFETY_CAR)
	public void setMarginalUtilityOfPerceivedSafety_car(final double value) {
		this.marginalUtilityOfPerceivedSafety_car = value;
	}
	@StringGetter( INPUT_PERCEIVED_SAFETY_CAR)
	public double getMarginalUtilityOfPerceivedSafety_car() {
		return this.marginalUtilityOfPerceivedSafety_car;
	}
	
	@StringSetter(INPUT_PERCEIVED_SAFETY_EBIKE)
	public void setMarginalUtilityOfPerceivedSafety_ebike(final double value) {
		this.marginalUtilityOfPerceivedSafety_ebike = value;
	}
	@StringGetter( INPUT_PERCEIVED_SAFETY_EBIKE)
	public double getMarginalUtilityOfPerceivedSafety_ebike() {
		return this.marginalUtilityOfPerceivedSafety_ebike;
	}
	
	@StringSetter(INPUT_PERCEIVED_SAFETY_ESCOOT)
	public void setMarginalUtilityOfPerceivedSafety_escoot(final double value) {
		this.marginalUtilityOfPerceivedSafety_escoot= value;
	}
	@StringGetter( INPUT_PERCEIVED_SAFETY_ESCOOT)
	public double getMarginalUtilityOfPerceivedSafety_escoot() {
		return this.marginalUtilityOfPerceivedSafety_escoot;
	}
	
	@StringSetter(INPUT_PERCEIVED_SAFETY_WALK)
	public void setMarginalUtilityOfPerceivedSafety_walk(final double value) {
		this.marginalUtilityOfPerceivedSafety_walk = value;
	}
	@StringGetter( INPUT_PERCEIVED_SAFETY_WALK)
	public double getMarginalUtilityOfPerceivedSafety_walk() {
		return this.marginalUtilityOfPerceivedSafety_walk;
	}


	
}
