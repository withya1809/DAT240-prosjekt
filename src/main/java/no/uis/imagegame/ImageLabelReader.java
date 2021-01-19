package no.uis.imagegame;

import java.io.*;
import java.util.*;

/**
 * Image label reader that parses image file name and label mappings files.
 */
public class ImageLabelReader {
    private HashMap<String, Integer> imageMapping;
    private HashMap<Integer, ArrayList<String>> reverseImageMapping;
    private HashMap<Integer, String> labelMapping;
    private HashMap<String, Integer> reverseLabelMapping;

    /**
     * @param labelMappingFile File path of the label mapping .csv file
     * @param imageMappingFile File path of the image path mapping .csv file
     */
    public ImageLabelReader(String labelMappingFile, String imageMappingFile) {
        this.imageMapping = new HashMap<String, Integer>();
        this.labelMapping = new HashMap<Integer, String>();
        this.reverseImageMapping = new HashMap<Integer, ArrayList<String>>();
        this.reverseLabelMapping = new HashMap<String, Integer>();

        try {
            Scanner imageScanner = new Scanner(new File(imageMappingFile));
            while (imageScanner.hasNextLine()) {
                String line = imageScanner.nextLine();
                String[] splittedLine = line.split(" ");
                int value = Integer.parseInt(splittedLine[1]);
                String key = splittedLine[0];
                this.imageMapping.put(key, value);
                if (!this.reverseImageMapping.containsKey(value)) {
                    this.reverseImageMapping.put(value, new ArrayList<String>());
                }
                this.reverseImageMapping.get(value).add(key);
            }
            imageScanner.close();
            Scanner labelScanner = new Scanner(new File(labelMappingFile));
            while (labelScanner.hasNextLine()) {
                String line = labelScanner.nextLine();
                String[] splittedLine = line.split(" ", 2);
                int key = Integer.parseInt(splittedLine[0]);
                String value = splittedLine[1];
                this.labelMapping.put(key, value);
                this.reverseLabelMapping.put(value, key);
            }
            labelScanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Given an image file get it's label
     * Note that each image has a single label
     * If the given image file is not found return null
     *
     * @param imageFileName File path of the image
     * @return Label the image file is appended to
     */
    public String getLabel(String imageFileName) {
        if (this.imageMapping.containsKey(imageFileName)) {
            int labelKey = this.imageMapping.get(imageFileName);
            String label = this.labelMapping.get(labelKey);
            return label;
        }
        return null;
    }

    /**
     * Given a label get all the image files corresponding to this label
     * Note that there could be multiple images for a given label
     * If the given label is not found return null
     *
     * @param label The given label
     * @return Array of the image files
     */
    public String[] getImageFiles(String label) {
        if (this.reverseLabelMapping.containsKey(label)) {
            int key = this.reverseLabelMapping.get(label);
            ArrayList<String> list = this.reverseImageMapping.get(key);
            String[] imageFiles = list.toArray(new String[0]);
            return imageFiles;
        }
        return null;
    }
}