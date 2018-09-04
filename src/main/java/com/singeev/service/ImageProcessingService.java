package com.singeev.service;

import com.singeev.entity.DetectedFace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.java2d.SunGraphics2D;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class ImageProcessingService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ImageProcessingService.class);


    public BufferedImage convertFileToImage(java.io.File photo) {
        BufferedImage img;
        try {
            img = ImageIO.read(photo);
        } catch (IOException e) {
            LOGGER.error("Can't convert photo from Telegram to java image: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        int scaleX = img.getWidth();
        int scaleY = img.getHeight();
        Image image = img.getScaledInstance(scaleX, scaleY, Image.SCALE_SMOOTH);
        BufferedImage buffered = new BufferedImage(scaleX, scaleY, TYPE_INT_RGB);
        buffered.getGraphics().drawImage(image, 0, 0 , null);
        return buffered;
    }

    public void markFacesOnImageAndSaveIt(BufferedImage bufferedImage, List<DetectedFace> faces, String tempFileName){
        Graphics2D graphics2D = bufferedImage.createGraphics();
        float lineWidth = ((SunGraphics2D) graphics2D).getCompClip().getHiX() * 0.01f;
        for(DetectedFace face: faces) {
            DetectedFace.FaceRectangle rectangle = face.getFaceRectangle();
            graphics2D.setColor(Color.GREEN);
            graphics2D.setStroke(new BasicStroke(lineWidth));
            graphics2D.drawRect(rectangle.getLeft(), rectangle.getTop(), rectangle.getWidth(), rectangle.getHeight());
        }
        graphics2D.dispose();

        java.io.File file = new java.io.File(tempFileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            ImageIO.write(bufferedImage, "jpg", file);
            LOGGER.info("Faces marked on the image. Image saved to temp file on disk: {}", tempFileName);
        } catch (IOException e) {
            LOGGER.error("Can't save temp file to disk: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
