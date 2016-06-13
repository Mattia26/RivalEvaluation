package maivisto.RivalEvaluation.splitter;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.split.splitter.Splitter;
/**
 * Implementation of Splitter
 * @author mattia
 *
 * @param <U> user type
 * @param <I> item type
 */
public class ColdSplitter<U, I> implements Splitter<U,I>{
	/**
	 * Percentage of cold start users in the training set
	 */
	private int percentage;
	/**
	 * Constructor
	 * @param percentage Percentage of cold start users in the training set
	 */
	public ColdSplitter(int percentage){
		
		this.percentage=percentage;
	}
	/**
	 * Splits the data set
	 * @param data The data set
	 * @return An array of splits
	 */
	   @SuppressWarnings("unchecked")
	public DataModel<U, I>[] split(final DataModel<U, I> data){
		   
		   HashSet<U> allUsers = new HashSet(data.getUsers());
		   HashSet<U> csUsers = selectColdUsers(allUsers);
		   HashMap<U,Integer> usersToPsz = splitColdUsers(csUsers);
		   Map<U,Map<I,Double>> histories = data.getUserItemPreferences();
		   DataModel<U,I> train = new DataModel<U,I>();
		   DataModel<U,I> test = new DataModel<U,I>();
		   final DataModel<U, I>[] splits = new DataModel[2];
		   
		   
		   for(U u : histories.keySet()){
			   Map<I,Double> preferences = histories.get(u);
			   Object[] ratedItems = preferences.keySet().toArray();
			   int n=preferences.size();
			   
			  
			   if(csUsers.contains(u)){
				   int psz = usersToPsz.get(u);
				   
				   for(int i=0;i<psz;i++)
					   train.addPreference(u,(I)ratedItems[i], preferences.get(ratedItems[i]));
				   for(int i=psz;i<n;i++)
					   test.addPreference(u, (I)ratedItems[i], preferences.get(ratedItems[i]));
					   
			   }
			   else{
				   for(int i=0; i<n; i++)
					   train.addPreference(u, (I)ratedItems[i], preferences.get(ratedItems[i]));
			   }
				   
			   
			   
			   
		   }
		  
		   splits[0] = train;
		   splits[1] = test;
		  
		
		return splits;
	}
	/**
	 * Extracts the cold users  
	 * @param allUsers HashSet containing all the users
	 * @return HashSet containing all the cold start users
	 */
	private HashSet<U> selectColdUsers(HashSet<U> allUsers){
		
		HashSet<U> csUsers = new HashSet<U>();
		Object[] users = allUsers.toArray();
		int numCsUsers = allUsers.size()*percentage/100;
		Random r = new Random();
		
		while(csUsers.size() < numCsUsers)
			csUsers.add((U)users[r.nextInt(users.length)]);
		
		return csUsers;
		
	}
	/**
	 * Splits the cold start users in three partitions
	 * @param csUsers HashSet containing all the cold start users
	 * @return HashMap with pairs <user,profile-size>
	 */
	@SuppressWarnings("unused")
	private HashMap<U,Integer> splitColdUsers(HashSet<U> csUsers){
		int[] profileSizes1 = {0,1,2,3,4,5,6};
		int[] profileSizes2 = {7,8,9,10,11,12,13};
		int[] profileSizes3 = {14,15,16,17,18,19};
		ArrayList<U> listUsers = new ArrayList<U>();
		HashMap<U,Integer> userToPsz = new HashMap<U,Integer>();
		
		for(U u:csUsers)
			listUsers.add(u);
		
		Collections.shuffle(listUsers);
		
		Random r = new Random();
		int csu0_5, csu6_12, csu13_19;
		
		if(csUsers.size() % 3 == 2){
			csu0_5 = csu6_12 = csUsers.size()/3 +1;
			csu13_19 = csUsers.size()/3;
		} 
		else if(csUsers.size() % 3 == 1){
			csu0_5 = csUsers.size()/3 +1;
			csu6_12 = csu13_19 = csUsers.size()/3;
		} 
		else
			csu0_5 = csu6_12 = csu13_19 = csUsers.size()/3;
		
		int p=0;
		
		for(U u:listUsers){
			
			if(csu0_5==0){
				p++;
				csu0_5=-1;
			}
			if(csu6_12==0){
				p++;
				csu6_12=-1;
			}
			
			int ps = 0;
			
			switch(p){
			
			case 0:
				ps=profileSizes1[r.nextInt(profileSizes1.length)];
				csu0_5--;
				break;
			case 1:
				ps=profileSizes2[r.nextInt(profileSizes2.length)];
				csu6_12--;
				break;
			case 2:
				ps=profileSizes3[r.nextInt(profileSizes3.length)];
				csu13_19--;
				break;
			}
			
			userToPsz.put(u, ps);
		}
		
		return userToPsz;
	}
	

}
