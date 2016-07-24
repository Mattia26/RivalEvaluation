package maivisto.RivalEvaluation;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import maivisto.RivalEvaluation.evaluate.MultipleEvaluationMetricPercentageRunner;
import maivisto.RivalEvaluation.recommend.MultipleBaselineRunner;
import maivisto.RivalEvaluation.splitter.ColdSplitterRunner;
import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.evaluation.metric.EvaluationMetricRunner;
import net.recommenders.rival.evaluation.strategy.MultipleStrategyRunner;
import net.recommenders.rival.split.parser.ParserRunner;


public class CompletePipelineRunner 
{
	public final static String PERCENTAGES = "split.percentages";
	
    public static void main( String[] args )
    {
    	
    	Properties props = new Properties();
    	
    	try {
			props.load(new FileInputStream("Configuration"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	run(props);
    	
    }
    
    public static void run(Properties props){
    	
    	String[] percentages = props.getProperty(PERCENTAGES).split(",");
    	
    	for(String percentage : percentages){
    		props.setProperty(EvaluationMetricRunner.METRIC, ".ranking.");
    		props.setProperty(ColdSplitterRunner.SPLIT_PERCENTAGE, percentage);
    		prepareSplit(props);
    		MultipleBaselineRunner.runBaselines(props);
        	prepareStrategy(props);
        	
    	}
    	
    	evaluate(props);
    	
    
    	
    }
    public static void evaluate(Properties properties){
    	try {
			MultipleEvaluationMetricPercentageRunner.run(properties);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public static void prepareStrategy(Properties properties){
    	try {
    		MultipleStrategyRunner.run(properties);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public static void prepareSplit(Properties properties){
    	
    	try {
			
    		DataModel<Long,Long> dataset = ParserRunner.run(properties);
			ColdSplitterRunner.run(properties, dataset, true);
		
    	} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	
    }
}
