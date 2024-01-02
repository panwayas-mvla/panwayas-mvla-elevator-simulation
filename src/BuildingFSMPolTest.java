import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import building.Building;
import myfileio.MyFileIO;

@TestMethodOrder(OrderAnnotation.class)
class BuildingFSMPolTest {
	private ElevatorSimController c;
	private Building b;
	private MyFileIO fio = new MyFileIO();
	private static boolean DEBUG = false;
	private static String os = null;
	private static String javaHome = null;
	private ElevatorLogCompare cmpLog = new ElevatorLogCompare();
	private static String test = "";

	private void updateSimConfigCSV(String fname) {
		File fh = fio.getFileHandle("ElevatorSimConfig.csv");
		String line = "";
		ArrayList<String> fileData = new ArrayList<>();
		try {
			BufferedReader br = fio.openBufferedReader(fh);
			while ( (line = br.readLine())!=null) {
				if (line.matches("passCSV.*")) 
					fileData.add("passCSV,"+fname);
				else
					fileData.add(line);
			}
			fio.closeFile(br);
			BufferedWriter bw = fio.openBufferedWriter(fh);
			for (String l : fileData)
				bw.write(l+"\n");
			fio.closeFile(bw);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void copyTestFile(String fname) {
		File ifh = fio.getFileHandle("test_data/"+fname);
		File ofh = fio.getFileHandle(fname);
		Path src = Paths.get(ifh.getPath());
		Path dest = Paths.get(ofh.getPath());
		try {
			Files.copy(src, dest,StandardCopyOption.REPLACE_EXISTING);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		updateSimConfigCSV(fname);
	}
	
	private void deleteTestCSV(String fname) {
		MyFileIO fio = new MyFileIO();
		File ifh = fio.getFileHandle(fname);
		ifh.delete();
		ifh = fio.getFileHandle(fname.replaceAll(".csv", "PassData.csv"));
		ifh.delete();
	}

	
	private static void deleteTestLog(String fname) {
		MyFileIO fio = new MyFileIO();
		File fh = fio.getFileHandle(fname);
		if (fh.exists()) {
			boolean status = fh.delete();
			if (!status) {
				System.out.println("Warning: Unable to delete "+fname+"\n"+
						"       You should remove this manually or cmpElevator results\n"+
						"       may be incorrect\n");
			}
		} 		
	}
	
    private static String getOperatingSystem() {
    	os = System.getProperty("os.name");
    	return os;
    }

    private static void getJavaHome() {
    	File fh = null;
    	javaHome = System.getProperty("java.home").replaceAll("jre","bin");
		if (DEBUG) System.out.println("JavaHome: "+javaHome);
		fh = new File(javaHome);
		if (!fh.exists()) 
			javaHome = null;
    }

    private void moveCmpFiles(String base) {
    	File ifh = new File(base+".log");
    	File ofh = new File("JUnitTestLogs/"+base+".log");
		Path src = Paths.get(ifh.getPath());
		Path dest = Paths.get(ofh.getPath());
		try {
			Files.move(src,dest,StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
    	ifh = new File(base+".cmp");
    	ofh = new File("JUnitTestLogs/"+base+".cmp");
		src = Paths.get(ifh.getPath());
		dest = Paths.get(ofh.getPath());
		try {
			Files.move(src,dest,StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
    }
    
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		System.out.println("Running on: "+getOperatingSystem());
		getJavaHome();
		File ifh = new File("ElevatorSimConfig.csv");
		File ofh = new File("ElevatorSimConfig.save");
		Path src = Paths.get(ifh.getPath());
		Path dest = Paths.get(ofh.getPath());
		Files.copy(src, dest,StandardCopyOption.REPLACE_EXISTING);
		ifh = new File("JUnitTestLogs");
		if (!ifh.exists()) {
			ifh.mkdir();
			System.out.println("Created file: JUnitTestLogs");
		}
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		File ifh = new File("ElevatorSimConfig.save");
		File ofh = new File("ElevatorSimConfig.csv");
		Path src = Paths.get(ifh.getPath());
		Path dest = Paths.get(ofh.getPath());
		Files.copy(src, dest,StandardCopyOption.REPLACE_EXISTING);
		ifh.delete();
	}
	
	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
		deleteTestLog(test+".log");
	}

	private void copyLogFiles(String base) {
    	File ifh = new File(base+".log");
    	File ofh = new File("JUnitTestLogs/"+base+".log");
		Path src = Paths.get(ifh.getPath());
		Path dest = Paths.get(ofh.getPath());
		try {
			Files.copy(src,dest,StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	private void runFSMTest (String cmd) {
		deleteTestCSV(test+".csv");
		copyLogFiles(test);
		boolean compareStatus = cmpLog.executeCompare(cmd.split("\\s+"));
		moveCmpFiles(test);
		assertTrue(compareStatus);
	}
	
	@Test
	@Order(1)
	//@Disabled
	void testPolTest1() {
		test = "PolTest1";
		System.out.println("\n\nExecuting Test: "+test+".csv");
		copyTestFile(test+".csv");
		c = new ElevatorSimController(null);
		b = c.getBuilding();
		b.enableLogging();
	    int i;
		for (i = 0; i < 51;i++) c.stepSim();
		b.closeLogs(i);
		deleteTestCSV(test+".csv");
		File fh = fio.getFileHandle(test+".cmp");
		String cmd = "java -jar cmpElevator.jar "+test+".log";
	    runFSMTest(cmd);
	}

	@Test
	@Order(2)
	//@Disabled
	void testPolTest2() {
		test = "PolTest2";
		System.out.println("\n\nExecuting Test: "+test+".csv");
		copyTestFile(test+".csv");
		c = new ElevatorSimController(null);
		b = c.getBuilding();
		b.enableLogging();
	    int i;
		for (i = 0; i < 55;i++) c.stepSim();
		b.closeLogs(i);
		deleteTestCSV(test+".csv");
		File fh = fio.getFileHandle(test+".cmp");
		String cmd = "java -jar cmpElevator.jar "+test+".log";
	    runFSMTest(cmd);
	}

	@Test
	@Order(3)
	//@Disabled
	void testPolTest3() {
		test = "PolTest3";
		System.out.println("\n\nExecuting Test: "+test+".csv");
		copyTestFile(test+".csv");
		c = new ElevatorSimController(null);
		b = c.getBuilding();
		b.enableLogging();
	    int i;
		for (i = 0; i < 106;i++) c.stepSim();
		b.closeLogs(i);
		deleteTestCSV(test+".csv");
		File fh = fio.getFileHandle(test+".cmp");
		String cmd = "java -jar cmpElevator.jar "+test+".log";
	    runFSMTest(cmd);
	}

	@Test
	@Order(4)
	//@Disabled
	void testPolTest4() {
		test = "PolTest4";
		System.out.println("\n\nExecuting Test: "+test+".csv");
		copyTestFile(test+".csv");
		c = new ElevatorSimController(null);
		b = c.getBuilding();
		b.enableLogging();
	    int i;
		for (i = 0; i < 143;i++) c.stepSim();
		b.closeLogs(i);
		deleteTestCSV(test+".csv");
		File fh = fio.getFileHandle(test+".cmp");
		String cmd = "java -jar cmpElevator.jar "+test+".log";
	    runFSMTest(cmd);
	}

	@Test
	@Order(5)
	//@Disabled
	void testPolTest5() {
		test = "PolTest5";
		System.out.println("\n\nExecuting Test: "+test+".csv");
		copyTestFile(test+".csv");
		c = new ElevatorSimController(null);
		b = c.getBuilding();
		b.enableLogging();
	    int i;
		for (i = 0; i < 193;i++) c.stepSim();
		b.closeLogs(i);
		deleteTestCSV(test+".csv");
		File fh = fio.getFileHandle(test+".cmp");
		String cmd = "java -jar cmpElevator.jar "+test+".log";
	    runFSMTest(cmd);
	}

}
