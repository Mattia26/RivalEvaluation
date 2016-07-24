package maivisto.RivalEvaluation.splitter;


import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.core.DataModelUtils;
import net.recommenders.rival.evaluation.metric.EvaluationMetricRunner;
import net.recommenders.rival.evaluation.strategy.AbstractStrategy;
import net.recommenders.rival.evaluation.strategy.StrategyRunner;
import net.recommenders.rival.recommend.frameworks.RecommendationRunner;
import net.recommenders.rival.split.splitter.CrossValidationSplitter;
import net.recommenders.rival.split.splitter.RandomSplitter;
import net.recommenders.rival.split.splitter.Splitter;
import net.recommenders.rival.split.splitter.TemporalSplitter;
/**
 * Runner for the ColdSplitter class
 * @author Mattia Menna
 *
 */
public final class ColdSplitterRunner {
	/**
     * Variable that represent the name of a property in the file.
     */
	public static final String SPLIT_PERCENTAGE = "split.percentage";
	/**
     * Variable that represent the name of a property in the file.
     */
	public static final String SPLIT_OUTPUT_FOLDER = "split.output.folder";
	/**
     * Variable that represent the name of a property in the file.
     */
	public static final String SPLIT_OUTPUT_OVERWRITE= "split.output.overwrite";
	/**
     * Variable that represent the name of a property in the file.
     */
	public static final String SPLIT_TRAINING_PREFIX = "split.training.prefix";
	/**
     * Variable that represent the name of a property in the file.
     */
	public static final String SPLIT_TRAINING_SUFFIX = "split.training.suffix";
	/**
     * Variable that represent the name of a property in the file.
     */
	public static final String SPLIT_TEST_PREFIX = "split.test.prefix";
	/**
     * Variable that represent the name of a property in the file.
     */
	public static final String SPLIT_TEST_SUFFIX= "split.test.suffix";
	/**
	 * Private constructor
	 */
	private ColdSplitterRunner(){
		
	}
	/**
	 * Runs the splitter
	 * @param properties Property containing the configuration settings
	 * @param data The data set
	 * @param doDataClear Boolean for the destruction of the data set DataModel
	 */
	public static void run(final Properties properties, final DataModel<Long,Long> data, final boolean doDataClear){
		System.out.println("Start splitting");
		DataModel<Long,Long>[] splits;
		
		String outputFolder = properties.getProperty(SPLIT_OUTPUT_FOLDER);
        Boolean overwrite = Boolean.parseBoolean(properties.getProperty(SPLIT_OUTPUT_OVERWRITE, "false"));
        String splitTrainingPrefix = properties.getProperty(SPLIT_TRAINING_PREFIX);
        String splitTrainingSuffix = properties.getProperty(SPLIT_TRAINING_SUFFIX);
        String splitTestPrefix = properties.getProperty(SPLIT_TEST_PREFIX);
        String splitTestSuffix = properties.getProperty(SPLIT_TEST_SUFFIX);
		
        Splitter<Long,Long> splitter = instantiateSplitter(properties);
        splits = splitter.split(data);
        if (doDataClear){
        	data.clear();
        }
        
        System.out.println("Saving splits...");
        
        for(int i = 0; i<splits.length/2; i++){
        	DataModel<Long,Long> training = splits[2*i];
        	DataModel<Long,Long> test = splits[2*i+1];
        	String trainingFile = outputFolder + splitTrainingPrefix + i + "_" +properties.getProperty(SPLIT_PERCENTAGE)+"%" + splitTrainingSuffix;
        	String testFile = outputFolder + splitTestPrefix + i + "_" +properties.getProperty(SPLIT_PERCENTAGE)+"%" + splitTestSuffix;
        	
            try {
            	DataModelUtils.saveDataModel(training, trainingFile, overwrite);
				DataModelUtils.saveDataModel(test, testFile, overwrite);
				properties.setProperty(StrategyRunner.TRAINING_FILE, trainingFile);
				properties.setProperty(StrategyRunner.TEST_FILE, testFile);
				properties.setProperty(RecommendationRunner.TRAINING_SET, trainingFile);
				properties.setProperty(RecommendationRunner.TEST_SET, testFile);
				properties.setProperty(EvaluationMetricRunner.TEST_FILE, testFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
	}

	/**
	 * Instantiates the splitter
	 * @param properties Property containing the configuration settings
	 * @return The instantiated Splitter
	 */
	public static Splitter<Long,Long> instantiateSplitter(final Properties properties){
		int splitterPercentage = Integer.parseInt(properties.getProperty(SPLIT_PERCENTAGE));
		Splitter<Long,Long> splitter = null;
		
		if(splitterPercentage != -1)
			splitter = new ColdSplitter<Long, Long>(splitterPercentage);
		
		return splitter;
		
	}
}

