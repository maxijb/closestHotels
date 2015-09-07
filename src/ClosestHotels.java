import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;


public class ClosestHotels {

	public static long startTime = System.currentTimeMillis();
	
	public static void main(String[] args) {
		
		//Config
		int k = 10;
		int n = 500000;
		int maxLat = 90;
		int maxLong = 180;
		Random rand = new Random();
		
		// Working data
		Hotel[] hotelsOrig = new Hotel[n];
		Hotel[] hotels = new Hotel[n];
		ArrayList<Distance>[] results = new ArrayList[n];
		long comparisons = 0;
		
		
		
		//init hotels
		log("Creating Hotels...");
		for (int i = 0; i < n; i++) {
			double lat = maxLat  * rand.nextDouble() * (rand.nextDouble() >= 0.5 ? 1 : -1);
			double lon = maxLong  * rand.nextDouble() * (rand.nextDouble() >= 0.5 ? 1 : -1);
			hotels[i] = new Hotel(i, lon, lat);
			//initialize results array
			results[i] = new ArrayList<Distance>();
		}

		//duplicate array
		System.arraycopy( hotels, 0, hotelsOrig, 0, n );
		printTimer();
		

		//Sort hotels by X
		log("Sorting Hotels...");
		Arrays.sort(hotels);
		printTimer();
		
		//Start comparing
		log("Comparing Hotels...");
		for (int i = 0; i < n; i++) {
			//l and r are the pointers to the left and rigth of the current hotels
			int l = i - 1;
			int r = i + 1;
			if (i % 10000 == 0) log(i);
			
			//In the heap we store the k minDistances
			PriorityQueue<Distance> minDistances = new PriorityQueue<Distance>(Collections.reverseOrder());;
			
			//working variables
			Hotel curHotel = hotels[i];
			Hotel next = null;
			double curDistance;
			boolean outOfItems = false;
			
			//repeat 
			do {
				//one more comparison
				comparisons++;
				
				//if in bounds, select the next closest hotel by X within the array bounds
				if (l < 0 && r < n ) {
					next = hotels[r++];
				} else if ( l >= 0 && r >= n) {
					next = hotels[l--];
				} else if (l >= 0 && r < n) {
					next = Math.abs(curHotel.getX() - hotels[l].getX()) < Math.abs(curHotel.getX() - hotels[r].getX()) ? hotels[l--] : hotels[r++];
				} else {
					//out of bounds, shoudl exit the loop
					outOfItems = true;
				}
				
				//calculate the real distance
				curDistance = euclideanDistance(curHotel, next);
				
				//if distance is lower than the maximun in the heap
				if (minDistances.size() < k || curDistance < minDistances.peek().getDistance()) {
					
					//ad this distance to the heap
					minDistances.add(new Distance(next.getId(), curDistance));
					
					//we dont want ot exceed k size
					if (minDistances.size() > k) { minDistances.poll(); }
				}
			
			//exit the loop if the next hotel X's distance is greater the max allowed euclidean distance 
			//AND the heap is complete OR we run out of items 
			} while ((Math.abs(next.getX() - curHotel.getX()) < minDistances.peek().getDistance() || minDistances.size() < k) && !outOfItems);
			
			// Copy the heap results to the final results array
			while(!minDistances.isEmpty()){
				Distance d = minDistances.poll();
				results[i].add(d);
				//Reverse to keep the lower result first
				Collections.reverse(results[i]);
			}
			
			
		}
		
		
		printTimer();
		//How many comparisons per hotel
		log("####### Average comparisons per hotel: " + (comparisons / n));
		log("..................................................");

		//Print some random examples of resutls
		logGroup(120000, results, hotels, hotelsOrig);
		logGroup(310050, results, hotels, hotelsOrig);
		logGroup(423040, results, hotels, hotelsOrig);
		
	}
	
	
	
	//Calculate eculidean distance
	public static double euclideanDistance(Hotel a, Hotel b) {
		return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2));
	}
	
	
	public static void logGroup(int index, ArrayList<Distance>[] groups,  Hotel[] hotels, Hotel[] hotelsOrig) {
		ArrayList<Distance> group = groups[index];
		Hotel hotel = hotels[index];
		log("Group for hotelId " + hotel.getId() + " with coors: " + hotel.getX() + ", " + hotel.getY());
		
		for (int i = 0; i < group.size(); i++) {
			int id = group.get(i).getId();
			Hotel next = hotelsOrig[id];
			log("Hotel " + id + " distance " + group.get(i).getDistance() + " coors: "  + next.getX() + ", " + next.getY() );
		}
	}
	
	public static void log(Object obj) {
		System.out.println(obj);
	}
	
	public static void printTimer() {
		long endTime = System.currentTimeMillis();
		long elapsed = endTime - startTime;
		log("#######  " + (elapsed / 1000) + " seconds have passed since last task.");
		startTime = System.currentTimeMillis();
	}

}
