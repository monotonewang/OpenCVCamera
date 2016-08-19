package com.example.wang.opencvdemo1;

import android.graphics.Bitmap;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;

public class Scene {

	final Mat image;
	final Mat descriptors = new Mat();
	final MatOfKeyPoint keypoints = new MatOfKeyPoint();//矩阵的特征点
	boolean firstTime = true;

	public Scene(Mat image) {
		this.image = image.clone();
		// DetectUtility.analyze(image, keypoints, descriptors);
	}

	public void preCompute() {
		if (firstTime) {
			DetectUtility.analyze(image, keypoints, descriptors);
			firstTime = false;
		}
	}

	/**
	 *
	 * @param frame
	 * @param isHomogrpahy 是否是单位矩阵
	 * @param imageOnly
     * @return
     */
	public SceneDetectData compare(Scene frame, boolean isHomogrpahy, boolean imageOnly) {
		// Info to store analysis stats 存储分析的状况
		SceneDetectData s = new SceneDetectData();

		// Detect key points and compute descriptors for inputFrame 检查主要的特征点比对框架
		MatOfKeyPoint f_keypoints = frame.keypoints;
		Mat f_descriptors = frame.descriptors;

		this.preCompute();
		frame.preCompute();

		// Compute matches 比对特征因子
		MatOfDMatch matches = DetectUtility.match(descriptors, f_descriptors);

		// Filter matches by distance

		MatOfDMatch filtered = DetectUtility.filterMatchesByDistance(matches);

		// If count of matches is OK, apply homography check
		s.original_key1 = (int) descriptors.size().height;
		s.original_key2 = (int) f_descriptors.size().height;

		s.original_matches = (int) matches.size().height;
		s.dist_matches = (int) filtered.size().height;

		if (isHomogrpahy) {
			MatOfDMatch homo = DetectUtility.filterMatchesByHomography(
					keypoints, f_keypoints, filtered);
			Bitmap bmp = DetectUtility.drawMatches(image, keypoints,
					frame.image, f_keypoints, homo, imageOnly);
			s.bmp = bmp;
			s.homo_matches = (int) homo.size().height;
			return s;
		} else {
			Bitmap bmp = DetectUtility.drawMatches(image, keypoints,
					frame.image, f_keypoints, filtered, imageOnly);
			s.bmp = bmp;
			s.homo_matches = -1;
			return s;
		}
	}
}
