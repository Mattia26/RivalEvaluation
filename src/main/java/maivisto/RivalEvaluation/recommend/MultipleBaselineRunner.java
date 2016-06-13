package maivisto.RivalEvaluation.recommend;

import java.util.ArrayList;
import java.util.Properties;

import net.recommenders.rival.recommend.frameworks.AbstractRunner;
import net.recommenders.rival.recommend.frameworks.RecommendationRunner;
import net.recommenders.rival.recommend.frameworks.AbstractRunner.RUN_OPTIONS;

/**
 * A class that runs multiple instances of recommender at once
 * @author Mattia Menna
 *
 */
public final class MultipleBaselineRunner {
	
	/**
	 * Variable that represent the name of a property in the file.
	*/
	public static final String LENSKIT_BASELINES = "baselines";
	
	/**
	 * Runs the baselines
	 * @param properties Property that contains configuration settings
	 */
	public static void runBaselines(Properties properties){
		
		for(AbstractRunner<Long,Long> rec : instantiateBaselines(properties)){
			try {
				
				rec.run(RUN_OPTIONS.OUTPUT_RECS);
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Instantiates the baselines
	 * @param properties Property that contains configuration settings
	 * @return A array with the instantiated AbstractRunners
	 */
	public static AbstractRunner<Long,Long>[] instantiateBaselines(Properties properties){
		ArrayList<AbstractRunner<Long,Long>> recList = new ArrayList<AbstractRunner<Long,Long>>(); 
		
		String[] recommenders = properties.getProperty(LENSKIT_BASELINES).split(",");
		
		for(String recommender : recommenders){
			properties.setProperty(RecommendationRunner.RECOMMENDER, recommender);
			recList.add(new LenskitBaselineRecommenderRunner(properties));
		}
		
		@SuppressWarnings("unchecked")
		AbstractRunner<Long,Long>[] recs = recList.toArray(new AbstractRunner[0]);
		
		return recs;
	}

}
