import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.indexer.FloatBufferIndexer;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_dnn;
import org.bytedeco.javacpp.opencv_dnn.Blob;
import org.bytedeco.javacpp.opencv_dnn.Importer;
import org.bytedeco.javacpp.opencv_dnn.Net;

public class CNN
{
	//captcha: 120
	//letter: 30
	public static int SIZE = 64;
	public Net net;
	public int size;
	public double[] scores;
	
	// import net and weights
	public CNN(String model, String weights)
	{
		Importer importer = opencv_dnn.createCaffeImporter(model, weights);
		net = new Net();
		importer.populateNet(net);
		scores = null;
	}
	
	// NOTE: uses javacpp Mat type
	// be careful about num channels
	// forward pass and cache scores
	public double[] forward(Mat img)
	{	
		// must allocate to new mat, or error
		Mat resized = new Mat();
		opencv_imgproc.resize(img, resized, new Size(SIZE, SIZE));

		Blob inputBlob = new Blob(resized);

		net.setBlob(".data", inputBlob);
		net.forward();
		
		Blob prob = net.getBlob("ip2");
		Mat probMat = prob.matRefConst();
		FloatBufferIndexer idx = probMat.createIndexer();
		
		double[] results = new double[probMat.size().width()];
		for(int i = 0; i < results.length; i++)
			results[i] = idx.get(0,i);
		
		scores = results;
		return results;
	}
	
	// return normalized copy of net output
	// NOTE: unity normalization -> min probability is always 0.05 
	public double[] normalize()
	{
		double[] results = new double[scores.length];
		
		// perform max-min normalization
		double min = Integer.MAX_VALUE;
		double max = Integer.MIN_VALUE;
		for(double v: scores)
		{
			min = Math.min(min, v);
			max = Math.max(max, v);
		}
		
		double sum = 0;
		for (int i = 0; i < results.length; i++)
		{
			results[i] = Math.max(.05, (scores[i] - min) / (max - min));
			sum += results[i];
		}
		
		for (int i = 0; i < results.length; i++)
		{
			results[i] /= sum;
		}
		
		return results;
	}
	
	public static Mat convertFast(BufferedImage bufferedImage) {
	    byte[] data = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
	    return new Mat(data, false).reshape(bufferedImage.getColorModel().getNumComponents(), bufferedImage.getHeight());
	}
	
	//must convert c.img() Mat type to javacpp.Mat type
	//also check num channels?
	public double[] forward(Captcha c) throws IOException
	{
		Mat tmp = new Mat(new BytePointer()) {{ address = c.toC3().getNativeObjAddr(); }};
		
		return forward(tmp);
	}
	
	public double[] forward(org.opencv.core.Mat m)
	{
		Mat tmp = new Mat(new BytePointer()) {{ address = m.getNativeObjAddr(); }};
		
		return forward(tmp);
	}
	
	// must perform forward pass first!!!
	public Pair[] getTop(int n)
	{
		ArrayList<Pair> list = new ArrayList<Pair>();
		for(int i = 0; i < scores.length; i++)
		{
			list.add(new Pair(scores[i], i));
		}
		Pair[] arr = list.toArray(new Pair[0]);
		Arrays.sort(arr);
		
		Pair[] top = new Pair[n];
		
		for(int i = 0; i < n; i++)
		{
			top[i] = arr[arr.length - 1 - i];
		}
		
		return top;
	}
}
