package bdv.jogl.VolumeRenderer.gui.GLWindow;


import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JFrame;

import bdv.jogl.VolumeRenderer.scene.AbstractScene;
import bdv.jogl.VolumeRenderer.scene.SceneEventListener;
import bdv.jogl.VolumeRenderer.scene.VolumeDataScene;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;


/**
 * Main gl supporting widget
 * @author michael
 *
 */
public class GLWindow extends JFrame {

	/**
	 * default version
	 */
	private static final long serialVersionUID = 1L;

	private final GLCanvas glCanvas;

	private AbstractScene renderScene;

	private CameraUpdater cUpdater;

	//TODO bench stuff
	private final int maxStamps = 61; 
	private final int startSteps = 1000;
	private final int startSamples = 10;
	private final int stopSamples = 10;
	private final int incrementSamples =2; 
	private int currentSamples=startSamples;
	private long measureBegin =-1;
	private long formerPrintStep =-1;
	private long framesScinceBegin = 0;


	private boolean startPhaseInProgress = false;
	private boolean benchmarkInProgress = false;
	private double timeStamp[] = new double[maxStamps];
	private ArrayList<Double> means = new ArrayList<Double>();
	private ArrayList<Double> medians = new ArrayList<Double>();
	private ArrayList<Double> maxs = new ArrayList<Double>();
	private ArrayList<Double> mins = new ArrayList<Double>();
	private ArrayList<Double> vars = new ArrayList<Double>();


	private int startStepsTaken =  0;
	private int measureStepsTaken = 0 ;
	//TODO bench stuff

	/**
	 * Sets context for the current scene
	 */
	private void adaptScene(){

		renderScene.addSceneEventListener(new SceneEventListener() {

			/**
			 * Re-renders if the scene was updated 
			 */
			@Override
			public void needsUpdate() {
				glCanvas.repaint();

			}
		});
		
		//add camera updater to scen camera
		cUpdater = new CameraUpdater(renderScene.getCamera());
		glCanvas.addMouseListener(cUpdater.getMouseListener());
		glCanvas.addMouseMotionListener(cUpdater.getMouseMotionListener());
		glCanvas.addMouseWheelListener(cUpdater.getMouseWheelListener());
	}



	/**
	 * @param scenes the scenes to set
	 */
	public void setScene(AbstractScene scenes) {
		this.renderScene = scenes;
		adaptScene();
	}

	/**
	 * @return the renderScene
	 */
	public AbstractScene getScene() {
		return renderScene;
	}


	/**
	 * @return the glCanvas
	 */
	public GLCanvas getGlCanvas() {
		return glCanvas;
	}

	//TODO bench
	/**
	 * Benchmark interface only!
	 */
	public void startBenchmark(){

		mins.clear();
		maxs.clear();
		means.clear();
		medians.clear();
		vars.clear();
		benchmarkInProgress = true;

		startBenchmark(startSamples);
	}
	
	/**
	 * Starts Benchmark with specific sample size
	 * @param startsamples the sample count
	 */
	public void startBenchmark(int startsamples){
		startStepsTaken = 0;
		measureStepsTaken = 0;
		startPhaseInProgress = true;
		measureBegin = -1;
		framesScinceBegin=0;

		currentSamples = startsamples;
		((VolumeDataScene)getScene()).getRenderer().setSamples(currentSamples);

		System.out.print("Mesurement started! [" + currentSamples +"/"+stopSamples+"]");

		glCanvas.repaint();
	}

	/**
	 * Warmup step without measurement s
	 */
	private void doWarmupStep(){
		startPhaseInProgress = startStepsTaken < startSteps;
		startStepsTaken++;	
	}

	/**
	 * Measurement step and evaluation
	 */
	private void doMeasurementStep(){

		if(benchmarkInProgress){
			long currentTime = System.nanoTime();
			if(measureBegin < 0){
				measureBegin = currentTime;
			}else{
				framesScinceBegin++;
			}
			if((currentTime- measureBegin) > 1000000000 && framesScinceBegin > 1){
				timeStamp[measureStepsTaken] = timeToFps(framesScinceBegin,currentTime- measureBegin);
				measureStepsTaken++;
				framesScinceBegin=0;
				measureBegin = currentTime;
			}
			if(measureStepsTaken >= maxStamps){

				evaluateStatistic();
				System.out.println("Mesurement done!");
				benchmarkInProgress = currentSamples < stopSamples;
				if(benchmarkInProgress){
					currentSamples += incrementSamples;
					startBenchmark(currentSamples);
				}else{		
					printResultsToFile();
				}
			}
		}
	}

	/**
	 * Main benchmark "state" machine
	 */
	private void doBenchmarkStep(){
		if(!benchmarkInProgress ){
			return;
		}
		if(startPhaseInProgress){
			doWarmupStep();
		}else{	
			doMeasurementStep();
		}
	}

	/**
	 * Print results to a file
	 */
	private void printResultsToFile() {
		PrintWriter resultWriter = null;
		try {
			resultWriter = new PrintWriter("benchmark_"+new Date()+".txt","UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int i = 0;
		resultWriter.write("samples\t\t\tfastes\t\t\tslowest\t\t\tmean\t\t\tmedian\t\t\tstandard derivation\n");
		for(int step = startSamples;step<= stopSamples; step +=incrementSamples){

			resultWriter.write(""+ (int)step + "\t\t\t"
					+mins.get(i).toString()+"\t\t\t"
					+maxs.get(i).toString()+"\t\t\t"
					+means.get(i).toString()+"\t\t\t"
					+medians.get(i).toString()+"\t\t\t"
					+vars.get(i).toString()+"\n");

			i++;
		}
		resultWriter.close();

	}

	/**
	 * Calculate statistic data form the measurements
	 */
	private void evaluateStatistic(){
		double[] fpsField = timeStamp.clone();

		//sort for median
		Arrays.sort(fpsField);

		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		double avg = 0;
		double median =0;
		//median 
		if(fpsField.length%2 ==0){
			//even
			median= 0.5* (fpsField[fpsField.length/2-1]+fpsField[fpsField.length/2]);

		}else{
			//uneven
			median = fpsField[(int)Math.floor(fpsField.length/2)];
		}

		HashMap<Double,Long>histogram  = new HashMap<Double, Long>();
		for(int i = 0; i<fpsField.length;i++ ){
			//min
			min = Math.min(min, fpsField[i]);

			//max
			max = Math.max(max, fpsField[i]);

			//mean acc
			avg += fpsField[i];

			//hist
			if(!histogram.containsKey(fpsField[i])){
				histogram.put(fpsField[i], 0l);
			}
			histogram.put(fpsField[i],1l + histogram.get(fpsField[i]));

			fpsField[i] = -1;
		}
		avg/=((double)fpsField.length);
		mins.add(min);
		maxs.add(max);
		means.add(avg);
		medians.add(median);
		//variance
		double variance = 0;
		for(Double fps : histogram.keySet()){
			Long occurence = histogram.get(fps);
			double pi = (((double)occurence)/((double)fpsField.length));
			variance+=Math.pow(fps - avg,2.0)*pi;
		}
		vars.add(Math.sqrt(variance));
	}

	/**
	 * Calculates fps from times and samples
	 * @param frames number of rendered frames
	 * @param timeInNs time elapse in nano seconds
	 * @return the framerate in  fps
	 */
	private double timeToFps(double frames,double timeInNs){
		return frames/(timeInNs / 1000000000.0);
	}
	
	/**
	 * Triggers re-rendering and prints progress
	 */
	private void prepareNextMeasurement(){
		if(benchmarkInProgress){
			if(!startPhaseInProgress){
				if(formerPrintStep != measureStepsTaken){

					if(measureStepsTaken %(maxStamps/ 10) ==0){
						System.out.print(".");
						formerPrintStep=measureStepsTaken;
					}
				}
			}
			glCanvas.repaint();
		}
	}
	//TODO bench

	/**
	 * constructor
	 * @param scene the scene to display
	 */
	public GLWindow(final VolumeDataScene scene){		
		// create render area
		//GLProfile glprofile = GLProfile.getDefault();
		GLProfile glprofile = GLProfile.get(GLProfile.GL4);
		GLCapabilities glcapabilities = new GLCapabilities( glprofile );

		glCanvas = new GLCanvas(glcapabilities );
		glCanvas.addGLEventListener(new GLEventListener() {

			@Override
			public synchronized void reshape(GLAutoDrawable drawable, int x, int y, int width,
					int height) {
				GL gl = drawable.getGL();
				GL4 gl2 = gl.getGL4();

				//resizes available scene
				renderScene.resize(gl2, x, y, width, height);
			}

			/**
			 * init the test context
			 */
			@Override
			public synchronized void init(GLAutoDrawable drawable) {

				GL gl = drawable.getGL();
				//gl =drawable.setGL(new TraceGL2(drawable.getGL().getGL2(), System.err));
				GL4 gl2 = gl.getGL4();

				//init available scene
				renderScene.init(gl2, drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
			}

			@Override
			public synchronized void dispose(GLAutoDrawable drawable) {
				GL gl = drawable.getGL();
				GL4 gl2 = gl.getGL4();

				//disposes available scene
				renderScene.dispose(gl2);
			}

			@Override
			public synchronized void display(GLAutoDrawable drawable) {		

				//TODO bench
				//doBenchmarkStep();
				//TODO bench
				GL gl = drawable.getGL();
				GL4 gl2 = gl.getGL4();

				//renders available scene
				renderScene.render(gl2);
				//TODO bench
				//prepareNextMeasurement();
				//TODO bench
			}
		});
		initWindowElements();
		setScene(scene);
	}

	/**
	 * Does define the layout of the Window
	 */
	private void initWindowElements(){
		setTitle("Open GL Window");
		//window size
		setSize(600,600);
		
		getContentPane().add(glCanvas);
	}

	/**
	 * @return the camera updater
	 */
	public CameraUpdater getCameraUpdater() {
		return cUpdater;
	}
}
