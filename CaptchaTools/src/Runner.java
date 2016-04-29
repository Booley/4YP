import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
//		String model = "models/letter_net_deploy.prototxt"; 
//		String weights = "models/snapshots/letter_net_iter_7000.caffemodel";
//		
//		int imnum = 1;
//		String file = String.format("images/letters/image%06d.png", imnum);
////		Mat img = opencv_imgcodecs.imread(file);
//		
////		Imshow im = new Imshow("");
////		im.showImage(Utils.bufferedImageToMat(ImageIO.read(new File(file))));
//		
//		Mat img = Utils.convertOpenCVToJavaCV(Utils.bufferedImageToMat(ImageIO.read(new File(file))));
//		
//		CNN net = new CNN(model, weights);
//		
//		double[] output = net.forward(img);
//		System.out.println(Arrays.toString(output));
//
//		for(Pair p: net.getTop(60))
//			System.out.println(p);
		
//		Utils.captchaBatch(10);
		Captcha c = new Captcha(250, 100);
		c.drawText("Fnlt1", 18, 85, 2, 2, 0.8, 0.2);
		c.rippleCentered(9.48144341, 178.70716858, 131.40489197);
		c.addNoise(1000);
//		c.saveImg();
		c.showImg();
		
		org.opencv.core.Mat sub = c.subregion(28, 80, 30, 30);
//		Captcha.showMat(sub);
		
		
		
//		CNN net = new CNN("/Users/bomoon/Documents/caffe-old/bo_models/wave_net/wave.prototxt", "/Users/bomoon/Documents/caffe-old/bo_models/wave_net/wave_weights.caffemodel", 64, "ip3");
//		CNN net = new CNN("/Users/bomoon/Documents/caffe-old/bo_models/letter_net/letter.prototxt", "/Users/bomoon/Documents/caffe-old/bo_models/letter_net/letter_weights.caffemodel", 28, "prob");
//		CNN net = new CNN("/Users/bomoon/Documents/caffe-old/bo_models/num_net/num.prototxt", "/Users/bomoon/Documents/caffe-old/bo_models/num_net/snapshots/num_iter_2000.caffemodel", 100, "prob");
//		CNN net = new CNN("/Users/bomoon/Documents/caffe-old/bo_models/position_net/position.prototxt", "/Users/bomoon/Documents/caffe-old/bo_models/position_net/snapshots/position_iter_10000.caffemodel", 28, "ip3");
//		Captcha img = new Captcha("/Users/bomoon/Documents/4YP/CaptchaTools/image0000.png");
//
//		double[] output = net.forward(sub);
//		System.out.println(Arrays.toString(output));
//		
//		ArrayList<Pair> pairs = new ArrayList<Pair>();
//		for(int i = 0; i < Utils.alphabet.length(); i++)
//		{
//			pairs.add(new Pair(output[i], i));
//		}
//		Collections.sort(pairs);
//		for(Pair p: pairs)
//			System.out.println(p);
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