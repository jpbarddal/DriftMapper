import com.opencsv.CSVWriter;
import main.experiments.ClassDistance;
import main.experiments.ConditionedCovariateDistance;
import main.experiments.CovariateDistance;
import main.experiments.PosteriorDistance;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ClassAssigner;
import weka.filters.unsupervised.attribute.Discretize;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by LoongKuan on 28/08/2016.
 **/

public class MainTest {
    public static void main(String[] args) {
        String[] standardFiles = new String[]{"airlines", "elecNormNew", "sensor"};
        //args = new String[]{"standardAll"};
        //args = new String[]{"all", "20130622", "20131129"};
        args = new String[]{"prior", "20130622", "20131129"};
        if (args[0].equals("prior")) {
            Instances[] dataSets = loadPairData(args[1], args[2]);
            for (int i = 1; i < 2; i++) {
                CovariateDistance experiment = new CovariateDistance(dataSets[0], dataSets[1], i);
                writeToCSV(experiment.getResultTable(),
                        new String[]{"Distance", "mean", "sd", "max_val", "max_att", "min_val", "min_att", "Attributes"},
                        "./data_out/martvard/" + args[1] + "_" + args[2]+ "_" + i + "-ple_prior.csv");
            }
        }
        else if (args[0].equals("all")) {
            Instances[] dataSets = loadPairData(args[1], args[2]);
            testAll(new int[]{1, 5}, dataSets, args[1] + "_" + args[2]);
        }
        else if (args[0].equals("standardAll")) {
            for (String file : standardFiles) {
                Instances allInstances = loadAnyDataSet("./datasets/"+ file +".arff");
                Instances[] dataSet = new Instances[2];
                dataSet[0] = new Instances(allInstances, 0, allInstances.size()/2);
                dataSet[1] = new Instances(allInstances, allInstances.size()/2 - 1, allInstances.size()/2);
                testAll(new int[]{1, 5}, dataSet, file);
            }
        }
    }

    private static void testAll(int[] nInterval, Instances[] dataSets, String name) {
        System.out.println("Running Tests on " + name);
        System.out.println("For " + nInterval[0] + " to " + nInterval[1] + " attributes");

        System.out.println("Running Covariate");
        for (int i = nInterval[0]; i < nInterval[1]; i++) {
            CovariateDistance experiment = new CovariateDistance(dataSets[0], dataSets[1], i);
            writeToCSV(experiment.getResultTable(),
                    new String[]{"Distance", "mean", "sd", "max_val", "max_att", "min_val", "min_att", "attributes"},
                    "./data_out/martvard/" + name + "_" + i + "-attributes_prior.csv");
        }
        System.out.println("Running ConditionedCovariate");
        for (int i = nInterval[0]; i < nInterval[1]; i++) {
            ConditionedCovariateDistance experiment = new ConditionedCovariateDistance(dataSets[0], dataSets[1], i);
            writeToCSV(experiment.getResultTable(),
                    new String[]{"Distance", "mean", "sd", "max_val", "max_att", "min_val", "min_att", "attributes", "class"},
                    "./data_out/martvard/" + name + "_" + i + "-attributes_ConditionedCovariate.csv");
        }
        System.out.println("Running Posterior");
        for (int i = nInterval[0]; i < nInterval[1]; i++) {
            PosteriorDistance experiment = new PosteriorDistance(dataSets[0], dataSets[1], i);
            writeToCSV(experiment.getResultTable(),
                    new String[]{"Distance", "mean", "sd", "max_val", "max_att", "min_val", "min_att", "attributes"},
                    "./data_out/martvard/" + name + "_" + i + "-attributes_posterior.csv");
        }
        System.out.println("Running Class");
        ClassDistance experiment = new ClassDistance(dataSets[0], dataSets[1], 0);
        writeToCSV(experiment.getResultTable(),
                new String[]{"Distance", "mean", "sd", "max_val", "max_att", "min_val", "min_att", "attributes"},
                "./data_out/martvard/" + name + "_class.csv");
    }

    private static Instances[] loadPairData(String filename1, String filename2) {
        try {
            // Load data sets into collated data set and discretize
            Instances instances1 = loadDataSet("./datasets/train_seed/"+filename1+".arff");
            Instances instances2 = loadDataSet("./datasets/train_seed/"+filename2+".arff");
            Instances instances3 = new Instances(instances1);
            instances3.addAll(instances2);
            instances3.deleteAttributeAt(0);
            instances3 = discretizeDataSet(instances3);
            instances1 = new Instances(instances3, 0, instances1.size());
            instances2 = new Instances(instances3, instances1.size(), instances2.size());
            return new Instances[]{instances1, instances2};
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return new Instances[2];
    }

    private static void writeToCSV(String[][] data, String[] header, String filename){
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(filename), ',');
            // feed in your array (or convert your data to an array)
            writer.writeNext(header);
            for (String[] dataLine : data) {
                writer.writeNext(dataLine);
            }
            writer.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static Instances loadDataSet(String filename) throws IOException {
        // Check if any attribute is numeric
        Instances result;
        BufferedReader reader;

        reader = new BufferedReader(new FileReader(filename));
        result = new Instances(reader);
        result.setClassIndex(result.numAttributes() - 1);
        reader.close();
        return result;
    }

    private static Instances discretizeDataSet(Instances dataSet) throws Exception{
        ArrayList<Integer> continuousIndex = new ArrayList<>();
        for (int i = 0; i < dataSet.numAttributes(); i++) {
            if (dataSet.attribute(i).isNumeric()) continuousIndex.add(i);
        }
        int[] attIndex = new int[continuousIndex.size()];
        for (int i = 0; i < continuousIndex.size(); i++) {
            attIndex[i] = continuousIndex.get(i);
        }

        Discretize filter = new Discretize();
        filter.setUseEqualFrequency(true);
        filter.setBins(5);
        filter.setAttributeIndicesArray(attIndex);
        filter.setInputFormat(dataSet);

        return Filter.useFilter(dataSet, filter);
    }

    private static Instances loadAnyDataSet(String filename) {
        try {
            Instances continuousData = loadDataSet(filename);
            if (filename.equals("./datasets/gas-sensor.arff")) {
                double[] classAttVals = continuousData.attributeToDoubleArray(0);
                Attribute classAtt = continuousData.attribute(0);
                continuousData.deleteAttributeAt(0);
                continuousData.insertAttributeAt(classAtt, continuousData.numAttributes());
                continuousData.setClassIndex(continuousData.numAttributes() - 1);
                for (int i = 0; i < classAttVals.length; i++) {
                    continuousData.get(i).setValue(continuousData.classIndex(), classAttVals[i]);
                }
            }
            return discretizeDataSet(continuousData);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return new Instances("E", new ArrayList<Attribute>(), 0);
        }
    }
}
