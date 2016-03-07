import java.io.IOException;


public class Runner
{
	//don't forget to comment out
	public static void main(String[] args) throws IOException
	{
		Captcha c = Captcha.generateBaseline(1);
		c.showImg();
	}
}
