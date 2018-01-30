/**
 * This file is part of CERMINE project.
 * Copyright (c) 2011-2018 ICM-UW
 *
 * CERMINE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CERMINE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with CERMINE. If not, see <http://www.gnu.org/licenses/>.
 */
package pl.edu.icm.cermine.libsvm.training;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import libsvm.svm_parameter;
import org.apache.commons.cli.*;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.structure.SVMInitialZoneClassifier;
import pl.edu.icm.cermine.structure.model.BxPage;
import pl.edu.icm.cermine.structure.model.BxZone;
import pl.edu.icm.cermine.structure.model.BxZoneLabel;
import pl.edu.icm.cermine.tools.BxDocUtils.DocumentsIterator;
import pl.edu.icm.cermine.tools.classification.general.BxDocsToTrainingSamplesConverter;
import pl.edu.icm.cermine.tools.classification.general.FeatureVectorBuilder;
import pl.edu.icm.cermine.tools.classification.general.PenaltyCalculator;
import pl.edu.icm.cermine.tools.classification.general.TrainingSample;
import pl.edu.icm.cermine.tools.classification.svm.SVMZoneClassifier;

/**
 * @author Dominika Tkaczyk (d.tkaczyk@icm.edu.pl)
 */
public class SVMInitialBuilder {

    protected static SVMZoneClassifier getZoneClassifier(List<TrainingSample<BxZoneLabel>> trainingSamples,
            int kernelType, double gamma, double C, int degree) throws IOException {
        PenaltyCalculator pc = new PenaltyCalculator(trainingSamples);
        int[] intClasses = new int[pc.getClasses().size()];
        double[] classesWeights = new double[pc.getClasses().size()];

        int labelIdx = 0;
        for (BxZoneLabel label : pc.getClasses()) {
            intClasses[labelIdx] = label.ordinal();
            classesWeights[labelIdx] = pc.getPenaltyWeigth(label);
            ++labelIdx;
        }

        FeatureVectorBuilder<BxZone, BxPage> featureVectorBuilder = SVMInitialZoneClassifier.getFeatureVectorBuilder();
        SVMZoneClassifier zoneClassifier = new SVMZoneClassifier(featureVectorBuilder);
        svm_parameter param = SVMZoneClassifier.getDefaultParam();
        param.svm_type = svm_parameter.C_SVC;
        param.gamma = gamma;
        param.C = C;
        param.degree = degree;
        param.kernel_type = kernelType;
        param.weight_label = intClasses;
        param.weight = classesWeights;

        zoneClassifier.setParameter(param);
        zoneClassifier.buildClassifier(trainingSamples);
        zoneClassifier.printWeigths(featureVectorBuilder);
        return zoneClassifier;
    }

    public static void main(String[] args) throws ParseException, AnalysisException, IOException, CloneNotSupportedException {
        Options options = new Options();
        options.addOption("input", true, "input path");
        options.addOption("output", true, "output model path");
        options.addOption("kernel", true, "kernel type");
        options.addOption("g", true, "gamma");
        options.addOption("C", true, "C");
        options.addOption("degree", true, "degree");
        options.addOption("cross", false, "");
        options.addOption("ext", true, "degree");

        CommandLineParser parser = new DefaultParser();
        CommandLine line = parser.parse(options, args);
        if (!line.hasOption("input") || !line.hasOption("output")) {
            System.err.println("Usage: SVMInitialBuilder [-kernel <kernel type>] [-d <degree>] [-g <gamma>] [-C <error cost>] [-ext <extension>] -input <input dir> -output <path>");
            System.exit(1);
        }
        Double C = 16.;
        if (line.hasOption("C")) {
            C = Double.parseDouble(line.getOptionValue("C"));
        }
        Double gamma = 1.;
        if (line.hasOption("g")) {
            gamma = Double.parseDouble(line.getOptionValue("g"));
        }
        String inDir = line.getOptionValue("input");
        String outFile = line.getOptionValue("output");
        String degreeStr = line.getOptionValue("degree");
        Integer degree = -1;
        if (degreeStr != null && !degreeStr.isEmpty()) {
            degree = Integer.parseInt(degreeStr);
        }
        Integer kernelType = svm_parameter.RBF;
        if (line.hasOption("kernel")) {
            switch (Integer.parseInt(line.getOptionValue("kernel"))) {
                case 0:
                    kernelType = svm_parameter.LINEAR;
                    break;
                case 1:
                    kernelType = svm_parameter.POLY;
                    break;
                case 2:
                    kernelType = svm_parameter.RBF;
                    break;
                case 3:
                    kernelType = svm_parameter.SIGMOID;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid kernel value provided");
            }
        }
        if (kernelType == svm_parameter.POLY && degree == -1) {
            System.err.println("Polynomial kernel requires the -degree option to be specified");
            System.exit(1);
        }

        String ext = "cermstr";
        if (line.hasOption("ext")) {
            ext = line.getOptionValue("ext");
        }

        if (!line.hasOption("cross")) {
            File input = new File(inDir);
            if (input.isDirectory()) {
                DocumentsIterator it = new DocumentsIterator(inDir, ext);
                FeatureVectorBuilder<BxZone, BxPage> featureVectorBuilder = SVMInitialZoneClassifier.getFeatureVectorBuilder();
                List<TrainingSample<BxZoneLabel>> trainingSamples
                        = BxDocsToTrainingSamplesConverter.getZoneTrainingSamples(it.iterator(), featureVectorBuilder,
                                BxZoneLabel.getLabelToGeneralMap());
                SVMZoneClassifier classifier = getZoneClassifier(trainingSamples, kernelType, gamma, C, degree);
                classifier.saveModel(outFile);
            } else {
                List<TrainingSample<BxZoneLabel>> trainingSamples = SVMZoneClassifier.loadProblem(inDir, SVMInitialZoneClassifier.getFeatureVectorBuilder());
                for (TrainingSample<BxZoneLabel> sample : trainingSamples) {
                    sample.setLabel(sample.getLabel().getGeneralLabel());
                }
                SVMZoneClassifier classifier = getZoneClassifier(trainingSamples, kernelType, gamma, C, degree);
                classifier.saveModel(outFile);
            }

        } else {
            int foldness = 5;
            List<TrainingSample<BxZoneLabel>>[] trainingSamplesSet = new List[foldness];

            for (int i = 0; i < foldness; i++) {

                File input = new File(inDir + "/" + i);
                List<TrainingSample<BxZoneLabel>> trainingSamples;
                if (input.isDirectory()) {
                    DocumentsIterator it = new DocumentsIterator(inDir + "/" + i, ext);

                    FeatureVectorBuilder<BxZone, BxPage> featureVectorBuilder = SVMInitialZoneClassifier.getFeatureVectorBuilder();
                    trainingSamples = BxDocsToTrainingSamplesConverter.getZoneTrainingSamples(it.iterator(), featureVectorBuilder,
                            BxZoneLabel.getLabelToGeneralMap());
                } else {
                    trainingSamples = SVMZoneClassifier.loadProblem(inDir + "/" + i, SVMInitialZoneClassifier.getFeatureVectorBuilder());
                }

                trainingSamplesSet[i] = trainingSamples;
            }

            for (int i = 0; i < foldness; i++) {
                List<TrainingSample<BxZoneLabel>> trainingSamples = new ArrayList<TrainingSample<BxZoneLabel>>();
                for (int j = 0; j < foldness; j++) {
                    if (i != j) {
                        trainingSamples.addAll(trainingSamplesSet[j]);
                    }
                }

                SVMZoneClassifier classifier = getZoneClassifier(trainingSamples, kernelType, gamma, C, degree);
                classifier.saveModel(outFile + "-" + i);
            }
        }
    }

}
