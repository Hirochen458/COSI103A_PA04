/**
 * Zhijian Chen
 * Chen5340@brandeis.edu
 * Apr 26th 2023
 * PA4
 * This class is used to implement the preemptive Tunnel function that let vehicle enter and exit and give ambulance the highest priority. 
 * Known bugs: None.
 */

package edu.brandeis.cs.cs131.pa4.submission;

import java.util.ArrayList;
import java.util.List;

import edu.brandeis.cs.cs131.pa4.tunnel.Ambulance;
import edu.brandeis.cs.cs131.pa4.tunnel.Car;
import edu.brandeis.cs.cs131.pa4.tunnel.Sled;
import edu.brandeis.cs.cs131.pa4.tunnel.Tunnel;

/**
 * The class for the Preemptive Tunnel, extending Tunnel.
 */
public class PreemptiveTunnel extends Tunnel {
	
	//Used to keep track the direction of vehicles in tunnel
			public String direction = "";
			
			//Used to keep track of the number of cars in tunnel
			public int numOfCar = 0;
			
			//Used to keep track of the number of sleds in tunnel;
			public int numOfSled = 0;
			
			//Used to show if there is a ambulance inside;
			public boolean emergency = false;
			
			//Used to keep track of all vehicles in tunnel
			public List<Vehicle> list=new ArrayList<Vehicle>();
	
	/**
	 * Creates a new instance of a basic tunnel with the given name
	 * @param name the name of the basic tunnel
	 */
	public PreemptiveTunnel(String name) {
		super(name);
	}
	
	/**
	 * This method is used to check if a vehicle is eligible to enter this tunnel and with preemptive function that give ambulance the highest priority. 
	 * @param the vehicle that need to enter the tunnel
	 * @return true if able to enter false otherwise.
	 */

	@Override
	protected boolean tryToEnterInner(Vehicle vehicle) {
		if(vehicle instanceof Ambulance) {
			if(emergency==false) {
				emergency = true;
				if(list.size()!=0) {
					for(int i = 0; i < list.size();i++) {
						list.get(i).pullOver=true;
						list.get(i).interrupt();
					}
				}
				list.add(vehicle);
				return true;
			}else {
				return false;
			}
		}else if(vehicle instanceof Car) {
			if(numOfCar ==0 && numOfSled ==0) {
				list.add(vehicle);
				direction = vehicle.getDirection().toString();
				numOfCar++;
				if(emergency == true) {
					vehicle.pullOver=true;
				}
				return true;
			}
			//if tunnel is not empty with vehicle inside
			else if(numOfCar < 3 && numOfSled == 0) {
				if(vehicle.getDirection().toString().equals(direction)) {
					list.add(vehicle);
					numOfCar++;
					if(emergency == true) {
						vehicle.pullOver=true;
					}
					return true;
				}else {
					return false;
				}
			}else {
				return false;
			}
		}else if(vehicle instanceof Sled){
			//tunnel must be empty for sled
			if(numOfCar ==0 && numOfSled ==0) {
				list.add(vehicle);
				direction = vehicle.getDirection().toString();
				numOfSled++;
				if(emergency == true) {
					vehicle.pullOver = true;
				}
				return true;
			}else {
				return false;
			}
		}
		return false;
	}
	
	
	/**
	 * This method is used to update the tunnel and let vehicle to exit the tunnel and update the tunnel with implemented of the preemptive function.
	 * @param the vehicle that need to exit the tunnel
	 */
	@Override
	protected void exitTunnelInner(Vehicle vehicle) {
		if(vehicle instanceof Ambulance) {
			
			emergency = false;
			list.remove(vehicle);
			if(list.size()!=0) {
				for(int i = 0; i < list.size();i++) {
					list.get(i).pullOver=false;
					list.get(i).vehicleContinue();
				}
			}
			
		}else if(vehicle instanceof Car) {
			list.remove(vehicle);
			numOfCar--;
			if(list.isEmpty()) {
				direction ="";
			}
		}else if(vehicle instanceof Sled) {
			list.remove(vehicle);
			numOfSled--;
			if(list.isEmpty()) {
				direction ="";
			}
		}
	}
	
}
