package sneer.skin.sound.speaker.buffer.tests;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import sneer.kernel.container.Inject;
import sneer.kernel.container.tests.TestThatIsInjected;
import sneer.pulp.clock.Clock;
import sneer.pulp.keymanager.KeyManager;
import sneer.pulp.keymanager.PublicKey;
import sneer.pulp.threadpool.mocks.ThreadPoolMock;
import sneer.skin.sound.PcmSoundPacket;
import sneer.skin.sound.speaker.buffer.SpeakerBuffer;
import sneer.skin.sound.speaker.buffer.SpeakerBuffers;
import wheel.lang.ImmutableByteArray;
import wheel.lang.Omnivore;


@RunWith(JMock.class)
public class SpeakerBufferTest extends TestThatIsInjected {
	
	@Inject private static SpeakerBuffers _subject;
	
	@Inject private static Clock _clock;
	@Inject private static KeyManager _keyManager;

	private final ThreadPoolMock _threads = new ThreadPoolMock();

	private final Mockery _mockery = new JUnit4Mockery();
	
	private final Omnivore<PcmSoundPacket> _consumer = _mockery.mock(Omnivore.class);
	
	@Override
	protected Object[] getBindings() {
		return new Object[]{ _threads };
	}

	@Test
	public void correctOrder() throws Exception {
		testBuffering(p1(), p2());
	}

	@Test
	public void inverseOrder() throws Exception {
		testBuffering(p2(), p1());
	}

	private void testBuffering(PcmSoundPacket first, PcmSoundPacket second) {
		_mockery.checking(new Expectations(){{
			Sequence main = _mockery.sequence("main");
			one(_consumer).consume(p1()); inSequence(main);
			one(_consumer).consume(p2()); inSequence(main);
		}});
		
		SpeakerBuffer buffer = _subject.createBufferFor(_consumer);
		buffer.consume(first);
		buffer.consume(second);
		
		_threads.stepper(0).step();
		_threads.stepper(0).step();
	}
	
	@Ignore
	@Test
	public void sequencing() {
		int[] input = new int[] {
			0, 1, 2, 3, //Happy
			-1, 2, // Less than 500 negative difference: discarded
			6, 4, 5, //Different order, no gap
			7, 11, 10, 9, 8, //Different order, no gap
			20, 22, 21, //Different order, with gap: will wait
			51, // More than 30 gap since last played (11): will cause 20, 21 to be played.
			600, 601, 602, // More than 500 gap: will cause buffer to drain (22, 51 will not be played)
			604, //Gap
			-700, -701 // More than 500 gap in the other direction will cause buffer to drain (604 will not be played)
		};

		SpeakerBuffer buffer = _subject.createBufferFor(sequenceRecorder());
		feedInputInDifferentThread(buffer, input);
		
		expectOutputSequence(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 20, 21, 600, 601, 602, -700, -701);
		
	}
	
	private Omnivore<? super PcmSoundPacket> sequenceRecorder() {
		throw new wheel.lang.exceptions.NotImplementedYet(); // Implement
	}

	private void expectOutputSequence(@SuppressWarnings("unused") int... sequences) {
		//Loop:
			_threads.stepper(0).step();
			
		throw new wheel.lang.exceptions.NotImplementedYet(); // Implement
	}

	private void feedInputInDifferentThread(SpeakerBuffer buffer, int[] input) {
		for (int sequence : input)
			buffer.consume(contactPacket(new byte[] { 7, 11, 13, 17 }, sequence));
	}

	@SuppressWarnings("deprecation")
	private PublicKey contactKey() {
		return _keyManager.generateMickeyMouseKey("contact");
	}

	private PcmSoundPacket contactPacket(byte[] pcm, int sequence) {
		return pcmSoundPacketFor(contactKey(), pcm, sequence);
	}
	
	private PcmSoundPacket pcmSoundPacketFor(PublicKey publicKey, final byte[] pcmPayload, int sequence) {
		return new PcmSoundPacket(publicKey, _clock.time(), new ImmutableByteArray(pcmPayload, pcmPayload.length), sequence);
	}
	
	private PcmSoundPacket p1() {
		return contactPacket(new byte[] { 1, 2, 3, 5 }, (short)1);
	}
	
	private PcmSoundPacket p2() {
		return contactPacket(new byte[] { 7, 11, 13, 17 }, (short)2);
	}
}