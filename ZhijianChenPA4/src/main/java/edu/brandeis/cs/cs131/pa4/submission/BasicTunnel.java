package edu.brandeis.cs.cs131.pa4.submission;

import java.util.ArrayList;
import java.util.List;

import edu.brandeis.cs.cs131.pa4.tunnel.Car;
import edu.brandeis.cs.cs131.pa4.tunnel.Sled;
import edu.brandeis.cs.cs131.pa4.submission.Vehicle;
import edu.brandeis.cs.cs131.pa4.tunnel.Tunnel;

/**
 * The BasicTunnel enforces a basic admittance policy.
 * It extends the Tunnel class.
 * 
 * You do not need to implement this class in PA4, but copying your solution from PA3 may be useful
 * for implementation of the PreemptiveTunnel.
 */
public class BasicTunnel extends Tunnel {
	//Used to keep track the direction of vehicles in tunnel
		public String direction = "";
		
		//Used to keep track of the number of cars in tunnel
		public int numOfCar = 0;
		
		//Used to keep track of the number of sleds in tunnel;
		public int numOfSled = 0;
		
		//Used to keep track of all vehicles in tunnel
		public List<Vehicle> list=new ArrayList<Vehicle>();
	

	/**
	 * Creates a new instance of a basic tunnel with the given name
	 * @param name the name of the basic tunnel
	 */
	public BasicTunnel(String name) {
		super(name);
	}

	@Override
	protected boolean tryToEnterInner(Vehicle vehicle) {
		if(vehicle instanceof Car) {
			//if tunnel is empty
			if(numOfCar ==0 && numOfSled ==0) {
				list.add(vehicle);
				direction = vehicle.getDirection().toString();
				numOfCar++;
				return true;
			}
			//if tunnel is not empty with vehicle inside
			else if(numOfCar < 3 && numOfSled == 0) {
				if(vehicle.getDirection().toString().equals(direction)) {
					list.add(vehicle);
					numOfCar++;
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
				return true;
			}else {
				return false;
			}
		}
		return false;
		
	}

	
	@Override
	protected void exitTunnelInner(Vehicle vehicle) {
		if(vehicle instanceof Car) {
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
