/**
 * Zhijian Chen
 * Chen5340@brandeis.edu
 * Apr 26th 2023
 * PA4
 * This class is used to create the instance of three kind of cars as while as their behaviors. 
 * Known bugs: None.
 */

package edu.brandeis.cs.cs131.pa4.submission;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import edu.brandeis.cs.cs131.pa4.submission.Vehicle;
import edu.brandeis.cs.cs131.pa4.logging.EventType;
import edu.brandeis.cs.cs131.pa4.logging.Log;
import edu.brandeis.cs.cs131.pa4.scheduler.Scheduler;
//import edu.brandeis.cs.cs131.pa4.tunnel.Ambulance;
import edu.brandeis.cs.cs131.pa4.tunnel.Direction;
import edu.brandeis.cs.cs131.pa4.tunnel.Tunnel;

/**
 * A Vehicle is a Thread which enters Tunnels. You must subclass
 * Vehicle to customize its behavior (e.g., Car, Sled, Ambulance).
 *
 * When you start a Vehicle, the Vehicle will use the given scheduler to try to be 
 * admitted in one of the tunnels. The scheduler.admit() will successfully return when a tunnel is found,
 * or it will wait and retry until it eventually admits that vehicle in a tunnel.
 * 
 * The Vehicle does not know about the available tunnels and 
 * relies on the scheduler to be entered in a tunnel.
 *
 * You may need to edit run() or doWhileInTunnel() for the 
 * PreemptivePriorityScheduler and allow a vehicle to be signaled 
 * by an Ambulance while running in a tunnel.
 * 
 * @author cs131a
 */
public abstract class Vehicle extends Thread implements Comparable<Vehicle>{
	/**
	 * The name of this vehicle
	 */
    private String            	name;
    /**
     * The direction of this vehicle
     */
    private Direction          	direction;
    
    /**
     * The scheduler that this vehicle will use
     */
    private Scheduler 			scheduler;
    
    /**
     * The priority of this vehicle
     */
    private int                	priority;
    /**
     * The speed of this vehicle
     */
    private int                	speed;
    /**
     * The log used to log operations
     */
    private Log 				log;
    
    /**
     * The pullover used to act the real pull over
     */
    public boolean              pullOver = false;
    
    /**
     * The currentLoction used to record location when its pull over
     */
    public long                  startLocation;
    
    /**
     * The total used to record the whole time the car need to run.
     */
    public long                  total;
    
    /**
     * The timesSleeped used to record the time that the car already run.
     */
    public long                  halfWayLocation;
    
    //The lock used to implement the mutual exclusion
	private final ReentrantLock lock = new ReentrantLock();
	
	private final Condition StopRightThere = lock.newCondition();
	
	
    
    /**
     * This is what your vehicle does while inside the tunnel to
     * simulate taking time to "cross" the tunnel. The faster your
     * vehicle is, the less time this will take.
     * You will need to change this method (or create a new one) in order
     * to support preemption for the PremptivePriorityScheduler: a vehicle
     * must be able to pull-over and wait for an ambulance to cross the tunnel it entered.
     * And this method is also modified for support the ambulance highest priority pass. 
     */
    public final void doWhileInTunnel() {
    	lock.lock();
    	total = (10-speed)*100;
    	if(this.pullOver ==true) {
    		try {
				this.StopRightThere.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	while(true) {
    		startLocation = System.currentTimeMillis();
    		try {
    			sleep(total);
    			break;
    		}catch(InterruptedException e) {
    			halfWayLocation = System.currentTimeMillis();
    			try {
    				total = total - (halfWayLocation - startLocation);
					StopRightThere.await();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
   	    	}	
    	}
    	lock.unlock();
    }
    
    /**
     * Find and cross through one of the tunnels via the scheduler.
     * 
     * When a thread is run, it asks the scheduler to admit it in one
     * of the tunnels. The scheduler is responsible for taking this vehicle
     * and going through its collection of available tunnels until it 
     * succeeds in entering one of them. Then, the vehicle thread will call 
     * doWhileInTunnel (to simulate doing some work inside the tunnel,
     * i.e., that it takes time to cross the tunnel) and finally exit that tunnel
     * through the scheduler.
     */
    public final void run() {
    	if(scheduler == null) { // Cannot operate if there is no scheduler set
        	throw new UnsupportedOperationException("There is no scheduler set!");
        }
    	//use the given scheduler to schedule this vehicle
		scheduler.admit(this);
		doWhileInTunnel();
		scheduler.exit(this);
		this.log.addToLog(this, EventType.COMPLETE);
    }
    
    /**
     * Initialize a Vehicle; called from Vehicle constructors.
     * 
     * @param name the name of the vehicle
     * @param direction the direction of the vehicle
     * @param scheduler the scheduler to be used for this vehicle
     * @param log the log to be use for logging
     */
    private void init(String name, Direction direction, Scheduler scheduler, Log log) {
        this.name      = name;
        this.direction = direction;
        this.speed     = getDefaultSpeed();
        this.scheduler = scheduler;
        this.log       = log;

        if(this.speed < 0 || this.speed > 9) {
            throw new RuntimeException("Vehicle has invalid speed");
        }
    }

    /**
     * Override in a subclass to determine the speed of the
     * vehicle.
     *
     * Must return a number between 0 and 9 (inclusive). Higher
     * numbers indicate greater speed. The faster a vehicle, the less
     * time it will spend in a tunnel.
     * 
     * @return the speed of this vehicle
     */
    protected abstract int getDefaultSpeed();
    
    /**
     * Create a Vehicle with default priority that can cross one of
     * a collection of tunnels with the given scheduler.
     * 
     * @param name      The name of this vehicle to be displayed in the
     *                  output.
     * @param direction The side of the tunnel being entered.
     * @param scheduler The scheduler to be used for this vehicle
     * @param log the log to be used for logging
     */
    public Vehicle(String name, Direction direction, Scheduler scheduler, Log log) {
        init(name, direction, scheduler, log);
    }

    /**
     * Create a Vehicle with default priority that can cross one of
     * a collection of tunnels.
     * 
     * @param name      The name of this vehicle to be displayed in the
     *                  output.
     * @param direction The side of the tunnel being entered.
     * @param log the log to be used for logging
     */
    public Vehicle(String name, Direction direction, Log log) {
    	this(name, direction, null, log);
    }
    
    /**
     * Create a Vehicle with default priority that can cross one of
     * a collection of tunnels and use the default log.
     * 
     * @param name      The name of this vehicle to be displayed in the
     *                  output.
     * @param direction The side of the tunnel being entered.
     */
    public Vehicle(String name, Direction direction) {
        this(name, direction, Tunnel.DEFAULT_LOG);
    }
    
    /**
     * Sets this vehicle's speed - used for preemptive priority scheduler test
     * @param speed the new speed to be set (0 to 9)
     */
    public void setSpeed(int speed) {
    	if(this.speed < 0 || this.speed > 9) {
            throw new RuntimeException("Invalid speed: "+ speed);
        }
    	this.speed = speed;
    }
    
    /** 
     * Sets this vehicle's priority - used for priority scheduling
     *
     * @param priority The new priority (between 0 and 4 inclusive)
     */
    public final void setVehiclePriority(int priority) {
        if(priority < 0 || priority > 4) {
            throw new RuntimeException("Invalid priority: " + priority);
        }
        this.priority = priority;
    }
    
    /**
     * Returns the priority of this vehicle
     *
     * @return This vehicle's priority.
     */
    public int getVehiclePriority() {
        return priority;
    }

    /**
     * Returns the name of this vehicle
     *
     * @return The name of this vehicle
     */
    public final String getVehicleName() {
        return name;
    }

    /**
     * Returns a string representation of this vehicle
     * 
     * @return the string representation of this vehicle
     */
    public String toString() {
    		return String.format("%s VEHICLE %s", this.direction, this.name);
    }
    
    /**
     * Sets the given scheduler as the scheduler of this vehicle
     * @param newScheduler the new scheduler to be used
     */
    public final void setScheduler(Scheduler newScheduler) {
        this.scheduler = newScheduler;
    }
    
    /**
     * Returns the direction of this vehicle
     *
     * @return the direction of this vehicle
     */
    public final Direction getDirection() {
        return direction;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.name);
        hash = 23 * hash + Objects.hashCode(this.direction);
        hash = 23 * hash + this.speed;
        hash = 23 * hash + this.priority;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Vehicle other = (Vehicle) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (this.direction != other.direction) {
            return false;
        }
        if (this.speed != other.speed) {
            return false;
        }
        if (this.priority != other.priority) {
            return false;
        }
        return true;
    }
    
	@Override
	public int compareTo(Vehicle o) {
		return o.getVehiclePriority() - getVehiclePriority();
	}
	
	/**
	 * This method is used to achieve pull over
	 */
	public void vehicleWait() {
		lock.lock();
		try {
			StopRightThere.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lock.unlock();
	}
	
	/**
	 * This method is used to achieve continue running after pull over.
	 */
	public void vehicleContinue() {
		lock.lock();
		StopRightThere.signalAll();
		lock.unlock();
	}
}
