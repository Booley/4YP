import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.util.*;

import org.bytedeco.javacpp.BytePointer;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

public class Utils
{
	public static String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
	
	//unfinished
	public static void nms(ArrayList<Box> boxes, double threshhold)
	{
		ArrayList<Integer> picked = new ArrayList<Integer>();
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
			idx.add(new Pair(y2.get(i), i));
		}
		
		Collections.sort(idx);
		
		while(idx.size() > 0)
		{
//			int last = idx.get(idx.size()-1);
			
		}
		
	}
	
	public static void slidingWindow(Mat img, CNN net)
	{
		int dx = 4;
		int dy = 4;
		int boxWidth = 50;
		int boxHeight = 50;
		
		Hashtable<Integer, Double> counts = new Hashtable<Integer, Double>();
		for (int i = 0; i < alphabet.length(); i++)
		{
			counts.put(i, 0.0);
		}
		
		
		// must do img pyramid

		// for each location
		int counter = 0;
		for (int i = 0; i + boxWidth < img.size().width; i += dx)
		{
			for (int j = 0; j + boxHeight < img.size().height; j += dy)
			{
				// extract subregion
				Mat sub = new Mat();
				Rect roi = new Rect(i, j, boxWidth, boxHeight);
				
				sub = img.submat(roi);
				
				int sum = (int) Core.sumElems(sub).val[0];
				if(sum / 255 > 121)
				{
					Imshow im = new Imshow("");
					im.showImage(sub);
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
		Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
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
				for (int period = 130; period < 250; period += 20)
				{
					for (int shift = 0; shift < period; shift += 20)
					{
						
						output.println(i);
						
						Captcha c = new Captcha(40, 40);
						c.drawLetter(letter, 20, 20, 2, 0, 2, 0);
						c.rippleCentered(amplitude, period, shift);
						
						c.saveImg(String.format("image%06d.png", counter++));
					}
				}
			}
			
			System.out.println("Finished letter: " + letter);
		}
		System.out.println("Total images made: " + counter);
		output.close();
	}
	
	// creates n captchas and stores labels in filename, based on ripples 
	public static void rippleBatch(int n, String filename) throws IOException
	{
		int numImgs = n;
		int width = 250; //220
		int height = 120; //100
		Random rdm = new Random();
		
		PrintWriter output = new PrintWriter(filename);

		for (int i = 0; i < numImgs; i++)
		{
			Captcha c = new Captcha(height, width);

			// determine parameters
			int numLetters = 3 + rdm.nextInt(10);

			int x = rdm.nextInt(width / 2 + 10) + 10;
			int y = rdm.nextInt((int) (height - 30)) + 20;

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
			int numPoints = rdm.nextInt(500);
			int numLines = rdm.nextInt(3);
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
			c.saveImg();
			output.printf("%d %d %d\n", amplitude, period, shift);
		}
		output.close();
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
