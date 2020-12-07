package tk.iovr.androidsocketconnect.easySocket.SocketServer.iowork;


import java.io.IOException;
import java.net.Socket;

import tk.iovr.androidsocketconnect.easySocket.SocketServer.HandlerIO;

public class IOManager implements IIOManager {

    //IO Write
    private IWriter writer;

    //IO Read
    private IReader reader;

    public IOManager(Socket socket) {
        try {
            initIO(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Init IO
    private void initIO(Socket socket) throws IOException {
        writer = new EasyWriter(socket.getOutputStream(), socket); //write
        HandlerIO handlerIO = new HandlerIO(writer);
        reader = new EasyReader(socket.getInputStream(), socket, handlerIO); //read
    }

    @Override
    public void sendBuffer(byte[] buffer) {
        if (writer != null)
            writer.offer(buffer);
    }

    @Override
    public void startIO() {
        if (writer != null)
            writer.openWriter();
        if (reader != null)
            reader.openReader();
    }

    @Override
    public void closeIO() {
        if (writer != null)
            writer.closeWriter();
        if (reader != null)
            reader.closeReader();
    }

//    /**
//     * makesureHeaderProtocolNotEmpty
//     */
//    private void makesureHeaderProtocolNotEmpty() {
//        IReaderProtocol protocol = connectionManager.getOptions().getReaderProtocol();
//        if (protocol == null) {
//            throw new NoNullExeption("The reader protocol can not be Null.");
//        }
//
//        if (protocol.getHeaderLength() == 0) {
//            throw new NoNullExeption("The header length can not be zero.");
//        }
//    }
}
