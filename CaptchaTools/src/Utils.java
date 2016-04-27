import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.util.*;

import org.bytedeco.javacpp.BytePointer;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class Utils
{
	public static String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
	
	private static int npmax(List<Integer> list, List<Pair> idx)
	{
		int max = list.get(idx.get(idx.size()-1).idx);
		
		for(int i = 0; i < idx.size()-1; i++)
		{
			max = Math.max(max, list.get(idx.get(i).idx));
		}
		
		return max;
	}
	
	private static int npmin(List<Integer> list, List<Pair> idx)
	{
		int min = list.get(idx.get(idx.size()-1).idx);
		
		for(int i = 0; i < idx.size()-1; i++)
		{
			min = Math.min(min, list.get(idx.get(i).idx));
		}
		
		return min;
	}
	
	//unfinished
	public static List<Box> nms(List<Box> boxes, double threshhold)
	{
		System.out.println(boxes.size());
		ArrayList<Integer> pick = new ArrayList<Integer>();
		LinkedList<Integer> x1 = new LinkedList<Integer>();
		LinkedList<Integer> y1 = new LinkedList<Integer>();
		LinkedList<Integer> x2 = new LinkedList<Integer>();
		LinkedList<Integer> y2 = new LinkedList<Integer>();
		
		for(Box b: boxes)
		{
			x1.add(b.x1);
			x2.add(b.x2);
			y1.add(b.y1);
			y2.add(b.y2);
		}
		
		ArrayList<Integer> areas = new ArrayList<Integer>();
		ArrayList<Pair> idx = new ArrayList<Pair>();
		for (int i = 0; i < boxes.size(); i++)
		{
			int area = (x2.get(i) - x1.get(i) + 1) * (y2.get(i) - y1.get(i) + 1);
			areas.add(area);
			idx.add(new Pair(y2.get(i), i));
		}
		
		Collections.sort(idx);

		while(idx.size() > 0)
		{
			System.out.println(idx.size());
			int last = idx.size() - 1;
			int i = idx.get(last).idx;
			pick.add(i);
			ArrayList<Integer> suppress = new ArrayList<Integer>();
			suppress.add(last);
			
			for(int k = 0; k < last; k++)
			{
				int j = idx.get(k).idx;
				
				int xx1 = Math.max(x1.get(i), x1.get(j));
				int yy1 = Math.max(y1.get(i), y1.get(j));
				int xx2 = Math.min(x2.get(i), x2.get(j));
				int yy2 = Math.min(y2.get(i), y2.get(j));
				
				int w = Math.max(0, xx2 - xx1 + 1);
				int h = Math.max(0, yy2 - yy1 + 1);
				double overlap = 1.0 * (w * h) / areas.get(j);
//				System.out.println(overlap);
				if(overlap > threshhold)
					suppress.add(k);
			}
			Collections.sort(suppress);

			for(int h = 0; h < suppress.size(); h++)
			{
				System.out.printf("%d %d %d\n", idx.size(), suppress.size()-h+1, suppress.get(suppress.size()-h-1));
				System.out.println(idx.remove((int) (suppress.get(suppress.size()-h-1))));
			}
			
//			int i = idx.get(idx.size() - 1).idx;
//			pick.add(i);
//			
//			System.out.println(idx.size());
//			int xx1 = Collections.max(x1);
//			int yy1 = Collections.max(y1);
//			int xx2 = Collections.min(x2);
//			int yy2 = Collections.min(y2);
//			
//			int w = Math.max(0,  xx2 - xx1 + 1);
//			int h = Math.max(0,  yy2 - yy1 + 1);
//			
//			ArrayList<Double> overlap = new ArrayList<Double>();
//			for(int j = 0; j < idx.size()-1; j++)
//			{
//				overlap.add(1.0 * (w*h) / areas.get(idx.get(j).idx));
//				System.out.printf("%d %d\n", w*h, areas.get(idx.get(j).idx));
//			}
//			
//			ArrayList<Pair> filtered = new ArrayList<Pair>();
//			for(int j = 0; j < overlap.size(); j++)
//			{
//				System.out.printf("overlap: %f\n", overlap.get(j));
//				if(overlap.get(j) > threshhold)
//					filtered.add(idx.get(j));
//			}
//			
//			idx = filtered;
		}
//		System.out.println(pick.size());
//		ArrayList<Box> finalBoxes = new ArrayList<Box>();
//		for(int i: pick)
//		{
//			finalBoxes.add(boxes.get(i));
//		}
//		
//		return finalBoxes;
		System.out.println(idx.size());
		ArrayList<Box> finalBoxes = new ArrayList<Box>();
		for(Pair p: idx)
			finalBoxes.add(boxes.get(p.idx));
		return finalBoxes;
	}
	
	public static List<Box> slidingWindow(Mat img, CNN net)
	{
		int dx = 6;
		int dy = 6;
		int boxWidth = 40;
		int boxHeight = 40;
		
		Hashtable<Integer, Double> counts = new Hashtable<Integer, Double>();
		for (int i = 0; i < alphabet.length(); i++)
		{
			counts.put(i, 0.0);
		}
		
		
		// must do img pyramid

		// for each location
		int counter = 0;
		ArrayList<Box> boxes = new ArrayList<Box>();
		Mat haar = Mat.zeros(boxWidth, boxHeight, img.type());
		for(int i = 10; i <= 30; i++)
		{
			for(int j = 10; j <= 30; j++)
			{
				haar.put(i, j, new double[]{1});
			}
		}
		
		for (int i = 0; i + boxWidth < img.size().width; i += dx)
		{
			for (int j = 0; j + boxHeight < img.size().height; j += dy)
			{
				// extract subregion
				Mat sub = new Mat();
				Rect roi = new Rect(i, j, boxWidth, boxHeight);
				
				sub = img.submat(roi);
				
				Mat and = new Mat();
				Core.bitwise_and(sub, haar, and);
				
				int sum = (int) Core.sumElems(and).val[0];
				System.out.println(sum);
				if(sum > 100)
				{
					boxes.add(new Box(i, j, i+boxWidth, j+boxHeight));
					Core.rectangle(img, new Point(i, j), new Point(i+boxWidth, j+boxHeight), new Scalar(100,100,100,1));
					
					Imshow im = new Imshow("");
//					im.showImage(sub);
					System.out.println(sum / 255);
					counter++;
					net.forward(convertOpenCVToJavaCV(toC3(sub)));
//					System.out.println(Arrays.toString(net.getTop(3)));
					Pair[] list = net.getTop(alphabet.length());
					for(Pair p: list)
					{
						counts.put(p.idx, counts.get(p.idx) + p.val);
					}
				}
			}
		}
		
		System.out.println(counter);
		Pair[] list = new Pair[alphabet.length()];
		
		for (int i = 0; i < alphabet.length(); i++)
		{
//			System.out.printf("%c\t%f\n", alphabet.charAt(i), counts.get(i));
			list[i] = new Pair(counts.get(i), i);	
		}
		Arrays.sort(list);
		
		for (int i = 0; i < list.length; i++)
		{
			System.out.println(list[list.length-1-i]);
		}
		
		return boxes;
	}
	
	public static Mat toC3(Mat img)
	{
		ArrayList<Mat> list = new ArrayList<Mat>();
		list.add(img);
		list.add(img);
		list.add(img);
		Mat m = new Mat();
		Core.merge(list, m);

		return m;
	}
	
	public static org.bytedeco.javacpp.opencv_core.Mat convertOpenCVToJavaCV(Mat m)
	{
		org.bytedeco.javacpp.opencv_core.Mat tmp = new org.bytedeco.javacpp.opencv_core.Mat(new BytePointer()) 
			{{ address = m.getNativeObjAddr(); }};
		
		return tmp;
	}
	
	//WARNING: change number of channels depending on image!!!!!!!!
	public static Mat bufferedImageToMat(BufferedImage bi)
	{
		Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC1);
		byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
		mat.put(0, 0, data);
		return mat;
	}

	//create single warped letters for OCR
	public static void letterBatch(String filename) throws IOException
	{
		//lots of parameters to change!
		PrintWriter output = new PrintWriter(new File(filename));
		int counter = 0;
		
		for(int i = 0; i < Utils.alphabet.length(); i++)
		{
			String letter = Utils.alphabet.charAt(i) + "";
			
			for (int amplitude = 0; amplitude < 30; amplitude += 6)
			{
				for (int period = 130; period < 250; period += 25)
				{
					for (int shift = 0; shift < period; shift += 25)
					{
						output.println(i);
						
						Captcha c = new Captcha(40, 40);
						c.drawLetter(letter, 20, 20, 2, 0, 2, 0);
						c.rippleCentered(amplitude, period, shift);
						
						if(Math.random() < 0.5)
						{
//							c.addNoise(300);
							c.dilate();
						}
						counter++;
						c.saveImg(String.format("images/image%06d.png", counter));
					}
				}
			}
			
			System.out.println("Finished letter: " + letter);
		}
		System.out.println("Total images made: " + counter);
		output.close();
		System.out.println(counter);
	}
	
	// creates n captchas and stores labels in filename, based on ripples 
	public static void captchaBatch(int n) throws IOException
	{
		int numImgs = n;
		int width = 240; //220
		int height = 110; //100
		Random rdm = new Random();
		
		PrintWriter outputWave = new PrintWriter("wave_labels.txt");
		PrintWriter outputNum = new PrintWriter("num_letters_labels.txt");
		PrintWriter outputPositions = new PrintWriter("position_labels.txt");
		
		int counter = 0;
		for (int i = 0; i < numImgs; i++)
		{
			if(i % 100 == 0)
				System.out.println(i);
			counter++;
			Captcha c = new Captcha(height, width);

			// determine parameters
			int numLetters = 3 + rdm.nextInt(6);

			int x = rdm.nextInt(width / 5) + 5;
			int y = rdm.nextInt((int) (height - 80)) + 60; //want max 90

			double spacing = rdm.nextDouble() / 5 + 0.7;
			int scale = rdm.nextInt(2) + 2;
			int thickness = rdm.nextInt(4) + 2;

			int amplitude = rdm.nextInt(30);
			int period = rdm.nextInt(120) + 130;
			int shift = rdm.nextInt(period);

			double letterSigma = rdm.nextDouble() / 1.6 + 0.4;
			double globalSigma = rdm.nextDouble() + 0.7;

			String text = "";
			for (int j = 0; j < numLetters; j++)
			{
				int letterIndex = rdm.nextInt(alphabet.length());
				text += alphabet.charAt(letterIndex);
			}

			c.drawText(text, x, y, scale, thickness, spacing, letterSigma);
			c.globalBlur(globalSigma);

			// insert noise
			int numPoints = rdm.nextInt(1200) + 300;
			int numLines = rdm.nextInt(5);
			c.addNoise(numPoints);
			for (int j = 0; j < numLines; j++)
			{
				int x1 = rdm.nextInt(width);
				int y1 = rdm.nextInt(height);
				int x2 = rdm.nextInt(width);
				int y2 = rdm.nextInt(height);

				c.drawLine(x1, y1, x2, y2);
			}

			c.ripple(amplitude, period, shift);

			// c.showImg();
			c.saveImg("images/test_captchas/" + String.format("image%06d.png", counter));
			outputWave.printf("%d %d %d\n", amplitude, period, shift);
			outputNum.printf("%d\n", numLetters);
			outputPositions.printf("%d %d\n", x, y);
		}

		outputWave.close();
		outputNum.close();
		outputPositions.close();
	}
}

class Box
{
	int x1, y1, x2, y2;

	public Box(int x1, int y1, int x2, int y2)
	{
		super();
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
	
}
