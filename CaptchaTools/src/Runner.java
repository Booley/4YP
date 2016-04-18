import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.opencv.core.Core;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class Runner
{
	static
	{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	//don't forget to comment out
	public static void main(String[] args) throws IOException
	{
		String model = "models/wave_net_deploy.prototxt"; 
		String weights = "models/snapshots/wave_net_iter_7000.caffemodel";
		
		int imnum = 1;
		String file = String.format("images/captchas/image%06d.png", imnum);
//		Mat img = opencv_imgcodecs.imread(file);
		
//		Imshow im = new Imshow("");
//		im.showImage(Utils.bufferedImageToMat(ImageIO.read(new File(file))));
		
		Mat img = Utils.convertOpenCVToJavaCV(Utils.bufferedImageToMat(ImageIO.read(new File(file))));
		
		CNN net = new CNN(model, weights);
		
		double[] output = net.forward(img);
		System.out.println(Arrays.toString(output));
		
////		
//		Captcha c = Captcha.generateBaseline(7);
//		c.denoise();
//		List<Box> boxes = Utils.slidingWindow(c.getImg(), net);
//		c.showImg();
//		
//		
//		System.out.println(boxes.size());
//		
//		List<Box> filtered = Utils.nms(boxes, 0.6);
//		System.out.println(filtered.size());
//		
//		org.opencv.core.Mat m = c.getImg();
//		for(Box b: filtered)
//		{
//			Core.rectangle(m, new Point(b.x1, b.y1), new Point(b.x2, b.y2), new Scalar(100,100,100,1));
//		}
//		Imshow im = new Imshow("");
//		im.showImage(m);
		
		
//		System.exit(0);
	
//		double[] results = net.forward(img);
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