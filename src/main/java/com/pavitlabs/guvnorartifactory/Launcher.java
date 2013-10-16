package gov.utah.dts.erep.guvnorartifactory;

import gov.utah.dts.erep.guvnorartifactory.services.PackageManager;
import gov.utah.dts.erep.guvnorartifactory.utils.EncryptionUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import jline.ArgumentCompletor;
import jline.Completor;
import jline.ConsoleReader;
import jline.SimpleCompletor;

public class Launcher {

	private static final Logger logger = Logger.getLogger(Thread.currentThread().getStackTrace()[0].getClassName());
	
	private static ConsoleReader reader; 
	private static Launcher launcher = new Launcher();
	private static String[] initCommandsList = 
	                new String[] {
	                              "1- Update Guvnor", 
	                              "2- Update Guvnor packages", 
	                              "3- Upload POJO Model", 
	                              "4- Build Package Snapshot", 
	                              "5- Diagnose",
	                              "6- Encrypt:", 
	                              "quit"
	                              };
	
	private static ApplicationContext context;
	
	private static ApplicationContext getContext(){
	    if (context == null){
	        context = new ClassPathXmlApplicationContext("guvnorArtifactory-context.xml"); 
	    }
	    return context;
	}
	
	public void loadReaderInitCompletors() throws IOException {
		reader = new ConsoleReader();
		reader.setBellEnabled(false);
		List<Completor> completors = new LinkedList<Completor>();
		completors.add(new SimpleCompletor(initCommandsList));
		reader.addCompletor(new ArgumentCompletor(completors));
	}
	
	public static void main(String[] args) throws IOException {
		launcher.run(args);
	}
	
	public void run(String[] args) throws IOException {
		if (args == null || args.length == 0) {
			launchCommandLine();
		} else {
			String packageName;
			int selection = Integer.parseInt(args[0]);
			switch (selection) {
			case 1: case 2 : case 5:
				String resourcesFolder = args[1].replace("\\", "/");
				String packages = "";
				if (args.length >= 3) {
					for (int i = 2; i < args.length; i++) {
						packages += args[i];
					}
				}
				process(selection, new File(resourcesFolder), packages.split(","));
				break;
			case 3:
				packageName = args[1];
				String modelFilePath = args[2].replace("\\", "/");
				uploadModel(packageName, new File(modelFilePath));
				break;
			case 4:
				packageName = args[1];
				String snapshotName = args[2];
				buildPackageSnapshot(packageName, snapshotName);
				break;
			case 6:
			    EncryptionUtils encryptionUtils = context.getBean(EncryptionUtils.class);
				System.out.println("Encrypted text - " + encryptionUtils.encrypt(args[1]));
				break;	
			default:
				break;
			}
		}
	}
	
	
	private String readLine(ConsoleReader reader, String promtMessage) throws IOException {
		String line = reader.readLine(promtMessage + "\nshell> ");
		return line.trim();
	}
	
	private String readLine(ConsoleReader reader, String promtMessage, char mask) throws IOException {
		String line = reader.readLine(promtMessage + "\nshell> ", mask);
		return line.trim();
	}
	

	public void launchCommandLine() throws IOException {
		greet();
		launcher.loadReaderInitCompletors();
		String line;
		PrintWriter out = new PrintWriter(System.out);
		while ((line = readLine(reader, "")) != null) {
			if ("help".equals(line)) {
				printHelp();
			} else if ("quit".equals(line)) {
				return;
			} else if (line.length()>0) {
				int selection = Character.getNumericValue(line.charAt(0));
				boolean isValidSelection = (selection > 0 && selection <= initCommandsList.length);
				while (isValidSelection){
					process(selection);	
					return;
				}
			} else {
				System.out.println("Invalid command, For assistance press TAB or type \"help\" then hit ENTER.");
			}
			out.flush();
		}
	}
	
	private void printHelp() {
		logger.info("1- Update Guvnor \t\t" + "- All packages will be updated");
		logger.info("2- Update Guvnor packages \t"+"- Pass Package names as comma separated arguments.");
		logger.info("3- Upload POJO Model \t\t\t"+"- Pass Package name and the Model jar as arguments");
		logger.info("4- Build package snapshot \t\t\t"+"- Pass Package name and the Snapshot name as arguments");
		logger.info("5- Diagnose \t\t\t"+"- Diagnose the resources folder for any artifacts that are breaking the package build.");
		logger.info("6- Encrypt \t\t\t"+"- Utility tool to encrypt passwords");
		logger.info("quit \t\t\t"+"- Exit");
	}

	private void process(int selection) throws IOException {
		File resourcesFolder;
		String packagesToUpdate;
		switch (selection) {
		case 1:
			System.out.println("This will update the assets in Guvnor. \nAssets will be overwritten. \nPlease use this carefully.");
			resourcesFolder = parseResourcesFolderInput();
			if(resourcesFolder != null)
				process(1, resourcesFolder);
			else 
				return;
			break;
		case 2:
			System.out.println("This will update the assets in the selected Guvnor package. \nAssets will be overwritten. As always, please use this carefully.");
			resourcesFolder = parseResourcesFolderInput();
			System.out.println("Enter the packages to be updated.(eg. Eligibility, RiskEngine)");
			packagesToUpdate = parsePackagesInput();
			if(resourcesFolder != null && packagesToUpdate != null)
				process(2, resourcesFolder, packagesToUpdate.split(","));
			else 
				return;
			break;
		case 3:
            System.out.println("This will upload the pojo model jar for the selected guvnor package.");
            File modelJar = parseModelJarPath();
            System.out.println("Enter the package that the model file is associated with.(eg. Eligibility / RiskEngine)");
            String modelPackageName = parsePackagesInput();
            if(modelJar != null && modelPackageName != null)
                uploadModel(modelPackageName, modelJar);
            else 
                return;
            break;
		case 4:
            System.out.println("This will build snapshots for the selected guvnor package.");
            System.out.println("Enter the package whose snapshot need to be built. (eg. Eligibility / RiskEngine)");
            String snapshotPackageName = parsePackagesInput();
            String snapshotName = parseSnapshotName();
            if(snapshotPackageName != null && snapshotName != null)
                buildPackageSnapshot(snapshotPackageName, snapshotName);
            else 
                return;
            break;    
		case 5:
            System.out.println("This will diagnose the resources folder for any artifacts that are breaking a guvnor package build.");
            resourcesFolder = parseResourcesFolderInput();
            System.out.println("Enter the packages to be diagnosed.(eg. Eligibility, RiskEngine)");
            String packagesToDiagnose = parsePackagesInput();
            if(resourcesFolder != null && packagesToDiagnose != null){
                getContext().getBean(PackageManager.class).diagnoseForCorruptedAssets(resourcesFolder, packagesToDiagnose.split(","));
            }
            else 
                return;
            break;
		case 6:
			System.out.println("Enter text that needs to be encrypted. Beware of any shoulder surfers/peekers around...");
			String plainText;
			while ((plainText = readLine(reader, "", '*')) != null){
				System.out.println("Encrypted text - " + context.getBean(EncryptionUtils.class).encrypt(plainText));
				return;
			}
			break;
		default:
			System.out.println("Please enter a valid selection.");
		}
	}

	private static void process(int selection, File resourcesFolder, String... packages) {
	    PackageManager packageManager = getContext().getBean(PackageManager.class);
	    switch (selection) {
		case 1:
		    packageManager.update(resourcesFolder);
			break;
		case 2:
		    packageManager.update(resourcesFolder, packages);
			break;
		case 5:
            packageManager.diagnoseForCorruptedAssets(resourcesFolder, packages);
            break;
		default:
			System.out.println("Not a valid selection. Exiting now. Goodbye!");
		}
	}
	
	private static void uploadModel(String packageName, File modeljar){
	    PackageManager packageManager = getContext().getBean(PackageManager.class);
	    packageManager.uploadModel(packageName, modeljar);
	}
	
	private static void buildPackageSnapshot(String packageName, String snapshotName){
	    PackageManager packageManager = getContext().getBean(PackageManager.class);
	    packageManager.buildSnapshot(packageName, snapshotName);
	}
	
	private File parseModelJarPath() throws IOException {
        String modelJarPath;
        File modelJar = null;
        reader = new ConsoleReader();
        Completor completor = new SimpleCompletor("quit");
        reader.addCompletor(new ArgumentCompletor(completor));
        while ((modelJarPath = readLine(reader, "Enter the path to the jar file")) != null){
            if ("quit".equals(modelJarPath)) {
                break;
            } else {
                modelJar = new File(modelJarPath);
                if (!modelJar.exists() || modelJar.isDirectory()) {
                    System.out.println("Model jar path is not valid. Please enter again");
                    parseModelJarPath();
                }
                return modelJar;
            }
        }
        return null;
    }

	private File parseResourcesFolderInput() throws IOException {
		String resourcesFolderPath;
		File resourcesFolder = null;
		reader = new ConsoleReader();
		Completor completor = new SimpleCompletor("quit");
		reader.addCompletor(new ArgumentCompletor(completor));
		while ((resourcesFolderPath = readLine(reader, "Enter the source directory")) != null){
			if ("quit".equals(resourcesFolderPath)) {
				break;
			} else {
				resourcesFolder = new File(resourcesFolderPath);
				if (!(resourcesFolder.exists() && resourcesFolder.isDirectory())) {
					System.out.println("Source directory is not valid. Please enter again");
					parseResourcesFolderInput();
				}
				return resourcesFolder;
			}
		}
		return null;
	}
	
	private String parsePackagesInput() throws IOException {
		String packagesInput;
		reader = new ConsoleReader();
		Completor completor = new SimpleCompletor("quit");
		reader.addCompletor(new ArgumentCompletor(completor));
		while ((packagesInput = readLine(reader, "Enter the package/packages name")) != null){
			if ("quit".equals(packagesInput)) {
				break;
			} else {
				return packagesInput;
			}
		}
		return null;
	}
	
	private String parseSnapshotName() throws IOException {
        String snapshotName;
        reader = new ConsoleReader();
        Completor completor = new SimpleCompletor("quit");
        reader.addCompletor(new ArgumentCompletor(completor));
        while ((snapshotName = readLine(reader, "Enter the snapshot name")) != null){
            if ("quit".equals(snapshotName)) {
                break;
            } else {
                return snapshotName;
            }
        }
        return null;
    }


	public void greet() throws IOException {
		System.out.println("   _____                                                     _    _   __              _                      ");
		System.out.println("  / ____|                                       /\\          | |  (_) / _|            | |                     ");
		System.out.println(" | |  __  _   _ __   __ _ __    ___   _ __     /  \\    _ __ | |_  _ | |_  __ _   ___ | |_  ___   _ __  _   _ ");
		System.out.println(" | | |_ || | | |\\ \\ / /| '_ \\  / _ \\ | '__|   / /\\ \\  | '__|| __|| ||  _|/ _` | / __|| __|/ _ \\ | '__|| | | |");
		System.out.println(" | |__| || |_| | \\ V / | | | || (_) || |     / ____ \\ | |   | |_ | || | | (_| || (__ | |_| (_) || |   | |_| |");
		System.out.println("  \\_____| \\__,_|  \\_/  |_| |_| \\___/ |_|    /_/    \\_\\|_|    \\__||_||_|  \\__,_| \\___| \\__|\\___/ |_|    \\__, |");
		System.out.println("                                                                                                        __/ |");
		System.out.println("                                                                                                       |___/ ");
		System.out.println("Welcome to Guvnor Artifactory. For assistance press TAB or type \"help\" then hit ENTER.");
	}

}
