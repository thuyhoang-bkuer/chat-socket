package com.client.util;

import com.client.chatwindow.Listener;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

public class ImageUtil {
    public static Image decodedImage;
    public static double width, height;

    public ImageUtil() {
        decodedImage = null;
        width = height = -1;
    }

    public static void imageEncode(String filePath) {
        byte base64Image[];
        File file = new File(filePath);
        try (FileInputStream imageInFile = new FileInputStream(file)) {
            // Reading a Image file from file system
            byte imageData[] = new byte[(int) file.length()];
            imageInFile.read(imageData);
            BufferedImage imageBuffer = ImageIO.read(new FileInputStream(file));
            base64Image = Base64.getEncoder().encode(imageData);

            Listener.sendPicture(base64Image);
        } catch (FileNotFoundException e) {
            System.out.println("Image not found" + e);
        } catch (IOException ioe) {
            System.out.println("Exception while reading the Image " + ioe);
        }

    }

    private static void scale(double tW, double tH) {
       width = 128.0;
       height = tH * width / tW;
    }


    public static void imageDecode(byte[] base64Image) {
        Runnable runable = new Runnable() {
            @Override
            public void run() {
                byte[] imageByteArray = Base64.getDecoder().decode(base64Image);
                ByteArrayInputStream bis = new ByteArrayInputStream(imageByteArray);
                decodedImage = new Image(bis);
                try {
                    BufferedImage imageBuffer = ImageIO.read(new ByteArrayInputStream(imageByteArray));
                    scale(imageBuffer.getWidth(), imageBuffer.getHeight());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        // Converting a Base64 String into Image byte array
        Thread thread = new Thread(runable);
        thread.start();

        synchronized (thread) {
            try {
                thread.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
