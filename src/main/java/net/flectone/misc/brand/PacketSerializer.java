package net.flectone.misc.brand;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class PacketSerializer {

    private final byte[] result;

    public PacketSerializer(@NotNull String string) {
        ByteBuf buf = Unpooled.buffer();
        writeString(string, buf);
        result = buf.array();
        buf.release();
    }

    private void writeString(@NotNull String s, @NotNull ByteBuf buf) {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        writeVarInt(b.length, buf);
        buf.writeBytes(b);
    }

    private void writeVarInt(int value, @NotNull ByteBuf output) {
        do {
            int part = value & 0x7F;
            value >>>= 7;
            if (value != 0) {
                part |= 0x80;
            }
            output.writeByte(part);
        } while (value != 0);
    }

    public byte @NotNull [] toArray() {
        return result;
    }

}
