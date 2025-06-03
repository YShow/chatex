package globals;

import java.util.HashMap;
import java.util.Map;

public enum PacketTypes {
    UNKONOWN((byte) 0),AUTHMESSAGETYPE((byte) 1), REJECTTYPE((byte) 2), TEXTMESSAGETYPE((byte) 3);

    private final byte value;
    private static final Map<Byte, PacketTypes> values;

    static {
	final var localvalues = new HashMap<Byte, PacketTypes>();
	for (final var types : PacketTypes.values()) {
	    localvalues.put(Byte.valueOf(types.getValue()), types);
	}

	values = Map.copyOf(localvalues);
    }

    public static final PacketTypes getType(final byte type) {
	return values.get(Byte.valueOf(type));
    }

    private PacketTypes(final byte newValue) {
	value = newValue;
    }

    public final byte getValue() {
	return value;
    }

}
