import java.nio.ByteBuffer;

public class ObjectAsArgument {
    private ByteBuffer byteBuffer;

    public void consume(ByteBuffer byteBuffer) {
        <weak_warning descr="An object 'byteBuffer' is used as an argument to its own method">byteBuffer.put(byteBuffer)</weak_warning>;
        this.byteBuffer.put(byteBuffer);
    }
}
