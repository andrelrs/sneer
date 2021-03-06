package sneer.bricks.softwaresharing.tests;

import static sneer.foundation.environments.Environments.my;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.junit.Test;

import sneer.bricks.hardware.cpu.lang.contracts.WeakContract;
import sneer.bricks.hardware.cpu.threads.latches.Latch;
import sneer.bricks.hardware.cpu.threads.latches.Latches;
import sneer.bricks.hardware.io.log.Logger;
import sneer.bricks.hardware.ram.collections.CollectionUtils;
import sneer.bricks.identity.seals.Seal;
import sneer.bricks.software.code.java.source.writer.JavaSourceWriter;
import sneer.bricks.software.code.java.source.writer.JavaSourceWriters;
import sneer.bricks.software.folderconfig.FolderConfig;
import sneer.bricks.software.folderconfig.testsupport.BrickTestBase;
import sneer.bricks.softwaresharing.BrickHistory;
import sneer.bricks.softwaresharing.BrickSpace;
import sneer.bricks.softwaresharing.BrickVersion;
import sneer.bricks.softwaresharing.FileVersion;
import sneer.bricks.softwaresharing.BrickHistory.Status;
import sneer.foundation.brickness.Brick;
import sneer.foundation.lang.Consumer;
import sneer.foundation.lang.Functor;
import sneer.foundation.testsupport.AssertUtils;

public class LocalBrickDiscoveryTest extends BrickTestBase {

	@Test (timeout = 4000)
	public void localBrickDiscoveryWithBricksInSubfolders() throws IOException {
		my(FolderConfig.class).srcFolder().set(newTmpFile("src"));
		generateBricks();

		my(BrickSpace.class);
		
		assertBrickInfo("brick.Foo"    , "Foo.java", "impl/FooImpl.java", "tests/FooTest.java");
		assertBrickInfo("brick.sub.Bar", "Bar.java", "impl/BarImpl.java");
	}

	private void assertBrickInfo(String brickName, String... expectedFileNames) {
		BrickHistory brick = waitForAvailableBrick(brickName, Status.CURRENT);

		assertEquals(1, brick.versions().size());
		BrickVersion singleVersion = brick.versions().get(0);
		Collection<String> names = my(CollectionUtils.class).map(singleVersion.files(), new Functor<FileVersion, String>() { @Override public String evaluate(FileVersion file) {
			return file.name();
		}});
		AssertUtils.assertContents(names, expectedFileNames);
	}

	
	private void generateBricks() throws IOException {
		File srcFolder = my(FolderConfig.class).srcFolder().get();
		JavaSourceWriter writer = my(JavaSourceWriters.class).newInstance(srcFolder);
		
		writer.write("brick.Foo", "@" + Brick.class.getName() + " public interface Foo {}");
		writer.write("brick.impl.FooImpl", "Contents ignored.");
		writer.write("brick.tests.FooTest", "Contents ignored.");
		writer.write("brick.sub.Bar", "@" + Brick.class.getName() + " public interface Bar {}");
		writer.write("brick.sub.impl.BarImpl", "Contents ignored.");
	}
	
	
	private BrickHistory waitForAvailableBrick(final String brickName, final Status status) {
		final Latch latch = my(Latches.class).produce();
		
		WeakContract contract = my(BrickSpace.class).newBuildingFound().addReceiver(new Consumer<Seal>() { @Override public void consume(Seal publisher) {
			if (isBrickAvailable(brickName, status)) latch.open();
		}});
		if (isBrickAvailable(brickName, status)) latch.open();

		latch.waitTillOpen();
		contract.dispose();
		
		return findBrick(brickName, status);
	}

	
	private boolean isBrickAvailable(final String brickName, final Status status) {
		return findBrick(brickName, status) != null;
	}


	private BrickHistory findBrick(final String brickName, final Status status) {
		for (BrickHistory brickInfo : my(BrickSpace.class).availableBricks()) {
			my(Logger.class).log(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Brick found: " + brickInfo.name() + " status: " + brickInfo.status().name());
			if (brickInfo.name().equals(brickName)
				&& brickInfo.status() == status)
				return brickInfo;
		};
		return null;
	}

}