package com.edu.servicesImpl;

import java.io.File;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import com.edu.services.ImageHashService;

@Service
public class ImageHashServiceImpl implements ImageHashService {

    static {
        nu.pattern.OpenCV.loadLocally();
    }

    @Override
    public String computePerceptualHash(File file) {
        Mat image = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
        Imgproc.resize(image, image, new Size(32, 32));

        double avg = Core.mean(image).val[0];

        StringBuilder hash = new StringBuilder();
        for (int i = 0; i < image.rows(); i++) {
            for (int j = 0; j < image.cols(); j++) {
                hash.append(image.get(i, j)[0] > avg ? "1" : "0");
            }
        }
        return hash.toString();
    }
}
