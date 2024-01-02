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
class BuildingFSMMvToFlrTest {
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
	void testMv2FCallPri1() {
		test = "Mv2FCallPri1";
		System.out.println("\n\nExecuting Test: "+test+".csv");
		copyTestFile(test+".csv");
		c = new ElevatorSimController(null);
		b = c.getBuilding();
		b.enableLogging();
	    int i;
		for (i = 0; i < 246;i++) c.stepSim();
		b.closeLogs(i);
		String cmd = "java -jar cmpElevator.jar "+test+".log";
	    runFSMTest(cmd);
	}

	@Test
	@Order(2)
	//@Disabled
	void testMv2FCallPri2() {
		test = "Mv2FCallPri2";
		System.out.println("\n\nExecuting Test: "+test+".csv");
		copyTestFile(test+".csv");
		c = new ElevatorSimController(null);
		b = c.getBuilding();
		b.enableLogging();
	    int i;
		for (i = 0; i < 86;i++) c.stepSim();
		b.closeLogs(i);
		String cmd = "java -jar cmpElevator.jar "+test+".log";
	    runFSMTest(cmd);
	}

	@Test
	@Order(3)
	//@Disabled
	void testMv2FCallPri3() {
		test = "Mv2FCallPri3";
		System.out.println("\n\nExecuting Test: "+test+".csv");
		copyTestFile(test+".csv");
		c = new ElevatorSimController(null);
		b = c.getBuilding();
		b.enableLogging();
	    int i;
		for (i = 0; i < 136;i++) c.stepSim();
		b.closeLogs(i);
		String cmd = "java -jar cmpElevator.jar "+test+".log";
	    runFSMTest(cmd);
	}

	@Test
	@Order(4)
	//@Disabled
	void testMv2FCallPri4() {
		test = "Mv2FCallPri4";
		System.out.println("\n\nExecuting Test: "+test+".csv");
		copyTestFile(test+".csv");
		c = new ElevatorSimController(null);
		b = c.getBuilding();
		b.enableLogging();
	    int i;
		for (i = 0; i < 152;i++) c.stepSim();
		b.closeLogs(i);
		String cmd = "java -jar cmpElevator.jar "+test+".log";
	    runFSMTest(cmd);
	}

	@Test
	@Order(5)
	//@Disabled
	void testMv2FCallPri5() {
		test = "Mv2FCallPri5";
		System.out.println("\n\nExecuting Test: "+test+".csv");
		copyTestFile(test+".csv");
		c = new ElevatorSimController(null);
		b = c.getBuilding();
		b.enableLogging();
	    int i;
		for (i = 0; i < 116;i++) c.stepSim();
		b.closeLogs(i);
		String cmd = "java -jar cmpElevator.jar "+test+".log";
	    runFSMTest(cmd);
	}

	@Test
	@Order(6)
	//@Disabled
	void testMv2FCallPri6() {
		test = "Mv2FCallPri6";
		System.out.println("\n\nExecuting Test: "+test+".csv");
		copyTestFile(test+".csv");
		c = new ElevatorSimController(null);
		b = c.getBuilding();
		b.enableLogging();
	    int i;
		for (i = 0; i < 106;i++) c.stepSim();
		b.closeLogs(i);
		String cmd = "java -jar cmpElevator.jar "+test+".log";
	    runFSMTest(cmd);
	}

	@Test
	@Order(7)
	//@Disabled
	void testMv2FCallPri7() {
		test = "Mv2FCallPri7";
		System.out.println("\n\nExecuting Test: "+test+".csv");
		copyTestFile(test+".csv");
		c = new ElevatorSimController(null);
		b = c.getBuilding();
		b.enableLogging();
	    int i;
		for (i = 0; i < 111;i++) c.stepSim();
		b.closeLogs(i);
		String cmd = "java -jar cmpElevator.jar "+test+".log";
	    runFSMTest(cmd);
	}

	@Test
	@Order(8)
	//@Disabled
	void testMv2FCallPri8() {
		test = "Mv2FCallPri8";
		System.out.println("\n\nExecuting Test: "+test+".csv");
		copyTestFile(test+".csv");
		c = new ElevatorSimController(null);
		b = c.getBuilding();
		b.enableLogging();
	    int i;
		for (i = 0; i < 111;i++) c.stepSim();
		b.closeLogs(i);
		String cmd = "java -jar cmpElevator.jar "+test+".log";
	    runFSMTest(cmd);
	}

	@Test
	@Order(9)
	//@Disabled
	void testMv2FCallPri9() {
		test = "Mv2FCallPri9";
		System.out.println("\n\nExecuting Test: "+test+".csv");
		copyTestFile(test+".csv");
		c = new ElevatorSimController(null);
		b = c.getBuilding();
		b.enableLogging();
	    int i;
		for (i = 0; i < 111;i++) c.stepSim();
		b.closeLogs(i);
		String cmd = "java -jar cmpElevator.jar "+test+".log";
	    runFSMTest(cmd);
	}

	@Test
	@Order(10)
	//@Disabled
	void testMv2FCallPri10() {
		test = "Mv2FCallPri10";
		System.out.println("\n\nExecuting Test: "+test+".csv");
		copyTestFile(test+".csv");
		c = new ElevatorSimController(null);
		b = c.getBuilding();
		b.enableLogging();
	    int i;
		for (i = 0; i < 116;i++) c.stepSim();
		b.closeLogs(i);
		String cmd = "java -jar cmpElevator.jar "+test+".log";
	    runFSMTest(cmd);
	}

	@Test
	@Order(11)
	//@Disabled
	void testMv2FCallPri11() {
		test = "Mv2FCallPri11";
		System.out.println("\n\nExecuting Test: "+test+".csv");
		copyTestFile(test+".csv");
		c = new ElevatorSimController(null);
		b = c.getBuilding();
		b.enableLogging();
	    int i;
		for (i = 0; i < 81;i++) c.stepSim();
		b.closeLogs(i);
		String cmd = "java -jar cmpElevator.jar "+test+".log";
	    runFSMTest(cmd);
	}

	@Test
	@Order(12)
	//@Disabled
	void testMv2FCallPri3a() {
		test = "Mv2FCallPri3a";
		System.out.println("\n\nExecuting Test: "+test+".csv");
		copyTestFile(test+".csv");
		c = new ElevatorSimController(null);
		b = c.getBuilding();
		b.enableLogging();
	    int i;
		for (i = 0; i < 138;i++) c.stepSim();
		b.closeLogs(i);
		String cmd = "java -jar cmpElevator.jar "+test+".log";
	    runFSMTest(cmd);
	}

	@Test
	@Order(13)
	//@Disabled
	void testMv2FCallPri4a() {
		test = "Mv2FCallPri4a";
		System.out.println("\n\nExecuting Test: "+test+".csv");
		copyTestFile(test+".csv");
		c = new ElevatorSimController(null);
		b = c.getBuilding();
		b.enableLogging();
	    int i;
		for (i = 0; i < 152;i++) c.stepSim();
		b.closeLogs(i);
		String cmd = "java -jar cmpElevator.jar "+test+".log";
	    runFSMTest(cmd);
	}



}
