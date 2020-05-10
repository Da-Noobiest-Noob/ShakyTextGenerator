//Code created by Jackson Jones 5/10/2020
//ShakyTextGenerator
//https://github.com/Da-Noobiest-Noob

import javafx.beans.property.IntegerPropertyBase;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.InputMismatchException;

public class Driver{
    private static File chosenFile;
    private int[][] originalImageArray;
    private int[][] largeImageArray;
    private final String FINAL_FILE_NAME = userInput("What is the name of your shaky font? \n(No File extensions like .png, .jpg...)",false);
    private int positionOfWidthEqualizer;
    private final Color SEMI_TRANSPARENT = new Color(255,255,255,1);
    Driver() {
        if (FINAL_FILE_NAME.equals("<Put Some User Input Here>"))
            exit();
        BufferedImage originalImage = chooseImageFile();
        int w = originalImage.getWidth();
        int h = originalImage.getHeight();
        if (w != h) {
            errorMessage("The width and height of the .png must be equal","Error");
            exit();
        }
        originalImageArray = new int[w][h];
        for( int i = 0; i < w; i++ ) {
            for (int j = 0; j < h; j++) {
                originalImageArray[i][j] = originalImage.getRGB(i, j);

            }
        }

        //Checks to make sure the BufferedImage has at least one pixel of transparency.
        if (!ensureTransparentBorder(originalImage,w,h))
            originalImageArray = addTransparentBorder(originalImageArray);

        positionOfWidthEqualizer = determineEqualizerPosition(originalImageArray);

        int[][] newImageArray = createNewImage(originalImageArray);
        BufferedImage finalImage = new BufferedImage(newImageArray.length,newImageArray[0].length,BufferedImage.TYPE_INT_ARGB);
        for( int i = 0; i < newImageArray.length; i++ ) {
            for (int j = 0; j < newImageArray[0].length; j++) {
                finalImage.setRGB(i, j, newImageArray[i][j]);
            }
        }

        File f = new File(FINAL_FILE_NAME + ".png");
        try {
            ImageIO.write(finalImage, "PNG", f);
        } catch (IOException e) {
            System.out.print(e.getMessage());
        }
        printJSONFile();
    }

    public void printJSONFile() {
        String indent = "  ";
        try {
            FileWriter w = new FileWriter(FINAL_FILE_NAME + ".json");
            w.write("{\n");
            w.write(indent + "\"providers\": [\n");
            indent = "    ";
            w.write(indent + "{\n");
            indent = "      ";
            w.write(indent + "\"type\": \"bitmap\",\n");
            String namespace = userInput("Enter the folder within minecraft:font that the .json file should be inside\n" +
                    "(Leave blank if there is none but don't hit cancel)",true);
            if (namespace.equals(""))
                w.write(indent + "\"file\": \"minecraft:font/" + FINAL_FILE_NAME + ".png\",\n");
            else
                w.write(indent + "\"file\": \"minecraft:font/" + namespace + "/" + FINAL_FILE_NAME + ".png\",\n");
            w.write(indent + "\"height\": " + largeImageArray[0].length + ",\n");
            int ascent = userInputNumber("Enter the ascent value (A number)\n" +
                    "(This cannot be greater than " + (largeImageArray[0].length-1) + ")",false);
            if (ascent > largeImageArray[0].length)
                ascent = largeImageArray[0].length-1;
            w.write(indent + "\"ascent\": " + ascent + ",\n");
            w.write(indent + "\"chars\": [\n");
            indent = "        ";
            String character = userInputChar("Enter the single character you will use in a command to display this shaky object\n" +
                    "(\\ue000-\\ue007 will be used for the other 8 characters)\n" +
                    "(If you enter more than one character, only the first will be used)",false);
            w.write(indent + "\"" + character + "\\ue000\\ue001\\ue002\\ue003\\ue004\\ue005\\ue006\\ue007\"\n");
            indent = "      ";
            w.write(indent + "]\n");
            indent = "    ";
            w.write(indent + "}\n");
            indent = "  ";
            w.write(indent + "]\n");
            w.write("}");
            w.close();
            if (namespace.equals(""))
            errorMessage("Use \"/tellraw @p {\"text\":\"" + character +
                    "\",\"font\":" + FINAL_FILE_NAME + "\",\"obfuscated\":true}","Command to show in world");
            else
                errorMessage("Use \"/tellraw @p {\"text\":\"" + character +
                        "\",\"font\":"  + namespace + "/" + FINAL_FILE_NAME + "\",\"obfuscated\":true}","Command to show in world");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    public boolean ensureTransparentBorder(BufferedImage image, int width, int height) {
        Color testColor;
        //Checks top stripe
        for (int i = 0; i < width; i++) {
            testColor = new Color(image.getRGB(i,0),true);
            if (testColor.getAlpha() > 0) {
                errorMessage(("The image border is not transparent at (" + i + ", " +
                        "0)\nA transparent border has been added for you"),"Info");
                return false;
            }
        }
        //Checks bottom stripe
        for (int i = 0; i < width; i++) {
            testColor = new Color(image.getRGB(i,height-1),true);
            if (testColor.getAlpha() > 0) {
                errorMessage(("The image border is not transparent at (" + i + ", " + (height - 1) +
                        ")\nA transparent border has been added for you"),"Info");
                return false;
            }
        }
        //Checks Left column
        for (int j = 0; j < width; j++) {
            testColor = new Color(image.getRGB(0, j),true);
            if (testColor.getAlpha() > 0) {
                errorMessage(("The image border is not transparent at (0, " + j +
                        ")\nA transparent border has been added for you"),"Info");
                return false;
            }
        }
        //Checks Right column
        for (int j = 0; j < width; j++) {
            testColor = new Color(image.getRGB(width-1, j),true);
            if (testColor.getAlpha() > 0) {
                errorMessage(("The image border is not transparent at (" + (width-1) + ", " + j +
                        ")\nA transparent border has been added for you"),"Info");
                return false;
            }
        }
        return true;
    }
    public int[][] addTransparentBorder(int[][] imageArrayArray) {
        //Creates new int[][] with an extra pixel on each side for a transparent border
        int[][] addedBorder = new int[imageArrayArray.length+2][imageArrayArray[0].length+2];
        //Sets all pixels to transparent
        for (int i = 0; i < addedBorder.length; i++) {
            for (int j = 0; j < addedBorder[0].length; j++) {
                addedBorder[i][j] = 16777215;
            }
        }
        //Sets everything inside the "transparent frame" to the same as before
        for (int i = 0; i < imageArrayArray.length; i++) {
            for (int j = 0; j < imageArrayArray[0].length; j++) {
                addedBorder[i+1][j+1] = imageArrayArray[i][j];
            }
        }
        return addedBorder;
    }


    public int[][] createNewImage(int[][] originalImageArray) {
        int width = originalImageArray[0].length;
        largeImageArray = new int[originalImageArray.length*9][width];
        offsetOneUpLeft((8*width),(9*width),largeImageArray[0].length,originalImageArray);
        offsetOneLeft((7*width),(8*width),largeImageArray[0].length,originalImageArray);
        offsetOneDownLeft((6*width),(7*width),largeImageArray[0].length,originalImageArray);
        offsetOneDown((5*width),(6*width),largeImageArray[0].length,originalImageArray);
        offsetOneDownRight((4*width),(5*width),largeImageArray[0].length,originalImageArray);
        offsetOneRight((3*width),(4*width),largeImageArray[0].length,originalImageArray);
        offsetOneUpRight((2*width),(3*width),largeImageArray[0].length,originalImageArray);
        offsetOneUp(width,(2*width),largeImageArray[0].length,originalImageArray);
        offsetNothing(0,width,largeImageArray[0].length,originalImageArray);

        return largeImageArray;
    }
    private void offsetNothing(int bound1, int bound2, int height, int[][] baseline) {
        for( int i = bound1; i < bound2; i++ )
            for (int j = 0; j < height; j++)
                largeImageArray[i][j] = originalImageArray[i][j];
        largeImageArray[bound2+positionOfWidthEqualizer][1] = SEMI_TRANSPARENT.getRGB();

    }
    private void offsetOneUp(int bound1, int bound2, int height, int[][] baseline) {
        int width = bound2 - bound1;
        for( int i = 0; i < width; i++ ) {
            for (int j = 0; j < height; j++) {
                if (j-1 >= 0)
                    largeImageArray[i + bound1][j - 1] = baseline[i][j];
            }
        }
        largeImageArray[bound2+positionOfWidthEqualizer][1] = SEMI_TRANSPARENT.getRGB();
    }
    private void offsetOneUpRight(int bound1, int bound2, int height, int[][] baseline) {
        int width = bound2 - bound1;
        for( int i = 0; i < width; i++ )
            for (int j = 0; j < height; j++)
                if (j-1 >= 0)
                    largeImageArray[i + bound1+1][j - 1] = baseline[i][j];
    }
    private void offsetOneRight(int bound1, int bound2, int height, int[][] baseline) {
        int width = bound2 - bound1;
        for( int i = 0; i < width; i++ ) {
            for (int j = 0; j < height; j++) {
                    largeImageArray[i + bound1+1][j] = baseline[i][j];
            }
        }
    }
    private void offsetOneDownRight(int bound1, int bound2, int height, int[][] baseline) {
        int width = bound2 - bound1;
        for( int i = 0; i < width; i++ )
            for (int j = 0; j < height; j++)
                if (j+1 < height)
                    largeImageArray[i + bound1+1][j + 1] = baseline[i][j];
    }
    private void offsetOneDown(int bound1, int bound2, int height, int[][] baseline) {
        int width = bound2 - bound1;
        for( int i = 0; i < width; i++ ) {
            for (int j = 0; j < height; j++) {
                if (j + 1 < height)
                    largeImageArray[i + bound1][j + 1] = baseline[i][j];
            }
        }
        largeImageArray[bound2+positionOfWidthEqualizer][1] = SEMI_TRANSPARENT.getRGB();
    }
    private void offsetOneDownLeft(int bound1, int bound2, int height, int[][] baseline) {
        int width = bound2 - bound1;
        for( int i = 0; i < width; i++ )
            for (int j = 0; j < height; j++)
                if (j+1 < height)
                    largeImageArray[i + bound1-1][j + 1] = baseline[i][j];
        largeImageArray[bound2+positionOfWidthEqualizer][1] = SEMI_TRANSPARENT.getRGB();
        largeImageArray[bound2+positionOfWidthEqualizer-1][1] = SEMI_TRANSPARENT.getRGB();
    }
    private void offsetOneLeft(int bound1, int bound2, int height, int[][] baseline) {
        int width = bound2 - bound1;
        for( int i = 0; i < width; i++ ) {
            for (int j = 0; j < height; j++) {
                largeImageArray[i + bound1 - 1][j] = baseline[i][j];
            }
        }
        largeImageArray[bound2+positionOfWidthEqualizer][1] = SEMI_TRANSPARENT.getRGB();
        largeImageArray[bound2+positionOfWidthEqualizer-1][1] = SEMI_TRANSPARENT.getRGB();
    }
    private void offsetOneUpLeft(int bound1, int bound2, int height, int[][] baseline) {
        int width = bound2 - bound1;
        for( int i = 0; i < width; i++ ) {
            for (int j = 0; j < height; j++) {
                if (j - 1 >= 0) {
                    largeImageArray[i + bound1 - 1][j - 1] = baseline[i][j];
                }
            }
        }
        largeImageArray[bound2+positionOfWidthEqualizer][1] = SEMI_TRANSPARENT.getRGB();
        largeImageArray[bound2+positionOfWidthEqualizer-1][1] = SEMI_TRANSPARENT.getRGB();
    }

    public int determineEqualizerPosition(int[][] imageArray) {
        int furthestTransparentRow = 0;
        boolean foundNonTransparent = true;
        boolean previosColumnTransparency;
        Color testColor;
        for (int i = 0; i < imageArray.length; i++) {
            previosColumnTransparency = !foundNonTransparent;
            foundNonTransparent = false;
            for (int j = 0; j < imageArray[0].length; j++) {
                testColor = new Color(imageArray[i][j],true);
                if (testColor.getAlpha() > 0)
                    foundNonTransparent = true;
            }
            if (!foundNonTransparent && !previosColumnTransparency) {
                furthestTransparentRow = i;
            }
        }
        return furthestTransparentRow - imageArray.length;
    }

    public BufferedImage chooseImageFile() {
        BufferedImage chosenImage = null;
        try {
            chooseFile();
        } catch (NullPointerException e) {
            JOptionPane.showMessageDialog(null, "Terminating Process", "Warning", JOptionPane.INFORMATION_MESSAGE);
            exit();
        }
        try {
            chosenImage = ImageIO.read(chosenFile);
        } catch (IOException e) {
            errorMessage("Invalid File - Terminating Process","Warning");
            exit();
        }
        return chosenImage;
    }

    //Uses a fileDialog to let the user choose a file.
    // Exits program if file is null
    // Exits program if file type is not png
    public void chooseFile() {
        FileDialog dialog = new FileDialog((Frame)null, "Select File to Open");
        dialog.setMode(FileDialog.LOAD);
        dialog.setVisible(true);
        String fileName = dialog.getDirectory() + dialog.getFile();
        if (fileName.equals("nullnull"))
            exit();
        if (!fileName.substring(fileName.length()-4).equals(".png")) {
            System.out.println("This is not a .png and will not work");
            exit();
        }
        chosenFile = new File(fileName);
        dialog.dispose();
    }

    //Exits the program
    private void exit() {
        System.exit(-1);
    }

    //Displays an error message.
    //Inputs message as what is shown to be the error
    //Inputs title as the writing on the box
    private void errorMessage(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    private String userInput(String message, boolean acceptNothing) {
        String input = JOptionPane.showInputDialog(null,message);
        if (input == null)
            input = "<Put Some User Input Here>";
        if (input.equals("") && !acceptNothing)
            input = "file_name";
        while (input.contains(" ")) {
            input = input.replace(" ","_");
        }
        input = input.toLowerCase();
        return input;
    }
    private int userInputNumber(String message, boolean acceptNothing) {
        String input = JOptionPane.showInputDialog(null,message);
        if (input == null)
            input = "1";
        if (input.equals("") && !acceptNothing)
            input = "1";
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return userInputNumber(message,false);
        }
    }
    private String userInputChar(String message, boolean acceptNothing) {
        String input = JOptionPane.showInputDialog(null,message);
        if (input == null)
            input = "a";
        if (input.equals("") && !acceptNothing)
            input = "a";
        if (input.length() >= 2 && input.substring(0,2).equals("\\u"))
            return input.substring(0,6);
        else
            return input.substring(0,1);
    }

    public static void main(String[] args) {
        Driver run = new Driver();
    }
}
