package edu.brandeis.cs.cs131.pa4.submission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import edu.brandeis.cs.cs131.pa4.submission.Vehicle;
import edu.brandeis.cs.cs131.pa4.scheduler.Scheduler;
import edu.brandeis.cs.cs131.pa4.tunnel.Tunnel;

/**
 * The priority scheduler assigns vehicles to tunnels based on their priority
 * It extends the Scheduler class.
 * 
 * You do not need to implement this class for PA4, but copying your solution from PA3
 * may be useful for the PreemptivePriorityScheduler.
 */
public class PriorityScheduler extends Scheduler{
	
	//The PriorityQuene used to keep the waiting vehicles.
		public PriorityQueue<Vehicle> waitingQueue = new PriorityQueue<>();
		
		//The hash map used to keep track of vehicles in tunnel.
		public HashMap<Vehicle, Tunnel> vehicleInside = new HashMap<Vehicle, Tunnel>();
		
		//The array list of tunnels.
		public ArrayList<Tunnel> tunnelList = new ArrayList<>(tunnels);
		
		//The lock used to implement the mutual exclusion
		private final ReentrantLock lock = new ReentrantLock();
		
		private final Condition tunnelIsFull = lock.newCondition();
	
	/**
	 * Creates an instance of a priority scheduler with the given name and tunnels
	 * @param name the name of the priority scheduler
	 * @param tunnels the tunnels where the vehicles will be scheduled to
	 */
	public PriorityScheduler(String name, Collection<Tunnel> tunnels) {
		super(name, tunnels);
	}

	@Override
	public Tunnel admit(Vehicle vehicle) {
		lock.lock();
		Tunnel entered = null;
		try {
			waitingQueue.add(vehicle);
			boolean success = false;
			
			while(success==false) {
				if(waitingQueue.isEmpty()||waitingQueue.peek().getVehiclePriority()<=vehicle.getVehiclePriority()) {
					for(int i = 0; i < tunnelList.size();i++) {
						if(tunnelList.get(i).tryToEnter(vehicle)) {
							vehicleInside.put(vehicle, tunnelList.get(i));
							waitingQueue.remove(vehicle);
							success=true;
							entered = tunnelList.get(i);
							break;
						}
					}
				}
				if(success==false) {
					tunnelIsFull.await();
				}

			}	
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lock.unlock();
		return entered;
	}
	
	@Override
	public void exit(Vehicle vehicle) {
		lock.lock();
		for(int i = 0; i < tunnelList.size();i++) {
			if(vehicleInside.containsKey(vehicle)) {
				vehicleInside.get(vehicle).exitTunnel(vehicle);
				vehicleInside.remove(vehicle);
				tunnelIsFull.signalAll();
				break;
			}
			
		}
		lock.unlock();
	}
	
}
