package com.example.wang.opencvdemo1;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class DetectUtility {
	final static FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
	//主要思路就是在特征点附近随机选取若干点对，将这些点对的灰度值的大小，组合成一个二进制串，并将这个二进制串作为该特征点的特征描述子
	/**
	 * 1-在图像块内平均采样；
	 * 2-p和q都符合(0,125S2)(0,125S2)的高斯分布；
	 * 3-p符合(0,1/25 S-2)(0,1/25 S-2)的高斯分布，而q符合(0,1/100 S-2)(0,1/100 S-2)的高斯分布；
	 * 4-在空间量化极坐标下的离散位置随机采样
	 * 5-把p固定为(0,0)(0,0)，qq在周围平均采样
	 */
//	final static DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.BRIEF);
	//特征检测器--- 计算特征点的特征描述子的抽象类。----创建特征描述子
	final static DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.BRISK);
	final static DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
	
	static void analyze(Mat image, MatOfKeyPoint keypoints, Mat descriptors){
		//输入图像.--检测得到的关键点--模板，指定要取关键点度的位置
		//调用detect函数检测出SURF特征关键点，保存在vector容器中
		detector.detect(image, keypoints);

		//提取特征描述子  --输入的图像.--输入的关键点，由FeatureDetector得到--计算出来的特征描述子
		extractor.compute(image, keypoints, descriptors);
	}

	static MatOfDMatch match(Mat desc1, Mat desc2){
		MatOfDMatch matches = new MatOfDMatch();
		//匹配两幅图中的描述子（descriptors）
		/**
		 * desc1--此匹配对应的查询图像的特征描述子索引
		 * desc2--此匹配对应的训练(模板)图像的特征描述子索引
		 * matches--匹配点数. 匹配点数的大小小于待查询的特征描述子的个数
		 */
		matcher.match(desc1, desc2, matches);	
		return matches;
	}
	
	static MatOfDMatch filterMatchesByDistance(MatOfDMatch matches){
		/**
		 * Dmatch
		 * 保存匹配特征的数据结构
		 * float distance
		 * 两个特征向量之间的欧氏距离，越小表明匹配度越高。
		 * int imgIdx
		 * 训练图像的索引(若有多个)
		 * int queryIdx
		 * 此匹配对应的查询图像的特征描述子索引
		 * int trainIdx
		 * 此匹配对应的训练(模板)图像的特征描述子索引
		 */
		List<DMatch> matches_original = matches.toList();
		List<DMatch> matches_filtered = new ArrayList<DMatch>();
		
		int DIST_LIMIT = 30;

		//检查所有的匹配距离,如果它添加到过滤匹配的列表
		// Check all the matches distance and if it passes add to list of filtered matches  
		Log.d("DISTFILTER", "ORG SIZE:" + matches_original.size() + "");
		for (int i = 0; i < matches_original.size(); i++) {
			DMatch d = matches_original.get(i); 
			if (Math.abs(d.distance) <= DIST_LIMIT) {
				matches_filtered.add(d);				
			}
		}
		Log.d("DISTFILTER", "FIL SIZE:" + matches_filtered.size() + "");
		
		MatOfDMatch mat = new MatOfDMatch();
		mat.fromList(matches_filtered);
		return mat;
	}
	
	static MatOfDMatch filterMatchesByHomography(MatOfKeyPoint keypoints1, MatOfKeyPoint keypoints2, MatOfDMatch matches){
		List<Point> lp1 = new ArrayList<Point>(500);
		List<Point> lp2 = new ArrayList<Point>(500);
		
		KeyPoint[] k1 = keypoints1.toArray();
		KeyPoint[] k2 = keypoints2.toArray();
		
		
		List<DMatch> matches_original = matches.toList();		

		if (matches_original.size() < 4){
			MatOfDMatch mat = new MatOfDMatch();
			return mat;
		}
		
		// Add matches keypoints to new list to apply homography
		for(DMatch match : matches_original){
			Point kk1 = k1[match.queryIdx].pt;
			Point kk2 = k2[match.trainIdx].pt;
			lp1.add(kk1);
			lp2.add(kk2);
		}	

		MatOfPoint2f srcPoints = new MatOfPoint2f(lp1.toArray(new Point[0]));
		MatOfPoint2f dstPoints  = new MatOfPoint2f(lp2.toArray(new Point[0]));
		
		Mat mask = new Mat();
		double a=0.2;
		Mat homography = Calib3d.findHomography(srcPoints, dstPoints, Calib3d.LMEDS, a);
		List<DMatch> matches_homo = new ArrayList<DMatch>();
		int size = (int) mask.size().height;
		for(int i = 0; i < size; i++){			
			if ( mask.get(i, 0)[0] == 1){
				DMatch d = matches_original.get(i);
				matches_homo.add(d);
			}
		}
		
		MatOfDMatch mat = new MatOfDMatch();
		mat.fromList(matches_homo);
		return mat;
	}
	
	static Bitmap drawMatches(Mat img1, MatOfKeyPoint key1, Mat img2, MatOfKeyPoint key2, MatOfDMatch matches, boolean imageOnly){
		Mat out = new Mat();
		Mat im1 = new Mat();
		Mat im2 = new Mat(); 
		Imgproc.cvtColor(img1, im1, Imgproc.COLOR_BGR2RGB);
		Imgproc.cvtColor(img2, im2, Imgproc.COLOR_BGR2RGB);
		if ( imageOnly){
			MatOfDMatch emptyMatch = new MatOfDMatch();
			MatOfKeyPoint emptyKey1 = new MatOfKeyPoint();
			MatOfKeyPoint emptyKey2 = new MatOfKeyPoint();
			//This function draws matches of keypoints from two images in the output image
			/**
			 *  源图像1
			 *  源图像1的特征点
			 *  源图像2.
			 *  源图像2的特征点
			 *   mat 匹配
			 *  out-输出图像
			 */
			Features2d.drawMatches(im1, emptyKey1, im2, emptyKey2, emptyMatch, out);
		} else {
			Features2d.drawMatches(im1, key1, im2, key2, matches, out);
		}
		//生成bitmap
		Bitmap bmp = Bitmap.createBitmap(out.cols(), out.rows(), Bitmap.Config.ARGB_8888);
		/**
		 * 	这个函数是用来进行颜色空间的转换
		 *OpenCV默认的图片通道是BGR。
		 * RGB <--> BGR：CV_BGR2BGRA、CV_RGB2BGRA、CV_BGRA2RGBA、CV_BGR2BGRA、CV_BGRA2BGR

		 RGB <--> 5X5：CV_BGR5652RGBA、CV_BGR2RGB555、（以此类推，不一一列举）

		 RGB <---> Gray：CV_RGB2GRAY、CV_GRAY2RGB、CV_RGBA2GRAY、CV_GRAY2RGBA

		 RGB <--> CIE XYZ：CV_BGR2XYZ、CV_RGB2XYZ、CV_XYZ2BGR、CV_XYZ2RGB

		 RGB <--> YCrCb（YUV） JPEG：CV_RGB2YCrCb、CV_RGB2YCrCb、CV_YCrCb2BGR、CV_YCrCb2RGB、CV_RGB2YUV（将YCrCb用YUV替代都可以）

		 RGB <--> HSV：CV_BGR2HSV、CV_RGB2HSV、CV_HSV2BGR、CV_HSV2RGB

		 RGB <--> HLS：CV_BGR2HLS、CV_RGB2HLS、CV_HLS2BGR、CV_HLS2RGB

		 RGB <--> CIE L*a*b*：CV_BGR2Lab、CV_RGB2Lab、CV_Lab2BGR、CV_Lab2RGB

		 RGB <--> CIE L*u*v：CV_BGR2Luv、CV_RGB2Luv、CV_Luv2BGR、CV_Luv2RGB

		 RGB <--> Bayer：CV_BayerBG2BGR、CV_BayerGB2BGR、CV_BayerRG2BGR、CV_BayerGR2BGR、CV_BayerBG2RGB、CV_BayerGB2RGB、 CV_BayerRG2RGB、CV_BayerGR2RGB（在CCD和CMOS上常用的Bayer模式）

		 YUV420 <--> RGB：CV_YUV420sp2BGR、CV_YUV420sp2RGB、CV_YUV420i2BGR、CV_YUV420i2RGB
		 */
		Imgproc.cvtColor(out, out, Imgproc.COLOR_BGR2RGB);

		//TODO
//		Core.putText(out, "FRAME", new Point(img1.width() / 2,30), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(0,255,255),3);
//		Core.putText(out, "MATCHED", new Point(img1.width() + img2.width() / 2,30), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(255,0,0),3);
		//mat---bitmap
		Utils.matToBitmap(out, bmp);
		return bmp;
	}
}
