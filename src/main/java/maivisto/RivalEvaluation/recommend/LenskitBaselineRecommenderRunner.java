package maivisto.RivalEvaluation.recommend;


import java.util.List;
import java.util.Properties;

import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.baseline.BaselineScorer;
import org.grouplens.lenskit.baseline.ItemMeanRatingItemScorer;
import org.grouplens.lenskit.baseline.MeanDamping;
import org.grouplens.lenskit.baseline.UserMeanBaseline;
import org.grouplens.lenskit.baseline.UserMeanItemScorer;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.PrefetchingUserDAO;
import org.grouplens.lenskit.data.dao.UserDAO;
import org.grouplens.lenskit.iterative.IterationCount;
import org.grouplens.lenskit.knn.item.ItemItemScorer;
import org.grouplens.lenskit.mf.funksvd.FeatureCount;
import org.grouplens.lenskit.mf.funksvd.FunkSVDItemScorer;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.transform.normalize.BaselineSubtractingUserVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer;
import org.grouplens.lenskit.vectors.similarity.CosineVectorSimilarity;
import org.grouplens.lenskit.vectors.similarity.VectorSimilarity;
import maivisto.RivalEvaluation.baselines.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.recommend.frameworks.RecommendationRunner;
import net.recommenders.rival.recommend.frameworks.RecommenderIO;
import net.recommenders.rival.recommend.frameworks.exceptions.RecommenderException;
import net.recommenders.rival.recommend.frameworks.lenskit.EventDAOWrapper;
/**
 * Extension of the LenskitRecommenderRunner class that configures and runs 
 * the baselines, it also saves the recommendations.
 * 
 * @author Mattia Menna
 *
 */
public class LenskitBaselineRecommenderRunner extends net.recommenders.rival.recommend.frameworks.lenskit.LenskitRecommenderRunner{
	
	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(LenskitBaselineRecommenderRunner.class);
	

	/**
	 * Inherited constructor
	 * @param props Property with configuration settings
	 */
	public LenskitBaselineRecommenderRunner(Properties props) {
		super(props);
		// TODO Auto-generated constructor stub
	}

	
	/**
	 * Runs the recommender with the training and test DataModel
	 * 
	 * @param opts Options for output format
	 * @param trainingModel training split DataModel
	 * @param testModel test split DataModel
	 * @return the recommendations DataModel
	 */
	public DataModel<Long, Long> run(final RUN_OPTIONS opts, final DataModel<Long, Long> trainingModel, final DataModel<Long, Long> testModel) throws RecommenderException {
        if (isAlreadyRecommended()) {
            return null;
        }
        // transform from core's DataModels to Lenskit's EventDAO
        EventDAO trainingModelLensKit = new EventDAOWrapper(trainingModel);
        EventDAO testModelLensKit = new EventDAOWrapper(testModel);

        return runLenskitRecommender(opts, trainingModelLensKit, testModelLensKit);
    }
	
	
	
	@SuppressWarnings("unchecked")
	/**
	 * Runs the recommender with the training and test EvenDAO
	 * 
	 * @param opts Options for the output format
	 * @param trainingModel Training split EvenDAO
	 * @param testModel Test split EventDAO
	 * @return The recommendations DataModel
	 */
    public DataModel<Long, Long> runLenskitRecommender(final RUN_OPTIONS opts, final EventDAO trainingModel, final EventDAO testModel) throws RecommenderException {
        if (isAlreadyRecommended()) {
            return null;
        }
        EventDAO dao = new EventCollectionDAO(Cursors.makeList(trainingModel.streamEvents()));
        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(EventDAO.class).to(dao);

        System.out.println(getFileName());
        switch(getProperties().getProperty(RecommendationRunner.RECOMMENDER)){
        case("ItemItem"):
        	config.bind(ItemScorer.class).to(ItemItemScorer.class);
        	config.within(ItemScorer.class).bind(VectorSimilarity.class).to(CosineVectorSimilarity.class);
        	config.within(ItemScorer.class).bind(UserVectorNormalizer.class).to(BaselineSubtractingUserVectorNormalizer.class);
        	config.within(ItemScorer.class).within(UserVectorNormalizer.class).bind(BaselineScorer.class,ItemScorer.class).to(ItemMeanRatingItemScorer.class);
        	config.within(ItemScorer.class).set(MeanDamping.class).to(5.0);
        	break;
        case("Popularity"):
        	config.bind(ItemRecommender.class).to(PopularityRec.class);
        	config.bind(ItemScorer.class).to(UserMeanItemScorer.class);
        	config.bind(UserMeanBaseline.class,ItemScorer.class).to(ItemMeanRatingItemScorer.class);
        	config.set(MeanDamping.class).to(5.0);
        	break;
        case("FunkSVD"):
        	config.bind(ItemScorer.class).to(FunkSVDItemScorer.class);
        	config.bind(BaselineScorer.class,ItemScorer.class).to(UserMeanItemScorer.class);
        	config.bind(UserMeanBaseline.class,ItemScorer.class).to(ItemMeanRatingItemScorer.class);
        	config.set(MeanDamping.class).to(5.0);
        	config.set(FeatureCount.class).to(30);
        	config.set(IterationCount.class).to(150);
        	break;
        case("RandomPopularity"):
        	config.bind(ItemRecommender.class).to(RandomPopularityRec.class);
        	config.bind(ItemScorer.class).to(UserMeanItemScorer.class);
        	config.bind(UserMeanBaseline.class,ItemScorer.class).to(ItemMeanRatingItemScorer.class);
        	config.set(MeanDamping.class).to(5.0);
        	break;
        	
        }
        UserDAO test = new PrefetchingUserDAO(testModel);
        Recommender rec = null;
        try {
            LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config);
            rec = engine.createRecommender();
        } catch (RecommenderBuildException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
            throw new RecommenderException("Problem with LenskitRecommenderEngine: " + e.getMessage());
        }
        ItemRecommender irec = null;
        if (rec != null) {
            irec = rec.getItemRecommender();
        }
        assert irec != null;

        DataModel<Long, Long> model = null;
        switch (opts) {
            case RETURN_AND_OUTPUT_RECS:
            case RETURN_RECS:
                model = new DataModel<Long, Long>();
                break;
            default:
                model = null;
        }
        String name = null;
        switch (opts) {
            case RETURN_AND_OUTPUT_RECS:
            case OUTPUT_RECS:
                name = getFileName();
                break;
            default:
                name = null;
        }
        boolean createFile = true;
        
        
        for (long user : test.getUserIds()) {
            List<ScoredId> recs = irec.recommend(user,Integer.parseInt(getProperties().getProperty(RecommendationRunner.FACTORS)));
            RecommenderIO.writeData(user, recs, getPath(), name, !createFile, model);
            createFile = false;
        }
        return model;
    }

}
