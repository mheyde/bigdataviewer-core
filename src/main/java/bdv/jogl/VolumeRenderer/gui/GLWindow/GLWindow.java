package bdv.jogl.VolumeRenderer.gui.GLWindow;


import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JFrame;

import bdv.jogl.VolumeRenderer.Scene.AbstractScene;
import bdv.jogl.VolumeRenderer.Scene.SceneEventListener;
import bdv.jogl.VolumeRenderer.Scene.VolumeDataScene;

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

	//TODO bench
	private final int maxStamps = 1000; 
	private final int startSteps = 1000;
	private final int startSamples = 100;
	private final int stopSamples = 3100;
	private final int incrementSamples =500; 
	private int currentSamples=startSamples;
	

	private boolean startPhaseInProgress = false;
	private boolean benchmarkInProgress = false;
	private long timeStamp[] = new long[maxStamps];
	private ArrayList<Double> means = new ArrayList<Double>();
	private ArrayList<Double> medians = new ArrayList<Double>();
	private ArrayList<Double> maxs = new ArrayList<Double>();
	private ArrayList<Double> mins = new ArrayList<Double>();
	private ArrayList<Double> vars = new ArrayList<Double>();
	
	
	private int startStepsTaken =  0;
	private int measureStepsTaken = 0 ;

	//TODO bench
			
	private void adaptScene(){
		
		renderScene.addSceneEventListener(new SceneEventListener() {
			
			@Override
			public void needsUpdate() {
				glCanvas.repaint();
				
			}
		});
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
		startBenchmark(startSamples);
	}
	public void startBenchmark(int startsamples){
		startStepsTaken = 0;
		measureStepsTaken = 0;
		benchmarkInProgress = true;
		startPhaseInProgress = true;

		
		currentSamples = startsamples;
		((VolumeDataScene)getScene()).getRenderer().setSamples(currentSamples);
		
		System.out.print("Mesurement started! [" + currentSamples +"/"+stopSamples+"]");
		
		glCanvas.repaint();
	}
	
	private void doWarmupStep(){
			startPhaseInProgress = startStepsTaken < startSteps;
			startStepsTaken++;	
	}
	
	private void doMeasurementStep(){

		if(benchmarkInProgress){
			
			timeStamp[measureStepsTaken] = System.nanoTime();
			measureStepsTaken++;
			if(measureStepsTaken >= maxStamps){

				evaluateResults();
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



	private void evaluateResults(){
		long[] times = new long[timeStamp.length-1];
		long latestTimeStep = timeStamp[0];
		//create times
		for(int i= 1; i <timeStamp.length ;i++){
			times[i-1] = timeStamp[i]-latestTimeStep;
			latestTimeStep = timeStamp[i];
			
		}
		
		//sort for median
		Arrays.sort(times);
		
		long max = Long.MIN_VALUE;
		long min = Long.MAX_VALUE;
		double avg = 0;
		double median =0;
		//median 
		if(times.length%2 ==0){
			//even
			median= 0.5* (double)(times[times.length/2-1]+times[times.length/2]);
			
		}else{
			//uneven
			median = (double)times[(int)Math.floor(times.length/2)];
		}
		
		HashMap<Long,Long>histogram  = new HashMap<Long, Long>();
		for(int i = 0; i<times.length;i++ ){
			//min
			min = Math.min(min, times[i]);
			
			//max
			max = Math.max(max, times[i]);
			
			//mean acc
			avg += times[i];
			
			//hist
			if(!histogram.containsKey(times[i])){
				histogram.put(times[i], 0l);
			}
			histogram.put(times[i],1l + histogram.get(times[i]));
			
			times[i] = -1;
		}
		avg/=((double)times.length);
		mins.add(timeToFps((double)min));
		maxs.add(timeToFps((double)max));
		means.add(timeToFps(avg));
		medians.add(timeToFps(median));
		//variance
		double variance = 0;
		for(Long time : histogram.keySet()){
			Long occurence = histogram.get(time);
			double pi = (((double)occurence)/((double)times.length));
			variance+=Math.pow(((double)time) - avg,2.0)*pi;
		}
		vars.add(Math.abs(timeToFps(avg+Math.sqrt(variance))-timeToFps(avg)));
	}
	
	private double timeToFps(double timeInNs){
		return 1.0/(timeInNs / 1000000000.0);
	}
	private void prepareNextMeasurement(){
		if(benchmarkInProgress   ){
			if(!startPhaseInProgress){
				if(measureStepsTaken %(maxStamps/ 10) ==0){
					System.out.print(".");
				}
			}
			glCanvas.repaint();
		}
	}
	//TODO bench
	
	/**
	 * constructor
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
		//		doBenchmarkStep();
				//TODO bench
				GL gl = drawable.getGL();
				GL4 gl2 = gl.getGL4();

				//renders available scene
				renderScene.render(gl2);
				//TODO bench
			//	prepareNextMeasurement();
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


		//sample size
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
