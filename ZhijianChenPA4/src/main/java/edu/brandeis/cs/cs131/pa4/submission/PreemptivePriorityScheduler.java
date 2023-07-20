/**
 * Zhijian Chen
 * Chen5340@brandeis.edu
 * Apr 26th 2023
 * PA4
 * This class is used to implement the PreemptivePriotirySchedulaer function that schedule the vehicles based on their priority and give ambulance the highest priority.
 * Known bugs: None.
 */

package edu.brandeis.cs.cs131.pa4.submission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import edu.brandeis.cs.cs131.pa4.scheduler.Scheduler;
import edu.brandeis.cs.cs131.pa4.tunnel.Ambulance;
import edu.brandeis.cs.cs131.pa4.tunnel.Tunnel;


/**
 * The preemptive priority scheduler assigns vehicles to tunnels based on their priority and supports 
 * preemption with ambulances.
 * It extends the Scheduler class.
 */
public class PreemptivePriorityScheduler extends Scheduler {
	
	//The PriorityQuene used to keep the waiting vehicles.
			public PriorityQueue<Vehicle> waitingQueue = new PriorityQueue<>();
			
			//The hash map used to keep track of vehicles in tunnel.
			public HashMap<Vehicle, Tunnel> vehicleInside = new HashMap<Vehicle, Tunnel>();
			
			//The array list of tunnels.
			public ArrayList<Tunnel> tunnelList = new ArrayList<>(tunnels);
			
			//The lock used to implement the mutual exclusion
			private final ReentrantLock lock = new ReentrantLock();
			
			private final Condition tunnelIsFull = lock.newCondition();
	
	public PreemptivePriorityScheduler(String name, Collection<Tunnel> tunnels) {
		super(name, tunnels);
	}

	
	/**
	 * It check if the vehicle is able to get into a tunnel, or keep the vehicle waiting if not and make sure the ambulance can get into a tunnel everytime.
	 * @param vehicle that used to be checked.
	 */
	@Override
	public Tunnel admit(Vehicle vehicle) {
		lock.lock();
		Tunnel entered = null;
		try {
			if(vehicle instanceof Ambulance) {
				for(int a = 0; a < tunnelList.size();a++) {
					if(tunnelList.get(a).tryToEnter(vehicle)) {
						vehicleInside.put(vehicle, tunnelList.get(a));
						entered = tunnelList.get(a);
						lock.unlock();
						return entered;
					}
				}
			}else {
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
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lock.unlock();
		return entered;
	
	}

	
	/**
	 * This method is used to exit the finished vehicles in the tunnel
	 */
	@Override
	public void exit(Vehicle vehicle) {
		
		lock.lock();
		if(vehicle instanceof Ambulance) {
			if(vehicleInside.containsKey(vehicle)) {
				vehicleInside.get(vehicle).exitTunnel(vehicle);
				vehicleInside.remove(vehicle);
			}
		}
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

