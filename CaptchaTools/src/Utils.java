import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class Utils
{
	
	
	//WARNING: change number of channels depending on image!!!!!!!!
	public static Mat bufferedImageToMat(BufferedImage bi)
	{
		Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
		byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
		mat.put(0, 0, data);
		return mat;
	}

	// creates n captchas and stores labels in filename
	public static void generateCaptchaBatch(int n, String filename) throws IOException
	{
		int numImgs = n;
		int width = 250;
		int height = 120;
		Random rdm = new Random();
		String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
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
