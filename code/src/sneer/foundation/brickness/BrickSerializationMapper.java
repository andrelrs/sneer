package sneer.foundation.brickness;

public interface BrickSerializationMapper {

	String serializationHandleFor(Class<?> klass);

	Class<?> classGiven(String serializationHandle) throws ClassNotFoundException;

}
