package me.catcoder.sidebar.util.buffer;

import java.util.UUID;

/**
 * An interface for writing network data.
 */
public interface NetOutput {
    /**
     * Writes a boolean.
     *
     * @param b Boolean to write.
     */
    public void writeBoolean(boolean b);

    /**
     * Writes a byte.
     *
     * @param b Byte to write.
     */
    public void writeByte(int b);

    /**
     * Writes a short.
     *
     * @param s Short to write.
     */
    public void writeShort(int s);

    /**
     * Writes a char.
     *
     * @param c Char to write.
     */
    public void writeChar(int c);

    /**
     * Writes a integer.
     *
     * @param i Integer to write.
     */
    public void writeInt(int i);

    /**
     * Writes a varint. A varint is a form of integer where only necessary bytes are written. This is done to save bandwidth.
     *
     * @param i Varint to write.
     */
    public void writeVarInt(int i);

    /**
     * Writes a long.
     *
     * @param l Long to write.
     */
    public void writeLong(long l);

    /**
     * Writes a varlong. A varlong is a form of long where only necessary bytes are written. This is done to save bandwidth.
     *
     * @param l Varlong to write.
     */
    public void writeVarLong(long l);

    /**
     * Writes a float.
     *
     * @param f Float to write.
     */
    public void writeFloat(float f);

    /**
     * Writes a double.
     *
     * @param d Double to write.
     */
    public void writeDouble(double d);

    /**
     * Writes a byte array.
     *
     * @param b Byte array to write.
     */
    public void writeBytes(byte b[]);

    /**
     * Writes a byte array, using the given amount of bytes.
     *
     * @param b      Byte array to write.
     * @param length Bytes to write.
     */
    public void writeBytes(byte b[], int length);

    /**
     * Writes a short array.
     *
     * @param s Short array to write.
     */
    public void writeShorts(short s[]);

    /**
     * Writes a short array, using the given amount of bytes.
     *
     * @param s      Short array to write.
     * @param length Shorts to write.
     */
    public void writeShorts(short s[], int length);

    /**
     * Writes an int array.
     *
     * @param i Int array to write.
     */
    public void writeInts(int i[]);

    /**
     * Writes an int array, using the given amount of bytes.
     *
     * @param i      Int array to write.
     * @param length Ints to write.
     */
    public void writeInts(int i[], int length);

    /**
     * Writes a long array.
     *
     * @param l Long array to write.
     */
    public void writeLongs(long l[]);

    /**
     * Writes a long array, using the given amount of bytes.
     *
     * @param l      Long array to write.
     * @param length Longs to write.
     */
    public void writeLongs(long l[], int length);

    /**
     * Writes a string.
     *
     * @param s String to write.
     */
    public void writeString(String s);

    /**
     * Writes a UUID.
     *
     * @param uuid UUID to write.
     */
    public void writeUUID(UUID uuid);

    /**
     * Flushes the output.
     *
     */
    public void flush();

    /**
     * Creates a new byte array with current data.
     * 
     * @return New byte array.
     */
    public byte[] toByteArray();
}