import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import com.xeiam.xchart.Chart;
import com.xeiam.xchart.QuickChart;
import com.xeiam.xchart.SwingWrapper;




public class ClosestHotels {

	public static long startTime = System.currentTimeMillis();
	
	public static void main(String[] args) {
		
		//Config
		int k = 10;
		int n = 1000000;
		int maxLat = 90;
		int maxLong = 180;
		Random rand = new Random();
		
		
		log(euclideanDistance(new Hotel(2, -179.98871990614697, 1), new Hotel(2, 179.99863582095406, 1), maxLong, true));
		
		
		
		// Working data
		Hotel[] hotelsOrig = new Hotel[n];
		Hotel[] hotels = new Hotel[n];
		HashMap<Integer, Integer> comparisonsTable = new HashMap<Integer, Integer>();
		ArrayList<Distance>[] results = new ArrayList[n];
		Integer comparisons = 0;
		
		
		
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
			comparisons = 0;
			double normalizedAX;
			double normalizedBX;
			//repeat 
			do {
				//one more comparison
				comparisons++;
				
				//if r goes out of bounds start over
				if (r >= n) r = 0;
				if (l < 0) l = n-1;
				
				
				//if l and r are togheter
				if (l == r || l -1 == r) {
					outOfItems = true;
					next = null;
				} else  {
					normalizedAX = normalizeLong(curHotel);
					next = Math.abs(normalizedAX - normalizeLong(hotels[l])) < Math.abs(normalizedAX - normalizeLong(hotels[r])) ? hotels[l--] : hotels[r++];
				}
				
				if (next != null) {
					
					//calculate the real distance
					curDistance = euclideanDistance(curHotel, next);
					
					//if distance is lower than the maximun in the heap
					if (minDistances.size() < k || curDistance < minDistances.peek().getDistance()) {
						
						//ad this distance to the heap
						minDistances.add(new Distance(next.getId(), curDistance));
						
						//we dont want ot exceed k size
						if (minDistances.size() > k) { minDistances.poll(); }
					}
				}
			
			//exit the loop if the next hotel X's distance is greater the max allowed euclidean distance 
			//AND the heap is complete OR we run out of items 
			} while (!outOfItems && (Math.abs(normalizeLong(curHotel) - normalizeLong(next)) < minDistances.peek().getDistance() || minDistances.size() < k));
			
			comparisonsTable.put(comparisons, comparisonsTable.containsKey(comparisons) ? comparisonsTable.get(comparisons)+1 : 1);
			
			// Copy the heap results to the final results array
			while(!minDistances.isEmpty()){
				Distance d = minDistances.poll();
				results[i].add(d);
				//Reverse to keep the lower result first
//				Collections.reverse(results[i]);
			}
			
			
		}
		
		
		printTimer();
		//How many comparisons per hotel
		log("####### Average comparisons per hotel: " + (comparisons / n));
		log("..................................................");

		//Print some random examples of resutls
		logGroup(0, results, hotels, hotelsOrig);
		logGroup(1, results, hotels, hotelsOrig);
		logGroup(n-1, results, hotels, hotelsOrig);
		

		
		//disaggregate comparisons number data into 2 ordered arrays
		Integer[] orderedKeys = comparisonsTable.keySet().toArray(new Integer[comparisonsTable.size()]);
		Arrays.sort(orderedKeys);
		double[] xKeys = new double[orderedKeys.length];
		double[] yKeys = new double[orderedKeys.length];
		for (int i = 0; i < orderedKeys.length; i++) {
			xKeys[i] = orderedKeys[i];
			yKeys[i] = comparisonsTable.get(orderedKeys[i]);
		}
 
	    // Create Chart
	    Chart chart = QuickChart.getChart("n="+n+" k="+k+" maxLatitude(y)="+maxLat+" maxLong(x)="+maxLong, "Comparisons", "Number of items", "y(x)", xKeys, yKeys);
	 	    // Show it
	    new SwingWrapper(chart).displayChart();
	 
		
		
		
	}
	
	
	public static double normalizeLong(Hotel a) {
		return (a.getX() + 360) % 360;
	}
	
	
	//Calculate eculidean distance
	public static double euclideanDistance(Hotel a, Hotel b, Integer maxX, boolean loguear) {
		double normalizedAX = (a.getX() + maxX*2) % (maxX*2);
		double normalizedBX = (b.getX() + maxX*2) % (maxX*2);
		
		if (loguear) { log(normalizedAX + " " + normalizedBX); }
		return Math.sqrt(Math.pow(normalizeLong(a) - normalizeLong(b), 2) + Math.pow(a.getY() - b.getY(), 2));
	}
	
	
	public static double euclideanDistance(Hotel a, Hotel b) {
		return Math.sqrt(Math.pow(normalizeLong(a) - normalizeLong(b), 2) + Math.pow(a.getY() - b.getY(), 2));
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
