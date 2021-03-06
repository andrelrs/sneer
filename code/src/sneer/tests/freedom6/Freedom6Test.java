package sneer.tests.freedom6;

import static sneer.foundation.environments.Environments.my;

import java.io.File;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import sneer.bricks.hardware.io.IO;
import sneer.tests.SovereignFunctionalTestBase;



public class Freedom6Test extends SovereignFunctionalTestBase {

	@Ignore
	@Test (timeout = 6000)
	public void syncAFile() throws IOException {
		File folder = createFolder("important_folder");
		a().setFolderToSync(folder);
		createTmpFileWithFileNameAsContent("important_folder/important_file.txt");
		
		b().lendBackupSpaceTo(a().ownName(), 10);
		a().waitForSync();

		File newFolder = createFolder("new_folder");
		a().setFolderToSync(newFolder);
		a().waitForSync();
		
		File recoveredFile = new File(newFolder, "important_file.txt");
		String contents = my(IO.class).files().readString(recoveredFile);
		assertEquals("new_folder/important_file.txt", contents);
	}

	private File createFolder(String fileName) {
		File importantFolderA = newTmpFile(fileName);
		importantFolderA.mkdir();
		return importantFolderA;
	}
	
}