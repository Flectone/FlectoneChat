package net.flectone.testing;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

public class PacketSerializer {

    private final byte[] result;

    public PacketSerializer(String string) {
        ByteBuf buf = Unpooled.buffer();
        writeString(string, buf);
        result = buf.array();
        buf.release();
    }

    private void writeString(String s, ByteBuf buf) {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        writeVarInt(b.length, buf);
        buf.writeBytes(b);
    }

    private void writeVarInt(int value, ByteBuf output) {
        do {
            int part = value & 0x7F;
            value >>>= 7;
            if (value != 0) {
                part |= 0x80;
            }
            output.writeByte(part);
        } while (value != 0);
    }

    public byte[] toArray() {
        return result;
    }

}
