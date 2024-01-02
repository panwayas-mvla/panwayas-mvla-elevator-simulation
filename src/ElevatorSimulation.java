import java.util.ArrayList;
import building.Elevator;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;


public class ElevatorSimulation extends Application {
	/** Instantiate the GUI fields */
	private ElevatorSimController controller;
	private final int NUM_FLOORS;
	private final int currFloor;

	/** The t. */
	private Timeline t;
	/** The time */
	private int ticks;

	/** you MUST use millisPerTick as the duration for your timeline */
	private static int millisPerTick = 150;

	/** Local copies of the states for tracking purposes */
	private final int STOP = Elevator.STOP;
	private final int MVTOFLR = Elevator.MVTOFLR;
	private final int OPENDR = Elevator.OPENDR;
	private final int OFFLD = Elevator.OFFLD;
	private final int BOARD = Elevator.BOARD;
	private final int CLOSEDR = Elevator.CLOSEDR;
	private final int MV1FLR = Elevator.MV1FLR;

	/** Sets dimensions and positions of elevator and icon */
	private final double elevatorStartingX = 150;
	private final double elevatorStartingY = 550;
	private final double elevatorWidth = 150;
	private final double elevatorHeight = 100;
	private final double iconSize = 40;

	/** Instantiates elements for GUI*/
	private ElevatorBox elevator;
	private StateIcon stateIcon;
	private BorderPane pane;
	private Scene scene;
	private Button runButton;
	private Button logButton;
	private Button stepButton;
	private TextField stepField;
	private Label timeLabel;
	private Text stateText;

	/** The triangle buttons on each floor for passengers to press. Sorted from lowest to highest floor,
	 * and lights up when pressed. */
	private ArrayList<Polygon> upTriangles;
	private ArrayList<Polygon> downTriangles;

	/** The texts in the queues display any waiting passengers, split by direction. */
	private ArrayList<Text> upQueues;
	private ArrayList<Text> downQueues;
	
	private boolean logging = false;

	/**
	 * Instantiates a new elevator simulation.
	 */
	public ElevatorSimulation() {
		controller = new ElevatorSimController(this);	
		NUM_FLOORS = controller.getNumFloors();
		currFloor = 0;
	}

	/**
	 * Creates the timeline to control time in simulation.
	 */
	private void initTimeline() {
		t =
				new Timeline(new KeyFrame(Duration.millis(millisPerTick),
						ae -> {
							controller.stepSim();
							ticks++;
							timeLabel.setText("Time: " + ticks + " ticks");
						}));
		startSim();
	}

	/**
	 * Start.
	 *
	 * @param primaryStage the primary stage
	 * @throws Exception the exception
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		// You need to design the GUI. Note that the test name should
		// appear in the Title of the window!!
		primaryStage.setTitle("Elevator Simulation - "+ controller.getTestName());
		primaryStage.show();

		//TODO: Complete your GUI, including adding any helper methods.
		//      Meet the 30 line limit...
		pane = new BorderPane();
		scene = new Scene(pane, 800, 800);
		setUpGUI();
		elevator = new ElevatorBox(pane, elevatorStartingX, elevatorStartingY, elevatorWidth, elevatorHeight);
		stateIcon = new StateIcon(pane, elevatorStartingX-60, elevatorStartingY+30, iconSize, iconSize);
		stateText = new Text("State: STOP");
		pane.getChildren().addAll(stateIcon, stateText);
		stateText.setX(50);
		stateText.setY(50);
		stateText.setScaleX(2);
		stateText.setScaleY(2);
		
		primaryStage.setTitle("Elevator Simulation"); // Set the stage title
		primaryStage.setScene(scene); // Place the scene in the stage
		primaryStage.show(); // Display the stage
		primaryStage.setResizable(false);
		initTimeline();
	}

	/**
	 * Set up all elements of GUI
	 */
	private void setUpGUI() {
		runButton = new Button("PLAY/PAUSE");
		logButton = new Button("LOG");
		stepButton = new Button("Step:  ");
		stepField = new TextField("1");
		stepField.setPrefWidth(40);
		timeLabel = new Label("TIME: " + ticks + " ticks");
		timeLabel.setScaleX(1.5);
		timeLabel.setScaleY(1.5);
		runButton.setOnAction(e -> {
			if (t.getStatus() == Animation.Status.RUNNING) pauseSim();
			else startSim();
		});
		logButton.setOnAction(e -> {
			if (!logging) {
				controller.enableLogging();
				logging = true;
			} else {
				controller.disableLogging();
				logging = false;
			}
		});
		stepButton.setOnAction(e -> executeSteps(Integer.parseInt(stepField.getText())));
		HBox topBar = new HBox(runButton, stepButton, stepField, logButton, timeLabel);
		topBar.setMargin(runButton, new Insets(0, 20, 0, 0));
		topBar.setMargin(stepField, new Insets(0, 20, 0, 0));
		topBar.setMargin(logButton, new Insets(0, 20, 0, 0));
		topBar.setMargin(timeLabel, new Insets(3, 20, 0, 20));
		pane.setTop(topBar);
		addTriangleButtons();
		addFloorLabels();
	}

	/**
	 * Add the passenger labels for up and down, and floors
	 */
	private void addFloorLabels() {
		ArrayList<Line> lines = new ArrayList<Line>();
		for (int i = 0; i < 6; i++) {
			lines.add(new Line(300, 650-100*i, 700, 650-100*i));
			pane.getChildren().add(lines.get(lines.size()-1));
		}
		double yPos;
		Text t;
		upQueues = new ArrayList<Text>();
		for (int i = 0; i < 5; i++) {
			yPos = 580-100*i;
			upQueues.add(new Text());
			t = upQueues.get(i);
			t.setX(400);
			t.setY(yPos);
			t.setScaleX(1.5);
			t.setScaleY(1.5);
			pane.getChildren().add(t);
		}
		downQueues = new ArrayList<Text>();
		for (int i = 0; i < 5; i++) {
			yPos = 530-100*i;
			downQueues.add(new Text());
			t = downQueues.get(i);
			t.setX(400);
			t.setY(yPos);
			t.setScaleX(1.5);
			t.setScaleY(1.5);
			pane.getChildren().add(t);
		}
	}

	/**
	 * Add the triangle 'button' icons on each floor
	 */
	private void addTriangleButtons() {
		double baseY;
		upTriangles = new ArrayList<Polygon>();
		for (int i = 0; i < 5; i++) {
			baseY = 595-100*i;
			if (i == 0) baseY = 610;
			upTriangles.add(new Polygon());
			upTriangles.get(upTriangles.size()-1).getPoints().addAll(new Double[] {
					315.0, baseY,
					345.0, baseY,
					330.0, baseY -25});
			pane.getChildren().add(upTriangles.get(i));
		}
		for (Polygon t : upTriangles) {
			t.setStroke(Color.GREEN);
			t.setStrokeWidth(2);
			t.setFill(Color.rgb(57, 255, 20));
		}
		downTriangles = new ArrayList<Polygon>();
		for (int i = 0; i < 5; i++) {
			baseY = 505-100*i;
			if (i == 4) baseY = 100;
			downTriangles.add(new Polygon());
			downTriangles.get(downTriangles.size()-1).getPoints().addAll(new Double[] {
					315.0, baseY,
					345.0, baseY,
					330.0, baseY +25});
			pane.getChildren().add(downTriangles.get(i));
		}
		for (Polygon t : downTriangles) {
			t.setStroke(Color.GREEN);
			t.setStrokeWidth(2);
			t.setFill(Color.rgb(57, 255, 20));
		}
	}

	/**
	 * Shows passengers going up and down on floor and calls.
	 * @param floor the floor to modify
	 * @param upPassString the toString of passengers going up
	 * @param downPassString the toString of passengers going down
	 */
	public void setFloorPassengers(int floor, String upPassString, String downPassString) {
		Polygon callIcon; // This is the triangle button that represents a pending call.
		if (floor != 5) { // Impossible to have up calls on 6th floor
			upQueues.get(floor).setText(upPassString);
			callIcon = upTriangles.get(floor);
			if (upPassString.equals("")) {
				callIcon.setStroke(Color.GREEN);
				callIcon.setFill(Color.rgb(57, 255, 20));
			} else {
				callIcon.setStroke(Color.rgb(0, 139, 139));
				callIcon.setFill(Color.CYAN);
			}
		}
		if (floor != 0) { // Impossible to have down calls on 1st floor
			downQueues.get(floor-1).setText(downPassString);
			callIcon = downTriangles.get(floor-1);
			if (downPassString.equals("")) {
				callIcon.setStroke(Color.GREEN);
				callIcon.setFill(Color.rgb(57, 255, 20));
			} else {
				callIcon.setStroke(Color.rgb(0, 139, 139));
				callIcon.setFill(Color.CYAN);
			}
		}
		
	}



	/**
	 * Updates the elevator state, location, direction, and # of passengers in the GUI
	 * @param currState the state to be at
	 * @param currFloor the floor to move to
	 * @param direction the direction of the elevator movement
	 * @param passengers the amount of passengers on elevator
	 */
	public void updateElevatorState(int currState, int currFloor, int direction, int passengers) {
		if (direction == 1) stateIcon.setMovingUp();
		else if (direction == -1) stateIcon.setMovingDown();
		updateFloor(currFloor);
		updateState(currState);
		elevator.setPassengers(passengers);
	}

	/**
	 * Updates the state icon and label to display state
	 * @param state the updated state
	 */
	public void updateState(int state) {
		// stop, mvtoflr, opendr, offld, board, closedr, mv1flr
		switch (state) {
		case STOP:
			stateIcon.setStopped();
			stateText.setText("State: STOP");
			break;
		case MVTOFLR:
			stateText.setText("State: MVTOFLR");
			break;
		case OPENDR:
			elevator.openDoor();
			stateText.setText("State: OPENDR");
			break;
		case OFFLD:
			stateText.setText("State: OFFLD");
			break;
		case BOARD:
			stateText.setText("State: BOARD");
			break;
		case CLOSEDR:
			elevator.closeDoor();
			stateText.setText("State: CLOSEDR");
			break;
		case MV1FLR:
			stateText.setText("State: MV1FLR");
			break;
		default:
			break;
		}
	}

	/**
	 * Moves the elevator and state icon to floor
	 * @param floor the floor to move to
	 */
	public void updateFloor(int floor) {
		int y = (550-100*(floor));
		stateIcon.setY(y+30);
		elevator.setYPos(y);
	}

	/**
	 * Starts the timeline
	 */
	private void startSim() {
		t.setCycleCount(Animation.INDEFINITE);
		t.play();
	}

	/**
	 * Pauses the timeline
	 */
	public void pauseSim() {
		t.pause();
	}

	/**
	 * Runs the timeline for steps amount
	 * @param steps
	 */
	private void executeSteps(int steps) {
		t.stop();
		t.setCycleCount(steps);
		t.play();
	}

	/**
	 * The main method. Allows command line to modulate the speed of the simulation.
	 *
	 * @param args the arguments
	 */
	public static void main (String[] args) {
		if (args.length>0) {
			for (int i = 0; i < args.length-1; i++) {
				if ("-m".equals(args[i])) {
					try {
						ElevatorSimulation.millisPerTick = Integer.parseInt(args[i+1]);
					} catch (NumberFormatException e) {
						System.out.println("Unable to update millisPerTick to "+args[i+1]);
					}
				}
			}
		}
		Application.launch(args);
	}

	/**
	 * State Icon will change icons to match the direction the elevator is moving in, and if in stop state.
	 */
	private class StateIcon extends Polygon {

		private double x;
		private double y;
		private double width;
		private double height;

		/**
		 * Creates a new state icon.
		 * @param pane the pane to add object in
		 * @param x the starting x-val
		 * @param y the starting y-val
		 * @param width the width of the icon
		 * @param height the height of the icon
		 */
		public StateIcon(Pane pane, double x, double y, double width, double height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			setStrokeWidth(3);
			setStroke(Color.BLACK);
			setStopped();
		}

		/**
		 * Sets the y position to a new position.
		 * @param y the new y position
		 */
		public void setY(int y) {
			setTranslateY(y-this.y);
			this.y = y;
		}

		/**
		 * Set the icon to a red square showing stop state.
		 */
		public void setStopped() {
			getPoints().clear();
			getPoints().addAll(new Double[] {
					x, y,
					x, y+height,
					x+width, y+height,
					x+width, y
			});
			setFill(Color.RED);
		}

		/**
		 * Set icon to show elevator moving up.
		 */
		public void setMovingUp() {
			getPoints().clear();
			getPoints().addAll(new Double[] {
					x, y+height,
					x+width, y+height,
					(x+(x+width))/2, y
			});
			setFill(Color.BLUE);
		}

		/**
		 * Set icon to show elevator moving down.
		 */
		public void setMovingDown() {
			getPoints().clear();
			getPoints().addAll(new Double[] {
					x, y,
					x+width, y,
					(x+(x+width))/2, y+height
			});
			setFill(Color.BLUE);
		}
	}

	/**
	 * ElevatorBox the elevator. Will manage the movement, passengers, and door condition of the elevator.
	 */
	private class ElevatorBox extends Rectangle {

		/** this is a white rectangle that covers up the right border of the rectangle when visible*/
		private Rectangle doorAnimation;
		private Text passLabel;

		/**
		 * Creates a new elevator box.
		 * @param pane the pane to add box to
		 * @param x the starting x
		 * @param y the starting y
		 * @param width the starting width
		 * @param height the starting height
		 */
		public ElevatorBox(Pane pane, double x, double y, double width, double height) {
			setX(x);
			setY(y);
			setWidth(width);
			setHeight(height);
			setStrokeWidth(5);
			setStroke(Color.BLACK);
			setFill(Color.WHITE);
			doorAnimation = new Rectangle(getX()+width-3, getY()+2, 6, height-4);
			doorAnimation.setFill(Color.WHITE);
			passLabel = new Text("(0)");
			passLabel.setX(x+50);
			passLabel.setY(y+50);
			passLabel.setScaleX(2);
			passLabel.setScaleY(2);
			closeDoor();
			pane.getChildren().addAll(this, doorAnimation, passLabel);
		}

		/**
		 * Moves the elevatorBox to a new y-position.
		 * @param y the new y
		 */
		public void setYPos(int y) {
			setY(y);
			doorAnimation.setY(y+2);
			passLabel.setY(y + 50);
		}

		/**
		 * Triggers the door opening animation.
		 */
		public void openDoor() {
			doorAnimation.setOpacity(.9);
		}

		/**
		 * Triggers the door closing animation.
		 */
		public void closeDoor() {
			doorAnimation.setOpacity(0);
		}

		/**
		 * Changes and displays the amount of passengers in elevator.
		 * @param passengers the amount of passengers on the elevator
		 */
		public void setPassengers(int passengers) {
			passLabel.setText("(" + passengers + ")");
		}
	}
}