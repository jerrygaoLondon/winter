/** 
 *
 * Copyright (C) 2015 Data and Web Science Group, University of Mannheim, Germany (code@dwslab.de)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.uni_mannheim.informatik.dws.winter.usecase.restaurants;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.matching.MatchingEngine;
import de.uni_mannheim.informatik.dws.winter.matching.MatchingEvaluator;
import de.uni_mannheim.informatik.dws.winter.matching.algorithms.RuleLearner;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.StandardRecordBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.BlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.RecordBlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.WekaMatchingRule;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.MatchingGoldStandard;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.Performance;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.XMLRecordReader;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.RecordComparatorEqual;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.RecordComparatorJaccard;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.RecordComparatorLevenshtein;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.RecordComparatorLowerCaseJaccard;
import de.uni_mannheim.informatik.dws.winter.model.io.CSVCorrespondenceFormatter;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.usecase.restaurants.model.Restaurant;

/**
 * //TODO Change Comments
 * 
 * 
 */
public class Restaurant_IdentityResolutionLearningMatchingRule {

	public static void main(String[] args) throws Exception {
		// loading data
		Map<String, Attribute> nodeMapping = new HashMap<>();
		nodeMapping.put("Name", Restaurant.NAME);
		nodeMapping.put("Address", Restaurant.ADDRESS);
		nodeMapping.put("City", Restaurant.CITY);
		nodeMapping.put("Phone", Restaurant.PHONE);
		nodeMapping.put("Style", Restaurant.STYLE);

		DataSet<Record, Attribute> dataZagats = new HashedDataSet<>();
		new XMLRecordReader("id", nodeMapping).loadFromXML(new File("usecase/restaurant/input/zagats.xml"),
				"/restaurants/restaurant", dataZagats);
		HashedDataSet<Record, Attribute> dataFodors = new HashedDataSet<>();
		new XMLRecordReader("id", nodeMapping).loadFromXML(new File("usecase/restaurant/input/fodors.xml"),
				"/restaurants/restaurant", dataFodors);

		// load the gold standard (test set)
		// load the gold standard (training set)
		MatchingGoldStandard gsTraining = new MatchingGoldStandard();
		gsTraining.loadFromCSVFile(new File("usecase/restaurant/goldstandard/gs_restaurant_training.csv"));

		// create a matching rule + provide classifier, options + Feature
		// Selection --> Comparators / Standard
		String options[] = new String[1];
		options[0] = "-U"; // unpruned tree
		String tree = "J48"; // new instance of tree
		WekaMatchingRule<Record, Attribute> matchingRule = new WekaMatchingRule<>(0.8, tree, options);

		// add comparators - Name
		matchingRule.addComparator(new RecordComparatorLevenshtein(Restaurant.NAME, Restaurant.NAME));
		matchingRule.addComparator(new RecordComparatorEqual(Restaurant.NAME, Restaurant.NAME));
		matchingRule.addComparator(new RecordComparatorJaccard(Restaurant.NAME, Restaurant.NAME));
		matchingRule.addComparator(new RecordComparatorLevenshtein(Restaurant.NAME, Restaurant.NAME));
		matchingRule.addComparator(new RecordComparatorLowerCaseJaccard(Restaurant.NAME, Restaurant.NAME, 0.3, true));
		
		matchingRule.addComparator(new RecordComparatorLevenshtein(Restaurant.ADDRESS, Restaurant.ADDRESS));
		matchingRule.addComparator(new RecordComparatorEqual(Restaurant.ADDRESS, Restaurant.ADDRESS));
		matchingRule.addComparator(new RecordComparatorJaccard(Restaurant.ADDRESS, Restaurant.ADDRESS));
		matchingRule.addComparator(new RecordComparatorLevenshtein(Restaurant.ADDRESS, Restaurant.ADDRESS));
		matchingRule.addComparator(new RecordComparatorLowerCaseJaccard(Restaurant.ADDRESS, Restaurant.ADDRESS, 0.3, true));
		
		matchingRule.addComparator(new RecordComparatorLevenshtein(Restaurant.PHONE, Restaurant.PHONE));
		matchingRule.addComparator(new RecordComparatorEqual(Restaurant.PHONE, Restaurant.PHONE));
		matchingRule.addComparator(new RecordComparatorJaccard(Restaurant.PHONE, Restaurant.PHONE));
		matchingRule.addComparator(new RecordComparatorLevenshtein(Restaurant.PHONE, Restaurant.PHONE));
		matchingRule.addComparator(new RecordComparatorLowerCaseJaccard(Restaurant.PHONE, Restaurant.PHONE, 0.3, true));
		
		matchingRule.addComparator(new RecordComparatorLevenshtein(Restaurant.STYLE, Restaurant.STYLE));
		matchingRule.addComparator(new RecordComparatorEqual(Restaurant.STYLE, Restaurant.STYLE));
		matchingRule.addComparator(new RecordComparatorJaccard(Restaurant.STYLE, Restaurant.STYLE));
		matchingRule.addComparator(new RecordComparatorLevenshtein(Restaurant.STYLE, Restaurant.STYLE));
		matchingRule.addComparator(new RecordComparatorLowerCaseJaccard(Restaurant.STYLE, Restaurant.STYLE, 0.3, true));
		

		// create a blocker (blocking strategy)
		StandardRecordBlocker<Record, Attribute> blocker = new StandardRecordBlocker<>(
				new RecordBlockingKeyGenerator<Record, Attribute>() {

					/**
					 * {@link BlockingKeyGenerator} for {@link Restaurant}s,
					 * which generates a blocking key based on the city in which
					 * a restaurant is located. E.g. Los Angeles
					 * 
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public void generateBlockingKeys(Record record,
							Processable<Correspondence<Attribute, Matchable>> correspondences,
							DataIterator<Pair<String, Record>> resultCollector) {
						resultCollector.next(new Pair<>((record.getValue(Restaurant.CITY)), record));

					}
				});

		// learning Matching rule
		RuleLearner<Record, Attribute> learner = new RuleLearner<>();
		learner.learnMatchingRule(dataFodors, dataZagats, null, matchingRule, gsTraining);

		// Store Matching Rule
		matchingRule.storeModel(new File("usecase/restaurant/matchingRule/restaurantMatchingModel.model"));

		// Initialize Matching Engine
		MatchingEngine<Record, Attribute> engine = new MatchingEngine<>();

		// Execute the matching
		Processable<Correspondence<Record, Attribute>> correspondences = engine.runIdentityResolution(dataFodors,
				dataZagats, null, matchingRule, blocker);

		// write the correspondences to the output file
		new CSVCorrespondenceFormatter().writeCSV(new File("usecase/restaurant/output/restaurant_correspondences.csv"),
				correspondences);

		// load the gold standard (test set)
		MatchingGoldStandard gsTest = new MatchingGoldStandard();
		gsTest.loadFromCSVFile(new File("usecase/restaurant/goldstandard/gs_restaurant_training.csv"));

		// evaluate your result
		MatchingEvaluator<Record, Attribute> evaluator = new MatchingEvaluator<Record, Attribute>(true);
		Performance perfTest = evaluator.evaluateMatching(correspondences.get(), gsTest);

		// print the evaluation result
		System.out.println("Fodors <-> Zagats");
		System.out.println(String.format("Precision: %.4f\nRecall: %.4f\nF1: %.4f", perfTest.getPrecision(),
				perfTest.getRecall(), perfTest.getF1()));
	}
	/*
	 * public static void createDatasetToTrain() throws Exception { // loading
	 * data HashedDataSet<Movie, Attribute> dataAcademyAwards = new
	 * HashedDataSet<>(); new MovieXMLReader().loadFromXML(new
	 * File("usecase/movie/input/academy_awards.xml"), "/movies/movie",
	 * dataAcademyAwards); HashedDataSet<Movie, Attribute> dataActors = new
	 * HashedDataSet<>(); new MovieXMLReader().loadFromXML(new
	 * File("usecase/movie/input/actors.xml"), "/movies/movie", dataActors);
	 * 
	 * // load the gold standard (test set) // load the gold standard (training
	 * set) MatchingGoldStandard gsTraining = new MatchingGoldStandard();
	 * gsTraining.loadFromCSVFile(new File(
	 * "usecase/movie/goldstandard/gs_academy_awards_2_actors.csv"));
	 * 
	 * // create a matching rule LinearCombinationMatchingRule<Movie, Attribute>
	 * matchingRule = new LinearCombinationMatchingRule<>( 0.0); // add
	 * comparators matchingRule.addComparator(new
	 * MovieTitleComparatorLevenshtein(), 0.5); matchingRule.addComparator(new
	 * MovieDateComparator10Years(), 0.5);
	 * 
	 * // create the data set for learning a matching rule (use this file in //
	 * RapidMiner) RuleLearner<Movie, Attribute> learner = new RuleLearner<>();
	 * FeatureVectorDataSet features =
	 * learner.generateTrainingDataForLearning(dataAcademyAwards, dataActors,
	 * gsTraining, matchingRule, null); new RecordCSVFormatter().writeCSV( new
	 * File(
	 * "usecase/movie/output/optimisation/academy_awards_2_actors_features.csv")
	 * , features); }
	 * 
	 * public static void firstMatching() throws Exception {
	 * 
	 * // loading data HashedDataSet<Movie, Attribute> dataAcademyAwards = new
	 * HashedDataSet<>(); new MovieXMLReader().loadFromXML(new
	 * File("usecase/movie/input/academy_awards.xml"), "/movies/movie",
	 * dataAcademyAwards); HashedDataSet<Movie, Attribute> dataActors = new
	 * HashedDataSet<>(); new MovieXMLReader().loadFromXML(new
	 * File("usecase/movie/input/actors.xml"), "/movies/movie", dataActors);
	 * 
	 * // create a matching rule LinearCombinationMatchingRule<Movie, Attribute>
	 * matchingRule = new LinearCombinationMatchingRule<>( 0.0); // add
	 * comparators matchingRule.addComparator(new MovieTitleComparatorEqual(),
	 * 1); matchingRule.addComparator(new MovieDateComparator10Years(), 1); //
	 * run normalization matchingRule.normalizeWeights();
	 * 
	 * // create a blocker (blocking strategy) StandardRecordBlocker<Movie,
	 * Attribute> blocker = new StandardRecordBlocker<>( new
	 * StaticBlockingKeyGenerator<Movie, Attribute>());
	 * 
	 * // Initialize Matching Engine MatchingEngine<Movie, Attribute> engine =
	 * new MatchingEngine<>();
	 * 
	 * // Execute the matching Processable<Correspondence<Movie, Attribute>>
	 * correspondences = engine.runIdentityResolution( dataAcademyAwards,
	 * dataActors, null, matchingRule, blocker);
	 * 
	 * // write the correspondences to the output file new
	 * CSVCorrespondenceFormatter().writeCSV(new
	 * File("usecase/movie/output/academy_awards_2_actors_correspondences.csv"),
	 * correspondences);
	 * 
	 * // load the gold standard (test set) MatchingGoldStandard gsTest = new
	 * MatchingGoldStandard(); gsTest.loadFromCSVFile(new File(
	 * "usecase/movie/goldstandard/gs_academy_awards_2_actors_test.csv"));
	 * 
	 * // evaluate your result MatchingEvaluator<Movie, Attribute> evaluator =
	 * new MatchingEvaluator<Movie, Attribute>(true); Performance perfTest =
	 * evaluator.evaluateMatching(correspondences.get(), gsTest);
	 * 
	 * // print the evaluation result
	 * System.out.println("Academy Awards <-> Actors"); System.out
	 * .println(String.format( "Precision: %.4f\nRecall: %.4f\nF1: %.4f",
	 * perfTest.getPrecision(), perfTest.getRecall(), perfTest.getF1())); }
	 * 
	 * public static void runWhole() throws Exception { // define the matching
	 * rule LinearCombinationMatchingRule<Movie, Attribute> rule = new
	 * LinearCombinationMatchingRule<>( -1.497, 0.5); rule.addComparator(new
	 * MovieTitleComparatorLevenshtein(), 1.849); rule.addComparator(new
	 * MovieDateComparator10Years(), 0.822);
	 * 
	 * // create the matching engine StandardRecordBlocker<Movie, Attribute>
	 * blocker = new StandardRecordBlocker<>( new
	 * MovieBlockingKeyByYearGenerator()); MatchingEngine<Movie, Attribute>
	 * engine = new MatchingEngine<>();
	 * 
	 * // load the data sets HashedDataSet<Movie, Attribute> ds1 = new
	 * HashedDataSet<>(); HashedDataSet<Movie, Attribute> ds2 = new
	 * HashedDataSet<>(); HashedDataSet<Movie, Attribute> ds3 = new
	 * HashedDataSet<>(); new MovieXMLReader().loadFromXML(new
	 * File("usecase/movie/input/academy_awards.xml"), "/movies/movie", ds1);
	 * new MovieXMLReader().loadFromXML(new
	 * File("usecase/movie/input/actors.xml"), "/movies/movie", ds2); new
	 * MovieXMLReader().loadFromXML(new
	 * File("usecase/movie/input/golden_globes.xml"), "/movies/movie", ds3);
	 * 
	 * // run the matching Processable<Correspondence<Movie, Attribute>>
	 * correspondences = engine.runIdentityResolution(ds1, ds2, null, rule,
	 * blocker); Processable<Correspondence<Movie, Attribute>> correspondences2
	 * = engine.runIdentityResolution(ds2, ds3, null, rule, blocker);
	 * 
	 * // write the correspondences to the output file new
	 * CSVCorrespondenceFormatter().writeCSV(new
	 * File("usecase/movie/output/academy_awards_2_actors_correspondences.csv"),
	 * correspondences); new CSVCorrespondenceFormatter().writeCSV(new
	 * File("usecase/movie/output/actors_2_golden_globes_correspondences.csv"),
	 * correspondences2);
	 * 
	 * printCorrespondences(new ArrayList<>(correspondences2.get()));
	 * 
	 * // load the gold standard (training set) MatchingGoldStandard gsTraining
	 * = new MatchingGoldStandard(); gsTraining.loadFromCSVFile(new File(
	 * "usecase/movie/goldstandard/gs_academy_awards_2_actors.csv"));
	 * 
	 * // create the data set for learning a matching rule (use this file in //
	 * RapidMiner) RuleLearner<Movie, Attribute> learner = new RuleLearner<>();
	 * FeatureVectorDataSet features =
	 * learner.generateTrainingDataForLearning(ds1, ds2, gsTraining, rule,
	 * null); new RecordCSVFormatter().writeCSV(new File(
	 * "usecase/movie/output/optimisation/academy_awards_2_actors_features.csv")
	 * , features);
	 * 
	 * // load the gold standard (test set) MatchingGoldStandard gsTest = new
	 * MatchingGoldStandard(); gsTest.loadFromCSVFile(new File(
	 * "usecase/movie/goldstandard/gs_academy_awards_2_actors_test.csv"));
	 * MatchingGoldStandard gs2 = new MatchingGoldStandard();
	 * gs2.loadFromCSVFile(new File(
	 * "usecase/movie/goldstandard/gs_actors_2_golden_globes.csv"));
	 * 
	 * // evaluate the result MatchingEvaluator<Movie, Attribute> evaluator =
	 * new MatchingEvaluator<>(true); Performance perfTest =
	 * evaluator.evaluateMatching(correspondences.get(), gsTest); Performance
	 * perf2 = evaluator.evaluateMatching(correspondences2.get(), gs2);
	 * 
	 * // print the evaluation result
	 * System.out.println("Academy Awards <-> Actors"); System.out
	 * .println(String.format( "Precision: %.4f\nRecall: %.4f\nF1: %.4f",
	 * perfTest.getPrecision(), perfTest.getRecall(), perfTest.getF1()));
	 * 
	 * System.out.println("Actors <-> Golden Globes");
	 * System.out.println(String.format(
	 * "Precision: %.4f\nRecall: %.4f\nF1: %.4f", perf2.getPrecision(),
	 * perf2.getRecall(), perf2.getF1())); }
	 * 
	 * private static void printCorrespondences( List<Correspondence<Movie,
	 * Attribute>> correspondences) { // sort the correspondences
	 * Collections.sort(correspondences, new Comparator<Correspondence<Movie,
	 * Attribute>>() {
	 * 
	 * @Override public int compare(Correspondence<Movie, Attribute> o1,
	 * Correspondence<Movie, Attribute> o2) { int score =
	 * Double.compare(o1.getSimilarityScore(), o2.getSimilarityScore()); int
	 * title = o1.getFirstRecord().getTitle()
	 * .compareTo(o2.getFirstRecord().getTitle());
	 * 
	 * if (score != 0) { return -score; } else { return title; } }
	 * 
	 * });
	 * 
	 * // print the correspondences for (Correspondence<Movie, Attribute>
	 * correspondence : correspondences) { System.out.println(String
	 * .format("%s,%s,|\t\t%.2f\t[%s] %s (%s) <--> [%s] %s (%s)",
	 * correspondence.getFirstRecord().getIdentifier(),
	 * correspondence.getSecondRecord().getIdentifier(),
	 * correspondence.getSimilarityScore(),
	 * correspondence.getFirstRecord().getIdentifier(),
	 * correspondence.getFirstRecord().getTitle(),
	 * correspondence.getFirstRecord().getDate() .toString("YYYY-MM-DD"),
	 * correspondence .getSecondRecord().getIdentifier(),
	 * correspondence.getSecondRecord().getTitle(),
	 * correspondence.getSecondRecord().getDate() .toString("YYYY-MM-DD"))); } }
	 */
}