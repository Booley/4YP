import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Captcha
{
	private static final int FONT = Core.FONT_HERSHEY_PLAIN;
	private static final Scalar COLOR = new Scalar(255);
	private static final Size KSIZE = new Size(5, 5);
	static
	{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	public int height; //70
	public int width; //150
	private Mat img;
	
	// create a blank captcha
	// reminder: (x, y) = (width, height)
	public Captcha(int width, int height)
	{
		this.height = height;
		this.width = width;
		
		img = Mat.zeros(height, width, CvType.CV_8UC1);
	}
	
	public Captcha clone()
	{
		Captcha copy = new Captcha(width, height);
		copy.setImg(img);
		return copy;
	}
	
	private void setImg(Mat m)
	{
		img = m.clone();
	}
	// intialize a captcha with an image, make b&w, and resize
//	public Captcha(int height, int width, String file) throws IOException
//	{
//		this.height = height;
//		this.width = width;
//		
//		BufferedImage bi = ImageIO.read(new File(file));
//		img = Utils.bufferedImageToMat(bi);
//		
//		//convert to black/white
//		Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY);
//		Core.subtract(Mat.ones(img.size(), img.type()).mul(Mat.ones(img.size(), img.type()), 255), img, img);
//		
//		resize(height, width);
//	}
//	
//	public Captcha(String file) throws IOException
//	{	
//		BufferedImage bi = ImageIO.read(new File(file));
//		height = bi.getHeight();
//		width = bi.getWidth();
//		originalHeight = height;
//		originalWidth = width;
//		
//		img = Utils.bufferedImageToMat(bi);
//		
//		//convert to black/white
//		Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY);
//		Core.subtract(Mat.ones(img.size(), img.type()).mul(Mat.ones(img.size(), img.type()), 255), img, img);
//		
//		resize(height, width);
//		baselineCopy = img.clone();
//	}
	
	public static void invert(Mat m)
	{
		Core.subtract(Mat.ones(m.size(), m.type()).mul(Mat.ones(m.size(), m.type()), 255), m, m);
	}
	
	public void ripple(double amplitude, double period, double shift)
	{
		Mat m = Mat.zeros(img.size(), img.type());
		for (int i = 0; i < width; i++)
		{
			int dy = (int)Math.round(amplitude * Math.sin(2 * Math.PI / period * (i - shift)));
			
			for (int j = 0; j < height; j++)
			{
				double newVal = img.get((j - dy + height) % height, i)[0];
				m.put(j, i, newVal);
			}
		}
		img = m;
	}
	
	public void rippleCentered(double amplitude, double period, double shift)
	{
		int maxy = -1;
		int miny = 10000;
		
		Mat m = Mat.zeros(img.size(), img.type());
		for (int i = 0; i < width; i++)
		{
			int dy = (int)Math.round(amplitude * Math.sin(2 * Math.PI / period * (i - shift)));
			maxy = Math.max(maxy, dy);
			miny = Math.min(miny, dy);
			
			for (int j = 0; j < height; j++)
			{
				double newVal = img.get((j - dy + height) % height, i)[0];
				m.put(j, i, newVal);
			}
		}
		
		Mat tmp = Mat.zeros(img.size(), img.type());
		int dist = (int)Math.round(amplitude * Math.sin(2 * Math.PI / period * (width/2 - shift)));
		for (int i = 0; i < width; i++)
		{
			for (int j = 0; j < height; j++)
			{
				tmp.put((j - (int)dist + height) % height, i, 
						m.get(j, i)[0]);
			}
		}
		
		img = tmp;
	}
	
	public void drawLetter(String letter, double x, double y, double scale, double angle, int thickness, double sigma)
	{
		drawLetter(letter, x, y, scale, angle, thickness, sigma, true, 255);
	}
	
	public void drawLetter(String letter, double x, double y, double scale, double angle, int thickness, double sigma, boolean isPresent, int color)
	{
		if(!isPresent)
			return;
		
		Size s = Core.getTextSize(letter, FONT, scale, thickness, null);
		
		// write text
		Mat tmp = Mat.zeros(img.size(), img.type());
		Core.putText(tmp, letter, new Point(x - s.width / 2, y + s.height / 2), FONT, scale, new Scalar(color), thickness);

		// rotate
		Mat rotateMatrix = Imgproc.getRotationMatrix2D(new Point(x, y), angle, 1);
		Imgproc.warpAffine(tmp, tmp, rotateMatrix, tmp.size());
		
		// blur
		if(sigma > 0)
		{
			Imgproc.GaussianBlur(tmp, tmp, KSIZE, sigma);
		}
		
		// combine with current img
		Core.add(img, tmp, img);
	}

	public void drawText(String text, double x, double y, double scale, int thickness, double spacing, double sigma)
	{
		// write text
		Mat tmp = Mat.zeros(img.size(), img.type());
		
		double currentX = x;
		for(int i = 0; i < text.length(); i++)
		{
			String letter = text.substring(i,i+1);
			Size s = Core.getTextSize(letter, FONT, scale, thickness, null);
			Core.putText(tmp, letter, new Point(currentX, y), FONT, scale, new Scalar(255), thickness);
			currentX += s.width * spacing;
		}
		
		// combine with current img
		Core.add(img, tmp, img);
	}
	
	public Mat getImg()
	{
		return img;
	}
	
	private static int id = 0;
	public void saveImg() throws IOException
	{
		Imshow im = new Imshow("idk");
		BufferedImage bufferedImg = im.toBufferedImage(img);
		File outputfile = new File(String.format("image%04d.png", id));
		id++;
		ImageIO.write(bufferedImg, "png", outputfile);
	}
	
	//must be filename.png
	public void saveImg(String name) throws IOException
	{
		Imshow im = new Imshow("idk");
		BufferedImage bufferedImg = im.toBufferedImage(img);
		File outputfile = new File(name);
		ImageIO.write(bufferedImg, "png", outputfile);
	}
	
	// median filter and threshold
	public void denoise()
	{
		Imgproc.medianBlur(img, img, 3);
		
		for(int i = 0; i < img.size().height; i++)
		{
			for (int j = 0; j < img.size().width; j++)
			{
				img.put(i, j, img.get(i, j)[0] < 110 ? 0 : 255);
			}
		}
	}
	
	// i.e. increase contrast
	public void dilate()
	{
		Imgproc.equalizeHist(img, img);
		Imgproc.dilate(img, img, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3,3)));
	}
	
	public void showImg()
	{
		Imshow im = new Imshow("Text");
		im.show(img);
	}

	// between 0 and 255 (?)
	public int[] getPixels()
	{
		int[] pixels = new int[img.rows() * img.cols()];

		for (int i = 0; i < img.rows(); i++)
		{
			for (int j = 0; j < img.cols(); j++)
			{
				pixels[j + i * img.cols()] = (int) img.get(i, j)[0];
			}
		}

		return pixels;
	}

	public void globalBlur(double sigma)
	{
		if(sigma > 0)
		{
			Imgproc.GaussianBlur(img, img, KSIZE, sigma);
		}
	}
	
	// generate a baseline Captcha object
	public static Captcha generateBaseline(int id) throws IOException
	{
		int HEIGHT = 150;
		int WIDTH = 70;
		Captcha c = new Captcha(HEIGHT, WIDTH);
		if(id == 1)
		{
			//non-rotated, occluded baseline
			c.drawLetter("S", 15, 20, 3, 0, 3, 1.2);
			c.drawLetter("M", 22, 32, 2, 0, 2, .5);
			c.drawLetter("K", 65, 40, 3, 0, 3, 1.1);
			c.drawLetter("B", 76, 25, 3, 0, 2, 1);
			c.drawLetter("D", 120, 40, 2, 0, 2, 2);
			c.drawLetter("F", 133, 30, 2, 0, 2, 1.3); // original y=40 
			
			Random rdm = new Random();
			for(int i = 0; i < 700; i++)
			{
				c.img.put(rdm.nextInt(HEIGHT), rdm.nextInt(WIDTH), 120);
			}
			
			c.drawLine(2, 20, 140, 50);
			c.drawLine(5, 60, 110, 20);
		}
		else if(id == 2)
		{
			//rotated, no occlusion
			c.drawLetter("F", 15, 20, 3, 12, 3, 1.2);
			c.drawLetter("C", 40, 32, 2, -20, 3, 1.5);
			c.drawLetter("X", 70, 40, 3, 29, 3, 1.1);
			c.drawLetter("Q", 100, 20, 3, -15, 3, 1.1);
			c.drawLetter("N", 130, 50, 3, -25, 3, 1.1);
		}
		else if(id == 3)
		{
			c.drawLetter("J", 15, 50, 2, -10, 3, 1.2);
			c.drawLetter("W", 32, 20, 3, -20, 3, 1.5);
			c.drawLetter("Z", 60, 38, 2, 29, 3, 1.1);
			c.drawLetter("W", 80, 25, 2, -20, 3, 1.5);
			c.drawLetter("P", 110, 30, 3, -20, 3, 1.1);
			c.drawLetter("H", 130, 50, 3, 30, 3, 1.1);
		}
		else if(id == 4)
		{
			c.drawLetter("E", 15, 50, 3, 0, 3, 1.2);
			c.drawLetter("U", 40, 32, 2, 0, 3, 1.5);
			c.drawLetter("G", 70, 20, 2, 0, 3, 1.1);
			c.drawLetter("S", 100, 45, 3, 0, 3, 1.1);
			c.drawLetter("N", 130, 20, 3, 0, 3, 1.1);
		}
		else if(id == 5)
		{
			c.drawLetter("V", 15, 40, 2, -10, 3, 1.2);
			c.drawLetter("S", 38, 25, 3, -20, 3, 1.5);
			c.drawLetter("A", 65, 40, 3, 29, 3, 1.1);
			c.drawLetter("L", 100, 30, 3, -20, 3, 1.1);
			c.drawLetter("R", 130, 18, 3, 30, 3, 1.1);
		}
		else if(id == 6)
		{
			File input = new File("captcha.png");
			BufferedImage image = ImageIO.read(input);
			c.img = Utils.bufferedImageToMat(image);
		}
		else if(id == 7)
		{
			String text = "lamps";
			c.drawText(text, 5, 50, 3, 2, 0.9, 1);
			c.drawLine(5, 35, 170, 40);
			c.ripple(10, 150, 125);
		}
//		
//		//add white noise
//		Random rdm = new Random();
//		for(int i = 0; i < 400; i++)
//		{
//			c.img.put(rdm.nextInt(HEIGHT), rdm.nextInt(WIDTH), 120);
//		}
		
		c.addNoise(400);
		return c;
	}
	
	//add white noise to img (but not to baselineCopy!)
	public void addNoise(int numPoints)
	{
		//add white noise
		Random rdm = new Random();
		for(int i = 0; i < numPoints; i++)
		{
			img.put(rdm.nextInt(height), rdm.nextInt(width), 120);
		}
	}
	
	public void drawLine(double x1, double y1, double x2, double y2)
	{
		Mat tmp = Mat.zeros(img.size(), img.type());
		
		Scalar scalar = new Scalar(255);
		Core.line(tmp, new Point(x1, y1), new Point(x2, y2), scalar, 1);
//		Imgproc.GaussianBlur(tmp, tmp, KSIZE, 1.3);
		
		Core.add(tmp, img, img);
	}
	
	public void drawCircle(double x, double y, int radius, double brightness, int thickness)
	{
		Core.circle(img, new Point(x, y), radius, new Scalar(brightness), thickness);
	}
	
	public void resize(int newWidth, int newHeight)
	{
		height = newHeight;
		width = newWidth;
		Size sz = new Size(newWidth, newHeight); //reverse
		Imgproc.resize(img, img, sz);
	}

	public static Mat toC3(Mat img)
	{
		Mat m = new Mat();
		ArrayList<Mat> list = new ArrayList<Mat>();
		list.add(img);
		list.add(img);
		list.add(img);

		Core.merge(list, m);
		img.convertTo(m,CvType.CV_8UC3, 255);
		return m;
	}
	
	public static void showMat(Mat m)
	{
		Imshow im = new Imshow("");
		im.showImage(toC3(m));
	}
	
	public Mat toC1()
	{
		Mat m = new Mat();
		img.convertTo(m, CvType.CV_32FC1, 1/255.0);

		return m;
	}
	
	// returns image between column x and the width (cuts out first x columns)
	public Mat crop(int x)
	{
		Rect roi = new Rect(Math.min(x, width - 1), 0, Math.max(0, width - 1 - x), height);
		
		Mat m = img.submat(roi).clone();
		m.convertTo(m, CvType.CV_32FC1, 1/255.0);
		return m;
	}
	
	// (x, y) = center of region
	// if box exceeds image boundary, crops to just the border
	// returns float matrix scaled between [0,1]
	public Mat subregion(int cx, int cy, double boxWidth, double boxHeight)
	{
		double x = Math.max(0, cx - boxWidth / 2.0);
		double y = Math.max(0, cy - boxHeight / 2.0);
		
		if(x + boxWidth > width)
			boxWidth = width - x - 1;
		if(y + boxHeight > height)
			boxHeight = height - y - 1;
		
		Mat sub = new Mat();
		Rect roi = new Rect((int)x, (int)y, (int)boxWidth, (int)boxHeight);
		
		sub = img.submat(roi).clone();
		
		Mat m = new Mat();
		sub.convertTo(m, CvType.CV_32FC1, 1/255.0);
		
		return m;
	}
}
