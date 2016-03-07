import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.indexer.FloatBufferIndexer;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_dnn.Blob;
import org.bytedeco.javacpp.opencv_dnn.Importer;
import org.bytedeco.javacpp.opencv_dnn.Net;

public class CNN
{
	public static int SIZE = 120;
	public Net net;
	
	// import net and weights
	public CNN(String model, String weights)
	{
		Importer importer = org.bytedeco.javacpp.opencv_dnn.createCaffeImporter(model, weights);
		net = new Net();
		importer.populateNet(net);
	}
	
	public double[] forward(Mat img)
	{
		opencv_imgproc.resize(img, img, new Size(SIZE, SIZE));
		Blob inputBlob = new Blob(img);

		net.setBlob(".data", inputBlob);
		net.forward();
		
		Blob prob = net.getBlob("ip2");
		Mat probMat = prob.matRefConst();
		FloatBufferIndexer idx = probMat.createIndexer();
		
		double[] results = new double[probMat.size().width()];
		for(int i = 0; i < results.length; i++)
			results[i] = idx.get(0,i);
		
		return results;
	}
	
}
