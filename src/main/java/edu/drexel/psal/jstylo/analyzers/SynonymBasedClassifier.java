package edu.drexel.psal.jstylo.analyzers;

import com.jgaap.generics.Document;
import edu.drexel.psal.jstylo.generics.Analyzer;
import java.util.*;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class SynonymBasedClassifier extends Analyzer {

	@Override
	public Map<String, Map<String, Double>> classify(Instances trainingSet, Instances testSet,
			List<Document> unknownDocs) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Evaluation runCrossValidation(Instances data, int folds, long randSeed) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Evaluation runCrossValidation(Instances data, int folds, long randSeed, int relaxFactor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] optionsDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String analyzerDescription() {
		// TODO Auto-generated method stub
		return null;
	}
}
