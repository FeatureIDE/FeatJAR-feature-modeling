package de.featjar.feature.model.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.io.format.ParseException;
import de.featjar.feature.model.IFeatureModel;

public class XmlFeatureModelFormatTest {

    @Test
    public void testSerialization() throws IOException, ParseException {
        // Constructing the path relative to the project root
        Path inputFilePath = Paths.get("src/test/java/testFeatureModels/basic.xml");
        
        XmlFeatureModelFormat format = new XmlFeatureModelFormat();
        Result<IFeatureModel> fm = IO.load(inputFilePath, format);
        IFeatureModel featureModel = fm.orElseThrow();
        
        // Assert that the feature model has features
        assertFalse(featureModel.getFeatures().isEmpty(), "Feature model should have features");
        
        String outputString = IO.print(featureModel, format);

        String inputString = Files.readString(inputFilePath);

        // Log the input and output strings for debugging
        System.out.println("Input XML:\n" + inputString);
        System.out.println("Output XML:\n" + outputString);

        // Normalize line endings for comparison
        inputString = inputString.replace("\r\n", "\n").trim();
        outputString = outputString.replace("\r\n", "\n").trim();

        assertEquals(inputString, outputString, "The input and output XML strings should be equal");
    }
}
