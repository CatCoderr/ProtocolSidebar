package me.catcoder.sidebar.util.buffer;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.viaversion.nbt.io.NBTIO;
import com.viaversion.nbt.tag.Tag;
import io.netty.buffer.ByteBuf;
import lombok.SneakyThrows;
import me.catcoder.sidebar.util.NbtComponentSerializer;
import org.jetbrains.annotations.Nullable;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * A NetOutput implementation using a ByteBuf as a backend.
 */
public class ByteBufNetOutput implements NetOutput {
    private ByteBuf buf;

    public ByteBufNetOutput(ByteBuf buf) {
        this.buf = buf;
    }

    @Override
    public void writeBoolean(boolean b) {
        this.buf.writeBoolean(b);
    }

    @Override
    public void writeComponent(String json) {
        JsonElement jsonElement = JsonParser.parseString(json);
        Tag tag = NbtComponentSerializer.jsonComponentToTag(jsonElement);

        writeAnyTag(tag, false);
    }

    @SneakyThrows
    @Override
    public <T extends Tag> void writeAnyTag(@Nullable T tag, boolean named) {
        NBTIO.writeTag(new DataOutputStream(new OutputStream() {
            @Override
            public void write(int b) {
                buf.writeByte(b);
            }
        }), tag, named);
    }

    @Override
    public void writeByte(int b) {
        this.buf.writeByte(b);
    }

    @Override
    public void writeShort(int s) {
        this.buf.writeShort(s);
    }

    @Override
    public void writeChar(int c) {
        this.buf.writeChar(c);
    }

    @Override
    public void writeInt(int i) {
        this.buf.writeInt(i);
    }

    @Override
    public void writeVarInt(int i) {
        while ((i & ~0x7F) != 0) {
            this.writeByte((i & 0x7F) | 0x80);
            i >>>= 7;
        }

        this.writeByte(i);
    }

    @Override
    public void writeLong(long l) {
        this.buf.writeLong(l);
    }

    @Override
    public void writeVarLong(long l) {
        while ((l & ~0x7F) != 0) {
            this.writeByte((int) (l & 0x7F) | 0x80);
            l >>>= 7;
        }

        this.writeByte((int) l);
    }

    @Override
    public void writeFloat(float f) {
        this.buf.writeFloat(f);
    }

    @Override
    public void writeDouble(double d) {
        this.buf.writeDouble(d);
    }

    @Override
    public void writeBytes(byte b[]) {
        this.buf.writeBytes(b);
    }

    @Override
    public void writeBytes(byte b[], int length) {
        this.buf.writeBytes(b, 0, length);
    }

    @Override
    public void writeShorts(short[] s) {
        this.writeShorts(s, s.length);
    }

    @Override
    public void writeShorts(short[] s, int length) {
        for (int index = 0; index < length; index++) {
            this.writeShort(s[index]);
        }
    }

    @Override
    public void writeInts(int[] i) {
        this.writeInts(i, i.length);
    }

    @Override
    public void writeInts(int[] i, int length) {
        for (int index = 0; index < length; index++) {
            this.writeInt(i[index]);
        }
    }

    @Override
    public void writeLongs(long[] l) {
        this.writeLongs(l, l.length);
    }

    @Override
    public void writeLongs(long[] l, int length) {
        for (int index = 0; index < length; index++) {
            this.writeLong(l[index]);
        }
    }

    @Override
    public byte[] toByteArray() {
        byte[] bytes = new byte[this.buf.readableBytes()];
        this.buf.readBytes(bytes);
        return bytes;
    }

    @Override
    public void writeString(String s) {
        if (s == null) {
            throw new IllegalArgumentException("String cannot be null!");
        }

        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > 32767) {
            throw new RuntimeException("String too big (was " + s.length() + " bytes encoded, max " + 32767 + ")");
        } else {
            this.writeVarInt(bytes.length);
            this.writeBytes(bytes);
        }
    }

    @Override
    public void writeUUID(UUID uuid) {
        this.writeLong(uuid.getMostSignificantBits());
        this.writeLong(uuid.getLeastSignificantBits());
    }

    @Override
    public void flush() {
    }
}