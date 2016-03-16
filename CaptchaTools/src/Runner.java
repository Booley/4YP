import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;


public class Runner
{
	
	//don't forget to comment out
	public static void main(String[] args) throws IOException
	{
//		Captcha c = new Captcha(100, 200);
//		c.drawText("what what", 50, 50, 2, 2, 1, 1);
////		c.showImg();
//		String model = "/Users/bomoon/Documents/Eclipse Workspace/OpenCVTest/models/captchanet_deploy.prototxt"; 
//		String weights = "/Users/bomoon/Documents/Eclipse Workspace/OpenCVTest/models/snapshots/captchanet_iter_100.caffemodel";
//		CNN net = new CNN(model, weights);
////		Mat img = opencv_imgcodecs.imread("/Users/bomoon/Documents/Eclipse Workspace/OpenCVTest/test_imgs/image0000.png");
		
//		System.out.println(Arrays.toString(net.forward(c)));
		
//		Utils.letterBatch("letter_labels.txt");
		
		
		String model = "models/letter_net_deploy.prototxt"; 
		String weights = "models/snapshots/letter_net_iter_2000.caffemodel";
		int imnum = 0;
		Mat img = opencv_imgcodecs.imread(String.format("models/letters_many/image%06d.png", imnum));
		System.out.println(img);
		CNN net = new CNN(model, weights);
		
		Captcha c = new Captcha(70, 100);
		c.drawText("3", 10, 40, 2, 2, 0.9, 0);
		c.rippleCentered(10, 100, 0);
//		c.denoise();
//		c.dilate();
		c.showImg();
		Utils.slidingWindow(c.getImg(), net);
		
		org.opencv.core.Mat resized = new org.opencv.core.Mat();
		Imgproc.pyrDown(c.getImg(), resized);
		Imshow im = new Imshow("");
		im.showImage(resized);
		Utils.slidingWindow(resized, net);
		
		
//		System.exit(0);
//		
//		double[] results = net.forward(img);
//		System.out.println(Arrays.toString(results));
//		
//		ArrayList<Pair> list = new ArrayList<Pair>();
//		for(int i = 0; i < results.length; i++)
//		{
//			list.add(new Pair(results[i], i));
//		}
//		Pair[] arr = list.toArray(new Pair[0]);
//		Arrays.sort(arr);
//		for(int i = 0; i < 15; i++)
//		{
//			System.out.println(arr[arr.length - 1 - i]);
//		}
//		
//		int maxIdx = 0;
//		double maxProb = -1;
//		for(int i = 0; i < results.length; i++)
//		{
//			if(maxProb < results[i])
//			{
//				maxProb = results[i];
//				maxIdx = i;
//			}
//		}
//		
//		Scanner file_labels = new Scanner(new File("models/letters_many/letter_labels.txt"));
//		ArrayList<Integer> labels = new ArrayList<Integer>();
//		while(file_labels.hasNextInt())
//		{
//			labels.add(file_labels.nextInt());
//		}
//		
//		
//		System.out.println("Guessed: " + Utils.alphabet.charAt(maxIdx));
//		System.out.println("Actual: " + Utils.alphabet.charAt(labels.get(imnum)));
	}
	
}

class Pair implements Comparable<Pair>
{
	double val;
	int idx;
	
	public Pair(double val, int idx)
	{
		this.val = val;
		this.idx = idx;
	}
	
	public int compareTo(Pair obj)
	{
		double diff = val - obj.val;
		if(diff < 0)
			return -1;
		else if(diff > 0)
			return 1;
		else
			return 0;
	}
	
	public String toString()
	{
		return String.format("%s\t%f", Utils.alphabet.charAt(idx), val);
	}
}