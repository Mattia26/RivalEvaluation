package maivisto.RivalEvaluation.evaluate.ranking;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.evaluation.Pair;
import net.recommenders.rival.evaluation.metric.EvaluationMetric;
import net.recommenders.rival.evaluation.metric.ranking.AbstractRankingMetric;

public class AggregateDiversity<U,I> extends AbstractRankingMetric<U,I> implements EvaluationMetric<U>{
	
	private static final int NUMITEMS = 7000;
	private static HashMap<Integer,Double> valuesAt;

    /**
     * Default constructor with predictions and groundtruth information.
     *
     * @param predictions predicted scores for users and items
     * @param test groundtruth information for users and items
     */
    public AggregateDiversity(final DataModel<U, I> predictions, final DataModel<U, I> test) {
        this(predictions, test, -1);
    }

    /**
     * Constructor where the relevance threshold can be initialized.
     *
     * @param predictions predicted ratings
     * @param test groundtruth ratings
     * @param relThreshold relevance threshold
     */
    public AggregateDiversity(final DataModel<U, I> predictions, final DataModel<U, I> test, final double relThreshold) {
        this(predictions, test, -1, new int[]{});
    }

    /**
     * Constructor where the cutoff levels can be initialized.
     *
     * @param predictions predicted ratings
     * @param test groundtruth ratings
     * @param relThreshold relevance threshold
     * @param ats cutoffs
     */
    public AggregateDiversity(final DataModel<U, I> predictions, final DataModel<U, I> test, final double relThreshold, final int[] ats) {
        super(predictions, test, -1, ats);
    }


	@Override
	public void compute() {
		
		if (!Double.isNaN(getValue())) {
            // since the data cannot change, avoid re-doing the calculations
            return;
        }
        iniCompute();
        
        Map<U, List<Pair<I, Double>>> data = processDataAsRankedTestRelevance();
         valuesAt = new HashMap<Integer,Double>();
        HashSet<I> distinctItems = new HashSet<I>();
        
        double value;
        
        for(Map.Entry<U, List<Pair<I, Double>>> e : data.entrySet()){
        	List<Pair<I, Double>> sortedList = e.getValue();
        
        	
        	for(Pair<I,Double> items : sortedList){
        		
        		I item = items.getFirst();
        		distinctItems.add(item);
        		}
        	
        }
        
        for(int at : getCutoffs()){
        	HashSet<I> distinctItemsAt = new HashSet<I>();
        	for(Map.Entry<U, List<Pair<I, Double>>> e : data.entrySet()){
        		
        
        		List<Pair<I, Double>> sortedList = e.getValue();
        		int rank = 0;
        		for(Pair<I,Double> item : sortedList){
        		
        			rank++;
        			if(at >= rank)
        				distinctItemsAt.add(item.getFirst());
        		
        			}
        	
        	}
        	valuesAt.put(at, (double)distinctItemsAt.size()/NUMITEMS);
        }
        value = (double)distinctItems.size() / NUMITEMS;
        
        setValue(value);
        
		
	}

	@Override
	public double getValueAt(int at) {
		if(valuesAt.containsKey(at))
			return valuesAt.get(at);
		return Double.NaN;
		
	}

	@Override
	public double getValueAt(U user, int at) {
		
		return (double)at/NUMITEMS;
	}
	
	 @Override
	    public String toString() {
	        return "AggregateDiversity_" + getRelevanceThreshold();
	    }
}
